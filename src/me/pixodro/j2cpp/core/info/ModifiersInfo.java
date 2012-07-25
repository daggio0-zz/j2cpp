package me.pixodro.j2cpp.core.info;

import java.util.List;

import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * User: bquenin
 * Date: 27/06/12
 * Time: 20:17
 */
public class ModifiersInfo {
  public boolean isStatic;
  public boolean isFinal;
  public boolean isPrivate;
  public boolean isProtected;
  public boolean isPublic;
  public boolean isVolatile;
  public boolean isAbstract;
  public boolean isPackagePrivate;

  public ModifiersInfo(final boolean aStatic, final boolean aFinal, final boolean aPrivate, final boolean aProtected, final boolean aPublic, final boolean aVolatile, final boolean anAbstract) {
    isStatic = aStatic;
    isFinal = aFinal;
    isPrivate = aPrivate;
    isProtected = aProtected;
    isPublic = aPublic;
    isVolatile = aVolatile;
    isAbstract = anAbstract;
  }

  public ModifiersInfo(final List<?> modifiers) {
    for (final Object modifierObject : modifiers) {
      final IExtendedModifier extendedModifier = (IExtendedModifier) modifierObject;
      if (extendedModifier.isModifier()) {
        final Modifier modifier = (Modifier) modifierObject;

        if (modifier.isStatic()) {
          isStatic = true;
        } else if (modifier.isFinal()) {
          isFinal = true;
        } else if (modifier.isPrivate()) {
          isPrivate = true;
        } else if (modifier.isProtected()) {
          isProtected = true;
        } else if (modifier.isPublic()) {
          isPublic = true;
        } else if (modifier.isVolatile()) {
          isVolatile = true;
        } else if (modifier.isAbstract()) {
          isAbstract = true;
        }
      } else if (extendedModifier.isAnnotation()) {
        // TODO: Manage annotation ?
      }
    }

    // Package visibility maps to private
    if (!(isPublic || isProtected || isPrivate)) {
      isPrivate = true;
      isPackagePrivate = true;
    }
  }
}
