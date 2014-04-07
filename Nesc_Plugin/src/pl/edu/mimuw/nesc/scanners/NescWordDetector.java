package pl.edu.mimuw.nesc.scanners;

import org.eclipse.jface.text.rules.IWordDetector;

public class NescWordDetector implements IWordDetector {

	@Override
	public boolean isWordStart(char c) {
		return Character.isJavaIdentifierStart(c);
	}

	@Override
	public boolean isWordPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}

}
