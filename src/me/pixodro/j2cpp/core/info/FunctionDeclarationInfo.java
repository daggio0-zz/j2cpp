package me.pixodro.j2cpp.core.info;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class FunctionDeclarationInfo extends AbstractFunctionInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();
  private final IASTSimpleDeclaration declaration;

  public FunctionDeclarationInfo(final MethodDeclaration methodDeclaration, final TypeDeclaration typeDeclaration, final TypeDeclarationInfo enclosingType, final CompilationUnitInfo compilationUnitInfo) {
    super(methodDeclaration, enclosingType, compilationUnitInfo);

    functionDeclarator.setName(new NameInfo(methodDeclaration.getName()).getName());

    declaration = f.newSimpleDeclaration(declSpecifier);
    declaration.addDeclarator(functionDeclarator);

    if (modifiers.isStatic) {
      declSpecifier.setStorageClass(IASTDeclSpecifier.sc_static);
    }
    if (modifiers.isAbstract) {
      // Virtual functions
      declSpecifier.setVirtual(true);
      functionDeclarator.setPureVirtual(true);
    } else if (typeDeclaration.isInterface()) {
      // Virtual functions
      declSpecifier.setVirtual(true);
    }
  }

  public IASTSimpleDeclaration getDeclaration() {
    return declaration;
  }
}
