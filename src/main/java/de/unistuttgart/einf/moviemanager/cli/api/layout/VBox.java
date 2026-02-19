package de.unistuttgart.einf.moviemanager.cli.api.layout;

import com.googlecode.lanterna.TextColor;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;

/**
 * A layout pane that arranges its children vertically.
 */
public class VBox extends Parent {

	private boolean showSeparators = false;
	private int spacing = 0;

	private boolean autoSizeHeight = false;

	/**
	 * Returns whether this pane renders separators between
	 * its children.
	 *
	 * @return whether this pane renders separators
	 */
	public boolean isShowSeparators() {
		return showSeparators;
	}

	/**
	 * Sets whether this pane should render separators between
	 * its children.
	 *
	 * @param showSeparators whether this pane should render separators
	 */
	public void setShowSeparators(boolean showSeparators) {
		this.showSeparators = showSeparators;
	}

	/**
	 * Returns the spacing between children.
	 *
	 * @return the spacing
	 */
	public int getSpacing() {
		return spacing;
	}

	/**
	 * Sets the spacing between children.
	 *
	 * @param spacing the spacing
	 */
	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	/**
	 * Returns whether the pane automatically adjusts its height
	 * to fit its children.
	 *
	 * @return whether the pane auto-sizes its height to fit children
	 */
	public boolean isAutoSizeHeight() {
		return autoSizeHeight;
	}

	/**
	 * Sets whether the pane should automatically adjust its height
	 * to fit its children.
	 *
	 * @param autoSizeHeight whether to auto-size height to fit children
	 */
	public void setAutoSizeHeight(boolean autoSizeHeight) {
		this.autoSizeHeight = autoSizeHeight;
	}

	@Override
	protected void drawContent(Painter painter) {
		super.drawContent(painter);
		if (!isShowSeparators()) {
			return;
		}

		for (Component child : getChildren()) {
			final int lineY = child.getY() + child.getHeight();

			painter.drawSmartHorizontalLine(
					toGlobalX(0),
					toGlobalY(lineY),
					getInnerWidth(),
					TextColor.ANSI.DEFAULT,
					null
			);
		}
	}

	@Override
	public void layoutChildren() {
		final int paddingTop = getPadding().getTop();
		final int paddingBottom = getPadding().getBottom();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getRight();

		final int width = getInnerWidth() - paddingLeft - paddingRight;
		final int spacing = getSpacing();

		int y = paddingTop;
		for (Component child : getChildren()) {
			child.setX(paddingLeft);
			child.setY(y);
			child.setWidth(width);

			y += child.getHeight() + spacing;
			if (isShowSeparators()) {
				y += 1;
			}
		}

		if (autoSizeHeight) {
			setHeight(y + paddingBottom + (isShowBorders() ? 2 : 0));
		}
	}

	@Override
	public void addChild(Component component) {
		super.addChild(component);
	}

	@Override
	public void removeChild(Component component) {
		super.removeChild(component);
	}

}
