/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.editors.ecl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class EclEditorToolkit extends EditorToolkit {

	private static final EclEditorToolkit INSTANCE = new EclEditorToolkit();

	public static EclEditorToolkit getInstance() {
		return INSTANCE;
	}

	private EclEditorToolkit() {
	}

	interface Partition {
		public static final String def = IDocument.DEFAULT_CONTENT_TYPE;
		public static final String comment = "__ecl_comment"; //$NON-NLS-1$
		public static final String string = "__ecl_string"; //$NON-NLS-1$
	}

	interface Style {
		public static final String commandName = "__ecl_style_commandName"; //$NON-NLS-1$
		public static final String paramName = "__ecl_style_paramName"; //$NON-NLS-1$

		public static final String def = "__ecl_style_def"; //$NON-NLS-1$
		public static final String comment = "__ecl_style_comment"; //$NON-NLS-1$
		public static final String string = "__ecl_style_string"; //$NON-NLS-1$
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
		var fCombinedPreferenceStore = new ChainedPreferenceStore(new IPreferenceStore[] { Q7UIPlugin.getDefault().getPreferenceStore(), generalTextStore });
		return fCombinedPreferenceStore;
	}

	@Override
	public String[] getContentTypes() {
		return new String[] { Partition.def, Partition.string,
				Partition.comment };
	}

	@Override
	public EnhancedRuleScanner getScanner(String ct) {
		if (ct.equals(Partition.def)) {
			return new EnhancedRuleScanner(this) {
				@Override
				protected String[] getProperties() {
					return new String[] { Style.commandName, Style.paramName,
							Style.def };
				}

				@Override
				protected List<IRule> createRules() {
					List<IRule> rules = new ArrayList<IRule>();
					Token defaultToken = getToken(Style.def);
					rules.add(new CommandNameRule() {
						public IToken getSuccessToken() {
							return getToken(Style.commandName);
						}

						@Override
						public IToken getDefaultReturnToken() {
							return getToken(Style.commandName);
						}
					});
					rules.add(new WordRule(new IWordDetector() {
						public boolean isWordStart(char c) {
							return EclLangUtil.isParamNameStart(c);
						}

						public boolean isWordPart(char c) {
							return EclLangUtil.isParamNamePart(c);
						}
					}, getToken(Style.paramName)));
					setDefaultReturnToken(defaultToken);
					return rules;
				}
			};
		} else if (ct.equals(Partition.string)) {
			return new SingleTokenScanner(this) {
				@Override
				protected String getSingleProperty() {
					return Style.string;
				}
			};
		} else if (ct.equals(Partition.comment)) {
			return new SingleTokenScanner(this) {
				@Override
				protected String getSingleProperty() {
					return Style.comment;
				}
			};
		} else
			throw new IllegalArgumentException();
	}

	@Override
	public String[] getPartitions() {
		return new String[] { Partition.string, Partition.comment };
	}

	@Override
	public IPartitionTokenScanner getPartitionScanner() {
		RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
		Token commentToken = new Token(Partition.comment);
		Token stringToken = new Token(Partition.string);
		scanner.setPredicateRules(new IPredicateRule[] {
				new EndOfLineRule("//", commentToken), //$NON-NLS-1$
				new MultiLineRule("/*", "*/", commentToken, (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$				
				new SingleLineRule("\"", "\"", stringToken, '\\'), //$NON-NLS-1$ //$NON-NLS-2$
				new SingleLineRule("'", "'", stringToken, '\\') }); //$NON-NLS-1$ //$NON-NLS-2$
		return scanner;
	}
}

abstract class CommandNameRule implements IPredicateRule {

	private ICharacterScanner scanner;

	public IToken evaluate(ICharacterScanner scanner) {
		this.scanner = scanner;
		int startColumn = scanner.getColumn();
		try {
			int ch = peek();
			if (!EclLangUtil.isCommandNameStart(ch))
				return Token.UNDEFINED;
			if (scanner.getColumn() < 0)
				return Token.UNDEFINED;
			if (scanner.getColumn() > 0) {
				ch = backPeek();
				while (EclLangUtil.isWhiteSpace(ch)) {
					if (scanner.getColumn() <= 0)
						break;
					ch = back();
				}
				if (scanner.getColumn() != 0) {
					if (!EclLangUtil.isCommandDelimiter(ch)) {
						gotoColumn(startColumn);
						return Token.UNDEFINED;
					}
				}
			}
			gotoColumn(startColumn);
			return evaluate(scanner, true);
		} catch (Exception e) {
			gotoColumn(startColumn);
			return Token.UNDEFINED;
		}
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		while (true) {
			int ch = scanner.read();
			if (ch == ICharacterScanner.EOF
					|| !EclLangUtil.isCommandNamePart(ch)) {
				break;
			}
		}
		scanner.unread();
		return getSuccessToken();
	}

	private int backPeek() {
		scanner.unread();
		return scanner.read();
	}

	private int peek() {
		int retVal = scanner.read();
		scanner.unread();
		return retVal;
	}

	private int back() {
		scanner.unread();
		scanner.unread();
		return scanner.read();
	}

	private void gotoColumn(int column) {
		while (column > scanner.getColumn()) {
			scanner.read();
		}
		while (column < scanner.getColumn()) {
			scanner.unread();
		}
	}

	public abstract IToken getDefaultReturnToken();

}