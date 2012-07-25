package me.pixodro.j2cpp.core.info;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/9/12
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleVariableDeclarationInfo extends AbstractVariableFragmentDeclarationInfo {
  public SingleVariableDeclarationInfo(final SingleVariableDeclaration singleVariableDeclaration) {
    super(singleVariableDeclaration, singleVariableDeclaration.getType());
  }
}
