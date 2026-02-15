package de.unistuttgart.einf.moviemanager.cli.api.widget;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;
import de.unistuttgart.einf.moviemanager.cli.api.InputResult;
import de.unistuttgart.einf.moviemanager.cli.api.InteractableBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An editable multi-line text area component.
 */
public class TextArea extends InteractableBase {

	private record VisualLine(String text, int sourceStartIndex, boolean isSoftBreak) {

		private int length() {
			return text.length();
		}

	}

	private record CursorCoordinate(int visualRow, int visualColumn) {}

	private String text;
	private final List<VisualLine> lines = new ArrayList<>();

	private Predicate<String> inputFilter;

	private int cursorIndex = 0;
	private Integer preferredCursorX = null;

	private int scrollY = 0;
	private int scrollX = 0;

	private boolean wrapping = true;
	private boolean autoSizeWidth = false;
	private boolean autoSizeHeight = true;

	private Consumer<String> onSubmit;

	/**
	 * Constructs a new empty text area.
	 */
	public TextArea() {
		this("");
	}

	/**
	 * Constructs a new text area with the given initial text.
	 *
	 * @param text the text
	 */
	public TextArea(String text) {
		this.text = text == null ? "" : text;
		recalculateLines();
	}

	/**
	 * Returns the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text the text
	 */
	public void setText(String text) {
		text = text == null ? "" : text;
		if (Objects.equals(this.text, text)) {
			return;
		}
		this.text = text;
		this.cursorIndex = Math.min(cursorIndex, this.text.length());
		recalculateLines();
	}

	/**
	 * Returns the input filter.
	 *
	 * @return the input filter
	 */
	public Predicate<String> getInputFilter() {
		return inputFilter;
	}

	/**
	 * Sets the input filter.
	 *
	 * @param inputFilter the input filter
	 */
	public void setInputFilter(Predicate<String> inputFilter) {
		this.inputFilter = inputFilter;
	}

	private boolean isTextAllowed(String text) {
		return inputFilter == null || inputFilter.test(text);
	}

	/**
	 * Returns the consumer that is called after editing is finished.
	 *
	 * @return the consumer
	 */
	public Consumer<String> getOnSubmit() {
		return onSubmit;
	}

	/**
	 * Sets the consumer that is called after editing is finished.
	 *
	 * @param onSubmit the consumer
	 */
	public void setOnSubmit(Consumer<String> onSubmit) {
		this.onSubmit = onSubmit;
	}

	/**
	 * Fires the consumer set by {@link #setOnSubmit(Consumer)}.
	 */
	public void fireOnSubmit() {
		if (onSubmit == null) {
			return;
		}
		onSubmit.accept(text);
	}

	/**
	 * Sets whether the text should wrap at the component's width.
	 * <p>
	 * If true, long lines will be wrapped to fit the component's width.
	 * Otherwise, the text area will either scroll horizontally or adjust
	 * its width to fit the longest line, depending on the auto-size settings.
	 *
	 * @param wrapping whether to soft-wrap the text
	 * @see #setAutoSizeHeight(boolean)
	 * @see #setAutoSizeWidth(boolean)
	 */
	public void setWrapping(boolean wrapping) {
		if (this.wrapping == wrapping) {
			return;
		}
		this.wrapping = wrapping;
		recalculateLines();
	}

	/**
	 * Returns whether the component automatically adjusts its width to fit the longest line of text.
	 *
	 * @return whether the component auto-sizes its width to fit the longest line
	 */
	public boolean isAutoSizeWidth() {
		return autoSizeWidth;
	}

	/**
	 * Sets whether the component should automatically adjust its width to fit the longest
	 * line of text.
	 *
	 * @param autoSizeWidth whether to auto-size the width to fit the longest line
	 */
	public void setAutoSizeWidth(boolean autoSizeWidth) {
		this.autoSizeWidth = autoSizeWidth;
	}

	/**
	 * Returns whether the component automatically adjusts its height to fit the content.
	 *
	 * @return whether the component auto-sizes its height to fit the content
	 */
	public boolean isAutoSizeHeight() {
		return autoSizeHeight;
	}

