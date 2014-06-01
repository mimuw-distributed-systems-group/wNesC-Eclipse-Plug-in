package pl.edu.mimuw.nesc.plugin.marker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Creates resolutions for markers at given location in document. Quick assist
 * processor may be invoked in two ways:
 * <ul>
 * <li>user selects "Quick Fix" option from context menu after clicking on
 * marked (underlined) area,</li>
 * <li>having cursor on marked (underlined) area, user pushes CRTL+1.</li>
 * </ul>
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public String getErrorMessage() {
		// TODO
		return null;
	}

	@Override
	public boolean canFix(Annotation annotation) {
		// TODO
		return false;
	}

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		// TODO
		return true;
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		final ISourceViewer viewer = invocationContext.getSourceViewer();
		final int documentOffset = invocationContext.getOffset();

		final List<Annotation> annotations = getRelevantAnnotations(viewer, documentOffset);

		if (annotations.isEmpty()) {
			return null;
		}
		// TODO: compute quick assist proposals
		return null;
	}

	private List<Annotation> getRelevantAnnotations(final ISourceViewer viewer, int documentOffset) {
		final List<Annotation> result = new ArrayList<>(1);

		final Iterator<?> iter = viewer.getAnnotationModel().getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation = (Annotation) iter.next();
			if (annotation instanceof MarkerAnnotation) {
				final MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				final IMarker marker = markerAnnotation.getMarker();

				final int markerStartOffset = marker.getAttribute(IMarker.CHAR_START, Integer.MAX_VALUE);
				final int markerEndOffset = marker.getAttribute(IMarker.CHAR_END, markerStartOffset);
				if (markerStartOffset <= documentOffset && documentOffset <= markerEndOffset) {
					result.add(annotation);
				}
			}
		}
		return result;
	}
}
