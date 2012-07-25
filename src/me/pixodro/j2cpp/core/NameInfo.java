package me.pixodro.j2cpp.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/15/12
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class NameInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  private final IASTName name;
  private final Name javaName;

  public NameInfo(final Name javaName) {
    this.javaName = javaName;
    if (javaName.isSimpleName()) {
      name = convertSimpleName((SimpleName) javaName);
    } else if (javaName.isQualifiedName()) {
      name = convertQualifiedName((QualifiedName) javaName);
    } else {
      throw new IllegalStateException("Unsupported javaName: " + javaName);
    }
  }

  private IASTName convertSimpleName(final SimpleName simpleName) {
    return f.newName(simpleName.getIdentifier().toCharArray());
  }

  private IASTName convertQualifiedName(final QualifiedName qualifiedName) {
    final ICPPASTQualifiedName iastQualifiedName = f.newQualifiedName();
    iastQualifiedName.addName(new NameInfo(qualifiedName.getQualifier()).name);
    iastQualifiedName.addName(new NameInfo(qualifiedName.getName()).name);
    return iastQualifiedName;
  }

  public IASTName getName() {
    return name;
  }

  public List<String> tokenize() {
    final List<String> tokens = new ArrayList<String>();
    if (javaName.isQualifiedName()) {
      final QualifiedName qualifiedName = (QualifiedName) javaName;
      tokens.addAll(new NameInfo(qualifiedName.getQualifier()).tokenize());
      tokens.add(qualifiedName.getName().getIdentifier());
    } else if (javaName.isSimpleName()) {
      final SimpleName simpleName = (SimpleName) javaName;
      tokens.add(simpleName.getIdentifier());
    } else {
      throw new IllegalStateException("Unsupported name: " + javaName);
    }
    return tokens;
  }
}
