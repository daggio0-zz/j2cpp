package me.pixodro.j2cpp.core.info;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/9/12
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class AbstractVariableDeclarationInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  private final IASTSimpleDeclaration declaration;

  private final CompilationUnitInfo compilationUnitInfo;

  public AbstractVariableDeclarationInfo(final VariableDeclarationStatement variableDeclarationStatement, final CompilationUnitInfo compilationUnitInfo) {
    this.compilationUnitInfo = compilationUnitInfo;
    final VariableDeclarationWrapper wrapper = new VariableDeclarationWrapper(variableDeclarationStatement);
    declaration = convertWrapper(wrapper);
  }

  public AbstractVariableDeclarationInfo(final VariableDeclarationExpression variableDeclarationExpression, final CompilationUnitInfo compilationUnitInfo) {
    this.compilationUnitInfo = compilationUnitInfo;
    final VariableDeclarationWrapper wrapper = new VariableDeclarationWrapper(variableDeclarationExpression);
    declaration = convertWrapper(wrapper);
  }

  private IASTSimpleDeclaration convertWrapper(final VariableDeclarationWrapper wrapper) {
    final ModifiersInfo modifiers = new ModifiersInfo(wrapper.modifiers());
    final TypeInfo typeInfo = new TypeInfo(wrapper.getType(), compilationUnitInfo);

    final IASTDeclSpecifier declSpecifier = typeInfo.getDeclSpecifier();

    if (modifiers.isStatic && modifiers.isFinal) {
      declSpecifier.setStorageClass(IASTDeclSpecifier.sc_static);
      declSpecifier.setConst(true);
    }
    if (modifiers.isStatic) {
      declSpecifier.setStorageClass(IASTDeclSpecifier.sc_static);
    }

    return convertFragments(wrapper, declSpecifier);
  }

  private IASTSimpleDeclaration convertFragments(final VariableDeclarationWrapper wrapper, final IASTDeclSpecifier declSpecifier) {
    final IASTSimpleDeclaration simpleDeclaration = f.newSimpleDeclaration(declSpecifier);
    for (final Object fragmentObject : wrapper.fragments()) {
      final VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentObject;
      final VariableDeclarationFragmentInfo fragmentInfo = new VariableDeclarationFragmentInfo(fragment, wrapper.getType(), compilationUnitInfo);
      simpleDeclaration.addDeclarator(fragmentInfo.getDeclarator());

    }
    return simpleDeclaration;
  }

  public IASTSimpleDeclaration getDeclaration() {
    return declaration;
  }

  private class VariableDeclarationWrapper {
    private VariableDeclarationStatement statement;
    private VariableDeclarationExpression expression;

    public VariableDeclarationWrapper(final VariableDeclarationStatement statement) {
      this.statement = statement;
    }

    public VariableDeclarationWrapper(final VariableDeclarationExpression expression) {
      this.expression = expression;
    }

    public List<?> modifiers() {
      if (statement != null) {
        return statement.modifiers();
      }
      return expression.modifiers();
    }

    public Type getType() {
      if (statement != null) {
        return statement.getType();
      }
      return expression.getType();
    }

    public List<?> fragments() {
      if (statement != null) {
        return statement.fragments();
      }
      return expression.fragments();
    }
  }
}
