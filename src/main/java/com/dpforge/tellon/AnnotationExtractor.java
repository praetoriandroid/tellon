package com.dpforge.tellon;

import com.dpforge.tellon.annotations.NotifyChanges;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

class AnnotationExtractor {
    private static final String ANNOTATION_NAME = NotifyChanges.class.getSimpleName();
    private static final String ANNOTATION_QUALIFIED_NAME = NotifyChanges.class.getName();

    private final VisitorContext visitorContext;

    AnnotationExtractor(VisitorContext visitorContext) {
        this.visitorContext = visitorContext;
    }

    String[] tryExtract(NodeWithAnnotations<?> node) {
        final NodeList<AnnotationExpr> annotations = node.getAnnotations();

        if (annotations.isEmpty()) {
            return null;
        }

        for (AnnotationExpr a : annotations) {
            if (verifyAnnotation(a)) {
                return extractArguments(a);
            }
        }
        return null;
    }

    private boolean verifyAnnotation(AnnotationExpr annotation) {
        if (visitorContext.isAnnotationImported()) {
            return ANNOTATION_NAME.equals(annotation.getNameAsString());
        }
        return ANNOTATION_QUALIFIED_NAME.equals(annotation.getNameAsString());
    }

    private static String[] extractArguments(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            return processArgumentExpression(((SingleMemberAnnotationExpr) annotation).getMemberValue());
        }
        throw new UnsupportedOperationException();
    }

    private static String[] processArgumentExpression(Expression expression) {
        if (expression instanceof ArrayInitializerExpr) {
            return processArrayExpression((ArrayInitializerExpr) expression);
        } else if (expression instanceof StringLiteralExpr) {
            return new String[]{((StringLiteralExpr) expression).getValue()};
        }
        throw new UnsupportedOperationException();
    }

    private static String[] processArrayExpression(ArrayInitializerExpr expression) {
        final NodeList<Expression> values = expression.getValues();
        final String[] result = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Expression val = values.get(i);
            if (val instanceof StringLiteralExpr) {
                result[i] = ((StringLiteralExpr) val).getValue();
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return result;
    }
}