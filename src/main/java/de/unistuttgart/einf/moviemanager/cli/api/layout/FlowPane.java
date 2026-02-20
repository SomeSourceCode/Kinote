package de.unistuttgart.einf.moviemanager.cli.api.layout;

import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;

/**
 * A layout pane that lays out its children horizontally, wrapping
 * to the next line if necessary.
 */
public class FlowPane extends Parent {

	private int hGap;
	private int vGap;

	private boolean autoAdjustHeight = false;

	/**
	 * Returns the horizontal spacing between children.
	 *
	 * @return the horizontal spacing
	 */
	public int getHGap() {
		return hGap;
	}

	/**
	 * Sets the horizontal spacing between children.
	 *
	 * @param hGap the horizontal spacing
	 */
	public void setHGap(int hGap) {
		this.hGap = hGap;
	}

	/**
	 * Returns the vertical spacing between children.
	 *
	 * @return the vertical spacing
	 */
	public int getVGap() {
		return vGap;
	}

	/**
	 * Sets the vertical spacing between children.
	 *
	 * @param vGap the vertical spacing
	 */
	public void setVGap(int vGap) {
		this.vGap = vGap;
	}

	/**
	 * Returns whether this pane adjust its height automatically
	 * according to the height of its children.
	 *
	 * @return whether the height auto-adjusts
	 */
	public boolean isAutoAdjustHeight() {
		return autoAdjustHeight;
	}

	/**
	 * Sets whether this pane adjust its height automatically
	 * according to the height of its children.
	 *
	 * @param autoAdjustHeight whether the height auto-adjusts
	 */
	public void setAutoAdjustHeight(boolean autoAdjustHeight) {
		this.autoAdjustHeight = autoAdjustHeight;
	}

	@Override
	public void layoutChildren() {
		final int innerWidth = getInnerWidth();
		if (innerWidth <= 0) {
			return;
		}

		final int paddingLeft = getPadding().getLeft();
		final int paddingTop = getPadding().getTop();
		final int paddingRight = getPadding().getRight();
		final int paddingBottom = getPadding().getBottom();

		final int wrapBoundaryX = innerWidth - paddingRight;

		int currentX = paddingLeft;
		int currentY = paddingTop;

		int maxLineHeight = 0;
		boolean isFirstInLine = true;
		boolean hasVisibleChildren = false;

		for (Component child : getChildren()) {
			if (child.isHidden()) {
				continue;
			}
			hasVisibleChildren = true;

			final int childWidth = child.getWidth();
			final int childHeight = child.getHeight();

			if (!isFirstInLine && (currentX + childWidth > wrapBoundaryX)) {
				currentX = paddingLeft;
				currentY += maxLineHeight + vGap;
				maxLineHeight = 0;
			}

			child.setX(currentX);
			child.setY(currentY);

			currentX += childWidth + hGap;
			maxLineHeight = Math.max(maxLineHeight, childHeight);
			isFirstInLine = false;
		}

		if (!autoAdjustHeight) {
			return;
		}

		final int neededInnerHeight = hasVisibleChildren
				? (currentY + maxLineHeight + paddingBottom)
				: (paddingTop + paddingBottom);

		final int neededTotalHeight = neededInnerHeight + (isShowBorders() ? 2 : 0);

		if (getHeight() != neededTotalHeight) {
			setHeight(neededTotalHeight);
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
