package me.pixodro.j2cpp.handlers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.pixodro.j2cpp.core.Converter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
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
          compilationUnits.add(compilationUnit);
        }
      }

      final Converter converter = new Converter();
      parser.createASTs(compilationUnits.toArray(new ICompilationUnit[compilationUnits.size()]), new String[0], converter, new NullProgressMonitor());

      final IFolder folder = fragmentRoot.getJavaProject().getProject().getFolder("out");
      folder.create(IResource.FORCE, true, null);

      converter.generateTo(folder);
    } catch (final Exception e) {
      e.printStackTrace();
      // throw new ExecutionException(e.getMessage());
    }

    return null;
  }
}
