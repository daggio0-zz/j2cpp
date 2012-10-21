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
import me.pixodro.j2cpp.core.rewrite.DeclSpecWriter;
import me.pixodro.j2cpp.core.rewrite.Scribe;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclSpecWriter extends DeclSpecWriter {
  private final ASTModificationHelper modificationHelper;

  public ModifiedASTDeclSpecWriter(final Scribe scribe, final ASTWriterVisitor visitor, final ModificationScopeStack stack, final NodeCommentMap commentMap) {
    super(scribe, visitor, commentMap);
    modificationHelper = new ASTModificationHelper(stack);
  }

  @Override
  protected IASTDeclaration[] getMembers(final IASTCompositeTypeSpecifier compDeclSpec) {
    return modificationHelper.createModifiedChildArray(compDeclSpec, compDeclSpec.getMembers(), IASTDeclaration.class, commentMap);
  }
}
