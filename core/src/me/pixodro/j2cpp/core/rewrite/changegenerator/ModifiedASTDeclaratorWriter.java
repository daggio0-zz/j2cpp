/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Institute for Software - initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Sergey Prigogin (Google)
 *******************************************************************************/
package me.pixodro.j2cpp.core.rewrite.changegenerator;

import me.pixodro.j2cpp.core.rewrite.ASTWriterVisitor;
import me.pixodro.j2cpp.core.rewrite.DeclaratorWriter;
import me.pixodro.j2cpp.core.rewrite.Scribe;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclaratorWriter extends DeclaratorWriter {
  private final ASTModificationHelper modificationHelper;

  public ModifiedASTDeclaratorWriter(final Scribe scribe, final ASTWriterVisitor visitor, final ModificationScopeStack stack, final NodeCommentMap commentMap) {
    super(scribe, visitor, commentMap);
    modificationHelper = new ASTModificationHelper(stack);
  }

  @Override
  protected void writeParameterDeclarations(final IASTStandardFunctionDeclarator funcDec, final IASTParameterDeclaration[] paraDecls) {
    final IASTParameterDeclaration[] modifiedParameters = modificationHelper.createModifiedChildArray(funcDec, paraDecls, IASTParameterDeclaration.class, commentMap);
    super.writeParameterDeclarations(funcDec, modifiedParameters);
  }

  @Override
  protected void writePointerOperators(final IASTDeclarator declarator, final IASTPointerOperator[] unmodifiedPointerOperations) {
    final IASTPointerOperator[] modifiedPointer = modificationHelper.createModifiedChildArray(declarator, unmodifiedPointerOperations, IASTPointerOperator.class, commentMap);
    super.writePointerOperators(declarator, modifiedPointer);
  }

  @Override
  protected void writeArrayModifiers(final IASTArrayDeclarator arrDecl, final IASTArrayModifier[] arrMods) {
    final IASTArrayModifier[] modifiedModifiers = modificationHelper.createModifiedChildArray(arrDecl, arrMods, IASTArrayModifier.class, commentMap);
    super.writeArrayModifiers(arrDecl, modifiedModifiers);
  }

  @Override
  protected void writeExceptionSpecification(final ICPPASTFunctionDeclarator funcDec, final IASTTypeId[] exceptions, ICPPASTExpression noexceptExpression) {
    IASTTypeId[] modifiedExceptions = modificationHelper.createModifiedChildArray(funcDec, exceptions, IASTTypeId.class, commentMap);
    // It makes a difference whether the exception array is identical to
    // ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION or not.
    if ((modifiedExceptions.length == 0) && (exceptions == ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION)) {
      modifiedExceptions = ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION;
    }

    noexceptExpression = modificationHelper.getNodeAfterReplacement(noexceptExpression);

    super.writeExceptionSpecification(funcDec, modifiedExceptions, noexceptExpression);
  }

  @Override
  protected void writeKnRParameterDeclarations(final ICASTKnRFunctionDeclarator knrFunct, final IASTDeclaration[] knrDeclarations) {
    final IASTDeclaration[] modifiedDeclarations = modificationHelper.createModifiedChildArray(knrFunct, knrDeclarations, IASTDeclaration.class, commentMap);
    super.writeKnRParameterDeclarations(knrFunct, modifiedDeclarations);
  }

  @Override
  protected void writeKnRParameterNames(final ICASTKnRFunctionDeclarator knrFunct, final IASTName[] parameterNames) {
    final IASTName[] modifiedNames = modificationHelper.createModifiedChildArray(knrFunct, parameterNames, IASTName.class, commentMap);
    super.writeKnRParameterNames(knrFunct, modifiedNames);
  }

  @Override
  protected IASTInitializer getInitializer(final IASTDeclarator decl) {
    return modificationHelper.getInitializer(decl);
  }
}
