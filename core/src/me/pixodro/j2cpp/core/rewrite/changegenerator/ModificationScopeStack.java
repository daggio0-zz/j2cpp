/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ModificationScopeStack {
  private final LinkedList<List<ASTModification>> scopeStack;
  private final ASTModificationStore modStore;

  public ModificationScopeStack(final ASTModificationStore modificationStore) {
    scopeStack = new LinkedList<List<ASTModification>>();
    modStore = modificationStore;
    final ArrayList<ASTModification> nullModList = new ArrayList<ASTModification>();
    nullModList.add(null);
    scopeStack.addFirst(nullModList);
  }

  public void pushScope(final IASTNode node) {
    final List<ASTModification> newMods = new ArrayList<ASTModification>();
    for (final ASTModification peekMod : scopeStack.peek()) {
      final ASTModificationMap nestedMods = modStore.getNestedModifications(peekMod);
      if (nestedMods != null) {
        newMods.addAll(nestedMods.getModificationsForNode(node));
      }
    }

    if (!newMods.isEmpty()) {
      scopeStack.addFirst(newMods);
    }
  }

  private List<ASTModification> getNestedModifikationsForNode(final IASTNode node) {
    final ASTModificationMap rootModifications = modStore.getRootModifications();
    if (rootModifications == null) {
      return Collections.emptyList();
    }
    return rootModifications.getModificationsForNode(node);
  }

  public void popScope(final IASTNode node) {
    final List<ASTModification> peek = scopeStack.peek();
    if (peek != null) {
      if (!peek.isEmpty() && (peek.get(0) != null)) {
        if (peek.get(0).getKind() == ModificationKind.REPLACE) {
          if (peek.get(0).getTargetNode() == node) {
            scopeStack.removeFirst();
          }
        } else if (peek.get(0).getNewNode() == node) {
          scopeStack.removeFirst();
        }
      }
    }
  }

  public Collection<IASTNode> getModifiedNodes() {
    final List<ASTModification> aktModList = scopeStack.peek();
    if (aktModList == null) {
      return getNestedModifiedNodes();
    }
    final Collection<IASTNode> nodes = new ArrayList<IASTNode>();
    for (final ASTModification modification : aktModList) {
      final ASTModificationMap nestedModifications = modStore.getNestedModifications(modification);
      if (nestedModifications != null) {
        nodes.addAll(nestedModifications.getModifiedNodes());
      }
    }
    return Collections.unmodifiableCollection(nodes);
  }

  private Collection<IASTNode> getNestedModifiedNodes() {
    final ASTModificationMap rootModifications = modStore.getRootModifications();
    if (rootModifications == null) {
      return Collections.emptyList();
    }
    return rootModifications.getModifiedNodes();
  }

  public List<ASTModification> getModificationsForNode(final IASTNode node) {
    final List<ASTModification> aktModList = scopeStack.peek();
    if (aktModList == null) {
      return getNestedModifikationsForNode(node);
    }
    final List<ASTModification> modForNodeList = new ArrayList<ASTModification>();
    for (final ASTModification modification : aktModList) {
      final ASTModificationMap nestedModifications = modStore.getNestedModifications(modification);
      if (nestedModifications != null) {
        modForNodeList.addAll(nestedModifications.getModificationsForNode(node));
      }
    }
    return Collections.unmodifiableList(modForNodeList);
  }

  public void clean(final IASTNode actualNode) {
    while (scopeStack.size() > 1) {
      for (final IASTNode currentModifiedNode : getModifiedNodes()) {
        for (final ASTModification currentMod : getModificationsForNode(currentModifiedNode)) {
          if (currentMod.getNewNode() == actualNode) {
            return;
          }
        }
      }
      if (!nodeIsChildOfModifications(actualNode, scopeStack.getFirst())) {
        if (scopeStack.getFirst().get(0).getTargetNode().getTranslationUnit() == actualNode.getTranslationUnit()) {
          scopeStack.removeFirst();
        } else {
          return;
        }
      } else {
        return;
      }
    }
  }

  private boolean nodeIsChildOfModifications(final IASTNode actualNode, final List<ASTModification> modifications) {
    for (final ASTModification currentModification : modifications) {
      if ((currentModification != null) && nodeIsChildOfModification(currentModification, actualNode)) {
        return true;
      }
    }
    return false;
  }

  private boolean nodeIsChildOfModification(final ASTModification modification, final IASTNode actualNode) {
    IASTNode nodeToTest = actualNode;
    while (nodeToTest != null) {
      if (modification.getNewNode() == nodeToTest) {
        return true;
      } else {
        nodeToTest = nodeToTest.getParent();
      }
    }
    return false;
  }
}
