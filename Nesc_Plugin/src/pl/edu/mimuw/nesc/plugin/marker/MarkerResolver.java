package pl.edu.mimuw.nesc.plugin.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * Creates resolutions for a given marker. The list of resolutions is presented
 * to user after selecting an entry from Problems View.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class MarkerResolver implements IMarkerResolutionGenerator {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		// TODO this is empty skeleton.
		return new IMarkerResolution[0];
	}
}
