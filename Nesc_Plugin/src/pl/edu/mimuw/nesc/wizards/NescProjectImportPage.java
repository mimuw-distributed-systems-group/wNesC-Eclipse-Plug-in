package pl.edu.mimuw.nesc.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardResourceImportPage;

public class NescProjectImportPage extends WizardResourceImportPage {

	protected NescProjectImportPage(String name, IStructuredSelection selection) {
		super(name, selection);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createSourceGroup(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ITreeContentProvider getFileProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ITreeContentProvider getFolderProvider() {
		// TODO Auto-generated method stub
		return null;
	}

}
