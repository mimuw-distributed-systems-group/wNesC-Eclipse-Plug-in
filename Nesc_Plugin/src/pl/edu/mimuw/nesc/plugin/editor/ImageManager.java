package pl.edu.mimuw.nesc.plugin.editor;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import pl.edu.mimuw.nesc.plugin.NescPlugin;

/**
 * Manages image resources for nesc plugin.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class ImageManager {

	/**
	 * Completion proposal image for local declarations.
	 */
	public static final Image COMPLETION_LOCAL;
	/**
	 * Completion proposal image for declarations from component scope.
	 */
	public static final Image COMPLETION_COMPONENT;
	/**
	 * Completion proposal image for global declarations.
	 */
	public static final Image COMPLETION_GLOBAL;
	/**
	 * Completion proposal image for templates without scope restrictions.
	 */
	public static final Image COMPLETION_TEMPLATE;

	static {
		COMPLETION_LOCAL = getImage("icons/autocompletion/local.gif"); //$NON-NLS-1$
		COMPLETION_COMPONENT = getImage("icons/autocompletion/component.png"); //$NON-NLS-1$
		COMPLETION_GLOBAL = getImage("icons/autocompletion/global.gif"); //$NON-NLS-1$
		COMPLETION_TEMPLATE = getImage("icons/autocompletion/template.gif"); //$NON-NLS-1$
	}

	private static Image getImage(String path) {
		final ImageRegistry registry = NescPlugin.getDefault().getImageRegistry();
		Image image = registry.get(path);
		if (image == null) {
			final ImageDescriptor descriptor = getImageDescriptor(path);
			registry.put(path, descriptor);
			image = registry.get(path);
		}
		return image;
	}

	private static URL getPluginResourceURL(String internalPath) throws MalformedURLException {
		return new URL("platform:/plugin/Nesc_Plugin/" + internalPath); //$NON-NLS-1$
	}

	private static ImageDescriptor getImageDescriptor(String path) {
		try {
			final URL iconUrl = getPluginResourceURL(path);
			return ImageDescriptor.createFromURL(iconUrl);
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
