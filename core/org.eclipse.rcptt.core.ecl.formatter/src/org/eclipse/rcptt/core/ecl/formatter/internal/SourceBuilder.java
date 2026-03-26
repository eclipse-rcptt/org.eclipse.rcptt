/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *     Google Gemini - test guided bugfixes
 *******************************************************************************/
package org.eclipse.rcptt.core.ecl.formatter.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rcptt.core.ecl.formatter.EclFormatterOptions;
import org.eclipse.rcptt.core.ecl.scanner.EclCharClasses;

/*
 * TODO:
 *   * Substitution spacing cleanup or linebreak/spacing remove flag.
 *   * In-line blocks.
 */

public class SourceBuilder {
	public static final Set<String> NO_WRAP_COMMANDS = new HashSet<String>(
			Arrays.asList("proc", "val"));

	private final EclFormatterOptions options;

	public SourceBuilder(EclFormatterOptions options) {
		this.options = options;
	}

	private List<Part> parts = new ArrayList<Part>();

	public Part append(String text, Block block, NewLine newLine, Spacing spacing,
			Glue glue, Quote quote) {
		Part part = new Part(text, block, newLine, spacing, glue, quote);
		parts.add(part);
		return part;
	}

	public void invalid(String text) {
		append(text, Block.None, NewLine.None,
				Spacing.Left, Glue.None, Quote.Never);
	}

