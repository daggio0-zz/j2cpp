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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Visits the nodes in consideration of {@link ASTModification}s.
 * 
 * @since 5.0
 * @author Emanuel Graf IFS
 */
public class ChangeGeneratorWriterVisitor extends ASTWriterVisitor {
  private final ASTModificationStore modificationStore;
  private final String fileScope;
  private final ModificationScopeStack stack;

  public ChangeGeneratorWriterVisitor(final ASTVisitor delegateVisitor, final ASTModificationStore modificationStore, final String fileScope, final NodeCommentMap commentMap) {
    super(commentMap);

    this.modificationStore = modificationStore;
    this.fileScope = fileScope;
    stack = new ModificationScopeStack(modificationStore);

    shouldVisitArrayModifiers = delegateVisitor.shouldVisitArrayModifiers;
    shouldVisitBaseSpecifiers = delegateVisitor.shouldVisitBaseSpecifiers;
    shouldVisitDeclarations = delegateVisitor.shouldVisitDeclarators;
    shouldVisitDeclarators = delegateVisitor.shouldVisitDeclarators;
    shouldVisitDeclSpecifiers = delegateVisitor.shouldVisitDeclSpecifiers;
    shouldVisitExpressions = delegateVisitor.shouldVisitExpressions;
    shouldVisitInitializers = delegateVisitor.shouldVisitInitializers;
    shouldVisitNames = delegateVisitor.shouldVisitNames;
    shouldVisitNamespaces = delegateVisitor.shouldVisitNamespaces;
    shouldVisitParameterDeclarations = delegateVisitor.shouldVisitParameterDeclarations;
    shouldVisitPointerOperators = delegateVisitor.shouldVisitPointerOperators;
    shouldVisitProblems = delegateVisitor.shouldVisitProblems;
    shouldVisitStatements = delegateVisitor.shouldVisitStatements;
    shouldVisitTemplateParameters = delegateVisitor.shouldVisitTemplateParameters;
    shouldVisitTranslationUnit = delegateVisitor.shouldVisitTranslationUnit;
    shouldVisitTypeIds = delegateVisitor.shouldVisitTypeIds;
  }

  public ChangeGeneratorWriterVisitor(final ASTModificationStore modStore, final NodeCommentMap nodeMap) {
    this(modStore, null, nodeMap);
  }

  public ChangeGeneratorWriterVisitor(final ASTModificationStore modStore, final String fileScope, final NodeCommentMap commentMap) {
    super(commentMap);
    modificationStore = modStore;
    this.fileScope = fileScope;
    shouldVisitTranslationUnit = true;
    stack = new ModificationScopeStack(modificationStore);
    declaratorWriter = new ModifiedASTDeclaratorWriter(scribe, this, stack, commentMap);
    expWriter = new ModifiedASTExpressionWriter(scribe, this, macroHandler, stack, commentMap);
    statementWriter = new ModifiedASTStatementWriter(scribe, this, stack, commentMap);
    declSpecWriter = new ModifiedASTDeclSpecWriter(scribe, this, stack, commentMap);
    declarationWriter = new ModifiedASTDeclarationWriter(scribe, this, stack, commentMap);
  }

  @Override
  protected IASTDeclarator getParameterDeclarator(final IASTParameterDeclaration parameterDeclaration) {
    IASTDeclarator newDecl = parameterDeclaration.getDeclarator();
    if (stack.getModifiedNodes().contains(newDecl)) {
      for (final ASTModification currentModification : stack.getModificationsForNode(newDecl)) {
        if ((currentModification.getKind() == ASTModification.ModificationKind.REPLACE) && (currentModification.getTargetNode() == parameterDeclaration)) {
          newDecl = (IASTDeclarator) currentModification.getNewNode();
        }
      }
    }
    return newDecl;
  }

  @Override
  protected IASTName getParameterName(final IASTDeclarator declarator) {
    IASTName newName = declarator.getName();
    if (stack.getModifiedNodes().contains(newName)) {
      for (final ASTModification currentModification : stack.getModificationsForNode(newName)) {
        if ((currentModification.getKind() == ASTModification.ModificationKind.REPLACE) && (currentModification.getTargetNode() == newName)) {
          newName = (IASTName) currentModification.getNewNode();
        }
      }
    }
    return newName;
  }

