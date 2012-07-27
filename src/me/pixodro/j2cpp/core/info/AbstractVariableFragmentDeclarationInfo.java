package me.pixodro.j2cpp.core.info;

import me.pixodro.j2cpp.core.Converter;

import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/9/12
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class AbstractVariableFragmentDeclarationInfo {
  protected static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  protected final ICPPASTDeclarator declarator;
  protected final ICPPASTDeclSpecifier declSpecifier;
  protected final IASTExpression initializer;

  public AbstractVariableFragmentDeclarationInfo(final VariableDeclaration variableDeclaration, final Type type, final CompilationUnitInfo compilationUnitInfo) {
    declSpecifier = new TypeInfo(type, compilationUnitInfo).getDeclSpecifier();

    if (type.isArrayType()) {
      final ICPPASTArrayDeclarator arrayDeclarator = f.newArrayDeclarator(new NameInfo(variableDeclaration.getName()).getName());
      arrayDeclarator.addPointerOperator(f.newPointer());
      if (variableDeclaration.getInitializer() != null) {
        final ArrayCreation arrayCreation = (ArrayCreation) variableDeclaration.getInitializer();
        for (final Object dimensionObject : arrayCreation.dimensions()) {
          final ExpressionInfo dimension = new ExpressionInfo((Expression) dimensionObject, null, compilationUnitInfo);
          arrayDeclarator.addArrayModifier(f.newArrayModifier(dimension.getExpression()));
        }
      } else {
        final ArrayType arrayType = (ArrayType) type;
        for (int i = 0; i < arrayType.getDimensions(); i++) {
          arrayDeclarator.addPointerOperator(f.newPointer());
        }
      }
      initializer = null;
      declarator = arrayDeclarator;
    } else {
      declarator = f.newDeclarator(new NameInfo(variableDeclaration.getName()).getName());
      initializer = variableDeclaration.getInitializer() == null ? null : new ExpressionInfo(variableDeclaration.getInitializer(), null, compilationUnitInfo).getExpression();
      final ITypeBinding typeBinding = type.resolveBinding();
      if ((initializer != null) && !Converter.collectionClasses.contains(typeBinding.getName())) {
        final IASTEqualsInitializer equalsInitializer = f.newEqualsInitializer(initializer);
        declarator.setInitializer(equalsInitializer);
      }
      if ((type.isSimpleType() || type.isParameterizedType()) && !typeBinding.isEnum()) {
        declarator.addPointerOperator(f.newPointer());
      }
    }
  }

  public ICPPASTDeclSpecifier getDeclSpecifier() {
    return declSpecifier;
  }

  public ICPPASTDeclarator getDeclarator() {
    return declarator;
  }
}
