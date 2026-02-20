package de.unistuttgart.einf.moviemanager.cli.api.popover;

import com.googlecode.lanterna.TextColor;
import de.unistuttgart.einf.moviemanager.cli.api.*;

/**
 * A popover that is rendered above the usual scene content.
 *
 * @see Scene#showPopover(Popover)
 */
public class Popover extends Parent {

	private final Component content;
	private PlacementStrategy placementStrategy;

	private Runnable onClosed;

	/**
	 * Constructs a new popover with the given content and placement strategy
	 *
	 * @param content the content
	 * @param placementStrategy the strategy
	 */
	public Popover(Component content, PlacementStrategy placementStrategy) {
		if (content == null) {
			throw new IllegalArgumentException("content must be non-null");
		}

		this.content = content;
		this.placementStrategy = placementStrategy;

		addChild(content);
	}

	@Override
	public void setParent(Parent parent) {
		throw new IllegalStateException("popover may not have a parent. Use scene.showPopover(popover) instead");
	}

	/**
	 * Returns the placement strategy.
	 *
	 * @return the strategy
	 */
	public PlacementStrategy getPlacementStrategy() {
		return placementStrategy;
	}

	/**
	 * Sets the placement strategy.
	 *
	 * @param placementStrategy the strategy
	 */
	public void setPlacementStrategy(PlacementStrategy placementStrategy) {
		this.placementStrategy = placementStrategy;
	}

	@Override
	public void draw(Painter painter) {
		if (isHidden()) {
			return;
		}
		for (int row = 0; row < getInnerHeight(); row++) {
			for (int col = 0; col < getInnerWidth(); col++) {
				painter.drawChar(toGlobalX(col), toGlobalY(row), ' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
			}
		}
		super.draw(painter);
	}

	@Override
	public void layoutChildren() {
		if (getScene() == null || placementStrategy == null) {
			return;
		}

		final int topPadding = getPadding().getTop();
		final int bottomPadding = getPadding().getBottom();
		final int leftPadding = getPadding().getLeft();
		final int rightPadding = getPadding().getRight();

		setWidth(content.getWidth() + leftPadding + rightPadding + (isShowBorders() ? 2 : 0));
		setHeight(content.getHeight() + topPadding + bottomPadding + (isShowBorders() ? 2 : 0));

		final Point2D coords = placementStrategy.calculatePosition(getScene(), this);

		setX(coords.x());
		setY(coords.y());

		content.setX(leftPadding);
		content.setY(topPadding);
	}

	/**
	 * Returns the runnable that is called when the popover is closed.
	 *
	 * @return the runnable
	 */
	public Runnable getOnClosed() {
		return onClosed;
	}

	/**
	 * Sets the runnable that is called when the popover is closed.
	 *
	 * @param onClosed the runnable
	 */
	public void setOnClosed(Runnable onClosed) {
		this.onClosed = onClosed;
	}

	/**
	 * Fires the runnable that is set by {@link #setOnClosed(Runnable)}.
	 */
	public void fireOnClosed() {
		if (onClosed == null) {
			return;
		}
		onClosed.run();
	}

}
