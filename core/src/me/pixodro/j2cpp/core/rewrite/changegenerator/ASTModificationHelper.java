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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.pixodro.j2cpp.core.rewrite.ContainerNode;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ASTModificationHelper {
  private final ModificationScopeStack modificationStore;

  public ASTModificationHelper(final ModificationScopeStack stack) {
    modificationStore = stack;
  }

  public <T extends IASTNode> T[] createModifiedChildArray(final IASTNode parent, final T[] unmodifiedChildren, final Class<T> clazz, final NodeCommentMap commentMap) {
    final ArrayList<T> modifiedChildren = new ArrayList<T>(Arrays.asList(unmodifiedChildren));

    for (final ASTModification parentModification : modificationsForNode(parent)) {
      switch (parentModification.getKind()) {
      case APPEND_CHILD:
        IASTNode newNode = parentModification.getNewNode();
        final T appendedTNode = cast(newNode, clazz);
        if (appendedTNode != null) {
          modifiedChildren.add(appendedTNode);
        } else if (newNode instanceof ContainerNode) {
          final ContainerNode nodeContainer = (ContainerNode) newNode;
          for (final IASTNode currentNode : nodeContainer.getNodes()) {
            final T tnode = cast(currentNode, clazz);
            if (tnode != null) {
              modifiedChildren.add(tnode);
            }
          }
        }
        break;

      case INSERT_BEFORE:
        newNode = parentModification.getNewNode();
        if (newNode instanceof ContainerNode) {
          final ContainerNode contNode = (ContainerNode) newNode;
          for (final IASTNode node : contNode.getNodes()) {
            insertNode(clazz, modifiedChildren, parentModification, node);
          }
        } else {
          insertNode(clazz, modifiedChildren, parentModification, newNode);
        }
        break;

      case REPLACE:
        break;
      }
    }

    for (final T currentChild : unmodifiedChildren) {
      for (final ASTModification childModification : modificationsForNode(currentChild)) {
        try {
          final T newNode = cast(childModification.getNewNode(), clazz);
          switch (childModification.getKind()) {
          case REPLACE:
            if (newNode != null) {
              copyComments(newNode, currentChild, commentMap);
              modifiedChildren.add(modifiedChildren.indexOf(childModification.getTargetNode()), newNode);
            }
            modifiedChildren.remove(childModification.getTargetNode());
            break;
          case INSERT_BEFORE:
          case APPEND_CHILD:
            throw new UnhandledASTModificationException(childModification);
          }
        } catch (final ClassCastException e) {
          throw new UnhandledASTModificationException(childModification);
        }
      }
    }
    return modifiedChildren.toArray(newArrayInstance(clazz, modifiedChildren.size()));
  }

  private void copyComments(final IASTNode newNode, final IASTNode oldNode, final NodeCommentMap commentMap) {
    // Attach all the comments that is attached to oldNode to newNode
    final List<IASTComment> leadingComments = commentMap.getLeadingCommentsForNode(oldNode);
    for (final IASTComment comment : leadingComments) {
      commentMap.addLeadingCommentToNode(newNode, comment);
    }

    final List<IASTComment> trailingComments = commentMap.getTrailingCommentsForNode(oldNode);
    for (final IASTComment comment : trailingComments) {
      commentMap.addTrailingCommentToNode(newNode, comment);
    }

    final List<IASTComment> freestandingComments = commentMap.getFreestandingCommentsForNode(oldNode);
    for (final IASTComment comment : freestandingComments) {
      commentMap.addFreestandingCommentToNode(newNode, comment);
    }

    // Detach comments from oldNode (to avoid memory leak)
    final Map<IASTNode, List<IASTComment>> leadingMap = commentMap.getLeadingMap();
    leadingMap.remove(oldNode);

    final Map<IASTNode, List<IASTComment>> trailingMap = commentMap.getTrailingMap();
    trailingMap.remove(oldNode);

    final Map<IASTNode, List<IASTComment>> freestandingMap = commentMap.getFreestandingMap();
    freestandingMap.remove(oldNode);
  }

  private <T> void insertNode(final Class<T> clazz, final ArrayList<T> modifiedChildren, final ASTModification parentModification, final IASTNode newNode) {
    final T insertedTNode = cast(newNode, clazz);

    final int targetNodeIndex = modifiedChildren.indexOf(parentModification.getTargetNode());
    if (targetNodeIndex >= 0) {
      modifiedChildren.add(targetNodeIndex, insertedTNode);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T[] newArrayInstance(final Class<T> clazz, final int size) {
    return (T[]) Array.newInstance(clazz, size);
  }

  @SuppressWarnings("unchecked")
  private <T> T cast(final IASTNode node, final Class<T> clazz) {
    if (clazz.isInstance(node)) {
      return (T) node;
    }
    return null;
  }

  public List<ASTModification> modificationsForNode(final IASTNode targetNode) {
    List<ASTModification> modificationsForNode;
    if (modificationStore.getModifiedNodes().contains(targetNode)) {
      modificationsForNode = modificationStore.getModificationsForNode(targetNode);
    } else {
      modificationsForNode = Collections.emptyList();
    }
    return modificationsForNode;
  }

  public IASTInitializer getInitializer(final IASTDeclarator decl) {
    final IASTInitializer initializer = decl.getInitializer();

    if (initializer != null) {
      for (final ASTModification childModification : modificationsForNode(initializer)) {
        switch (childModification.getKind()) {
        case REPLACE:
          if (childModification.getNewNode() instanceof IASTInitializer) {
            return (IASTInitializer) childModification.getNewNode();
          } else if (childModification.getNewNode() == null) {
            return null;
          }
          throw new UnhandledASTModificationException(childModification);
        case INSERT_BEFORE:
          throw new UnhandledASTModificationException(childModification);

        case APPEND_CHILD:
          throw new UnhandledASTModificationException(childModification);
        }
      }
    } else {
      for (final ASTModification parentModification : modificationsForNode(decl)) {
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

  @SuppressWarnings("unchecked")
  public <T extends IASTNode> T getNodeAfterReplacement(final T replacedNode) {
    final List<ASTModification> modifications = modificationsForNode(replacedNode);
    for (final ASTModification currentModification : modifications) {
      try {
        if (currentModification.getKind() == ModificationKind.REPLACE) {
          return (T) currentModification.getNewNode();
        }
      } catch (final ClassCastException e) {
        throw new UnhandledASTModificationException(currentModification);
      }
    }
    return replacedNode;
  }
}
