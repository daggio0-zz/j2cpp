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
import me.pixodro.j2cpp.core.rewrite.DeclarationWriter;
import me.pixodro.j2cpp.core.rewrite.Scribe;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclarationWriter extends DeclarationWriter {
  private final ASTModificationHelper modificationHelper;

  public ModifiedASTDeclarationWriter(final Scribe scribe, final ASTWriterVisitor visitor, final ModificationScopeStack stack, final NodeCommentMap commentMap) {
    super(scribe, visitor, commentMap);
    modificationHelper = new ASTModificationHelper(stack);
  }

  @Override
  protected void writeDeclarationsInNamespace(final ICPPASTNamespaceDefinition namespaceDefinition, final IASTDeclaration[] declarations) {
    final IASTDeclaration[] modifiedDeclarations = modificationHelper.createModifiedChildArray(namespaceDefinition, declarations, IASTDeclaration.class, commentMap);
    super.writeDeclarationsInNamespace(namespaceDefinition, modifiedDeclarations);
  }

  @Override
  protected void writeCtorChainInitializer(final ICPPASTFunctionDefinition funcDec, final ICPPASTConstructorChainInitializer[] ctorInitChain) {
    final ICPPASTConstructorChainInitializer[] modifiedInitializer = modificationHelper.createModifiedChildArray(funcDec, ctorInitChain, ICPPASTConstructorChainInitializer.class, commentMap);
    super.writeCtorChainInitializer(funcDec, modifiedInitializer);
  }
}
