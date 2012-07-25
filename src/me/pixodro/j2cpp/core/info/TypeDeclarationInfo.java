package me.pixodro.j2cpp.core.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * User: bquenin
 * Date: 27/06/12
 * Time: 19:41
 */
public class TypeDeclarationInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  final SimpleName name;
  final List<TypeDeclarationInfo> subTypes = new ArrayList<TypeDeclarationInfo>();
  final List<FieldDeclarationInfo> fields = new ArrayList<FieldDeclarationInfo>();
  final List<MethodDeclarationInfo> methods = new ArrayList<MethodDeclarationInfo>();
  public List<FieldDeclarationInfo> orderedFields = new ArrayList<FieldDeclarationInfo>();

  final ModifiersInfo modifiers;
  private final TypeDeclarationInfo enclosingType;

  private final TypeDeclaration typeDeclaration;

  public TypeDeclarationInfo(final TypeDeclaration typeDeclaration, final TypeDeclarationInfo enclosingType, final List<String> namespace) {
    this.typeDeclaration = typeDeclaration;
    this.enclosingType = enclosingType;
    modifiers = new ModifiersInfo(typeDeclaration.modifiers());
    name = typeDeclaration.getName();

    for (final TypeDeclaration subtypeDeclaration : typeDeclaration.getTypes()) {
      final TypeDeclarationInfo subTypeDeclarationInfo = new TypeDeclarationInfo(subtypeDeclaration, this, namespace);
      subTypes.add(subTypeDeclarationInfo);
    }
    for (final FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
      final FieldDeclarationInfo fieldDeclarationInfo = new FieldDeclarationInfo(fieldDeclaration);
      fields.add(fieldDeclarationInfo);
    }
    for (final MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
      final MethodDeclarationInfo methodDeclarationInfo = new MethodDeclarationInfo(methodDeclaration, typeDeclaration, namespace, enclosingType);
      methods.add(methodDeclarationInfo);
    }

    // If the type is nested, simulate Java inner class access visibility by creating a
    // reference to the outer type on the constructor and store it in the object
    if (enclosingType != null) {
      final ICPPASTDeclSpecifier parentReferenceDeclSpecifier = f.newTypedefNameSpecifier(new NameInfo(enclosingType.getName()).getName());
      final IASTSimpleDeclaration parentReference = f.newSimpleDeclaration(parentReferenceDeclSpecifier);
      final ICPPASTDeclarator parentReferenceDeclarator = f.newDeclarator(f.newName("__parent".toCharArray()));
      parentReference.addDeclarator(parentReferenceDeclarator);
      parentReferenceDeclarator.addPointerOperator(f.newPointer());
      final ModifiersInfo modifiersInfo = new ModifiersInfo(false, false, true, false, false, false, false);
      fields.add(new FieldDeclarationInfo(parentReference, modifiersInfo));
    }
  }

  public SimpleName getName() {
    return name;
  }

  public List<TypeDeclarationInfo> subTypes() {
    return Collections.unmodifiableList(subTypes);
  }

  public List<FieldDeclarationInfo> fields() {
    return Collections.unmodifiableList(fields);
  }

  public List<MethodDeclarationInfo> methods() {
    return Collections.unmodifiableList(methods);
  }

  public ModifiersInfo getModifiers() {
    return modifiers;
  }

  public TypeDeclarationInfo getEnclosingType() {
    return enclosingType;
  }

  public TypeDeclaration getTypeDeclaration() {
    return typeDeclaration;
  }
}