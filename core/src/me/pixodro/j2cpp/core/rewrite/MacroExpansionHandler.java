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
 * Sergey Prigogin (Google)
 *******************************************************************************/
package me.pixodro.j2cpp.core.rewrite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacroReferenceName;
import org.eclipse.core.runtime.CoreException;

/**
 * Recognizes nodes that are the result of an macro expansion and replaces them
 * with a suitable macro call.
 * 
 * @author Emanuel Graf IFS
 */
public class MacroExpansionHandler {
  private final Scribe scribe;
  private int lastMacroExpOffset;
  private IASTTranslationUnit ast;
  private Map<String, List<IIndexName>> macroExpansion = new TreeMap<String, List<IIndexName>>();

  public MacroExpansionHandler(final Scribe scribe) {
    this.scribe = scribe;
  }

  protected boolean checkisMacroExpansionNode(final IASTNode node) {
    return checkisMacroExpansionNode(node, true);
  }

  protected boolean isStatementWithMixedLocation(final IASTStatement node) {
    final IASTNodeLocation[] nodeLocations = getNodeLocations(node);
    if ((nodeLocations != null) && (nodeLocations.length > 1)) {
      for (final IASTNodeLocation loc : nodeLocations) {
        if (loc instanceof IASTMacroExpansionLocation) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean macroExpansionAlreadyPrinted(final IASTNode node) {
    final IASTNodeLocation[] locs = node.getNodeLocations();
    if (locs.length == 1) {
      if (locs[0] instanceof IASTMacroExpansionLocation) {
        final IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];
        if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean checkisMacroExpansionNode(IASTNode node, final boolean write) {
    final IASTTranslationUnit unit = node.getTranslationUnit();
    if ((ast == null) || !ast.equals(unit)) {
      initEmptyMacros(unit);
    }
    final IASTNodeLocation[] locs = getNodeLocations(node);
    if ((locs != null) && (locs.length == 1)) {
      if (locs[0] instanceof IASTMacroExpansionLocation) {
        final IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];

        if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
          return true;
        }
        if (write) {
          lastMacroExpOffset = macroNode.asFileLocation().getNodeOffset();
          node = node.getOriginalNode();
          scribe.print(node.getRawSignature());
        }
        return true;

      }
    }
    handleEmptyMacroExpansion(node);
    return false;
  }

  private IASTNodeLocation[] getNodeLocations(final IASTNode node) {
    return node.getOriginalNode().getNodeLocations();
  }

  private void handleEmptyMacroExpansion(final IASTNode node) {
    if (node.getTranslationUnit() == null) {
      return;
    }
    final String file = node.getContainingFilename();
    final List<IIndexName> exps = macroExpansion.get(file);
    if ((exps != null) && !exps.isEmpty()) {
      final IASTFileLocation fileLocation = getFileLocation(node);
      if (fileLocation != null) {
        final int nOff = fileLocation.getNodeOffset();
        for (final IIndexName iIndexName : exps) {
          if (iIndexName instanceof PDOMMacroReferenceName) {
            final PDOMMacroReferenceName mName = (PDOMMacroReferenceName) iIndexName;
            final int eOff = mName.getFileLocation().getNodeOffset();
            final int eLength = mName.getFileLocation().getNodeLength();
            if ((eOff < nOff) && (Math.abs(((eOff + eLength) - nOff)) < 3)) {
              scribe.print(mName.toString() + " "); //$NON-NLS-1$
            }
          }
        }
      }
    }
  }

  private IASTFileLocation getFileLocation(final IASTNode node) {
    return node.getOriginalNode().getFileLocation();
  }

  private void initEmptyMacros(final IASTTranslationUnit unit) {
    if (unit != null) {
      ast = unit;
      final IIndex index = ast.getIndex();
      if (index != null) {
        macroExpansion = new TreeMap<String, List<IIndexName>>();
        final IASTPreprocessorMacroDefinition[] md = ast.getMacroDefinitions();

        final TreeSet<String> paths = new TreeSet<String>();
        for (final IASTPreprocessorIncludeStatement is : ast.getIncludeDirectives()) {
          if (!is.isSystemInclude()) {
            paths.add(is.getContainingFilename());
          }
        }
        paths.add(ast.getContainingFilename());

        for (final IASTPreprocessorMacroDefinition iastPreprocessorMacroDefinition : md) {
          if (iastPreprocessorMacroDefinition.getExpansion().length() == 0) {
            try {
              final IIndexMacro[] macroBinding = index.findMacros(iastPreprocessorMacroDefinition.getName().toCharArray(), IndexFilter.ALL, null);
              if (macroBinding.length > 0) {
                final IIndexName[] refs = index.findReferences(macroBinding[0]);
                for (final IIndexName iIndexName : refs) {
                  final String filename = iIndexName.getFileLocation().getFileName();
                  List<IIndexName> fileList = macroExpansion.get(filename);
                  if (paths.contains(filename)) {
                    if (fileList == null) {
                      fileList = new ArrayList<IIndexName>();
                      macroExpansion.put(filename, fileList);
                    }
                    fileList.add(iIndexName);
                  }
                }
              }
            } catch (final CoreException e) {
              e.printStackTrace();
            }
          }
        }
      } else {
        macroExpansion = Collections.emptyMap();
      }
    }
  }

  public void reset() {
    lastMacroExpOffset = -1;
  }
}
