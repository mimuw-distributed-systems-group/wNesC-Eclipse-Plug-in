package pl.edu.mimuw.nesc.plugin.editor;

import java.nio.file.Paths;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

import pl.edu.mimuw.nesc.preprocessor.directive.IncludeDirective;

public class NescElementHyperlink implements IHyperlink {
	
	private IRegion region;
	private IncludeDirective include;
	private NescEditor editor;

	public NescElementHyperlink(IRegion region, IncludeDirective include, NescEditor nescEditor) {
		this.region = region;
		this.include = include;
		this.editor = nescEditor;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return "Hyperlink test";
	}

	@Override
	public void open() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		
		// If we have a project we first check its contents for the include file
		if (this.editor.getOpenFileProject() != null) {
			IProject project = this.editor.getOpenFileProject();
			IFile file = project.getFile(include.getFileName());
			try {
				IDE.openEditor(page, file, true);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			return;
		}
		
		IEditorInput input = null;
		// If we did not find the file in the project, then we try to read the file from disc
		if (include.getFilePath().isPresent()) {
			IPath uri = new Path(include.getFilePath().get());
			IFileStore	fs = EFS.getLocalFileSystem().getStore(uri);
			input = new FileStoreEditorInput(fs);
		}
		
		try {
			if (input != null)
				IDE.openEditor(page, input, NescEditor.EDITOR_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

}
