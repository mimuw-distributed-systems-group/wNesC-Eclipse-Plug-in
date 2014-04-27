package pl.edu.mimuw.nesc.plugin.editor.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import pl.edu.mimuw.nesc.plugin.editor.NescEditor;

/*
 * This listener if installed will automatically save the opened document in the editor in
 * which it is installed.
 */
public class AutosaveListener implements IWindowListener, IPartListener2, IPageListener {
	private static AutosaveListener listener = new AutosaveListener();
	private static IProgressMonitor monitor = new NullProgressMonitor();

	protected AutosaveListener() {
	}

	public static AutosaveListener getInstance() {
		return listener;
	}

	// IWindowListener
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		window.removePageListener(this);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		window.addPageListener(this);
	}

	// IPageListener
	@Override
	public void pageActivated(IWorkbenchPage page) {
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		page.removePartListener(this);
	}

	@Override
	public void pageOpened(IWorkbenchPage page) {
		page.addPartListener(this);
	}

	// IPartListener
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof NescEditor) {
			((NescEditor) partRef.getPart(false)).doSave(monitor);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof NescEditor) {
			((NescEditor) partRef.getPart(false)).doSave(monitor);
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}
}
