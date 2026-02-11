package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.ComponentBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.util.TextUtils;

import java.util.List;
import java.util.Objects;

/**
 * Text.
 */
public class Text extends ComponentBase {

	private String text;
	private boolean wrapping;

	private List<String> cachedWrappedLines;
	private String lastCalculatedText;
	private int lastCalculatedWidth = -1;

	/**
	 * Constructs a new empty text.
	 */
	public Text() {

	}

	/**
	 * Creates a new text.
	 *
	 * @param text the text to display
	 */
	public Text(String text) {
		this.text = text;
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
	 * @param text the text.
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Returns whether the text is wrapped. If set to false,
	 * the text will adjust its width automatically in order to fit
	 * in a single line.
	 * <p>
	 * If true, the text will wrap to fit inside the set width, adjusting
	 * only the height accordingly.
	 *
	 * @return whether text is wrapped.
	 */
	public boolean isWrapping() {
		return wrapping;
	}

	/**
	 * Sets whether the text should be wrapped. If set to false,
	 * the text will adjust its width automatically in order to fit
	 * in a single line.
	 * <p>
	 * If true, the text will wrap to fit inside the set width, adjusting
	 * only the height accordingly.
	 *
	 * @param wrapping whether text should be wrapped.
	 */
	public void setWrapping(boolean wrapping) {
		this.wrapping = wrapping;
	}

	@Override
	public void layout() {
		final int paddingTop = getPadding().getTop();
		final int paddingBottom = getPadding().getBottom();

		if (!wrapping) {
			final int paddingLeft = getPadding().getLeft();
			final int paddingRight = getPadding().getRight();

			final int textWidth = text == null ? 0 : text.length();

			setWidth(textWidth + paddingLeft + paddingRight);
			setHeight(1 + paddingTop + paddingBottom);
			return;
		}

		final List<String> wrappedLines = getWrappedLines();
		setHeight(wrappedLines.size() + paddingTop + paddingBottom);
	}

	@Override
	protected void drawContent(Painter painter) {
		final int paddingTop = getPadding().getTop();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getLeft();

		final int width = getInnerWidth() - paddingLeft - paddingRight;

		if (!wrapping) {
			painter.drawString(toGlobalX(paddingLeft), toGlobalY(paddingTop), width, text == null ? "" : text);
			return;
		}

		final List<String> wrappedLines = getWrappedLines();
		for (int i = 0; i < wrappedLines.size(); i++) {
			final String line = wrappedLines.get(i);
			painter.drawString(toGlobalX(paddingLeft), toGlobalY(paddingTop + i), line);
		}
	}

	private List<String> getWrappedLines() {
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getRight();
		final int contentWidth = getInnerWidth() - paddingLeft - paddingRight;

		if (Objects.equals(text, lastCalculatedText) && lastCalculatedWidth == contentWidth && cachedWrappedLines != null) {
			return cachedWrappedLines;
		}

		lastCalculatedText = text;
		lastCalculatedWidth = contentWidth;

		cachedWrappedLines = TextUtils.wrapText(text, contentWidth);
		return cachedWrappedLines;
	}

}
