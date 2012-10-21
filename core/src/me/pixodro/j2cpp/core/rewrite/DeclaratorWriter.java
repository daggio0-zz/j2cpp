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
 * Thomas Corbat (IFS)
 *******************************************************************************/
package me.pixodro.j2cpp.core.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of declarator nodes. The actual string operations are delegated
 * to the {@link Scribe} class.
 * 
 * @see IASTDeclarator
 * @author Emanuel Graf IFS
 */
public class DeclaratorWriter extends NodeWriter {
  private static final String AMPERSAND_AMPERSAND = "&&"; //$NON-NLS-1$
  private static final String PURE_VIRTUAL = " = 0"; //$NON-NLS-1$
  private static final String ARROW_OPERATOR = "->"; //$NON-NLS-1$

  public DeclaratorWriter(final Scribe scribe, final ASTWriterVisitor visitor, final NodeCommentMap commentMap) {
    super(scribe, visitor, commentMap);
  }

  protected void writeDeclarator(final IASTDeclarator declarator) {
    if (declarator instanceof IASTStandardFunctionDeclarator) {
      writeFunctionDeclarator((IASTStandardFunctionDeclarator) declarator);
    } else if (declarator instanceof IASTArrayDeclarator) {
      writeArrayDeclarator((IASTArrayDeclarator) declarator);
    } else if (declarator instanceof IASTFieldDeclarator) {
      writeFieldDeclarator((IASTFieldDeclarator) declarator);
    } else if (declarator instanceof ICASTKnRFunctionDeclarator) {
      writeCKnRFunctionDeclarator((ICASTKnRFunctionDeclarator) declarator);
    } else {
      writeDefaultDeclarator(declarator);
    }

    visitor.setSpaceNeededBeforeName(false);
    writeTrailingComments(declarator, false);
  }

  protected void writeDefaultDeclarator(final IASTDeclarator declarator) {
    final IASTPointerOperator[] pointOps = declarator.getPointerOperators();
    writePointerOperators(declarator, pointOps);
    final IASTName name = declarator.getName();
    name.accept(visitor);
    writeNestedDeclarator(declarator);
    final IASTInitializer init = getInitializer(declarator);
    if (init != null) {
      init.accept(visitor);
    }
  }

  protected void writePointerOperators(final IASTDeclarator declarator, final IASTPointerOperator[] pointOps) {
    for (final IASTPointerOperator operator : pointOps) {
      writePointerOperator(operator);
    }
  }

  private void writeFunctionDeclarator(final IASTStandardFunctionDeclarator funcDec) {
    final IASTPointerOperator[] pointOps = funcDec.getPointerOperators();
    writePointerOperators(funcDec, pointOps);
    // XXX: Lambda declarators happen to have null names rather than empty ones when parsed
    if (funcDec.getName() != null) {
      funcDec.getName().accept(visitor);
    }
    writeNestedDeclarator(funcDec);
    writeParameters(funcDec);
    writeInitializer(funcDec);
    if (funcDec instanceof ICPPASTFunctionDeclarator) {
      writeCppFunctionDeclarator((ICPPASTFunctionDeclarator) funcDec);
    }
  }

  private void writeInitializer(final IASTStandardFunctionDeclarator funcDec) {
    final IASTInitializer init = getInitializer(funcDec);
    if (init != null) {
      init.accept(visitor);
    }
  }

  private void writeParameters(final IASTStandardFunctionDeclarator funcDec) {
    final IASTParameterDeclaration[] paraDecls = funcDec.getParameters();
    scribe.print('(');
    writeParameterDeclarations(funcDec, paraDecls);
    scribe.print(')');
  }

  private void writeNestedDeclarator(final IASTDeclarator funcDec) {
    final IASTDeclarator nestedDeclarator = funcDec.getNestedDeclarator();
    if (nestedDeclarator != null) {
      if (visitor.isSpaceNeededBeforeName()) {
        scribe.printSpace();
        visitor.setSpaceNeededBeforeName(false);
      }
      scribe.print('(');
      nestedDeclarator.accept(visitor);
      scribe.print(')');
    }
  }

  private void writeCppFunctionDeclarator(final ICPPASTFunctionDeclarator funcDec) {
    if (funcDec.isConst()) {
      scribe.printSpace();
      scribe.print(Keywords.CONST);
    }
    if (funcDec.isVolatile()) {
      scribe.printSpace();
      scribe.print(Keywords.VOLATILE);
    }
    if (funcDec.isMutable()) {
      scribe.printSpace();
      scribe.print(Keywords.MUTABLE);
    }
    if (funcDec.isOverride()) {
      scribe.printSpace();
      scribe.print(Keywords.cOVERRIDE);
    }
    if (funcDec.isFinal()) {
      scribe.printSpace();
      scribe.print(Keywords.cFINAL);
    }
    if (funcDec.isPureVirtual()) {
      scribe.print(PURE_VIRTUAL);
    }
    writeExceptionSpecification(funcDec, funcDec.getExceptionSpecification(), funcDec.getNoexceptExpression());
    if (funcDec.getTrailingReturnType() != null) {
      scribe.printSpace();
      scribe.print(ARROW_OPERATOR);
      scribe.printSpace();
      funcDec.getTrailingReturnType().accept(visitor);
    }
  }

