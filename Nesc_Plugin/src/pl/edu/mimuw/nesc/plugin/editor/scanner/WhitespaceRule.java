package pl.edu.mimuw.nesc.plugin.editor.scanner;

/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.text.rules.IToken;

/**
 * A simple whitespace rule with configurable token.
 */
public class WhitespaceRule extends SingleCharRule {

	public WhitespaceRule(IToken token) {
		super(token);
	}

	@Override
	protected boolean isRuleChar(int ch) {
		switch (ch) {
		case ' ':
		case '\t':
		case '\r':
		case '\n':
			return true;
		default:
			return false;
		}
	}

}