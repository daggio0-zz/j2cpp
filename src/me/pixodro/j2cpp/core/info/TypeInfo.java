package me.pixodro.j2cpp.core.info;

import me.pixodro.j2cpp.core.Converter;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

/**
 * User: bquenin
 * Date: 27/06/12
 * Time: 20:03
 */
public class TypeInfo {
  private static final CPPNodeFactory f = CPPNodeFactory.getDefault();

  private final ICPPASTDeclSpecifier declSpecifier;
  private IASTExpression javaDefaultValue;
  private boolean simple;
  private boolean array;
  private final Type type;
  private final CompilationUnitInfo compilationUnitInfo;

  public TypeInfo(final Type type, final CompilationUnitInfo compilationUnitInfo) {
    this.type = type;
    this.compilationUnitInfo = compilationUnitInfo;
    declSpecifier = convertType(type);
  }

  private ICPPASTDeclSpecifier convertType(final Type javaType) {
    if (javaType.isPrimitiveType()) {
      return convertPrimitiveType((PrimitiveType) javaType);
    } else if (javaType.isParameterizedType()) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_string_literal, "nullptr");
      return convertParameterizedType((ParameterizedType) javaType);
    } else if (javaType.isSimpleType()) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_string_literal, "nullptr");
      return convertSimpleType((SimpleType) javaType);
    } else if (javaType.isArrayType()) {
      array = true;
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_string_literal, "nullptr");
      return convertArrayType((ArrayType) javaType);
    } else if (javaType.isWildcardType()) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_string_literal, "nullptr");
      System.out.println("WildcardType = " + javaType);
    } else if (javaType.isQualifiedType()) {
      System.out.println("QualifiedType = " + javaType);
    }
    throw new IllegalStateException("Unsupported type " + javaType);
  }

  private ICPPASTDeclSpecifier convertPrimitiveType(final PrimitiveType primitiveType) {
    final ICPPASTSimpleDeclSpecifier simpleDeclSpecifier = f.newSimpleDeclSpecifier();
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.VOID)) {
      simpleDeclSpecifier.setType(IBasicType.Kind.eVoid);
      return simpleDeclSpecifier;
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BOOLEAN)) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_false, "false");
      simpleDeclSpecifier.setType(IBasicType.Kind.eBoolean);
      return simpleDeclSpecifier;
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.CHAR)) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_char_constant, "\0");
      simpleDeclSpecifier.setType(IBasicType.Kind.eChar);
      return simpleDeclSpecifier;
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.FLOAT)) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_float_constant, "0");
      simpleDeclSpecifier.setType(IBasicType.Kind.eFloat);
      return simpleDeclSpecifier;
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.DOUBLE)) {
      javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_float_constant, "0");
      simpleDeclSpecifier.setType(IBasicType.Kind.eDouble);
      return simpleDeclSpecifier;
    }
    javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
    compilationUnitInfo.hppStdIncludes.add("cstdint");
    compilationUnitInfo.cppStdIncludes.add("cstdint");
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BYTE)) {
      return f.newTypedefNameSpecifier(f.newName("int8_t".toCharArray()));
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.SHORT)) {
      return f.newTypedefNameSpecifier(f.newName("int16_t".toCharArray()));
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.INT)) {
      return f.newTypedefNameSpecifier(f.newName("int32_t".toCharArray()));
    }
    if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.LONG)) {
      return f.newTypedefNameSpecifier(f.newName("int64_t".toCharArray()));
    }
    throw new IllegalStateException("Unsupported primitive type: " + primitiveType);
  }

  private ICPPASTDeclSpecifier convertSimpleType(final SimpleType simpleType) {
    final ICPPASTSimpleDeclSpecifier simpleDeclSpecifier = f.newSimpleDeclSpecifier();
    if (simpleType.getName().isSimpleName()) {
      final SimpleName simpleName = (SimpleName) simpleType.getName();

      if (simpleName.getIdentifier().equals(Void.class.getName())) {
        simpleDeclSpecifier.setType(IBasicType.Kind.eVoid);
        return simpleDeclSpecifier;
      }
      if (simpleName.getIdentifier().equals(Boolean.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_false, "false");
        simpleDeclSpecifier.setType(IBasicType.Kind.eBoolean);
        return simpleDeclSpecifier;
      }
      if (simpleName.getIdentifier().equals(Character.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_char_constant, "\0");
        simpleDeclSpecifier.setType(IBasicType.Kind.eChar);
        return simpleDeclSpecifier;
      }
      if (simpleName.getIdentifier().equals(Float.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_float_constant, "0");
        simpleDeclSpecifier.setType(IBasicType.Kind.eFloat);
        return simpleDeclSpecifier;
      }
      if (simpleName.getIdentifier().equals(Double.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_float_constant, "0");
        simpleDeclSpecifier.setType(IBasicType.Kind.eDouble);
        return simpleDeclSpecifier;
      }
      if (simpleName.getIdentifier().equals(Byte.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
        compilationUnitInfo.hppStdIncludes.add("cstdint");
        compilationUnitInfo.cppStdIncludes.add("cstdint");
        return f.newTypedefNameSpecifier(f.newName("int8_t".toCharArray()));
      }
      if (simpleName.getIdentifier().equals(Short.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
        compilationUnitInfo.hppStdIncludes.add("cstdint");
        compilationUnitInfo.cppStdIncludes.add("cstdint");
        return f.newTypedefNameSpecifier(f.newName("int16_t".toCharArray()));
      }
      if (simpleName.getIdentifier().equals(Integer.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
        compilationUnitInfo.hppStdIncludes.add("cstdint");
        compilationUnitInfo.cppStdIncludes.add("cstdint");
        return f.newTypedefNameSpecifier(f.newName("int32_t".toCharArray()));
      }
      if (simpleName.getIdentifier().equals(Long.class.getSimpleName())) {
        javaDefaultValue = f.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
        compilationUnitInfo.hppStdIncludes.add("cstdint");
        compilationUnitInfo.cppStdIncludes.add("cstdint");
        return f.newTypedefNameSpecifier(f.newName("int64_t".toCharArray()));
      }
    }
    simple = true;
    final ITypeBinding typeBinding = type.resolveBinding();
    IASTName name;

    if (typeBinding.isNested()) {
      final ICPPASTQualifiedName qualifiedName = f.newQualifiedName();
      qualifiedName.addName(f.newName(typeBinding.getDeclaringClass().getName().toCharArray()));
      qualifiedName.addName(f.newName(typeBinding.getName().toCharArray()));
      compilationUnitInfo.hppIncludes.add(typeBinding.getDeclaringClass().getName());
      compilationUnitInfo.cppIncludes.add(typeBinding.getDeclaringClass().getName());
      name = qualifiedName;
    } else {
      compilationUnitInfo.hppIncludes.add(typeBinding.getName());
      compilationUnitInfo.cppIncludes.add(typeBinding.getName());
      name = new NameInfo(simpleType.getName()).getName();
    }
    return f.newTypedefNameSpecifier(name);
  }

  private ICPPASTDeclSpecifier convertArrayType(final ArrayType arrayType) {
    return convertType(arrayType.getElementType());
  }

  private ICPPASTNamedTypeSpecifier convertParameterizedType(final ParameterizedType parameterizedType) {
    final SimpleType simpleType = (SimpleType) parameterizedType.getType();
    String parameterizedTypeName = simpleType.getName().toString();

    // Here we can check if the parameterized type is a Java collection
    if (Converter.setClasses.contains(parameterizedTypeName)) {
      compilationUnitInfo.hppStdIncludes.add("set");
      compilationUnitInfo.cppStdIncludes.add("set");
      parameterizedTypeName = "set";
    } else if (Converter.listClasses.contains(parameterizedTypeName)) {
      compilationUnitInfo.hppStdIncludes.add("list");
      compilationUnitInfo.cppStdIncludes.add("list");
      parameterizedTypeName = "list";
    } else if (Converter.mapClasses.contains(parameterizedTypeName)) {
      compilationUnitInfo.hppStdIncludes.add("map");
      compilationUnitInfo.cppStdIncludes.add("map");
      parameterizedTypeName = "map";
    }

    final ICPPASTTemplateId templateId = f.newTemplateId(f.newName(parameterizedTypeName.toCharArray()));
    for (final Object parameterTypeObject : parameterizedType.typeArguments()) {
      final TypeInfo parameterTypeInfo = new TypeInfo((Type) parameterTypeObject, compilationUnitInfo);
      final ICPPASTDeclarator parameterDeclarator = f.newDeclarator(f.newName());
      if (parameterTypeInfo.isSimple()) {
        parameterDeclarator.addPointerOperator(f.newPointer());
      }
      final IASTTypeId typeId = f.newTypeId(parameterTypeInfo.getDeclSpecifier(), parameterDeclarator);
      templateId.addTemplateArgument(typeId);
    }
    return f.newTypedefNameSpecifier(templateId);
  }

  public ICPPASTDeclSpecifier getDeclSpecifier() {
    return declSpecifier;
  }

  public boolean isSimple() {
    return simple;
  }

  public IASTExpression getJavaDefaultValue() {
    return javaDefaultValue;
  }

  public boolean isArray() {
    return array;
  }

  public Type getType() {
    return type;
  }
}
