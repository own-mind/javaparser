package com.github.javaparser.symbolsolver;

import static com.github.javaparser.symbolsolver.AbstractSymbolResolutionTest.adaptPath;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SolveNestedClassByNameExprTest {
    @Test
    public void test() throws IOException {
        final Path sourceRoot = adaptPath("src/test/resources/SolveNestedClassByNameExprTest");
        final Path testClass = sourceRoot.resolve("A.java");

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(
                new CombinedTypeSolver(new JavaParserTypeSolver(sourceRoot), new ReflectionTypeSolver(true)));

        StaticJavaParser.setConfiguration(new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
                .setSymbolResolver(symbolSolver));

        CompilationUnit parsed = StaticJavaParser.parse(testClass);
        MethodDeclaration foo =
                parsed.getClassByName("A").get().getMethodsByName("foo").get(0);
        List<Statement> statementList = foo.getBody().get().getStatements();

        for (int i = 0; i < statementList.size(); i++) {
            Statement statement = statementList.get(i);
            MethodCallExpr callExpr =
                    statement.asExpressionStmt().getExpression().asMethodCallExpr();
            // NOTE: Ideally in case of `B.staticCall()`, `B` should be TypeExpr.
            // If this line fails because the scope returns TypeExpr, modify the test case to accept that.
            NameExpr nameExpr = callExpr.getScope().get().asNameExpr();
            ResolvedValueDeclaration resolved = assertDoesNotThrow(nameExpr::resolve);
            assertEquals("A.Test" + i, resolved.getType().describe());
        }
    }
}
