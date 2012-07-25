package me.pixodro.j2cpp.core.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class EnumDeclarationInfo {
  final SimpleName name;
  final List<EnumConstantDeclarationInfo> enumConstantDeclarations = new ArrayList<EnumConstantDeclarationInfo>();

  final ModifiersInfo modifiers;

  public EnumDeclarationInfo(final EnumDeclaration enumDeclaration) {
    modifiers = new ModifiersInfo(enumDeclaration.modifiers());
    name = enumDeclaration.getName();

    for (final Object enumConstantDeclarationObject : enumDeclaration.enumConstants()) {
      final EnumConstantDeclarationInfo enumConstantDeclarationInfo = new EnumConstantDeclarationInfo((EnumConstantDeclaration) enumConstantDeclarationObject);
      enumConstantDeclarations.add(enumConstantDeclarationInfo);
    }
  }

  public SimpleName getName() {
    return name;
  }

  public List<EnumConstantDeclarationInfo> enumConstantDeclarations() {
    return Collections.unmodifiableList(enumConstantDeclarations);
  }

  public ModifiersInfo getModifiers() {
    return modifiers;
  }
}
