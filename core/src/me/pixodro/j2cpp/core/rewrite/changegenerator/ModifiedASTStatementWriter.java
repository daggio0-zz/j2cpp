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
 *******************************************************************************/
package me.pixodro.j2cpp.core.rewrite.changegenerator;

import me.pixodro.j2cpp.core.rewrite.ASTWriterVisitor;
import me.pixodro.j2cpp.core.rewrite.Scribe;
import me.pixodro.j2cpp.core.rewrite.StatementWriter;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTStatementWriter extends StatementWriter {
  private final ASTModificationHelper modificationHelper;

  public ModifiedASTStatementWriter(final Scribe scribe, final ASTWriterVisitor visitor, final ModificationScopeStack stack, final NodeCommentMap commentMap) {
    super(scribe, visitor, commentMap);
    modificationHelper = new ASTModificationHelper(stack);
  }

  @Override
  protected void writeBodyStatement(final IASTStatement statement, final boolean isDoStatement) {
    final IASTStatement replacementNode = modificationHelper.getNodeAfterReplacement(statement);
    super.writeBodyStatement(replacementNode, isDoStatement);
  }

  @Override
  protected void writeDeclarationWithoutSemicolon(final IASTDeclaration declaration) {
    final IASTDeclaration replacementNode = modificationHelper.getNodeAfterReplacement(declaration);
    super.writeDeclarationWithoutSemicolon(replacementNode);
  }

  @Override
  protected IASTStatement[] getNestedStatements(final IASTCompoundStatement compoundStatement) {
    return modificationHelper.createModifiedChildArray(compoundStatement, compoundStatement.getStatements(), IASTStatement.class, commentMap);
  }
}