  @Override
  public int leave(final ICPPASTBaseSpecifier specifier) {
    super.leave(specifier);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final ICPPASTNamespaceDefinition namespace) {
    super.leave(namespace);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final ICPPASTTemplateParameter parameter) {
    super.leave(parameter);
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final ICPPASTBaseSpecifier specifier) {
    if (doBeforeEveryNode(specifier) == PROCESS_CONTINUE) {
      return super.visit(specifier);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final ICPPASTNamespaceDefinition namespace) {
    if (doBeforeEveryNode(namespace) == PROCESS_CONTINUE) {
      return super.visit(namespace);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final ICPPASTTemplateParameter parameter) {
    if (doBeforeEveryNode(parameter) == PROCESS_CONTINUE) {
      return super.visit(parameter);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTDeclaration declaration) {
    super.leave(declaration);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTDeclarator declarator) {
    super.leave(declarator);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTDeclSpecifier declSpec) {
    super.leave(declSpec);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTEnumerator enumerator) {
    super.leave(enumerator);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTExpression expression) {
    super.leave(expression);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTInitializer initializer) {
    super.leave(initializer);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTName name) {
    super.leave(name);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTParameterDeclaration parameterDeclaration) {
    super.leave(parameterDeclaration);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTPointerOperator pointerOperator) {
    super.leave(pointerOperator);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTProblem problem) {
    super.leave(problem);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTStatement statement) {
    super.leave(statement);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTTranslationUnit tu) {
    super.leave(tu);
    return PROCESS_SKIP;
  }

  @Override
  public int leave(final IASTTypeId typeId) {
    super.leave(typeId);
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTDeclaration declaration) {
    if (doBeforeEveryNode(declaration) == PROCESS_CONTINUE) {
      return super.visit(declaration);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTDeclarator declarator) {
    if (doBeforeEveryNode(declarator) == PROCESS_CONTINUE) {
      return super.visit(declarator);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTDeclSpecifier declSpec) {
    if (doBeforeEveryNode(declSpec) == PROCESS_CONTINUE) {
      return super.visit(declSpec);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTEnumerator enumerator) {
    if (doBeforeEveryNode(enumerator) == PROCESS_CONTINUE) {
      return super.visit(enumerator);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTArrayModifier arrayModifier) {
    if (doBeforeEveryNode(arrayModifier) == PROCESS_CONTINUE) {
      return super.visit(arrayModifier);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTExpression expression) {
    if (doBeforeEveryNode(expression) == PROCESS_CONTINUE) {
      return super.visit(expression);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTInitializer initializer) {
    if (doBeforeEveryNode(initializer) == PROCESS_CONTINUE) {
      return super.visit(initializer);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTName name) {
    if (doBeforeEveryNode(name) == PROCESS_CONTINUE) {
      return super.visit(name);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTParameterDeclaration parameterDeclaration) {
    if (doBeforeEveryNode(parameterDeclaration) == PROCESS_CONTINUE) {
      return super.visit(parameterDeclaration);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTPointerOperator pointerOperator) {
    if (doBeforeEveryNode(pointerOperator) == PROCESS_CONTINUE) {
      return super.visit(pointerOperator);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTProblem problem) {
    if (doBeforeEveryNode(problem) == PROCESS_CONTINUE) {
      return super.visit(problem);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTStatement statement) {
    if (doBeforeEveryNode(statement) == PROCESS_CONTINUE) {
      return super.visit(statement);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTTranslationUnit tu) {
    final ASTModificationHelper helper = new ASTModificationHelper(stack);
    final IASTDeclaration[] declarations = helper.createModifiedChildArray(tu, tu.getDeclarations(), IASTDeclaration.class, commentMap);
    for (final IASTDeclaration currentDeclaration : declarations) {
      currentDeclaration.accept(this);
    }
    return PROCESS_SKIP;
  }

  @Override
  public int visit(final IASTTypeId typeId) {
    if (doBeforeEveryNode(typeId) == PROCESS_CONTINUE) {
      return super.visit(typeId);
    }
    return PROCESS_SKIP;
  }

  protected int doBeforeEveryNode(final IASTNode node) {
    stack.clean(node);
    if (fileScope != null) {
      final String file = getCorrespondingFile(node);
      if (!fileScope.equals(file)) {
        return PROCESS_SKIP;
      }
    }

    // Check all insert before and append modifications for the current node.
    // If necessary put it onto the stack.
    for (final IASTNode currentModifiedNode : stack.getModifiedNodes()) {
      for (final ASTModification currentMod : stack.getModificationsForNode(currentModifiedNode)) {
        if (currentMod.getNewNode() == node) {
          if (currentMod.getKind() != ModificationKind.REPLACE) {
            stack.pushScope(currentModifiedNode);
            return PROCESS_CONTINUE;
          }
        }
      }
    }
    // Check all replace modifications for the current node. Visit the replacing node if found.
    for (final IASTNode currentModifiedNode : stack.getModifiedNodes()) {
      for (final ASTModification currentMod : stack.getModificationsForNode(currentModifiedNode)) {
        if ((currentMod.getTargetNode() == node) && (currentMod.getKind() == ModificationKind.REPLACE)) {
          if (currentMod.getNewNode() != null) {
            stack.pushScope(node);
            currentMod.getNewNode().accept(this);
            stack.popScope(node);
          }
          return PROCESS_SKIP;
        }
      }
    }

    return PROCESS_CONTINUE;
  }

  private String getCorrespondingFile(final IASTNode node) {
    if (node.getFileLocation() != null) {
      return node.getFileLocation().getFileName();
    }

    if (node.getParent() != null) {
      return getCorrespondingFile(node.getParent());
    }

    for (final IASTNode modifiedNode : modificationStore.getRootModifications().getModifiedNodes()) {
      for (final ASTModification modification : modificationStore.getRootModifications().getModificationsForNode(modifiedNode)) {
        if (modification.getNewNode() == node) {
          return getCorrespondingFile(modification.getTargetNode());
        }
      }
    }
    return null;
  }
}
