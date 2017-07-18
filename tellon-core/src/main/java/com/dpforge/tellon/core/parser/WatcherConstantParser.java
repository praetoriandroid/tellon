package com.dpforge.tellon.core.parser;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class WatcherConstantParser {

    public WatcherMap parse(SourceCode sourceCode) {
        final WatcherMap fields = new WatcherMap();
        new Visitor(fields).visit(sourceCode.toCompilationUnit(), null);
        return fields;
    }

    private static class Visitor extends VoidVisitorAdapter<Void> {
        private final WatcherMap map;

        private Visitor(final WatcherMap map) {
            this.map = map;
        }

        @Override
        public void visit(FieldDeclaration field, Void arg) {
            verifyField(field);
            for (VariableDeclarator var : field.getVariables()) {
                final String name = var.getNameAsString();
                if (!var.getInitializer().isPresent()) {
                    throw new RuntimeException("Field '" + name + "' has no initializer");
                }
                final Expression initializer = var.getInitializer().get();
                if (initializer instanceof StringLiteralExpr) {
                    final String value = ((StringLiteralExpr) initializer).getValue();
                    if (map.put(name, value) != null) {
                        throw new RuntimeException("Field '" + name + "' initialized more than once");
                    }
                } else if (initializer instanceof NameExpr) {
                    map.put(name, processNameReference((NameExpr) initializer));
                } else {
                    throw new UnsupportedOperationException("Field '" + name + "' must be initialized with string literal or string literal array");
                }
            }
            super.visit(field, arg);
        }

        private List<String> processNameReference(NameExpr expr) {
            final String name = expr.getNameAsString();
            final List<String> mapped = map.get(name);
            if (mapped == null || mapped.isEmpty()) {
                throw new RuntimeException("Reference to non-declared name: " + name);
            }
            return Collections.unmodifiableList(mapped);
        }

        private static void verifyField(FieldDeclaration field) {
            if (!field.isStatic()) {
                throw new RuntimeException("Field not static");
            }
            if (!field.isFinal()) {
                throw new RuntimeException("Field not final");
            }
            if (!"String".equals(field.getElementType().toString())) {
                throw new RuntimeException("Field must be of type String");
            }
            if (field.getElementType().getParentNode().isPresent()) {
                if (field.getElementType().getParentNode().get() instanceof ArrayType) {
                    throw new RuntimeException("Field must be constant string not array");
                }
            }
        }
    }
}
