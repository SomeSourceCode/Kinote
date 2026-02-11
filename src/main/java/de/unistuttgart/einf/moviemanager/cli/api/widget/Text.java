package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.ComponentBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;

/**
 * Text.
 */
public class Text extends ComponentBase {

	private String text;

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

	@Override
	public void layout() {
		final int paddingTop = getPadding().getTop();
		final int paddingBottom = getPadding().getBottom();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getRight();

		final int textWidth = text == null ? 0 : text.length();

		setWidth(textWidth + paddingLeft + paddingRight);
		setHeight(1 + paddingTop + paddingBottom);
	}

	@Override
	protected void drawContent(Painter painter) {
		final int paddingTop = getPadding().getTop();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getLeft();

		final int width = getInnerWidth() - paddingLeft - paddingRight;

		painter.drawString(toGlobalX(paddingLeft), toGlobalY(paddingTop), width, text == null ? "" : text);
	}

}
