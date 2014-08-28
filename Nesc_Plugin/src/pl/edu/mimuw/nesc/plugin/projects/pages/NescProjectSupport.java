package pl.edu.mimuw.nesc.plugin.projects.pages;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Stack;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

import pl.edu.mimuw.nesc.plugin.natures.NescProjectNature;

public class NescProjectSupport {

	public static IProject createProject(String projectName, URI location) {
		Assert.isNotNull(projectName);
		Assert.isTrue(projectName.trim().length() > 0);

		IProject project = createBaseProject(projectName, location);
		try {
			addNature(project);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}

		return project;
	}

	/**
	 * Creates a new file in a nesC project. The correctness of the path
	 * (whether it really begins with a nesC project and the file does not
	 * exist) is not checked by this method. It should be checked before
	 * calling it.
	 *
	 * @param fullPath Full path of the file to create (in the Eclipse
	 *        "filesystem")
	 * @param contents Stream to read the contents of the newly created file
	 *                 from.
	 * @return Object that represents the newly created file or null if the
	 *         creation fails.
	 */
	public static IFile createFile(String fullPath, InputStream contents) {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IFile newFile = root.getFile(new Path(fullPath));

		try {
			// Create the file
			newFile.create(contents, true, null);
			return newFile;
		} catch(CoreException e) {
			return null;
		}
	}

	private static IProject createBaseProject(String projectName, URI location) {
		// it is acceptable to use the ResourcesPlugin class
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!newProject.exists()) {
			URI projectLocation = location;
			IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
			if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				projectLocation = null;
			}

			desc.setLocationURI(projectLocation);
			try {
				newProject.create(desc, null);
				if (!newProject.isOpen()) {
					newProject.open(null);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return newProject;
	}

	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
		for (String path : paths) {
			IFolder etcFolders = newProject.getFolder(path);
			createFolder(etcFolders);
		}
	}

	private static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(NescProjectNature.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = NescProjectNature.NATURE_ID;
			description.setNatureIds(newNatures);

			IProgressMonitor monitor = null;
			project.setDescription(description, monitor);
		}
	}
	
	public static void importAllFilesInProjectFolder(IProject project) throws CoreException {
		IPath projectDirectory = project.getLocation();
		if (projectDirectory != null) {
			Stack<File> subdirs = new Stack<>();
			for (File f: projectDirectory.toFile().listFiles()) {
				if (!f.getName().equals(".project") && !f.getName().equals(".settings"))
				subdirs.push(f);
			}
			while (!subdirs.isEmpty()) {
				File current = subdirs.pop();
				if (current.isDirectory()) {
					for (File f : current.listFiles()) {
						subdirs.push(f);
					}
					IFolder folder = project.getFolder(current.getAbsolutePath());
					if (!folder.exists()) {
						folder.createLink(new Path(current.getAbsolutePath()), IResource.NONE, null);
					}
				} else {
					IFile file = project.getFile(current.getAbsolutePath());
					if (!file.exists()) {
						file.createLink(new Path(current.getAbsolutePath()), IResource.NONE, null);
					}
				}
			}
		}
	}

	public static QualifiedName getProjectMainConfQualName(String projectName) {
		return new QualifiedName("pl.edu.mimuw.nesc.project." + projectName, "main-configuration");
	}

	public static QualifiedName getProjectIsTinyOsProjQualName(String projectName) {
		return new QualifiedName("pl.edu.mimuw.nesc.project." + projectName, "is-TinyOS-project");
	}

	public static QualifiedName getProjectTinyOsPlatformQualName(String projectName) {
		return new QualifiedName("pl.edu.mimuw.nesc.project." + projectName, "TinyOS-platform");
	}

}