  protected void writeExceptionSpecification(final ICPPASTFunctionDeclarator funcDec, final IASTTypeId[] exceptions, final ICPPASTExpression noexceptExpression) {
    if (exceptions != ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
      scribe.printSpace();
      scribe.printStringSpace(Keywords.THROW);
      scribe.print('(');
      writeNodeList(exceptions);
      scribe.print(')');
    }
    if (noexceptExpression != null) {
      scribe.printSpace();
      scribe.print(Keywords.NOEXCEPT);
      if (noexceptExpression != CPPASTFunctionDeclarator.NOEXCEPT_DEFAULT) {
        scribe.printSpace();
        scribe.print('(');
        noexceptExpression.accept(visitor);
        scribe.print(')');
      }
    }
  }

  protected void writeParameterDeclarations(final IASTStandardFunctionDeclarator funcDec, final IASTParameterDeclaration[] paramDecls) {
    writeNodeList(paramDecls);
    if (funcDec.takesVarArgs()) {
      if (paramDecls.length > 0) {
        scribe.print(COMMA_SPACE);
      }
      scribe.print(VAR_ARGS);
    }
  }

  private void writePointer(final IASTPointer operator) {
    if (operator instanceof ICPPASTPointerToMember) {
      final ICPPASTPointerToMember pointerToMemberOp = (ICPPASTPointerToMember) operator;
      if (pointerToMemberOp.getName() != null) {
        pointerToMemberOp.getName().accept(visitor);
        scribe.print('*');
      }
    } else {
      scribe.print('*');
    }

    if (operator.isConst()) {
      scribe.printStringSpace(Keywords.CONST);
    }
    if (operator.isVolatile()) {
      scribe.printStringSpace(Keywords.VOLATILE);
    }
    if (operator.isRestrict()) {
      scribe.printStringSpace(Keywords.RESTRICT);
    }
  }

  public void writePointerOperator(final IASTPointerOperator operator) {
    if (operator instanceof IASTPointer) {
      final IASTPointer pointOp = (IASTPointer) operator;
      writePointer(pointOp);
    } else if (operator instanceof ICPPASTReferenceOperator) {
      if (((ICPPASTReferenceOperator) operator).isRValueReference()) {
        scribe.print(AMPERSAND_AMPERSAND);
      } else {
        scribe.print('&');
      }
    }
  }

  private void writeArrayDeclarator(final IASTArrayDeclarator arrDecl) {
    final IASTPointerOperator[] pointOps = arrDecl.getPointerOperators();
    writePointerOperators(arrDecl, pointOps);
    final IASTName name = arrDecl.getName();
    name.accept(visitor);

    writeNestedDeclarator(arrDecl);

    final IASTArrayModifier[] arrMods = arrDecl.getArrayModifiers();
    writeArrayModifiers(arrDecl, arrMods);
    final IASTInitializer initializer = getInitializer(arrDecl);
    if (initializer != null) {
      initializer.accept(visitor);
    }
  }

  protected IASTInitializer getInitializer(final IASTDeclarator decl) {
    return decl.getInitializer();
  }

  protected void writeArrayModifiers(final IASTArrayDeclarator arrDecl, final IASTArrayModifier[] arrMods) {
    for (final IASTArrayModifier modifier : arrMods) {
      writeArrayModifier(modifier);
    }
  }

  protected void writeArrayModifier(final IASTArrayModifier modifier) {
    scribe.print('[');
    final IASTExpression ex = modifier.getConstantExpression();
    if (ex != null) {
      ex.accept(visitor);
    }
    scribe.print(']');
  }

  private void writeFieldDeclarator(final IASTFieldDeclarator fieldDecl) {
    final IASTPointerOperator[] pointOps = fieldDecl.getPointerOperators();
    writePointerOperators(fieldDecl, pointOps);
    fieldDecl.getName().accept(visitor);
    scribe.printSpace();
    scribe.print(':');
    scribe.printSpace();
    fieldDecl.getBitFieldSize().accept(visitor);
    final IASTInitializer initializer = getInitializer(fieldDecl);
    if (initializer != null) {
      initializer.accept(visitor);
    }
  }

  private void writeCKnRFunctionDeclarator(final ICASTKnRFunctionDeclarator knrFunct) {
    knrFunct.getName().accept(visitor);
    scribe.print('(');
    writeKnRParameterNames(knrFunct, knrFunct.getParameterNames());
    scribe.print(')');
    scribe.newLine();
    writeKnRParameterDeclarations(knrFunct, knrFunct.getParameterDeclarations());
  }

  protected void writeKnRParameterDeclarations(final ICASTKnRFunctionDeclarator knrFunct, final IASTDeclaration[] knrDeclarations) {
    for (int i = 0; i < knrDeclarations.length; ++i) {
      scribe.noNewLines();
      knrDeclarations[i].accept(visitor);
      scribe.newLines();
      if ((i + 1) < knrDeclarations.length) {
        scribe.newLine();
      }
    }
  }

  protected void writeKnRParameterNames(final ICASTKnRFunctionDeclarator knrFunct, final IASTName[] parameterNames) {
    writeNodeList(parameterNames);
  }
}
