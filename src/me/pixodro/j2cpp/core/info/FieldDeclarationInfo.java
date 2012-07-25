package me.pixodro.j2cpp.core.info;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * User: bquenin
 * Date: 27/06/12
 * Time: 19:52
 */
public class FieldDeclarationInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  private final IASTSimpleDeclaration declaration;
  private final ModifiersInfo modifiers;
  private TypeInfo typeInfo;
  private final Map<IASTName, IASTExpression> fragments = new HashMap<IASTName, IASTExpression>();
  private final FieldDeclaration fieldDeclaration;

  private CompilationUnitInfo compilationUnitInfo;

  public FieldDeclarationInfo(final IASTSimpleDeclaration declaration, final ModifiersInfo modifiers) {
    this.declaration = declaration;
    this.modifiers = modifiers;
    fieldDeclaration = null;
  }

  public FieldDeclarationInfo(final FieldDeclaration fieldDeclaration, final CompilationUnitInfo compilationUnitInfo) {
    this.fieldDeclaration = fieldDeclaration;
    this.compilationUnitInfo = compilationUnitInfo;
    modifiers = new ModifiersInfo(fieldDeclaration.modifiers());
    typeInfo = new TypeInfo(fieldDeclaration.getType(), compilationUnitInfo);

    final IASTDeclSpecifier declSpecifier = typeInfo.getDeclSpecifier();
    declaration = convertFragments(declSpecifier);

    if (modifiers.isStatic && modifiers.isFinal) {
      declSpecifier.setStorageClass(IASTDeclSpecifier.sc_static);
      declSpecifier.setConst(true);
    }
    if (modifiers.isStatic) {
      declSpecifier.setStorageClass(IASTDeclSpecifier.sc_static);
    }
  }

  private IASTSimpleDeclaration convertFragments(final IASTDeclSpecifier declSpecifier) {
    final IASTSimpleDeclaration simpleDeclaration = f.newSimpleDeclaration(declSpecifier);
    for (final Object fragmentObject : fieldDeclaration.fragments()) {
      final VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentObject;
      final VariableDeclarationFragmentInfo fragmentInfo = new VariableDeclarationFragmentInfo(fragment, fieldDeclaration.getType(), compilationUnitInfo);
      fragments.put(new NameInfo(fragment.getName()).getName(), fragmentInfo.initializer);
      if (fragmentInfo.getDeclarator().getInitializer() == null) {
        fragmentInfo.getDeclarator().setInitializer(f.newEqualsInitializer(typeInfo.getJavaDefaultValue()));
      }
      // if (!(modifiers.isStatic && modifiers.isFinal)) {
      // fragmentInfo.getDeclarator().setInitializer(null);
      // }
      simpleDeclaration.addDeclarator(fragmentInfo.getDeclarator());
    }
    return simpleDeclaration;
  }

  public IASTSimpleDeclaration getDeclaration() {
    return declaration;
  }

  public ModifiersInfo getModifiers() {
    return modifiers;
  }

  public TypeInfo getTypeInfo() {
    return typeInfo;
  }

  public Map<IASTName, IASTExpression> getFragments() {
    return fragments;
  }

  public FieldDeclaration getFieldDeclaration() {
    return fieldDeclaration;
  }
}