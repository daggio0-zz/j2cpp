package me.pixodro.j2cpp.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.pixodro.j2cpp.core.info.CompilationUnitInfo;
import me.pixodro.j2cpp.core.rewrite.ASTWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

public class Converter extends ASTRequestor {
  public static List<String> excludedImports = new ArrayList<String>();
  public static List<String> excludedJavaMethods = new ArrayList<String>();
  public static List<String> setClasses = new ArrayList<String>();
  public static List<String> listClasses = new ArrayList<String>();
  public static List<String> mapClasses = new ArrayList<String>();
  public static List<String> collectionClasses = new ArrayList<String>();

  static {
    excludedImports.add(Object.class.getSimpleName());
    excludedImports.add(StringBuilder.class.getSimpleName());
    excludedJavaMethods.add("equals");
    excludedJavaMethods.add("hashCode");
    excludedJavaMethods.add("toString");
    setClasses.add(Set.class.getSimpleName());
    setClasses.add(HashSet.class.getSimpleName());
    setClasses.add(LinkedHashSet.class.getSimpleName());
    listClasses.add(List.class.getSimpleName());
    listClasses.add(ArrayList.class.getSimpleName());
    listClasses.add(LinkedList.class.getSimpleName());
    mapClasses.add(Map.class.getSimpleName());
    mapClasses.add(HashMap.class.getSimpleName());
    collectionClasses.addAll(setClasses);
    collectionClasses.addAll(listClasses);
    collectionClasses.addAll(mapClasses);
  }

  private final List<CompilationUnitInfo> compilationUnitInfos = new ArrayList<CompilationUnitInfo>();

  @Override
  public void acceptAST(final ICompilationUnit source, final CompilationUnit compilationUnit) {
    super.acceptAST(source, compilationUnit);
    try {
      compilationUnitInfos.add(new CompilationUnitInfo(compilationUnit));
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void acceptBinding(final String bindingKey, final IBinding binding) {
    super.acceptBinding(bindingKey, binding);
  }

  public void generateTo(final IFolder folder) {
    for (final CompilationUnitInfo compilationUnitInfo : compilationUnitInfos) {
      try {
        final String baseName = compilationUnitInfo.getName();

        final String headerOutput = generateHeader(compilationUnitInfo);
        final InputStream headerStream = new ByteArrayInputStream(headerOutput.getBytes());
        final IFile headerFile = folder.getFile(baseName + ".h");
        headerFile.create(headerStream, IResource.FORCE, null);

        final String compilationUnitOutput = generateCompilationUnit(compilationUnitInfo);
        final InputStream compilationUnitStream = new ByteArrayInputStream(compilationUnitOutput.getBytes());
        final IFile compilationUnitFile = folder.getFile(baseName + ".cpp");
        compilationUnitFile.create(compilationUnitStream, IResource.FORCE, null);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String generateHeader(final CompilationUnitInfo compilationUnitInfo) {
    final ASTWriter writer = new ASTWriter();
    final StringBuffer output = new StringBuffer();
    output.append("#ifndef __").append(compilationUnitInfo.getName()).append("_H_\n");
    output.append("#define __").append(compilationUnitInfo.getName()).append("_H_\n");
    output.append("\n");
    for (final String include : compilationUnitInfo.hppStdIncludes) {
      output.append("#include <").append(include).append(">\n");
    }
    // for (final String include : compilationUnitInfo.hppIncludes) {
    // output.append("#include \"").append(include).append(".h\"\n");
    // }
    output.append("\n");
    output.append(writer.write(compilationUnitInfo.getHpp()));
    output.append("\n");
    output.append("#endif //__").append(compilationUnitInfo.getName()).append("_H_\n");
    return output.toString();
  }

  private String generateCompilationUnit(final CompilationUnitInfo compilationUnitInfo) {
    final ASTWriter writer = new ASTWriter();
    final StringBuffer output = new StringBuffer();
    for (final String include : compilationUnitInfo.cppStdIncludes) {
      output.append("#include <").append(include).append(">\n");
    }
    // for (final String include : compilationUnitInfo.cppIncludes) {
    // output.append("#include \"").append(include).append(".h\"\n");
    // }
    output.append("#include \"").append(compilationUnitInfo.getName()).append(".h\"\n");
    output.append("\n");
    output.append(writer.write(compilationUnitInfo.getCpp()));
    return output.toString();
  }

  public static boolean isSTLType(final String typeName) {
    if ("set".equalsIgnoreCase(typeName)) {
      return true;
    } else if ("list".equalsIgnoreCase(typeName)) {
      return true;
    } else if ("map".equalsIgnoreCase(typeName)) {
      return true;
    }
    return false;
  }
}
