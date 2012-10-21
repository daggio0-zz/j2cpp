package me.pixodro.j2cpp.core.info;

import java.util.HashSet;
import java.util.Set;

import me.pixodro.j2cpp.core.Converter;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * User: bquenin
 * Date: 21/06/12
 * Time: 00:25
 */
public class CompilationUnitInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();
  private final CPPASTTranslationUnit hpp = new CPPASTTranslationUnit();
  private final CPPASTTranslationUnit cpp = new CPPASTTranslationUnit();
  public final Set<String> hppIncludes = new HashSet<String>();
  public final Set<String> hppStdIncludes = new HashSet<String>();
  public final Set<String> cppIncludes = new HashSet<String>();
  public final Set<String> cppStdIncludes = new HashSet<String>();
  private final String name;

  public CompilationUnitInfo(final CompilationUnit compilationUnit) {
    // Only handle one class per AST
    if (compilationUnit.types().size() != 1) {
      throw new IllegalArgumentException("Multiple type declaration per compilation unit not supported");
    }

    // for (final Object importObject : compilationUnit.imports()) {
    // final ImportDeclaration importDeclaration = (ImportDeclaration) importObject;
    // if (importDeclaration.isStatic()) {
    // continue;
    // }
    // final NameInfo importName = new NameInfo(importDeclaration.getName());
    //
    // final List<String> tokens = importName.tokenize();
    // // Exclude java imports as they're not supported
    // if (!tokens.get(0).contains("java")) {
    // hppIncludes.add(tokens.get(tokens.size() - 1));
    // }
    // }

    // Namespaces
    // ICPPASTNamespaceDefinition currentNamespace = null, rootNamespace = null;
    // PackageDeclaration packageDeclaration = java.getPackage();
    // List<String> packageName = new ArrayList<String>();
    // if (packageDeclaration == null) {
    // throw new IllegalStateException("No package declaration for compilation unit");
    // }
    //
    // packageName.addAll(new NameInfo(packageDeclaration.getName()).tokenize());
    // for (String name : packageName) {
    // if (currentNamespace == null) {
    // rootNamespace = currentNamespace = f.newNamespaceDefinition(f.newName(name.toCharArray()));
    // } else {
    // ICPPASTNamespaceDefinition newNamespace =
    // f.newNamespaceDefinition(f.newName(name.toCharArray()));
    // currentNamespace.addDeclaration(newNamespace);
    // currentNamespace = newNamespace;
    // }
    // }

    // Determine the type
    final AbstractTypeDeclaration type = (AbstractTypeDeclaration) compilationUnit.types().iterator().next();
    if (type instanceof TypeDeclaration) {
      name = new NameInfo(((TypeDeclaration) type).getName()).getNameAsString();
      // for (Object importObject : java.imports()) {
      // ImportDeclaration importDeclaration = (ImportDeclaration) importObject;
      // if (importDeclaration.isStatic()) {
      // continue;
      // }
      // NameInfo importName = new NameInfo(importDeclaration.getName());
      //
      // List<String> tokens = importName.tokenize();
      // // Exclude java imports as they're not supported
      // if (!tokens.get(0).contains("java")) {
      // hpp.addDeclaration(f.newUsingDirective(importName.getName()));
      // }
      // }

      final TypeDeclarationInfo typeDeclarationInfo = new TypeDeclarationInfo((TypeDeclaration) type, null, null, this);
      // Header file
      // if (rootNamespace == null) { // default package
      hpp.addDeclaration(buildTypeDeclaration(typeDeclarationInfo));
      // } else {
      // if (currentNamespace == null) {
      // throw new IllegalStateException("Current namespace shouldn't be null");
      // }
      // currentNamespace.addDeclaration(buildTypeDeclaration(typeDeclarationInfo));
      // hpp.addDeclaration(rootNamespace);
      // }

      // CPP File
      buildTypeDefinition(cpp, typeDeclarationInfo);
    } else if (type instanceof EnumDeclaration) {
      name = new NameInfo(((EnumDeclaration) type).getName()).getNameAsString();
      final EnumDeclarationInfo enumDeclarationInfo = new EnumDeclarationInfo((EnumDeclaration) type);
      hpp.addDeclaration(buildEnumDeclaration(enumDeclarationInfo));
      // } else if (type instanceof AnnotationTypeDeclaration) {
    } else {
      throw new UnsupportedOperationException("Unsupported type declaration " + type.getClass().getName());
    }
  }

  private IASTDeclaration buildEnumDeclaration(final EnumDeclarationInfo enumDeclarationInfo) {
    // Ugly hack because ASTWriter doesn't support "new enums"
    final IASTName enumerationName = f.newName(("class " + enumDeclarationInfo.getName().getIdentifier()).toCharArray());
    final ICPPASTEnumerationSpecifier enumerationSpecifier = f.newEnumerationSpecifier(enumerationName);

    enumerationSpecifier.setIsScoped(true);
    for (final EnumConstantDeclarationInfo enumConstantDeclaration : enumDeclarationInfo.enumConstantDeclarations()) {
      enumerationSpecifier.addEnumerator(buildEnumConstantDeclaration(enumConstantDeclaration));
    }
    return f.newSimpleDeclaration(enumerationSpecifier);
  }

  private IASTEnumerator buildEnumConstantDeclaration(final EnumConstantDeclarationInfo enumConstantDeclaration) {
    return f.newEnumerator(new NameInfo(enumConstantDeclaration.getName()).getName(), null);
  }

  private IASTDeclaration buildTypeDeclaration(final TypeDeclarationInfo typeDeclarationInfo) {
    final ICPPASTCompositeTypeSpecifier compositeTypeSpecifier = f.newCompositeTypeSpecifier(ICPPASTCompositeTypeSpecifier.k_class, new NameInfo(typeDeclarationInfo.getName()).getName());
    if (typeDeclarationInfo.getTypeDeclaration().getSuperclassType() != null) {
      final TypeInfo typeInfo = new TypeInfo(typeDeclarationInfo.getTypeDeclaration().getSuperclassType(), this);
      compositeTypeSpecifier.addBaseSpecifier(f.newBaseSpecifier(typeInfo.getName(), 0, false));
    }

    compositeTypeSpecifier.addDeclaration(f.newVisibilityLabel(ICPPASTVisibilityLabel.v_private));
    for (final TypeDeclarationInfo subType : typeDeclarationInfo.subTypes()) {
      if (subType.getModifiers().isPrivate) {
        compositeTypeSpecifier.addDeclaration(buildTypeDeclaration(subType));
      }
    }
    for (final FieldDeclarationInfo field : typeDeclarationInfo.fields()) {
      if (field.getModifiers().isPrivate) {
        compositeTypeSpecifier.addDeclaration(field.getDeclaration());
        typeDeclarationInfo.orderedFields.add(field);
      }
    }
    for (final MethodDeclarationInfo method : typeDeclarationInfo.methods()) {
      if (method.getModifiers().isPrivate && !Converter.excludedJavaMethods.contains(method.getMethodDeclaration().getName().getIdentifier())) {
        compositeTypeSpecifier.addDeclaration(method.getDeclaration());
      }
    }

    compositeTypeSpecifier.addDeclaration(f.newVisibilityLabel(ICPPASTVisibilityLabel.v_protected));
    for (final TypeDeclarationInfo subType : typeDeclarationInfo.subTypes()) {
      if (subType.getModifiers().isProtected) {
        compositeTypeSpecifier.addDeclaration(buildTypeDeclaration(subType));
      }
    }
    for (final FieldDeclarationInfo field : typeDeclarationInfo.fields()) {
      if (field.getModifiers().isProtected) {
        compositeTypeSpecifier.addDeclaration(field.getDeclaration());
        typeDeclarationInfo.orderedFields.add(field);
      }
    }
    for (final MethodDeclarationInfo method : typeDeclarationInfo.methods()) {
      if (method.getModifiers().isProtected && !Converter.excludedJavaMethods.contains(method.getMethodDeclaration().getName().getIdentifier())) {
        compositeTypeSpecifier.addDeclaration(method.getDeclaration());
      }
    }

    compositeTypeSpecifier.addDeclaration(f.newVisibilityLabel(ICPPASTVisibilityLabel.v_public));
    for (final TypeDeclarationInfo subType : typeDeclarationInfo.subTypes()) {
      if (subType.getModifiers().isPublic) {
        compositeTypeSpecifier.addDeclaration(buildTypeDeclaration(subType));
      }
    }
    for (final FieldDeclarationInfo field : typeDeclarationInfo.fields()) {
      if (field.getModifiers().isPublic) {
        compositeTypeSpecifier.addDeclaration(field.getDeclaration());
        typeDeclarationInfo.orderedFields.add(field);
      }
    }
    for (final MethodDeclarationInfo method : typeDeclarationInfo.methods()) {
      if (method.getModifiers().isPublic && !Converter.excludedJavaMethods.contains(method.getMethodDeclaration().getName().getIdentifier())) {
        compositeTypeSpecifier.addDeclaration(method.getDeclaration());
      }
    }

    // Build includes
    // for (final FieldDeclarationInfo field : typeDeclarationInfo.fields()) {
    // if (field.getTypeInfo() != null) {
    // if (field.getTypeInfo().getType().isSimpleType()) {
    // final SimpleType simpleType = (SimpleType) field.getTypeInfo().getType();
    // if (!simpleType.resolveBinding().isMember()) {
    // hppIncludes.add(new NameInfo(simpleType.getName()).tokenize().get(0));
    // }
    // }
    // }
    // }
    // for (final MethodDeclarationInfo method : typeDeclarationInfo.methods()) {
    // final Type returnType = method.getMethodDeclaration().getReturnType2();
    // if (returnType != null) {
    // if (returnType.isSimpleType()) {
    // final SimpleType simpleType = (SimpleType) returnType;
    // if (!simpleType.resolveBinding().isMember()) {
    // hppIncludes.add(new NameInfo(simpleType.getName()).tokenize().get(0));
    // }
    // }
    // }
    // }
    return f.newSimpleDeclaration(compositeTypeSpecifier);
  }

  private void buildTypeDefinition(final CPPASTTranslationUnit translationUnit, final TypeDeclarationInfo typeDeclarationInfo) {
    for (final MethodDeclarationInfo method : typeDeclarationInfo.methods()) {
      // if (method.isConstructor()) {
      // // We need to add member initializers to constructors
      // for (final FieldDeclarationInfo field : typeDeclarationInfo.orderedFields) {
      // if (!(field.getModifiers().isStatic || (field.getTypeInfo() == null) ||
      // field.getTypeInfo().isArray())) {
      // Type type = field.getTypeInfo().getType();
      // if (type.isParameterizedType()) {
      // ParameterizedType parameterizedType = (ParameterizedType) type;
      // SimpleType simpleType = (SimpleType) parameterizedType.getType();
      // String parameterizedTypeName = simpleType.getName().toString();
      // if (STLConverter.isSTLType(parameterizedTypeName)) {
      // continue;
      // }
      // }
      // for (final Map.Entry<IASTName, IASTExpression> fragment : field.getFragments().entrySet())
      // {
      // final List<IASTInitializerClause> initializerClauses = new
      // ArrayList<IASTInitializerClause>();
      // initializerClauses.add(fragment.getValue() == null ?
      // field.getTypeInfo().getJavaDefaultValue() : fragment.getValue());
      // final ICPPASTConstructorInitializer initializer =
      // f.newConstructorInitializer(initializerClauses.toArray(new
      // IASTInitializerClause[initializerClauses.size()]));
      // final ICPPASTConstructorChainInitializer chainInitializer =
      // f.newConstructorChainInitializer(fragment.getKey(), initializer);
      // method.getDefinition().addMemberInitializer(chainInitializer);
      // }
      // }
      // }
      // }
      if ((method.getDefinition() != null) && !Converter.excludedJavaMethods.contains(method.getMethodDeclaration().getName().getIdentifier())) {
        translationUnit.addDeclaration(method.getDefinition());
      }
    }
    for (final TypeDeclarationInfo subType : typeDeclarationInfo.subTypes()) {
      buildTypeDefinition(translationUnit, subType);
    }
  }

  public CPPASTTranslationUnit getHpp() {
    return hpp;
  }

  public CPPASTTranslationUnit getCpp() {
    return cpp;
  }

  public String getName() {
    return name;
  }
}
