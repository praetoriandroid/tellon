package com.dpforge.tellon.core.parser;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class AnnotatedBlockTest {

    @Test(expected = IllegalStateException.class)
    public void nodeWithoutPosition() {
        final ClassOrInterfaceDeclaration node = new ClassOrInterfaceDeclaration(
                EnumSet.of(Modifier.PUBLIC),
                true,
                "Bar");
        AnnotatedBlock.fromNode(SourceCode.createFromContent(), node, Collections.emptyList());
    }

    @Test(expected = IllegalStateException.class)
    public void javadocWithoutPosition() {
        final ClassOrInterfaceDeclaration node = new ClassOrInterfaceDeclaration(
                EnumSet.of(Modifier.PUBLIC),
                true,
                "Bar");
        node.setRange(new Range(new Position(1, 1), new Position(5, 1)));
        node.setJavadocComment("Test");
        AnnotatedBlock.fromNode(SourceCode.createFromContent(), node, Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullWatchers() {
        final ClassOrInterfaceDeclaration node = new ClassOrInterfaceDeclaration(
                EnumSet.of(Modifier.PUBLIC),
                true,
                "Bar");
        node.setRange(new Range(new Position(1, 1), new Position(2, 1)));
        AnnotatedBlock.fromNode(SourceCode.createFromContent("class Bar {", "}"), node, null);
    }

    @Test
    public void toStringTest() {
        final ClassOrInterfaceDeclaration node = new ClassOrInterfaceDeclaration(
                EnumSet.of(Modifier.PUBLIC),
                true,
                "Bar");
        node.setRange(new Range(new Position(1, 1), new Position(2, 2)));
        final AnnotatedBlock block = AnnotatedBlock.fromNode(
                SourceCode.createFromContent("class Bar {", " }"),
                node,
                Collections.singletonList("test-watcher"));
        assertEquals("TYPE 'Bar' {line=0, column=0} - {line=1, column=1} [test-watcher]", block.toString());
    }
}