package me.pixodro.j2cpp.core;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * User: bquenin
 * Date: 27/06/12
 * Time: 20:57
 */
public class MethodDeclarationInfo {
  private final MethodDeclaration methodDeclaration;
  private final ModifiersInfo modifiers;
  private final IASTSimpleDeclaration declaration;
  private final ICPPASTFunctionDefinition definition;

  public MethodDeclarationInfo(final MethodDeclaration methodDeclaration, final TypeDeclaration typeDeclaration, @SuppressWarnings("unused") final List<String> namespace, final TypeDeclarationInfo enclosingType) {
    this.methodDeclaration = methodDeclaration;
    modifiers = new ModifiersInfo(methodDeclaration.modifiers());

    // Declaration
    declaration = new FunctionDeclarationInfo(methodDeclaration, typeDeclaration, enclosingType).getDeclaration();

    // Definition
    if (!(modifiers.isAbstract || typeDeclaration.isInterface())) {
      definition = new FunctionDefinitionInfo(methodDeclaration, typeDeclaration, enclosingType).getDefinition();
    } else {
      definition = null;
    }
  }

  public IASTSimpleDeclaration getDeclaration() {
    return declaration;
  }

  public ICPPASTFunctionDefinition getDefinition() {
    return definition;
  }

  public ModifiersInfo getModifiers() {
    return modifiers;
  }

  public boolean isConstructor() {
    return methodDeclaration.isConstructor();
  }

  public MethodDeclaration getMethodDeclaration() {
    return methodDeclaration;
  }
}