	/**
	 * Sets whether the component should automatically adjust its height to fit the content.
	 *
	 * @param autoSizeHeight whether to auto-size the height to fit the content
	 */
	public void setAutoSizeHeight(boolean autoSizeHeight) {
		this.autoSizeHeight = autoSizeHeight;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	/* *************************************************************** *
	 *                             Layout                              *
	 * *************************************************************** */

	@Override
	public void layout() {
		if (autoSizeWidth) {
			int maxLineLength = 0;
			int cursorVisualColumn = 0;
			int currentSourceIndex = 0;

			final String[] paragraphs = text.split("\n", -1);
			for (String paragraph : paragraphs) {
				maxLineLength = Math.max(maxLineLength, paragraph.length());

				final int lineEndIndex = currentSourceIndex + paragraph.length();
				if (cursorIndex >= currentSourceIndex && cursorIndex <= lineEndIndex) {
					cursorVisualColumn = cursorIndex - currentSourceIndex;
				}

				currentSourceIndex += paragraph.length() + 1; // +1 for \n
			}

			final int horizontalPadding = getPadding().getLeft() + getPadding().getRight() + (isShowBorders() ? 2 : 0);
			final int extraSpace = isFocused() && cursorVisualColumn >= maxLineLength ? 1 : 0;

			setWidth(maxLineLength + horizontalPadding + extraSpace);
		}

		recalculateLines();

		if (autoSizeHeight) {
			final CursorCoordinate cursorCoords = getCursorCoordinates();
			int requiredHeight = lines.size();
			if (cursorCoords.visualRow() >= requiredHeight) {
				requiredHeight = cursorCoords.visualRow() + 1;
			}

			final int verticalPadding = getPadding().getTop() + getPadding().getBottom() + (isShowBorders() ? 2 : 0);

			setHeight(requiredHeight + verticalPadding);
		}
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		ensureCursorVisible();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		ensureCursorVisible();
	}

	/* *************************************************************** *
	 *                            Rendering                            *
	 * *************************************************************** */

	@Override
	protected void drawContent(Painter painter) {
		if (isFocused()) {
			ensureCursorVisible();
		} else {
			scrollX = 0;
			scrollY = 0;
		}

		final int visibleHeight = getInnerHeight();
		final int visibleWidth = getInnerWidth();

		for (int i = 0; i < visibleHeight; i++) {
			final int lineIndex = scrollY + i;
			if (lineIndex >= lines.size()) {
				break;
			}

			final VisualLine line = lines.get(lineIndex);

			String content = line.text();
			if (scrollX > 0) {
				if (scrollX < content.length()) {
					content = content.substring(scrollX);
				} else {
					content = "";
				}
			}

			painter.drawString(toGlobalX(0), toGlobalY(i), visibleWidth, content);
		}

		if (isFocused()) {
			drawCursor(painter);
		}
	}

	private void drawCursor(Painter painter) {
		final CursorCoordinate coordinate = getCursorCoordinates();

		final int localY = coordinate.visualRow() - scrollY;
		final int localX = coordinate.visualColumn() - (wrapping ? 0 : scrollX);

		if (localY < 0 || localY >= getInnerHeight()) {
			return;
		}
		if (localX < 0 || localX >= getInnerWidth()) {
			return;
		}

		char charUnderCursor = ' ';
		if (coordinate.visualRow() < lines.size()) {
			final String lineText = lines.get(coordinate.visualRow()).text();
			if (coordinate.visualColumn() < lineText.length()) {
				charUnderCursor = lineText.charAt(coordinate.visualColumn());
			}
		}

		painter.drawChar(
				toGlobalX(localX),
				toGlobalY(localY),
				charUnderCursor,
				TextColor.ANSI.BLACK,
				TextColor.ANSI.WHITE_BRIGHT
		);
	}

	private void recalculateLines() {
		lines.clear();

		final int width = getInnerWidth();
		final int height = getInnerHeight();

		if (width <= 0) {
			return;
		}

		if (!wrapping) {
			for (String line : text.split("\n", -1)) {
				lines.add(new VisualLine(line, 0, false));
			}
			return;
		}

		int currentStart = 0;
		final String[] paragraphs = text.split("\n", -1);

		for (String paragraph : paragraphs) {
			if (paragraph.isEmpty()) {
				lines.add(new VisualLine("", currentStart, false));
				currentStart++;
				continue;
			}

			int paragraphIndex = 0;
			while (paragraphIndex < paragraph.length()) {
				final int remainingLength = paragraph.length() - paragraphIndex;
				int chunkLength = Math.min(remainingLength, width);

				if (chunkLength < remainingLength) {
					final int lastSpace = paragraph.lastIndexOf(' ', paragraphIndex + chunkLength);
					if (lastSpace > paragraphIndex) {
						chunkLength = lastSpace - paragraphIndex + 1;
					}
				}

				final String visualText = paragraph.substring(paragraphIndex, paragraphIndex + chunkLength);
				final boolean isSoftBreak = (paragraphIndex + chunkLength < paragraph.length());

				lines.add(new VisualLine(visualText, currentStart + paragraphIndex, isSoftBreak));
				paragraphIndex += chunkLength;
			}
			currentStart += paragraph.length() + 1;
		}

		if (lines.isEmpty()) {
			lines.add(new VisualLine("", 0, false));
		}
	}

	/* *************************************************************** *
	 *                       Navigation/Actions                        *
	 * *************************************************************** */

	private CursorCoordinate getCursorCoordinates() {
		for (int i = 0; i < lines.size(); i++) {
			final VisualLine line = lines.get(i);
			final int lineStart = line.sourceStartIndex();
			final int lineEnd = lineStart + line.length();

			if (cursorIndex >= lineStart && cursorIndex < lineEnd) {
				final int localColumn = cursorIndex - lineStart;
				if (localColumn >= getInnerWidth() && line.isSoftBreak()) {
					return new CursorCoordinate(i + 1, 0);
				}

				return new CursorCoordinate(i, localColumn);
			}

			if (cursorIndex == lineEnd) {
				if (line.isSoftBreak()) {
					continue;
				}

				if ((cursorIndex - lineStart) >= getInnerWidth()) {
					return new CursorCoordinate(i + 1, 0);
				}

				return new CursorCoordinate(i, cursorIndex - lineStart);
			}
		}

		final int lastRow = Math.max(0, lines.size() - 1);
		int lastColumn = 0;
		if (!lines.isEmpty()) {
			lastColumn = lines.get(lastRow).length();
		}

		if (wrapping && lastColumn >= getInnerWidth()) {
			return new CursorCoordinate(lastRow + 1, 0);
		}

		return new CursorCoordinate(lastRow, lastColumn);
	}

	private boolean isSoftWrapCausingSpace(int index) {
		if (index < 0 || index >= text.length() || Character.isWhitespace(text.charAt(index))) {
			return false;
		}

		for (VisualLine line : lines) {
			if (line.sourceStartIndex() + line.length() == index + 1) {
				return line.isSoftBreak();
			}
		}
		return false;
	}

	private void ensureCursorVisible() {
		final CursorCoordinate coordinate = getCursorCoordinates();

		// vertical scroll
		if (autoSizeHeight) {
			scrollY = 0;
		} else if (coordinate.visualRow() < scrollY) {
			scrollY = coordinate.visualRow();
		} else if (coordinate.visualRow() >= scrollY + getInnerHeight()) {
			scrollY = coordinate.visualRow() - getInnerHeight() + 1;
		}

		// horizontal scroll
		if (wrapping || autoSizeWidth) {
			scrollX = 0;
			return;
		}
		int maxContentWidth = 0;
		for (VisualLine line : lines) {
			maxContentWidth = Math.max(maxContentWidth, line.length());
		}
		final int maxScrollX = Math.max(0, maxContentWidth - getInnerWidth() + 1);

		if (scrollX > maxScrollX) {
			scrollX = maxScrollX;
		}

		if (coordinate.visualColumn() < scrollX) {
			scrollX = coordinate.visualColumn();
		} else if (coordinate.visualColumn() >= scrollX + getInnerWidth()) {
			scrollX = coordinate.visualColumn() - getInnerWidth() + 1;
		}
	}

	/**
	 * Inserts the given character at the cursor position.
	 *
	 * @param character the character
	 */
	public void insert(char character) {
		final String oldText = text;
		if (cursorIndex >= text.length()) {
			text += character;
		} else {
			text = text.substring(0, cursorIndex) + character + text.substring(cursorIndex);
		}

		if (!isTextAllowed(text)) {
			text = oldText;
			return;
		}

		cursorIndex++;
		preferredCursorX = null;
		recalculateLines();
		ensureCursorVisible();
	}

	/**
	 * Deletes the character at the cursor.
	 */
	public void backspace() {
		if (cursorIndex <= 0) {
			return;
		}
		text = text.substring(0, cursorIndex - 1) + text.substring(cursorIndex);
		cursorIndex--;
		preferredCursorX = null;
		recalculateLines();
		ensureCursorVisible();
	}

	/**
	 * Deletes the character to the right of the cursor.
	 */
	public void delete() {
		if (cursorIndex >= text.length()) {
			return;
		}
		text = text.substring(0, cursorIndex) + text.substring(cursorIndex + 1);
		preferredCursorX = null;
		recalculateLines();
		ensureCursorVisible();
	}

	/**
	 * Moves the cursor to the left.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveLeft() {
		if (cursorIndex <= 0) {
			return false;
		}
		cursorIndex--;

		if (isSoftWrapCausingSpace(cursorIndex)) {
			cursorIndex--;
		}

		preferredCursorX = null;
		ensureCursorVisible();
		return true;
	}

	/**
	 * Moves the cursor to the right.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveRight() {
		if (cursorIndex >= text.length()) {
			return false;
		}
		cursorIndex++;

		if (isSoftWrapCausingSpace(cursorIndex)) {
			cursorIndex++;
		}

		preferredCursorX = null;
		ensureCursorVisible();
		return true;
	}

	/**
	 * Moves the cursor one line up, preserving the visual column.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveUp() {
		final CursorCoordinate coordinate = getCursorCoordinates();
		if (coordinate.visualRow() <= 0) {
			return false;
		}

		final VisualLine targetLine = lines.get(coordinate.visualRow() - 1);

		if (preferredCursorX == null) {
			preferredCursorX = coordinate.visualColumn();
		}

		final int maxColumn = targetLine.isSoftBreak() ? Math.max(0, targetLine.length() - 1) : targetLine.length();
		final int targetColumn = Math.min(preferredCursorX, maxColumn);
		final int newCursorIndex = targetLine.sourceStartIndex() + targetColumn;

		if (newCursorIndex == cursorIndex) {
			return false;
		}

		cursorIndex = newCursorIndex;

		ensureCursorVisible();
		return true;
	}

	/**
	 * Moves the cursor one line down, preserving the visual column.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveDown() {
		final CursorCoordinate coordinate = getCursorCoordinates();
		if (coordinate.visualRow() >= lines.size() - 1) {
			return false;
		}

		final VisualLine targetLine = lines.get(coordinate.visualRow() + 1);

		if (preferredCursorX == null) {
			preferredCursorX = coordinate.visualColumn();
		}

		final int maxColumn = targetLine.isSoftBreak() ? Math.max(0, targetLine.length() - 1) : targetLine.length();
		final int targetColumn = Math.min(preferredCursorX, maxColumn);
		final int newCursorIndex = targetLine.sourceStartIndex() + targetColumn;

		if (newCursorIndex == cursorIndex) {
			return false;
		}

		cursorIndex = newCursorIndex;

		ensureCursorVisible();
		return true;
	}

	/**
	 * Moves the cursor to the start of the current line.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveToLineStart() {
		final CursorCoordinate coordinate = getCursorCoordinates();
		if (coordinate.visualColumn() == 0 || coordinate.visualRow() >= lines.size()) {
			return false;
		}

		cursorIndex = lines.get(coordinate.visualRow()).sourceStartIndex();
		preferredCursorX = 0;

		ensureCursorVisible();
		return true;
	}

	/**
	 * Moves the cursor to the end of the current line.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveToLineEnd() {
		final CursorCoordinate coordinate = getCursorCoordinates();
		if (coordinate.visualRow() >= lines.size()) {
			return false;
		}

		final VisualLine line = lines.get(coordinate.visualRow());
		final int endColumn = line.isSoftBreak() ? Math.max(0, line.length() - 1) : line.length();

		if (coordinate.visualColumn() >= endColumn) {
			return false;
		}

		cursorIndex = line.sourceStartIndex() + endColumn;
		preferredCursorX = endColumn;

		ensureCursorVisible();
		return true;
	}

	private boolean isWordChar(char character) {
		return Character.isLetterOrDigit(character) || '_' == character;
	}

	/**
	 * Moves the cursor one word to the right.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveWordRight() {
		if (cursorIndex >= text.length()) {
			return false;
		}

		final int oldCursorIndex = cursorIndex;

		while (cursorIndex < text.length() && Character.isWhitespace(text.charAt(cursorIndex))) {
			cursorIndex++;
		}

		final boolean startIsWord = cursorIndex < text.length() && isWordChar(text.charAt(cursorIndex));

		while (cursorIndex < text.length()) {
			final char character = text.charAt(cursorIndex);
			if (Character.isWhitespace(character) || isWordChar(character) != startIsWord) {
				break;
			}
			cursorIndex++;
		}

		if (isSoftWrapCausingSpace(cursorIndex)) {
			cursorIndex++;
		}

		if (cursorIndex == oldCursorIndex) {
			return false;
		}

		preferredCursorX = null;
		ensureCursorVisible();
		return true;
	}

	/**
	 * Moves the cursor one word to the left.
	 *
	 * @return whether the cursor position changed
	 */
	private boolean moveWordLeft() {
		if (cursorIndex <= 0) {
			return false;
		}

		final int oldCursorIndex = cursorIndex;

		do {
			cursorIndex--;
		} while (cursorIndex > 0 && Character.isWhitespace(text.charAt(cursorIndex)));

		final boolean startIsWord = isWordChar(text.charAt(cursorIndex));

		while (cursorIndex > 0) {
			final char previousChar = text.charAt(cursorIndex - 1);
			if (Character.isWhitespace(previousChar) || isWordChar(previousChar) != startIsWord) {
				break;
			}
			cursorIndex--;
		}

		if (isSoftWrapCausingSpace(cursorIndex)) {
			cursorIndex--;
		}

		if (cursorIndex == oldCursorIndex) {
			return false;
		}

		preferredCursorX = null;
		ensureCursorVisible();
		return true;
	}

	/* *************************************************************** *
	 *                              Input                              *
	 * *************************************************************** */

	@Override
	public InputResult handleInput(KeyStroke key, CliMode mode) {
		if (mode == CliMode.COMMAND) {
			return InputResult.UNHANDLED;
		}

		switch (key.getKeyType()) {
			case ArrowLeft -> {
				if (!moveLeft()) {
					return InputResult.UNHANDLED;
				}
				return InputResult.HANDLED;
			}
			case ArrowRight -> {
				if (!moveRight()) {
					return InputResult.UNHANDLED;
				}
				return InputResult.HANDLED;
			}
			case ArrowUp -> {
				if (!moveUp() && !moveToLineStart()) {
					return InputResult.UNHANDLED;
				}
				return InputResult.HANDLED;
			}
			case ArrowDown -> {
				if (!moveDown() && !moveToLineEnd()) {
					return InputResult.UNHANDLED;
				}
				return InputResult.HANDLED;
			}
			case Home -> {
				moveToLineStart();
				return InputResult.HANDLED;
			}
			case End -> {
				moveToLineEnd();
				return InputResult.HANDLED;
			}
			case Character -> {
				if (!key.isAltDown()) {
					break;
				}
				switch (key.getCharacter()) {
					case 'b' -> {
						moveWordLeft();
						return InputResult.HANDLED;
					}
					case 'f' -> {
						moveWordRight();
						return InputResult.HANDLED;
					}
				}
			}
		}

		if (mode == CliMode.NAVIGATION) {
			switch (key.getKeyType()) {
				case Character -> {
					switch (key.getCharacter()) {
						case 'A' -> {
							moveToLineEnd();
							return InputResult.ENTER_EDIT_MODE;
						}
						case 'c' -> {
							int start = text.lastIndexOf('\n', Math.max(0, cursorIndex - 1));
							start = (start == -1) ? 0 : start + 1;

							int end = text.indexOf('\n', cursorIndex);
							if (end == -1) {
								end = text.length();
							}

							final String paragraph = text.substring(start, end);
							copyToClipboard(paragraph);
							return InputResult.HANDLED;
						}
						case 'C' -> {
							copyToClipboard(text);
							return InputResult.HANDLED;
						}
					}
				}
			}
		}

		if (mode == CliMode.EDIT) {
			switch (key.getKeyType()) {
				case Character -> {
					insert(key.getCharacter());
					return InputResult.HANDLED;
				}
				case Backspace -> {
					backspace();
					return InputResult.HANDLED;
				}
				case Delete -> {
					delete();
					return InputResult.HANDLED;
				}
				case Enter -> {
					insert('\n');
					return InputResult.HANDLED;
				}
				case Escape -> {
					fireOnSubmit();
					return InputResult.LEAVE_EDIT_MODE;
				}
			}
		}

		return InputResult.UNHANDLED;
	}

	private void copyToClipboard(String text) {
		System.setProperty("apple.awt.UIElement", "true");

		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final StringSelection data = new StringSelection(text);
		clipboard.setContents(data, null);
	}

}
