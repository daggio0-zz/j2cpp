package me.pixodro.j2cpp.core.info;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/15/12
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class VariableDeclarationFragmentInfo extends AbstractVariableFragmentDeclarationInfo {
  public VariableDeclarationFragmentInfo(final VariableDeclaration variableDeclaration, final Type type) {
    super(variableDeclaration, type);
  }
}
