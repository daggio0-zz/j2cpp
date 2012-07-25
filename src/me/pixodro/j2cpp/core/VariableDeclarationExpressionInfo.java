package me.pixodro.j2cpp.core;

import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/9/12
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariableDeclarationExpressionInfo extends AbstractVariableDeclarationInfo {
  public VariableDeclarationExpressionInfo(final VariableDeclarationExpression variableDeclarationStatement) {
    super(variableDeclarationStatement);
  }
}
