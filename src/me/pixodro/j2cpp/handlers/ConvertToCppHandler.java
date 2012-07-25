package me.pixodro.j2cpp.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.pixodro.j2cpp.core.CompilationUnitInfo;

import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ConvertToCppHandler extends AbstractHandler {
  class Requestor extends ASTRequestor {

    @Override
    public void acceptAST(final ICompilationUnit source, final CompilationUnit compilationUnit) {
      super.acceptAST(source, compilationUnit);
      try {
        System.out.println("Converting = " + source);
        final CompilationUnitInfo compilationUnitInfo = new CompilationUnitInfo(compilationUnit);

        final IFolder folder = source.getJavaProject().getProject().getFolder("out");
        if (!folder.exists()) {
          folder.create(IResource.NONE, true, null);
        }

        // Assuming ".java" extension
        final String baseName = source.getElementName().substring(0, source.getElementName().length() - 5);

        final IFile headerFile = folder.getFile(baseName + ".h");
        if (!headerFile.exists()) {
          final ASTWriter writer = new ASTWriter();
          final StringBuffer output = new StringBuffer();
          output.append("#ifndef __").append(baseName).append("_H_\n");
          output.append("#define __").append(baseName).append("_H_\n");
          // for (final String include : CompilationUnitInfo.hppStdIncludes) {
          // output.append("#include <").append(include).append(">\n");
          // }
          // for (final String include : CompilationUnitInfo.hppIncludes) {
          // output.append("#include \"").append(include).append(".h\"\n");
          // }
          // if (!CompilationUnitInfo.hppStdIncludes.isEmpty()) {
          // output.append("using namespace std;\n");
          // }
          output.append(writer.write(compilationUnitInfo.getHpp()));
          output.append("#endif //__").append(baseName).append("_H_\n");
          final InputStream stream = new ByteArrayInputStream(output.toString().getBytes());
          headerFile.create(stream, IResource.FORCE, null);
        }

        final IFile compilationUnitFile = folder.getFile(baseName + ".cpp");
        if (!compilationUnitFile.exists()) {
          final ASTWriter writer = new ASTWriter();
          final StringBuffer output = new StringBuffer();
          // for (final String include : CompilationUnitInfo.cppStdIncludes) {
          // output.append("#include <").append(include).append(">\n");
          // }
          // for (final String include : CompilationUnitInfo.cppIncludes) {
          // output.append("#include \"").append(include).append(".h\"\n");
          // }
          // output.append("#include \"").append(baseName).append(".h\"\n");
          // if (!CompilationUnitInfo.cppStdIncludes.isEmpty()) {
          // output.append("using namespace std;\n");
          // }
          output.append(writer.write(compilationUnitInfo.getCpp()));
          final InputStream stream = new ByteArrayInputStream(output.toString().getBytes());
          compilationUnitFile.create(stream, IResource.FORCE, null);
        }
      } catch (final Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    @Override
    public void acceptBinding(final String bindingKey, final IBinding binding) {
      // TODO Auto-generated method stub
      super.acceptBinding(bindingKey, binding);
    }

  }

  /**
   * The constructor.
   */
  public ConvertToCppHandler() {
  }

  /**
   * the command has been executed, so extract the needed information
   * from the application context.
   */
  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {
    try {
      final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
      final IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
      final IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) selection.getFirstElement();

      final ASTParser parser = ASTParser.newParser(AST.JLS4);
      // In order to parse 1.6 code, some compiler options need to be set to 1.6
      final Map<?, ?> options = JavaCore.getOptions();
      JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
      parser.setCompilerOptions(options);
      parser.setProject(fragmentRoot.getJavaProject());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setResolveBindings(true);
      parser.setStatementsRecovery(true);
      parser.setBindingsRecovery(true);
      parser.setIgnoreMethodBodies(false);

      final Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
      for (final Object packageFragmentObject : fragmentRoot.getChildren()) {
        final IPackageFragment packageFragment = (IPackageFragment) packageFragmentObject;
        for (final ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
          System.out.println("compilationUnit = " + compilationUnit);
          compilationUnits.add(compilationUnit);
        }
      }

      parser.createASTs(compilationUnits.toArray(new ICompilationUnit[compilationUnits.size()]), new String[0], new Requestor(), new NullProgressMonitor());
    } catch (final Exception e) {
      e.printStackTrace();
      throw new ExecutionException(e.getMessage());
    }

    return null;
  }
}
