package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;

/**
 * <p>
 * Template extension which contains an additional field <code>image</code>
 * indicating the image to be displayed in a completion proposal list.
 * </p>
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescTemplate extends Template {

	private final Image image;

	public NescTemplate(Template template, Image image) {
		super(template);
		this.image = image;
	}

	public Image getImage() {
		return image;
	}

}
