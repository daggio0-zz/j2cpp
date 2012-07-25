package me.pixodro.j2cpp.core.info;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/10/12
 * Time: 12:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatementInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  private IASTStatement statement;
  private final TypeDeclaration typeDeclaration;

  private final CompilationUnitInfo compilationUnitInfo;

  public StatementInfo(final Statement javaStatement, final TypeDeclaration typeDeclaration, final CompilationUnitInfo compilationUnitInfo) {
    this.typeDeclaration = typeDeclaration;
    this.compilationUnitInfo = compilationUnitInfo;
    if (javaStatement instanceof Block) {
      statement = convertBlock((Block) javaStatement);
    } else if (javaStatement instanceof ExpressionStatement) {
      statement = convertExpressionStatement((ExpressionStatement) javaStatement);
    } else if (javaStatement instanceof IfStatement) {
      statement = convertIfStatement((IfStatement) javaStatement);
    } else if (javaStatement instanceof ForStatement) {
      statement = convertForStatement((ForStatement) javaStatement);
    } else if (javaStatement instanceof WhileStatement) {
      statement = convertWhileStatement((WhileStatement) javaStatement);
    } else if (javaStatement instanceof DoStatement) {
      statement = convertDoStatement((DoStatement) javaStatement);
    } else if (javaStatement instanceof SwitchStatement) {
      statement = convertSwitchStatement((SwitchStatement) javaStatement);
    } else if (javaStatement instanceof SwitchCase) {
      statement = convertSwitchCase((SwitchCase) javaStatement);
    } else if (javaStatement instanceof BreakStatement) {
      statement = f.newBreakStatement();
    } else if (javaStatement instanceof ContinueStatement) {
      statement = f.newContinueStatement();
    } else if (javaStatement instanceof EnhancedForStatement) {
      statement = convertEnhancedForStatement((EnhancedForStatement) javaStatement);
    } else if (javaStatement instanceof ReturnStatement) {
      statement = convertReturnStatement((ReturnStatement) javaStatement);
    } else if (javaStatement instanceof EmptyStatement) {
      statement = f.newNullStatement();
    } else if (javaStatement instanceof VariableDeclarationStatement) {
      statement = convertVariableDeclarationStatement((VariableDeclarationStatement) javaStatement);
    } else if (javaStatement instanceof ConstructorInvocation) {
      statement = convertConstructorInvocation((ConstructorInvocation) javaStatement);
    } else if (javaStatement instanceof LabeledStatement) {
    } else if (javaStatement instanceof TypeDeclarationStatement) {
    } else if (javaStatement instanceof SuperConstructorInvocation) {
    } else if (javaStatement instanceof AssertStatement) {
    } else if (javaStatement instanceof TryStatement) {
    } else if (javaStatement instanceof ThrowStatement) {
    } else if (javaStatement instanceof SynchronizedStatement) {
    } else {
      throw new RuntimeException("unsupported javaStatement: " + javaStatement);
    }
  }

  private IASTStatement convertConstructorInvocation(final ConstructorInvocation constructorInvocation) {
    final IASTExpression call = new ExpressionInfo(typeDeclaration.getName(), typeDeclaration, compilationUnitInfo).getExpression();
    final List<IASTInitializerClause> initializerClauses = new ArrayList<IASTInitializerClause>();
    for (final Object argumentObject : constructorInvocation.arguments()) {
      final ExpressionInfo argument = new ExpressionInfo((Expression) argumentObject, typeDeclaration, compilationUnitInfo);
      initializerClauses.add(argument.getExpression());
    }
    return f.newExpressionStatement(f.newFunctionCallExpression(call, initializerClauses.toArray(new IASTInitializerClause[initializerClauses.size()])));
  }

  private IASTStatement convertEnhancedForStatement(final EnhancedForStatement enhancedForStatement) {
    final ICPPASTRangeBasedForStatement rangeBased = f.newRangeBasedForStatement();

    final SingleVariableDeclarationInfo parameter = new SingleVariableDeclarationInfo(enhancedForStatement.getParameter(), compilationUnitInfo);

    final ICPPASTSimpleDeclSpecifier declSpecifier = f.newSimpleDeclSpecifier();
    declSpecifier.setStorageClass(IASTDeclSpecifier.sc_auto);

    final ICPPASTDeclarator declarator = parameter.getDeclarator();
    declarator.addPointerOperator(f.newReferenceOperator());
    final IASTSimpleDeclaration declaration = f.newSimpleDeclaration(declSpecifier);
    declaration.addDeclarator(declarator);

    rangeBased.setDeclaration(declaration);

    final ExpressionInfo expressionInfo = new ExpressionInfo(enhancedForStatement.getExpression(), typeDeclaration, compilationUnitInfo);
    rangeBased.setInitializerClause(expressionInfo.getExpression());

    final StatementInfo body = new StatementInfo(enhancedForStatement.getBody(), typeDeclaration, compilationUnitInfo);
    rangeBased.setBody(body.getStatement());
    return rangeBased;
  }

  private IASTStatement convertVariableDeclarationStatement(final VariableDeclarationStatement variableDeclarationStatement) {
    final VariableDeclarationStatementInfo statementInfo = new VariableDeclarationStatementInfo(variableDeclarationStatement, compilationUnitInfo);
    final IASTDeclarationStatement delcarationStatement = f.newDeclarationStatement(statementInfo.getDeclaration());
    return delcarationStatement;
  }

  private IASTStatement convertReturnStatement(final ReturnStatement returnStatement) {
    return returnStatement.getExpression() == null ? f.newReturnStatement(null) : f.newReturnStatement(new ExpressionInfo(returnStatement.getExpression(), typeDeclaration, compilationUnitInfo).getExpression());
  }

  private IASTStatement convertSwitchCase(final SwitchCase switchCase) {
    return switchCase.getExpression() == null ? f.newDefaultStatement() : f.newCaseStatement(new ExpressionInfo(switchCase.getExpression(), typeDeclaration, compilationUnitInfo).getExpression());
  }

  private IASTStatement convertSwitchStatement(final SwitchStatement switchStatement) {
    final ExpressionInfo controller = new ExpressionInfo(switchStatement.getExpression(), typeDeclaration, compilationUnitInfo);
    final IASTCompoundStatement body = f.newCompoundStatement();
    for (final Object statementObject : switchStatement.statements()) {
      final StatementInfo statementInfo = new StatementInfo((Statement) statementObject, typeDeclaration, compilationUnitInfo);
      body.addStatement(statementInfo.getStatement());
    }
    return f.newSwitchStatement(controller.getExpression(), body);
  }

  private IASTStatement convertDoStatement(final DoStatement doStatement) {
    final ExpressionInfo condition = new ExpressionInfo(doStatement.getExpression(), typeDeclaration, compilationUnitInfo);
    final StatementInfo body = new StatementInfo(doStatement.getBody(), typeDeclaration, compilationUnitInfo);
    return f.newDoStatement(body.getStatement(), condition.getExpression());
  }

  private IASTStatement convertWhileStatement(final WhileStatement whileStatement) {
    final ExpressionInfo condition = new ExpressionInfo(whileStatement.getExpression(), typeDeclaration, compilationUnitInfo);
    final StatementInfo body = new StatementInfo(whileStatement.getBody(), typeDeclaration, compilationUnitInfo);
    return f.newWhileStatement(condition.getExpression(), body.getStatement());
  }

  private IASTStatement convertForStatement(final ForStatement forStatement) {
    IASTStatement init;
    if (forStatement.initializers().isEmpty()) {
      init = f.newDefaultStatement();
    } else {
      final VariableDeclarationExpressionInfo initExpression = new VariableDeclarationExpressionInfo((VariableDeclarationExpression) forStatement.initializers().get(0), compilationUnitInfo);
      final IASTDeclarationStatement declarationStatement = f.newDeclarationStatement(initExpression.getDeclaration());
      init = declarationStatement;
    }

    final ExpressionInfo condition = new ExpressionInfo(forStatement.getExpression(), typeDeclaration, compilationUnitInfo);

    IASTExpression iterationExpression;
    if (forStatement.updaters().isEmpty()) {
      iterationExpression = f.newIdExpression(f.newName());
    } else {
      final ICPPASTExpressionList expressionList = f.newExpressionList();
      for (final Object updaterObject : forStatement.updaters()) {
        final IASTExpression updater = new ExpressionInfo((Expression) updaterObject, typeDeclaration, compilationUnitInfo).getExpression();
        expressionList.addExpression(updater);
      }
      iterationExpression = expressionList;
    }

    final StatementInfo body = new StatementInfo(forStatement.getBody(), typeDeclaration, compilationUnitInfo);

    return f.newForStatement(init, condition.getExpression(), iterationExpression, body.getStatement());
  }

  private IASTStatement convertIfStatement(final IfStatement ifStatement) {
    final ExpressionInfo condition = new ExpressionInfo(ifStatement.getExpression(), typeDeclaration, compilationUnitInfo);
    final StatementInfo thenStatement = new StatementInfo(ifStatement.getThenStatement(), typeDeclaration, compilationUnitInfo);
    final StatementInfo elseStatement = ifStatement.getElseStatement() == null ? null : new StatementInfo(ifStatement.getElseStatement(), typeDeclaration, compilationUnitInfo);
    return f.newIfStatement(condition.getExpression(), thenStatement.getStatement(), elseStatement == null ? null : elseStatement.getStatement());
  }

  private IASTStatement convertExpressionStatement(final ExpressionStatement expressionStatement) {
    return f.newExpressionStatement(new ExpressionInfo(expressionStatement.getExpression(), typeDeclaration, compilationUnitInfo).getExpression());
  }

  private IASTStatement convertBlock(final Block block) {
    final IASTCompoundStatement compoundStatement = f.newCompoundStatement();
    for (final Object statementObject : block.statements()) {
      final StatementInfo statementInfo = new StatementInfo((Statement) statementObject, typeDeclaration, compilationUnitInfo);
      compoundStatement.addStatement(statementInfo.getStatement());
    }
    return compoundStatement;
  }

  public IASTStatement getStatement() {
    return statement;
  }
}
