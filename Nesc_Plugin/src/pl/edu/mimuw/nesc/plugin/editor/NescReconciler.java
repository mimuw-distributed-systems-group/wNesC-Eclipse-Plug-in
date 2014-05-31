package pl.edu.mimuw.nesc.plugin.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Adapted form CDT CReconciler
 */
public class NescReconciler extends MonoReconciler {

	static class SingletonJob extends Job implements ISchedulingRule {
		private Runnable fCode;

		SingletonJob(String name, Runnable code) {
			super(name);
			fCode= code;
			setPriority(Job.SHORT);
			setRule(this);
			setUser(false);
			setSystem(true);
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				fCode.run();
			}
			return Status.OK_STATUS;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	/**
	 * Internal part listener for activating the reconciler.
	 */
	private class PartListener implements IPartListener2 {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == fTextEditor) {
				setEditorActive(false);
			}
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == fTextEditor) {
				NescReconciler.this.scheduleReconciling();
				setEditorActive(true);
			}
		}
	}

	/**
	 * Internal Shell activation listener for activating the reconciler.
	 */
	private class ActivationListener extends ShellAdapter {
		private Control fControl;

		public ActivationListener(Control control) {
			Assert.isNotNull(control);
			fControl= control;
		}

		@Override
		public void shellActivated(ShellEvent e) {
			if (!fControl.isDisposed() && fControl.isVisible()) {
				if (hasCModelChanged())
					NescReconciler.this.scheduleReconciling();
				setEditorActive(true);
			}
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			if (!fControl.isDisposed() && fControl.getShell() == e.getSource()) {
				setEditorActive(false);
			}
		}
	}

	/** The reconciler's editor */
	private ITextEditor fTextEditor;
	/** The part listener */
	private IPartListener2 fPartListener;
	/** The shell listener */
	private ShellListener fActivationListener;
	/** Tells whether the C model might have changed. */
	private volatile boolean fHasCModelChanged= false;
	/** Tells whether this reconciler's editor is active. */
	private volatile boolean fIsEditorActive= true;
	/** Tells whether a reconcile is in progress. */
	private volatile boolean fIsReconciling= false;

	private boolean fInitialProcessDone= false;
	private Job fTriggerReconcilerJob;

	/**
	 * Create a reconciler for the given editor and strategy.
	 *
	 * @param editor the text editor
	 * @param strategy  the Nesc reconciling strategy
	 */
	public NescReconciler(ITextEditor editor, CompositeReconcilingStrategy strategy) {
		super(strategy, false);
		fTextEditor= editor;
	}

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);

		fPartListener= new PartListener();
		IWorkbenchPartSite site= fTextEditor.getSite();
		IWorkbenchWindow window= site.getWorkbenchWindow();
		window.getPartService().addPartListener(fPartListener);

		fActivationListener= new ActivationListener(textViewer.getTextWidget());
		Shell shell= window.getShell();
		shell.addShellListener(fActivationListener);

		fTriggerReconcilerJob= new SingletonJob("Trigger Reconciler", new Runnable() { //$NON-NLS-1$
			@Override
			public void run() {
				forceReconciling();
			}});
	}

	@Override
	public void uninstall() {
		fTriggerReconcilerJob.cancel();

		IWorkbenchPartSite site= fTextEditor.getSite();
		IWorkbenchWindow window= site.getWorkbenchWindow();
		window.getPartService().removePartListener(fPartListener);
		fPartListener= null;

		Shell shell= window.getShell();
		if (shell != null && !shell.isDisposed())
			shell.removeShellListener(fActivationListener);
		fActivationListener= null;

		super.uninstall();
	}

	protected void scheduleReconciling() {
		if (!fInitialProcessDone)
			return;
		if (fTriggerReconcilerJob.cancel()) {
			fTriggerReconcilerJob.schedule(50);
		}
	}

	@Override
	protected void forceReconciling() {
		if (!fInitialProcessDone)
			return;
		super.forceReconciling();
	}

	@Override
	protected void aboutToBeReconciled() {
		// If we ever want anything to happen just before reconciling
		// something more than a comment must be written here.

		// Without the check this does not work. It should be checked if saving is allowed in such situations
		if (fTextEditor.isDirty()) {
			/*
			 * FIXME: Probably file does not need to be saved every time. But
			 * then we need to pass unsaved file to NesC frontend somehow. Which
			 * is better (easier :))?
			 */
			fTextEditor.doSave(null);
		}
	}

	@Override
	protected void initialProcess() {
		super.initialProcess();
		fInitialProcessDone= true;
		if (!fIsReconciling && isEditorActive() && hasCModelChanged()) {
			NescReconciler.this.scheduleReconciling();
		}
	}

	@Override
	protected void process(DirtyRegion dirtyRegion) {
		fIsReconciling= true;
		setCModelChanged(false);
		super.process(dirtyRegion);
		fIsReconciling= false;
	}

	/**
	 * Tells whether the C Model has changed or not.
	 *
	 * @return <code>true</code> iff the C Model has changed
	 */
	private synchronized boolean hasCModelChanged() {
		return fHasCModelChanged;
	}

	/**
	 * Sets whether the C Model has changed or not.
	 *
	 * @param state <code>true</code> iff the C model has changed
	 */
	private synchronized void setCModelChanged(boolean state) {
		fHasCModelChanged= state;
	}

	/**
	 * Tells whether this reconciler's editor is active.
	 *
	 * @return <code>true</code> iff the editor is active
	 */
	private synchronized boolean isEditorActive() {
		return fIsEditorActive;
	}

	/**
	 * Sets whether this reconciler's editor is active.
	 *
	 * @param state <code>true</code> iff the editor is active
	 */
	private synchronized void setEditorActive(boolean active) {
		fIsEditorActive= active;
		if (!active) {
			fTriggerReconcilerJob.cancel();
		}
	}
}
