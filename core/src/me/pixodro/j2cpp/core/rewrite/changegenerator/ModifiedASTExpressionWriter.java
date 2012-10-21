/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Institute for Software - initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package me.pixodro.j2cpp.core.rewrite.changegenerator;

import me.pixodro.j2cpp.core.rewrite.ASTWriterVisitor;
import me.pixodro.j2cpp.core.rewrite.ExpressionWriter;
import me.pixodro.j2cpp.core.rewrite.MacroExpansionHandler;
import me.pixodro.j2cpp.core.rewrite.Scribe;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTExpressionWriter extends ExpressionWriter {
  private final ASTModificationHelper modificationHelper;

  public ModifiedASTExpressionWriter(final Scribe scribe, final ASTWriterVisitor visitor, final MacroExpansionHandler macroHandler, final ModificationScopeStack stack, final NodeCommentMap commentMap) {
    super(scribe, visitor, macroHandler, commentMap);
    modificationHelper = new ASTModificationHelper(stack);
  }

  @Override
  protected void writeExpressions(final IASTExpressionList expList, final IASTExpression[] expressions) {
    final IASTExpression[] modifiedExpressions = modificationHelper.createModifiedChildArray(expList, expressions, IASTExpression.class, commentMap);
    super.writeExpressions(expList, modifiedExpressions);
  }

  @Override
  protected IASTInitializer getNewInitializer(final ICPPASTNewExpression newExp) {
    final IASTInitializer initializer = newExp.getInitializer();
    if (initializer != null) {
      for (final ASTModification childModification : modificationHelper.modificationsForNode(initializer)) {
        switch (childModification.getKind()) {
        case REPLACE:
          if (childModification.getNewNode() instanceof IASTInitializer) {
            return (IASTInitializer) childModification.getNewNode();
          }
          break;
        case INSERT_BEFORE:
          throw new UnhandledASTModificationException(childModification);

        case APPEND_CHILD:
          throw new UnhandledASTModificationException(childModification);
        }
      }
    } else {
      for (final ASTModification parentModification : modificationHelper.modificationsForNode(newExp)) {
        if (parentModification.getKind() == ModificationKind.APPEND_CHILD) {
          final IASTNode newNode = parentModification.getNewNode();
          if (newNode instanceof IASTInitializer) {
            return (IASTInitializer) newNode;
          }
        }
      }
    }
    return initializer;
  }
}
