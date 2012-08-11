package me.pixodro.j2cpp.core.info;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class FunctionDefinitionInfo extends AbstractFunctionInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();
  private final ICPPASTFunctionDefinition definition;

  public FunctionDefinitionInfo(final MethodDeclaration methodDeclaration, final TypeDeclaration typeDeclaration, final TypeDeclarationInfo enclosingType, final CompilationUnitInfo compilationUnitInfo) {
    super(methodDeclaration, enclosingType, compilationUnitInfo);
    final ICPPASTQualifiedName qualifiedName = f.newQualifiedName();
    // for (String name : namespace) {
    // qualifiedName.addName(f.newName(name.toCharArray()));
    // }
    if (enclosingType != null) {
      qualifiedName.addName(new NameInfo(enclosingType.getName()).getName());
    }
    qualifiedName.addName(new NameInfo(typeDeclaration.getName()).getName());
    qualifiedName.addName(new NameInfo(methodDeclaration.getName()).getName());

    // Declarator use for function definition (cpp)
    functionDeclarator.setName(qualifiedName);

    // Method body
    final StatementInfo statementInfo = new StatementInfo(methodDeclaration.getBody(), typeDeclaration, compilationUnitInfo);
    definition = f.newFunctionDefinition(declSpecifier, functionDeclarator, statementInfo.getStatement());
    if (methodDeclaration.isConstructor() && (enclosingType != null)) {
      final ICPPASTInitializerList initializerList = f.newInitializerList();
      initializerList.addClause(f.newIdExpression(f.newName("__parent".toCharArray())));
      definition.addMemberInitializer(f.newConstructorChainInitializer(f.newName("__parent".toCharArray()), initializerList));
    }
  }

  public ICPPASTFunctionDefinition getDefinition() {
    return definition;
  }
}
