package de.unistuttgart.einf.moviemanager.cli.api.layout;

import com.googlecode.lanterna.TextColor;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;

/**
 * A layout pane that arranges its children horizontally.
 */
public class HBox extends Parent {

	private boolean showSeparators = false;
	private int spacing = 0;

	private boolean autoSizeWidth = false;

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
	 * Returns whether the pane automatically adjusts its width
	 * to fit its children.
	 *
	 * @return whether the pane auto-sizes its width to fit children
	 */
	public boolean isAutoSizeWidth() {
		return autoSizeWidth;
	}

	/**
	 * Sets whether the pane should automatically adjust its width
	 * to fit its children.
	 *
	 * @param autoSizeWidth whether to auto-size width to fit children
	 */
	public void setAutoSizeWidth(boolean autoSizeWidth) {
		this.autoSizeWidth = autoSizeWidth;
	}

	@Override
	protected void drawContent(Painter painter) {
		super.drawContent(painter);
		if (!isShowSeparators()) {
			return;
		}

		for (Component child : getChildren()) {
			final int lineX = child.getX() + child.getWidth();

			painter.drawSmartVerticalLine(
					toGlobalX(lineX),
					toGlobalY(0),
					getInnerHeight(),
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

		final int height = getInnerHeight() - paddingTop - paddingBottom;
		final int spacing = getSpacing();

		int x = paddingLeft;
		for (Component child : getChildren()) {
			child.setX(x);
			child.setY(paddingTop);
			child.setHeight(height);

			x += child.getWidth() + spacing;
			if (isShowSeparators()) {
				x += 1;
			}
		}

		if (autoSizeWidth) {
			setWidth(x + paddingRight + (isShowSeparators() ? 1 : 0));
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
