package me.pixodro.j2cpp.core.info;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/10/12
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  private IASTExpression expression;

  private final TypeDeclaration typeDeclaration;
  private final CompilationUnitInfo compilationUnitInfo;

  ExpressionInfo(final Expression javaExpression, final TypeDeclaration typeDeclaration, final CompilationUnitInfo compilationUnitInfo) {
    this.typeDeclaration = typeDeclaration;
    this.compilationUnitInfo = compilationUnitInfo;
    if (javaExpression instanceof ClassInstanceCreation) {
      expression = convertClassInstanceCreation((ClassInstanceCreation) javaExpression);
    } else if (javaExpression instanceof ConditionalExpression) {
      expression = convertConditionalExpression((ConditionalExpression) javaExpression);
    } else if (javaExpression instanceof PrefixExpression) {
      expression = convertPrefixExpression((PrefixExpression) javaExpression);
    } else if (javaExpression instanceof NumberLiteral) {
      expression = f.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, ((NumberLiteral) javaExpression).getToken());
    } else if (javaExpression instanceof NullLiteral) {
      expression = f.newIdExpression(f.newName("nullptr".toCharArray()));
    } else if (javaExpression instanceof Assignment) {
      expression = convertAssignment((Assignment) javaExpression);
    } else if (javaExpression instanceof SimpleName) {
      expression = convertSimleName((SimpleName) javaExpression);
    } else if (javaExpression instanceof QualifiedName) {
      expression = convertQualifiedName((QualifiedName) javaExpression);
    } else if (javaExpression instanceof ThisExpression) {
      expression = f.newLiteralExpression(IASTLiteralExpression.lk_this, "this");
    } else if (javaExpression instanceof InfixExpression) {
      expression = convertInfixExpression((InfixExpression) javaExpression);
    } else if (javaExpression instanceof ArrayAccess) {
      expression = convertArrayAccess((ArrayAccess) javaExpression);
    } else if (javaExpression instanceof MethodInvocation) {
      expression = convertMethodInvocation((MethodInvocation) javaExpression);
    } else if (javaExpression instanceof BooleanLiteral) {
      expression = ((BooleanLiteral) javaExpression).booleanValue() ? f.newLiteralExpression(IASTLiteralExpression.lk_true, "true") : f.newLiteralExpression(IASTLiteralExpression.lk_false, "false");
    } else if (javaExpression instanceof FieldAccess) {
      expression = convertFieldAccess((FieldAccess) javaExpression);
    } else if (javaExpression instanceof ParenthesizedExpression) {
      expression = f.newUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, new ExpressionInfo(((ParenthesizedExpression) javaExpression).getExpression(), typeDeclaration, compilationUnitInfo).getExpression());
    } else if (javaExpression instanceof PostfixExpression) {
      expression = convertPostfixExpression((PostfixExpression) javaExpression);
    } else if (javaExpression instanceof ArrayCreation) {
      expression = f.newIdExpression(f.newName("TODO ArrayCreation".toCharArray()));
    } else if (javaExpression instanceof ArrayInitializer) {
      expression = f.newIdExpression(f.newName("TODO ArrayInitializer".toCharArray()));
    } else if (javaExpression instanceof Annotation) {
      expression = f.newIdExpression(f.newName("TODO Annotation".toCharArray()));
    } else if (javaExpression instanceof SuperFieldAccess) {
      expression = f.newIdExpression(f.newName("TODO SuperFieldAccess".toCharArray()));
    } else if (javaExpression instanceof CastExpression) {
      expression = f.newIdExpression(f.newName("TODO CastExpression".toCharArray()));
    } else if (javaExpression instanceof StringLiteral) {
      expression = f.newIdExpression(f.newName("TODO StringLiteral".toCharArray()));
    } else if (javaExpression instanceof CharacterLiteral) {
      expression = f.newIdExpression(f.newName("TODO CharacterLiteral".toCharArray()));
    } else if (javaExpression instanceof TypeLiteral) {
      expression = f.newIdExpression(f.newName("TODO TypeLiteral".toCharArray()));
    } else if (javaExpression instanceof SuperMethodInvocation) {
      expression = f.newIdExpression(f.newName("TODO SuperMethodInvocation".toCharArray()));
    } else if (javaExpression instanceof InstanceofExpression) {
      expression = f.newIdExpression(f.newName("TODO InstanceofExpression".toCharArray()));
    } else if (javaExpression instanceof VariableDeclarationExpression) {
      throw new IllegalStateException("we should never encouter VariableDeclarationExpression");
    } else {
      throw new RuntimeException("unsupported expression: " + javaExpression);
    }
  }

  private IASTExpression convertSimleName(final SimpleName simpleName) {
    final IBinding binding = simpleName.resolveBinding();
    final ITypeBinding typeBinding = simpleName.resolveTypeBinding();
    if (binding.getKind() == IBinding.VARIABLE) {
      final IVariableBinding variableBinding = (IVariableBinding) binding;
      if (variableBinding.isEnumConstant()) {
        final ICPPASTQualifiedName qualifiedName = f.newQualifiedName();
        qualifiedName.addName(f.newName(typeBinding.getName().toCharArray()));
        qualifiedName.addName(new NameInfo(simpleName).getName());
        return f.newIdExpression(qualifiedName);
      } else if (((variableBinding.getModifiers() & Modifier.STATIC) != 0) && ((variableBinding.getModifiers() & Modifier.FINAL) != 0)) {
        final ICPPASTQualifiedName qualifiedName = f.newQualifiedName();
        qualifiedName.addName(f.newName(variableBinding.getDeclaringClass().getName().toCharArray()));
        qualifiedName.addName(new NameInfo(simpleName).getName());
        return f.newIdExpression(qualifiedName);
      } else if (typeDeclaration != null) {
        final List<String> tokens = new NameInfo(typeDeclaration.getName()).tokenize();
        if ((variableBinding.getDeclaringClass() != null) && !variableBinding.getDeclaringClass().getName().equals(tokens.get(tokens.size() - 1))) {
          if (!typeDeclaration.resolveBinding().isSubTypeCompatible(variableBinding.getDeclaringClass())) {
            final ICPPASTFieldReference fieldReference = f.newFieldReference(new NameInfo(simpleName).getName(), f.newIdExpression(f.newName("__parent".toCharArray())));
            fieldReference.setIsPointerDereference(true);
            return fieldReference;
          }
        }
      }
      variableBinding.getDeclaringClass();
      typeBinding.getDeclaringClass();
    }
    return f.newIdExpression(new NameInfo(simpleName).getName());
  }

  private IASTExpression convertQualifiedName(final QualifiedName qualifiedName) {
    final IBinding binding = qualifiedName.resolveBinding();
    final ITypeBinding typeBinding = qualifiedName.resolveTypeBinding();
    if (binding.getKind() == IBinding.VARIABLE) {
      final IVariableBinding variableBinding = (IVariableBinding) binding;
      if (variableBinding.isEnumConstant()) {
        final ICPPASTQualifiedName qName = f.newQualifiedName();
        qName.addName(f.newName(typeBinding.getName().toCharArray()));
        qName.addName(new NameInfo(qualifiedName.getName()).getName());
        return f.newIdExpression(qName);
      } else if (((variableBinding.getModifiers() & Modifier.STATIC) != 0) && ((variableBinding.getModifiers() & Modifier.FINAL) != 0)) {
        final ICPPASTQualifiedName qName = f.newQualifiedName();
        qName.addName(f.newName(variableBinding.getDeclaringClass().getName().toCharArray()));
        qName.addName(new NameInfo(qualifiedName.getName()).getName());
        return f.newIdExpression(qName);
        // } else if (typeBinding.isEnum()) {
        // IASTIdExpression idExpression = f.newIdExpression(new NameInfo(simpleName).getName());
        // return f.newUnaryExpression(IASTUnaryExpression.op_star, idExpression);
      }
    }
    final ExpressionInfo expressionInfo = new ExpressionInfo(qualifiedName.getQualifier(), typeDeclaration, compilationUnitInfo);
    final ICPPASTFieldReference fieldReference = f.newFieldReference(new NameInfo(qualifiedName.getName()).getName(), expressionInfo.getExpression());
    fieldReference.setIsPointerDereference(true);
    return fieldReference;
  }

  private IASTExpression convertMethodInvocation(final MethodInvocation methodInvocation) {
    IASTExpression call;
    if (methodInvocation.getExpression() != null) {
      final IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
      if ((methodBinding != null) && ((methodBinding.getModifiers() & Modifier.STATIC) != 0)) {
        final ICPPASTQualifiedName qualifiedName = f.newQualifiedName();
        qualifiedName.addName(f.newName(methodBinding.getDeclaringClass().getName().toCharArray()));
        qualifiedName.addName(new NameInfo(methodInvocation.getName()).getName());
        call = f.newIdExpression(qualifiedName);
      } else {
        final Expression qualifier = methodInvocation.getExpression();
        final ICPPASTFieldReference fieldReference = f.newFieldReference(new NameInfo(methodInvocation.getName()).getName(), new ExpressionInfo(qualifier, typeDeclaration, compilationUnitInfo).getExpression());
        final ITypeBinding typeBinding = qualifier.resolveTypeBinding();
        if ((typeBinding != null) && !(typeBinding.isPrimitive() || typeBinding.isEnum())) {
          fieldReference.setIsPointerDereference(true);
        }
        call = fieldReference;
      }
    } else {
      call = new ExpressionInfo(methodInvocation.getName(), typeDeclaration, compilationUnitInfo).getExpression();
    }

    final List<IASTInitializerClause> initializerClauses = new ArrayList<IASTInitializerClause>();
    for (final Object argumentObject : methodInvocation.arguments()) {
      final ExpressionInfo argument = new ExpressionInfo((Expression) argumentObject, typeDeclaration, compilationUnitInfo);
      initializerClauses.add(argument.getExpression());
    }
    return f.newFunctionCallExpression(call, initializerClauses.toArray(new IASTInitializerClause[initializerClauses.size()]));
  }

  private IASTExpression convertConditionalExpression(final ConditionalExpression conditionalExpression) {
    final IASTExpression condition = new ExpressionInfo(conditionalExpression.getExpression(), typeDeclaration, compilationUnitInfo).getExpression();
    final IASTExpression positive = new ExpressionInfo(conditionalExpression.getThenExpression(), typeDeclaration, compilationUnitInfo).getExpression();
    final IASTExpression negative = new ExpressionInfo(conditionalExpression.getElseExpression(), typeDeclaration, compilationUnitInfo).getExpression();
    return f.newConditionalExpession(condition, positive, negative);
  }

  private IASTExpression convertArrayAccess(final ArrayAccess arrayAccess) {
    final IASTExpression arrayExpr = new ExpressionInfo(arrayAccess.getArray(), typeDeclaration, compilationUnitInfo).getExpression();
    final IASTExpression subscript = new ExpressionInfo(arrayAccess.getIndex(), typeDeclaration, compilationUnitInfo).getExpression();
    return f.newArraySubscriptExpression(arrayExpr, subscript);
  }

  private IASTExpression convertPrefixExpression(final PrefixExpression prefixExpression) {
    final OperatorInfo operatorInfo = new OperatorInfo(prefixExpression.getOperator());
    final ExpressionInfo operand = new ExpressionInfo(prefixExpression.getOperand(), typeDeclaration, compilationUnitInfo);
    return f.newUnaryExpression(operatorInfo.getOperator(), operand.getExpression());
  }

  private IASTExpression convertPostfixExpression(final PostfixExpression postfixExpression) {
    final OperatorInfo operatorInfo = new OperatorInfo(postfixExpression.getOperator());
    final ExpressionInfo operand = new ExpressionInfo(postfixExpression.getOperand(), typeDeclaration, compilationUnitInfo);
    return f.newUnaryExpression(operatorInfo.getOperator(), operand.getExpression());
  }

  private IASTExpression convertInfixExpression(final InfixExpression infixExpression) {
    final OperatorInfo operatorInfo = new OperatorInfo(infixExpression.getOperator());
    final ExpressionInfo leftOperand = new ExpressionInfo(infixExpression.getLeftOperand(), typeDeclaration, compilationUnitInfo);
    final ExpressionInfo rightOperand = new ExpressionInfo(infixExpression.getRightOperand(), typeDeclaration, compilationUnitInfo);
    return f.newBinaryExpression(operatorInfo.getOperator(), leftOperand.getExpression(), rightOperand.getExpression());
  }

  private IASTExpression convertClassInstanceCreation(final ClassInstanceCreation classInstanceCreation) {
    final TypeInfo typeInfo = new TypeInfo(classInstanceCreation.getType(), compilationUnitInfo);
    final ICPPASTDeclarator declarator = f.newDeclarator(f.newName());

    final List<IASTInitializerClause> initializerClauses = new ArrayList<IASTInitializerClause>();
    for (final Object argumentObject : classInstanceCreation.arguments()) {
      final ExpressionInfo argument = new ExpressionInfo((Expression) argumentObject, typeDeclaration, compilationUnitInfo);
      initializerClauses.add(argument.getExpression());
    }
    final ICPPASTConstructorInitializer constructorInitializer = f.newConstructorInitializer(initializerClauses.toArray(new IASTInitializerClause[initializerClauses.size()]));

    return f.newNewExpression(null, constructorInitializer, f.newTypeId(typeInfo.getDeclSpecifier(), declarator));
  }

  private IASTExpression convertAssignment(final Assignment assignment) {
    final OperatorInfo operatorInfo = new OperatorInfo(assignment.getOperator());
    final ExpressionInfo leftHandSide = new ExpressionInfo(assignment.getLeftHandSide(), typeDeclaration, compilationUnitInfo);
    final ExpressionInfo rightHandSide = new ExpressionInfo(assignment.getRightHandSide(), typeDeclaration, compilationUnitInfo);
    return f.newBinaryExpression(operatorInfo.getOperator(), leftHandSide.getExpression(), rightHandSide.getExpression());
  }

  private IASTExpression convertFieldAccess(final FieldAccess fieldAccess) {
    final ExpressionInfo expressionInfo = new ExpressionInfo(fieldAccess.getExpression(), typeDeclaration, compilationUnitInfo);
    final ICPPASTFieldReference fieldReference = f.newFieldReference(new NameInfo(fieldAccess.getName()).getName(), expressionInfo.getExpression());
    fieldReference.setIsPointerDereference(true);
    return fieldReference;
  }

  public IASTExpression getExpression() {
    return expression;
  }
}