	public void standaloneSlComment(String text) {
		append("// ", Block.None, NewLine.None,
				Spacing.None, Glue.Right, Quote.Never);
		append(trimComment(text), Block.None, NewLine.After,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void slCommentAtLineEnd(String text) {
		append("// ", Block.None, NewLine.None,
				Spacing.Left, Glue.Left, Quote.Never);
		append(trimComment(text), Block.None, NewLine.After,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void slCommentAtSequenceEnd(String text) {
		append("// ", Block.None, NewLine.None,
				Spacing.Left, Glue.Left, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void softSlCommentAtLineEnd(String text) {
		append("// ", Block.None, NewLine.None,
				Spacing.Left, Glue.Both, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);

		append("", Block.None, NewLine.Soft,
				Spacing.None, Glue.Right, Quote.Never);
	}

	public void standaloneMlComment(String text) {
		append("/* ", Block.None, NewLine.None,
				Spacing.None, Glue.Right, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
		append(" */", Block.None, NewLine.After,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void inlineMlComment(String text) {
		append("/* ", Block.None, NewLine.None,
				Spacing.Left, Glue.Both, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
		append(" */", Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void mlCommentAtLineStart(String text) {
		append("/* ", Block.None, NewLine.None,
				Spacing.None, Glue.Right, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
		append(" */", Block.None, NewLine.None,
				Spacing.Right, Glue.Both, Quote.Never);
	}

	public void mlCommentAtLineEnd(String text) {
		append("/* ", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
		append(" */", Block.None, NewLine.After,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void softInlineMlComment(String text) {
		append("/* ", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(trimComment(text), Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
		append(" */", Block.None, NewLine.None,
				Spacing.Right, Glue.None, Quote.Never);
	}

	public void linebreak() {
		append("", Block.None, NewLine.After,
				Spacing.None, Glue.None, Quote.Never);
	}

	public void semicolon() {
		append(";", Block.None, NewLine.None,
				Spacing.Right, Glue.Left, Quote.Never);
	}

	public void pipe() {
		append("|", Block.None, NewLine.None,
				Spacing.Both, Glue.Right, Quote.Never);
	}

	public void plus() {
		append("+", Block.None, NewLine.None,
				Spacing.Left, Glue.Left, Quote.Never);
	}

	public void commandName(String name) {
		append(name, Block.None, NewLine.None,
				Spacing.None,
				NO_WRAP_COMMANDS.contains(name.toLowerCase()) ? Glue.Right : Glue.None,
				Quote.Never);
	}

	public void boolArg(String text) {
		append("-", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(text, Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public Part positionalLiteralArg(String value, boolean wrap) {
		return append(value, Block.None, NewLine.None,
				Spacing.Left, Glue.None, Quote.Literal).wrappable(wrap);
	}

	public Part positionalQuotedLiteralArg(String value, boolean wrap) {
		return append(value, Block.None, NewLine.None,
				Spacing.Left, Glue.None, Quote.Always).wrappable(wrap);
	}

	public void positionalVariableArg(String name) {
		append("$", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(name, Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void variableEmit(String name) {
		append("$", Block.None, NewLine.None,
				Spacing.None, Glue.Right, Quote.Never);
		append(name, Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public Part namedLiteralArg(String name, String value) {
		namedLiteralArgName(name);
		return namedLiteralArgValue(value);
	}

	public void namedLiteralArgName(String name) {
		append("-", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(name, Block.None, NewLine.None,
				Spacing.Right, Glue.Both, Quote.Never);
	}

	public Part namedLiteralArgValue(String value) {
		return append(value, Block.None, NewLine.None,
				Spacing.Left, Glue.Left, Quote.Literal).wrappable(true);
	}

	public void namedVariableArg(String name, String variableName) {
		append("-", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(name, Block.None, NewLine.None,
				Spacing.Right, Glue.Both, Quote.Never);
		append("$", Block.None, NewLine.None,
				Spacing.Left, Glue.Both, Quote.Never);
		append(variableName, Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void pipelineArgName(String text) {
		append("-", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(text, Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
	}

	public void pipelineArgOpen() {
		append("[", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
	}

	public void pipelineArgClose() {
		append("]", Block.None, NewLine.None,
				Spacing.None, Glue.Left, Quote.Never);
	}

	public void scriptArgName(String text) {
		append("-", Block.None, NewLine.None,
				Spacing.Left, Glue.Right, Quote.Never);
		append(text, Block.None, NewLine.None,
				Spacing.None, Glue.Both, Quote.Never);
	}

	public void scriptArgOpen() {
		append("{", Block.Open, NewLine.After,
				Spacing.Left, Glue.Left, Quote.Never);
	}

	public void scriptArgClose() {
		append("}", Block.Close, NewLine.Before,
				Spacing.None, Glue.None, Quote.Never);
	}

	@Override
	public String toString() {
		final StringBuilder output = new StringBuilder();

		List<List<Part>> lines = findLines(parts);
		cleanBlocks(lines);

		int level = 0;
		final int last = lines.size() - 1;
		for (int i = 0; i < lines.size(); ++i) {
			final List<Part> l = lines.get(i);

			List<List<Part>> segments = findUnbreakableSegments(l);

			level += calculatePrelineLevelChange(l);
			wrap(output, level, segments);
			level += calculatePostlineLevelChange(l);
			if (level < 0)
				level = 0;

			if (i != last)
				output.append('\n');
		}

		// remove trailing linebreaks
		while (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
			output.setLength(output.length() - 1);

		return output.toString();
	}

	// --

	private void cleanBlocks(List<List<Part>> lines) {
		for (int i = 0; i < lines.size() - 1; ++i) {
			Part current = lastPart(lines.get(i));
			Part next = firstPart(lines.get(i + 1));

			if (current != null
					&& current.block == Block.Open
					&& (next == null ||
					next.newLine == NewLine.After && next.text.length() == 0
							&& lastPart(lines.get(i + 1)) == next))
				lines.remove(i + 1);
		}

		for (int i = lines.size() - 1; i >= 1; --i) {
			Part current = firstPart(lines.get(i));
			Part prev = lastPart(lines.get(i - 1));
			if (current != null
					&& current.block == Block.Close
					&& (prev == null ||
					prev.newLine == NewLine.After && prev.text.length() == 0
							&& firstPart(lines.get(i - 1)) == prev))
				lines.remove(i - 1);
		}
	}

	private Part firstPart(List<Part> line) {
		return line.size() > 0 ? line.get(0) : null;
	}

	private Part lastPart(List<Part> line) {
		return line.size() > 0 ? line.get(line.size() - 1) : null;
	}

	private int calculatePrelineLevelChange(List<Part> line) {
		for (Part p : line)
			if (p.block == Block.Close)
				return -1;

		return 0;
	}

	private int calculatePostlineLevelChange(List<Part> line) {
		for (Part p : line)
			if (p.block == Block.Open)
				return 1;

		return 0;
	}

	private void wrap(StringBuilder output, int level, List<List<Part>> line) {
		if (line.size() == 0 || (line.size() == 1 && line.get(0).size() == 1 && line.get(0).get(0).text.length() == 0))
			return; // this is a blank line

		int indent = level * options.indent;
		int currentLength = 0;

		boolean wrapped = false;
		Spacing lastSpacing = Spacing.None;

		for (List<Part> s : findSoftSegments(line)) {
			int segmentLength = getSegmentLength(s);

			boolean spacingAtStart = currentLength > 0 &&
					isSegmentStartHasSpacing(s, lastSpacing);
			if (spacingAtStart)
				segmentLength += 1;

			int fullLength = currentLength + indent + segmentLength +
					(wrapped ? options.wrapIndent : 0);

			while (segmentLength > 0 && fullLength > options.wrapAt
					&& firstPart(s).newLine != NewLine.Soft) {
				List<Part> newSegment = tryToWrapLitterals(s,
						currentLength + indent + (wrapped ? options.wrapIndent : 0) +
								(spacingAtStart ? 1 : 0));

				if (newSegment != s) {
					if (currentLength == 0)
						indent(output, indent + (wrapped ? options.wrapIndent : 0));
					else if (spacingAtStart)
						output.append(' ');
					formatSegment(output, newSegment);

					output.append('\n');

					// recalculate line info
					currentLength = 0;
					segmentLength = getSegmentLength(s);
					spacingAtStart = false;
					wrapped = true;

					fullLength = indent + segmentLength + options.wrapIndent;
				} else
					break;
			}

			if (currentLength > 0 // allows line to grow up
					&& (segmentLength > 0 && fullLength > options.wrapAt)
					|| firstPart(s).newLine == NewLine.Soft) {
				output.append('\n');
				currentLength = 0;

				for (Part p : s)
					if (p.text.length() != 0 || p.newLine != NewLine.Soft) {
						p.wrapped = true;
						break;
					}
				wrapped = true;
			}

			if (currentLength == 0) {
				indent(output, indent + (wrapped ? options.wrapIndent : 0));
			} else if (spacingAtStart) {
				output.append(' ');
				++currentLength;
			}

			currentLength += formatSegment(output, s);

			lastSpacing = s.get(s.size() - 1).spacing;
		}
	}

	private List<Part> tryToWrapLitterals(List<Part> segment, int currentLength) {
		final List<Part> first = new ArrayList<Part>();
		final List<Part> rest = new ArrayList<Part>(segment);
		while (rest.size() > 0) {
			final Part p = rest.get(0);
			first.add(p);
			rest.remove(0);
			final int segmentLength = getSegmentLength(first);

			if (currentLength + segmentLength > options.wrapAt) {
				if (!p.wrappable)
					return segment;

				final int literalLength = p.length();
				if (literalLength < options.minLiteralLengthToWrap)
					return segment;

				if (p.quote != Quote.Always && p.quote != Quote.OnWrap)
					return segment;

				final boolean quoted = p.quote == Quote.Always || p.wrapped;
				final int excess = currentLength + segmentLength - options.wrapAt;

				// Generated by Google Gemini:
				// p.text is ALREADY escaped. We need to split it such that it doesn't break escape sequences.
				// splitPoint is roughly length - excess.
				// Since Quote.Always adds 2 chars (quotes), and we are wrapping to 'wrapAt - 2' (roughly, for plus),
				// the calculation logic from before holds:
				// splitPoint = p.text.length() - excess - (quoted ? 2 : 4);

				int splitPoint = p.text.length() - excess - (quoted ? 2 : 4);

				if (splitPoint <= 0)
					return segment;

				String sub = p.text.substring(0, splitPoint);
				int lastSlash = sub.length() - 1;
				int slashCount = 0;
				while (lastSlash >= 0 && sub.charAt(lastSlash) == '\\') {
					slashCount++;
					lastSlash--;
				}
				// Odd number of trailing slashes, means we split in middle of escape sequence (e.g. \\, \n, etc.)
				// If ...\\ (2 slashes) -> OK.
				// If ...\ (1 slash) -> It is start of escape sequence (like \n or \\ or \"). BAD.
				if (slashCount % 2 != 0) {
					splitPoint -= 1;
				}

				if (splitPoint <= 0 || literalLength - excess < options.minLiteralLengthToStayOnWrap)
					return segment;

				first.remove(first.size() - 1);
				// Use private constructor with escape=false because p.text substring is already escaped
				first.add(new Part(p.text.substring(0, splitPoint),
						Block.None, NewLine.None, p.spacing, Glue.None, Quote.Always, false));
				first.add(new Part("+", Block.None, NewLine.None, Spacing.Left, Glue.None,
						Quote.Never));

				// Use private constructor with escape=false
				final Part newLiteral = new Part(p.text.substring(splitPoint),
						Block.None, NewLine.None, p.spacing, Glue.None, Quote.Always, false);
				newLiteral.wrappable = true;
				rest.add(0, newLiteral);

				segment.clear();
				segment.addAll(rest);
				return first;
			}
		}

		return segment;
	}

	private List<List<Part>> findSoftSegments(List<List<Part>> line) {
		List<List<Part>> newLine = new ArrayList<List<Part>>();

		for (List<Part> s : line) {
			List<Part> newSegment = new ArrayList<Part>();
			newLine.add(newSegment);

			for (Part p : s)
				if (p.newLine == NewLine.Soft) {
					if (newSegment.size() == 0)
						newSegment.add(p);
					else {
						newSegment = new ArrayList<Part>();
						newLine.add(newSegment);
						newSegment.add(p);
					}
				} else
					newSegment.add(p);
		}
		return newLine;
	}

	private boolean[] buildSpacesMap(List<Part> segment) {
		boolean map[] = new boolean[segment.size() + 1];

		for (int i = 0; i < segment.size(); ++i) {
			Part p = segment.get(i);

			switch (p.spacing) {
			case None:
				break;
			case Left:
				map[i] = true;
				break;
			case Right:
				map[i + 1] = true;
				break;
			case Both:
				map[i] = map[i + 1] = true;
				break;
			}
		}

		for (int i = 0; i < segment.size(); ++i) {
			Part p = segment.get(i);
			if (p.newLine == NewLine.Soft && p.text.length() == 0)
				map[i + 1] = false;
		}

		return map;
	}

	private int formatSegment(StringBuilder builder, List<Part> segment) {
		int length = 0;
		boolean spacesMap[] = buildSpacesMap(segment);

		int last = segment.size() - 1;
		for (int i = 0; i < segment.size(); ++i) {
			Part p = segment.get(i);
			length += p.format(builder);
			if (i != last && spacesMap[i + 1]) {
				builder.append(' ');
				++length;
			}
		}

		return length;
	}

	private int indent(StringBuilder builder, int spaces) {
		int result = spaces;

		if (options.useTabs) {
			for (int i = 0; i < spaces / options.tabSize; ++i)
				builder.append('\t');
			spaces = spaces % options.tabSize;
		}
		for (int i = 0; i < spaces; ++i)
			builder.append(' ');

		return result;
	}

	private int getSegmentLength(List<Part> segment) {
		int length = 0;
		for (Part p : segment)
			length += p.length();

		boolean spacesMap[] = buildSpacesMap(segment);
		for (int i = 1; i < segment.size(); ++i)
			if (spacesMap[i])
				++length;

		return length;
	}

	private boolean isSegmentStartHasSpacing(List<Part> segment, Spacing endSpacing) {
		Part start = null;

		for (Part p : segment)
			if (p.text.length() != 0 || p.newLine != NewLine.Soft) {
				start = p;
				break;
			}

		if (start == null)
			return endSpacing == Spacing.Both || endSpacing == Spacing.Right;

		switch (segment.get(0).spacing) {
		case None:
			return endSpacing == Spacing.Both || endSpacing == Spacing.Right;
		case Left:
			return true;
		case Right:
			return endSpacing == Spacing.Both || endSpacing == Spacing.Right;
		case Both:
			return true;
		default:
			throw new IllegalStateException();
		}
	}

	private List<List<Part>> findLines(List<Part> parts) {
		List<List<Part>> lines = new ArrayList<List<Part>>();
		List<Part> line = new ArrayList<Part>();
		for (Part p : parts) {
			switch (p.newLine) {
			case None:
				line.add(p);
				break;
			case Before:
				lines.add(line);
				line = new ArrayList<Part>();
				line.add(p);
				break;
			case After:
				line.add(p);
				lines.add(line);
				line = new ArrayList<Part>();
				break;
			case Soft:
				line.add(p);
				break;
			}
		}

		if (!line.isEmpty())
			lines.add(line);

		return lines;
	}

	private static List<List<Part>> findUnbreakableSegments(List<Part> parts) {
		List<List<Part>> segments = new ArrayList<List<Part>>();
		if (parts.size() == 0)
			return segments;

		Part prev = parts.get(0);
		List<Part> segment = newSegment(segments, prev);

		for (int i = 1; i < parts.size(); ++i) {
			Part p = parts.get(i);

			switch (p.glue) {
			case None:
				switch (prev.glue) {
				case None:
					segment = newSegment(segments, p);
					break;
				case Left:
					segment = newSegment(segments, p);
					break;
				case Right:
					segment.add(p);
					break;
				case Both:
					segment.add(p);
					break;
				}
				break;
			case Left:
				switch (prev.glue) {
				case None:
					segment.add(p);
					break;
				case Left:
					segment.add(p);
					break;
				case Right:
					segment.add(p);
					break;
				case Both:
					segment.add(p);
					break;
				}
				break;
			case Right:
				switch (prev.glue) {
				case None:
					segment = newSegment(segments, p);
					break;
				case Left:
					segment = newSegment(segments, p);
					break;
				case Right:
					segment.add(p);
					break;
				case Both:
					segment.add(p);
					break;
				}
				break;
			case Both:
				switch (prev.glue) {
				case None:
					segment.add(p);
					break;
				case Left:
					segment.add(p);
					break;
				case Right:
					segment.add(p);
					break;
				case Both:
					segment.add(p);
					break;
				}
				break;
			}

			prev = p;
		}

		// last segment may be empty, remove it
		if (segments.get(segments.size() - 1).isEmpty())
			segments.remove(segments.size() - 1);

		return segments;
	}

	private static List<Part> newSegment(List<List<Part>> segments, Part... parts) {
		List<Part> segment = new ArrayList<Part>();
		for (Part p : parts)
			segment.add(p);
		segments.add(segment);
		return segment;
	}

	public static enum Block {
		None,
		Open,
		Close
	}

	public static enum NewLine {
		None,
		Before,
		After,
		Soft
	}

	public static enum Spacing {
		None,
		Left,
		Right,
		Both
	}

	public static enum Glue {
		None,
		Left,
		Right,
		Both
	}

	public static enum Quote {
		Never,
		OnWrap,
		Literal,
		Always
	}

	public static class Part {
		private String text;
		private Block block;
		private Spacing spacing;
		private Glue glue;
		private NewLine newLine;

		private Quote quote;

		private boolean wrappable = false;
		private boolean wrapped = false;

		private Part(String text, Block block, NewLine newLine, Spacing spacing, Glue glue, Quote quote) {
			this(text, block, newLine, spacing, glue, quote, true);
		}

		private Part(String text, Block block, NewLine newLine, Spacing spacing, Glue glue, Quote quote,
				boolean escape) {
			this.text = text;
			this.block = block;
			this.newLine = newLine;
			this.spacing = spacing;
			this.glue = glue;
			this.quote = quote;

			if (escape) {
				if (quote == Quote.Literal)
					handleQuotingAndEscaping();
				else if (quote == Quote.Always)
					this.text = escape(text);
			}
		}

		public void setQuoting(Quote mode) {
			quote = mode;
		}

		private Part wrappable(boolean value) {
			wrappable = value;
			return this;
		}

		private int format(StringBuilder builder) {
			boolean quotes;
			switch (quote) {
			case Never:
				quotes = false;
				break;
			case OnWrap:
				quotes = wrapped;
				break;
			case Always:
				quotes = true;
				break;
			default:
				throw new IllegalStateException();
			}

			if (quotes)
				builder.append('"');
			builder.append(text);
			if (quotes)
				builder.append('"');

			return text.length() + (quotes ? 2 : 0);
		}

		private int length() {
			int length = text.length();
			switch (quote) {
			case Never:
				break;
			case OnWrap:
				if (wrapped)
					length += 2;
				break;
			case Always:
				length += 2;
				break;
			}
			return length;
		}

		private void handleQuotingAndEscaping() {
			if (text.length() == 0) {
				quote = Quote.Always;
				return;
			}

			if (isNoQuotesIdentifier(text)) {
				quote = Quote.OnWrap;
				return;
			}

			boolean isNumeric = true;
			for (int i = 0; i < text.length(); ++i) {
				char ch = text.charAt(i);

				if (!(ch >= '0' && ch <= '9')) {
					isNumeric = false;
					break;
				}
			}

			if (isNumeric) {
				quote = Quote.Never;
				return;
			}

			quote = Quote.Always;
			text = escape(text);
		}

		private boolean isNoQuotesIdentifier(String text) {
			if (text.length() == 0)
				return false;

			if (!EclCharClasses.isIdentifierStart(text.charAt(0)))
				return false;

			for (int i = 1; i < text.length(); ++i) {
				char c = text.charAt(i);
				if (c == '-' || !EclCharClasses.isIdentifier(c))
					return false;
			}

			return true;
		}

		private String escape(String text) {
			StringBuilder result = new StringBuilder();

			for (int i = 0; i < text.length(); ++i) {
				final char ch = text.charAt(i);
				switch (ch) {
				case '\b':
					result.append("\\b");
					break;
				case '\t':
					result.append("\\t");
					break;
				case '\n':
					result.append("\\n");
					break;
				case '\f':
					result.append("\\f");
					break;
				case '\r':
					result.append("\\r");
					break;
				case '\\':
					result.append("\\\\");
					break;
				case '\'':
					result.append('\'');
					break;
				case '"':
					result.append("\\\"");
					break;
				default:
					result.append(ch);
					break;
				}
			}

			return result.toString();
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private String trimComment(String comment) {
		int length = comment.length();
		int start = 0;

		while (start < length &&
				EclCharClasses.isWhitespace(comment.charAt(start)))
			++start;

		while ((start < length) &&
				EclCharClasses.isWhitespace(comment.charAt(length - 1))) {
			--length;
		}
		return ((start > 0) || (length < comment.length())) ?
				comment.substring(start, length) : comment;
	}
}
