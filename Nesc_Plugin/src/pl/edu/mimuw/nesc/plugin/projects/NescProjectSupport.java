package pl.edu.mimuw.nesc.plugin.projects;

import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
			project = null;
		}

		return project;
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
