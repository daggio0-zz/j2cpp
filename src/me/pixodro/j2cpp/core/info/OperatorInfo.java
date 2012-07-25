package me.pixodro.j2cpp.core.info;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/10/12
 * Time: 11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperatorInfo {
  final int operator;

  public OperatorInfo(final Assignment.Operator javaOperator) {
    if (Assignment.Operator.ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_assign;
    } else if (Assignment.Operator.PLUS_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_plusAssign;
    } else if (Assignment.Operator.MINUS_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_minusAssign;
    } else if (Assignment.Operator.TIMES_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_multiplyAssign;
    } else if (Assignment.Operator.DIVIDE_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_divideAssign;
    } else if (Assignment.Operator.BIT_AND_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_binaryAndAssign;
    } else if (Assignment.Operator.BIT_OR_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_binaryOrAssign;
    } else if (Assignment.Operator.BIT_XOR_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_binaryXorAssign;
    } else if (Assignment.Operator.REMAINDER_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_moduloAssign;
    } else if (Assignment.Operator.LEFT_SHIFT_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_shiftLeftAssign;
    } else if (Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_shiftRightAssign;
    } else if (Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_shiftRightAssign;
    } else {
      throw new RuntimeException("Unsupported assignment operator: " + javaOperator);
    }
  }

  public OperatorInfo(final InfixExpression.Operator javaOperator) {
    if (InfixExpression.Operator.TIMES.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_multiply;
    } else if (InfixExpression.Operator.DIVIDE.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_divide;
    } else if (InfixExpression.Operator.REMAINDER.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_modulo;
    } else if (InfixExpression.Operator.PLUS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_plus;
    } else if (InfixExpression.Operator.MINUS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_minus;
    } else if (InfixExpression.Operator.LEFT_SHIFT.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_shiftLeft;
    } else if (InfixExpression.Operator.RIGHT_SHIFT_SIGNED.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_shiftRight;
    } else if (InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_shiftRight;
    } else if (InfixExpression.Operator.LESS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_lessThan;
    } else if (InfixExpression.Operator.GREATER.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_greaterThan;
    } else if (InfixExpression.Operator.LESS_EQUALS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_lessEqual;
    } else if (InfixExpression.Operator.GREATER_EQUALS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_greaterEqual;
    } else if (InfixExpression.Operator.EQUALS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_equals;
    } else if (InfixExpression.Operator.NOT_EQUALS.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_notequals;
    } else if (InfixExpression.Operator.XOR.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_binaryXor;
    } else if (InfixExpression.Operator.OR.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_binaryOr;
    } else if (InfixExpression.Operator.AND.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_binaryAnd;
    } else if (InfixExpression.Operator.CONDITIONAL_OR.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_logicalOr;
    } else if (InfixExpression.Operator.CONDITIONAL_AND.equals(javaOperator)) {
      operator = IASTBinaryExpression.op_logicalAnd;
    } else {
      throw new RuntimeException("Unsupported infix expression operator: " + javaOperator);
    }
  }

  public OperatorInfo(final PostfixExpression.Operator javaOperator) {
    if (PostfixExpression.Operator.DECREMENT.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_postFixDecr;
    } else if (PostfixExpression.Operator.INCREMENT.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_postFixIncr;
    } else {
      throw new RuntimeException("Unsupported postfix expression operator: " + javaOperator);
    }
  }

  public OperatorInfo(final PrefixExpression.Operator javaOperator) {
    if (PrefixExpression.Operator.DECREMENT.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_prefixDecr;
    } else if (PrefixExpression.Operator.INCREMENT.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_prefixIncr;
    } else if (PrefixExpression.Operator.COMPLEMENT.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_tilde;
    } else if (PrefixExpression.Operator.MINUS.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_minus;
    } else if (PrefixExpression.Operator.NOT.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_not;
    } else if (PrefixExpression.Operator.PLUS.equals(javaOperator)) {
      operator = IASTUnaryExpression.op_plus;
    } else {
      throw new RuntimeException("Unsupported postfix expression operator: " + javaOperator);
    }
  }

  public int getOperator() {
    return operator;
  }
}
