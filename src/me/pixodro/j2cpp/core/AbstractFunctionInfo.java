package me.pixodro.j2cpp.core;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class AbstractFunctionInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();
  protected final CPPASTFunctionDeclarator functionDeclarator;
  protected final ICPPASTDeclSpecifier declSpecifier;
  protected final ModifiersInfo modifiers;

  public AbstractFunctionInfo(final MethodDeclaration methodDeclaration, final TypeDeclarationInfo enclosingType) {
    modifiers = new ModifiersInfo(methodDeclaration.modifiers());
    functionDeclarator = new CPPASTFunctionDeclarator();

    // Return type
    if (methodDeclaration.isConstructor()) {
      declSpecifier = f.newSimpleDeclSpecifier();
    } else {
      final TypeInfo returnTypeInfo = new TypeInfo(methodDeclaration.getReturnType2());
      declSpecifier = returnTypeInfo.getDeclSpecifier();
      if (returnTypeInfo.isSimple() && !returnTypeInfo.getType().resolveBinding().isEnum()) {
        functionDeclarator.addPointerOperator(f.newPointer());
      } else if (methodDeclaration.getReturnType2().isArrayType()) {
        final ArrayType arrayType = (ArrayType) methodDeclaration.getReturnType2();
        for (int i = 0; i < arrayType.getDimensions(); i++) {
          functionDeclarator.addPointerOperator(f.newPointer());
        }
      }
    }

    // Parameters
    if (methodDeclaration.isConstructor() && (enclosingType != null)) {
      // If the type is nested, simulate Java inner class access visibility by creating a
      // reference to the outer type on the constructor
      final ICPPASTDeclSpecifier parentReferenceDeclSpecifier = f.newTypedefNameSpecifier(new NameInfo(enclosingType.getName()).getName());
      final ICPPASTDeclarator parentReferenceDeclarator = f.newDeclarator(f.newName("__parent".toCharArray()));
      parentReferenceDeclarator.addPointerOperator(f.newPointer());
      final IASTParameterDeclaration parentReference = f.newParameterDeclaration(parentReferenceDeclSpecifier, parentReferenceDeclarator);
      functionDeclarator.addParameterDeclaration(parentReference);
    }

    for (final Object parameterObject : methodDeclaration.parameters()) {
      final SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameterObject;
      final SingleVariableDeclarationInfo declarationInfo = new SingleVariableDeclarationInfo(parameter);
      final IASTParameterDeclaration parameterDeclaration = f.newParameterDeclaration(declarationInfo.getDeclSpecifier(), declarationInfo.getDeclarator());
      functionDeclarator.addParameterDeclaration(parameterDeclaration);
    }
  }
}
