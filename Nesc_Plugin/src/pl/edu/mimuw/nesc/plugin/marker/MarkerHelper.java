package pl.edu.mimuw.nesc.plugin.marker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.ast.Location;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectManager;
import pl.edu.mimuw.nesc.problem.NescError;
import pl.edu.mimuw.nesc.problem.NescIssue;
import pl.edu.mimuw.nesc.problem.NescWarning;

import com.google.common.base.Optional;

/**
 * Utility class for creating markers.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class MarkerHelper {

	public static final String ERROR_MARKER = "pl.edu.mimuw.nesc.plugin.marker.ErrorMarker";

	/**
	 * Updates markers in the entire project.
	 *
	 * @param project
	 *            project
	 * @throws CoreException
	 */
	public void updateMarkers(IProject project) throws CoreException {
		project.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		project.accept(new ProjectResourceVisitor(project));

		/* Set errors for the project scope. */
		final ProjectData data = ProjectManager.getProjectData(project);
		if (data == null) {
			return;
		}
		for (NescIssue issue : data.getIssues()) {
			final IMarker marker = project.createMarker(ERROR_MARKER);
			marker.setAttribute(IMarker.MESSAGE, issue.getMessage());
			setMarkerType(marker, issue);
		}
	}

	/**
	 * Updates markers for a given file in a specified project's context.
	 *
	 * @param project
	 *            a current project
	 * @param file
	 *            a current file
	 * @param data
	 *            a file's data
	 * @throws CoreException
	 */
	public void updateMarkers(IProject project, IFile file, FileData data) throws CoreException {
		/* Delete existing markers in the current file. */
		file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

		/*
		 * Get the document associated with a given file to be able to count offsets
		 * of marker's start and end locations.
		 */
		final IDocumentProvider provider = new TextFileDocumentProvider();
		provider.connect(file);
		final IDocument document = provider.getDocument(file);
		if (document == null) {
			System.err.println("The associated document is null; file=" + file);
			return;
		}

		/* Create a new marker for each issue. */
		for (NescIssue issue : data.getIssues().values()) {
			IMarker marker = file.createMarker(ERROR_MARKER);
			marker.setAttribute(IMarker.MESSAGE, issue.getMessage());
			setMarkerType(marker, issue);
			try {
				setMarkerLocation(document, marker, issue.getStartLocation(), issue.getEndLocation());
			} catch (CoreException e) {
				// Marker with inaccurate location.
			}
		}
	}

	private void setMarkerType(IMarker marker, NescIssue issue) {
		issue.accept(new NescProblemVisitor(), marker);
	}

	private void setMarkerLocation(IDocument document, IMarker marker, Optional<Location> startLocationOptional,
			Optional<Location> endLocationOptional) throws CoreException {
		if (!startLocationOptional.isPresent()) {
			// Marker without location.
			return;
		}
		final Location startLocation = startLocationOptional.get();

		/*
		 * Some errors should point to the end of the file (e.g.
		 * "unexpected end of file") but line is set to -1 in such cases.
		 */
		if (startLocation.getLine() < 0) {
			marker.setAttribute(IMarker.CHAR_START, document.getLength() - 1);
			marker.setAttribute(IMarker.CHAR_END, document.getLength() - 1);
			return;
		}

		marker.setAttribute(IMarker.LINE_NUMBER, startLocation.getLine());

		try {
			final int startOffset = getOffset(document, startLocation);
			final int endOffset;
			if (endLocationOptional.isPresent()) {
				final Location endLocation = endLocationOptional.get();
				/* End offset is exclusive - add one. */
				endOffset = getOffset(document, endLocation) + 1;
			} else {
				endOffset = startOffset + 1;
			}
			/*
			 * Set the exact position when it is sure that no location causes
			 * an exception.
			 */
			marker.setAttribute(IMarker.CHAR_START, startOffset);
			marker.setAttribute(IMarker.CHAR_END, endOffset);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}

	private int getOffset(IDocument document, Location location) throws BadLocationException {
		return document.getLineOffset(location.getLine() - 1) + location.getColumn() - 1;
	}

	/**
	 * Sets marker priority and severity according to the given problem class.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private final class NescProblemVisitor implements NescIssue.Visitor<Void, IMarker> {

		@Override
		public Void visit(NescError error, IMarker marker) {
			try {
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			} catch (CoreException e) {
				// Nothing to do.
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Void visit(NescWarning warning, IMarker marker) {
			try {
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} catch (CoreException e) {
				// Nothing to do.
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private final class ProjectResourceVisitor implements IResourceVisitor {

		private final IProject project;

		private ProjectResourceVisitor(IProject project) {
			this.project = project;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() == IResource.FILE) {
				final FileData data = ProjectManager.getFileData(project, resource.getRawLocation().toOSString());
				if (data != null) {
					updateMarkers(project, (IFile) resource, data);
				}
			}
			return true;
		}
	}
}
