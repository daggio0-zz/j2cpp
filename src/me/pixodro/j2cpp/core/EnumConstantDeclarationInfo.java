package me.pixodro.j2cpp.core;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class EnumConstantDeclarationInfo {
  private final SimpleName name;

  public EnumConstantDeclarationInfo(final EnumConstantDeclaration enumConstantDeclaration) {
    name = enumConstantDeclaration.getName();
  }

  public SimpleName getName() {
    return name;
  }
}
