package de.unistuttgart.einf.moviemanager.cli.api.popover;

import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Point2D;
import de.unistuttgart.einf.moviemanager.cli.api.Scene;

/**
 * Places the popover relative to an anchor component.
 */
public class AnchoredPlacement implements PlacementStrategy {

	/**
	 * Defines the side of the anchor where the popover is placed.
	 */
	public enum Side {

		/**
		 * Places the popover above the anchor.
		 */
		TOP,
		/**
		 * Places the popover below the anchor.
		 */
		BOTTOM,
		/**
		 * Places the popover to the left of the anchor.
		 */
		LEFT,
		/**
		 * Places the popover to the right of the anchor.
		 */
		RIGHT

	}

	/**
	 * Defines how the popover aligns with the anchor along the chosen {@link Side Side}.
	 */
	public enum Alignment {

		/**
		 * Aligns the starting edge of the popover with the starting edge of the anchor.
		 * <ul>
		 *     <li>For {@link Side#TOP TOP}/{@link Side#BOTTOM BOTTOM}: Aligns the left edges.</li>
		 *     <li>For {@link Side#LEFT LEFT}/{@link Side#RIGHT RIGHT}: Aligns the top edges.</li>
		 * </ul>
		 */
		START,

		/**
		 * Centers the popover relative to the anchor.
		 */
		CENTER,

		/**
		 * Aligns the ending edge of the popover with the ending edge of the anchor.
		 * <ul>
		 *     <li>For {@link Side#TOP TOP}/{@link Side#BOTTOM BOTTOM}: Aligns the right edges.</li>
		 *     <li>For {@link Side#LEFT LEFT}/{@link Side#RIGHT RIGHT}: Aligns the bottom edges.</li>
		 * </ul>
		 */
		END

	}

	private final Component anchor;

	private Side side;
	private Alignment alignment;

	private int offsetX;
	private int offsetY;

	/**
	 * Constructs a new anchored placement with the given anchor,
	 * anchored on the bottom and aligned at the start.
	 *
	 * @param anchor the anchor
	 * @throws IllegalArgumentException if anchor is null
	 */
	public AnchoredPlacement(Component anchor) {
		this(anchor, Side.BOTTOM, Alignment.START);
	}

	/**
	 * Constructs a new anchored placement with the given anchor,
	 * side and alignment.
	 *
	 * @param anchor the anchor
	 * @param side the side
	 * @param alignment the alignment
	 * @throws IllegalArgumentException if anchor is null
	 */
	public AnchoredPlacement(Component anchor, Side side, Alignment alignment) {
		if (anchor == null) {
			throw new IllegalArgumentException("anchor must be non-null");
		}
		this.anchor = anchor;
		setSide(side);
		setAlignment(alignment);
	}

	/**
	 * Returns the side the popover will be placed relative to.
	 *
	 * @return the side
	 */
	public Side getSide() {
		return side;
	}

	/**
	 * Sets the side the popover will be placed relative to.
	 *
	 * @param side the side
	 * @throws IllegalArgumentException if side is null
	 */
	public void setSide(Side side) {
		if (side == null) {
			throw new IllegalArgumentException("side must be non-null");
		}
		this.side = side;
	}

	/**
	 * Returns the alignment used to position the popover on a side.
	 *
	 * @return the alignment
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * Sets the alignment used to position the popover on a side.
	 *
	 * @param alignment the alignment
	 */
	public void setAlignment(Alignment alignment) {
		if (alignment == null) {
			throw new IllegalArgumentException("alignment must be non-null");
		}
		this.alignment = alignment;
	}

	/**
	 * Returns the x offset that is added to the calculated position.
	 *
	 * @return the x offset
	 */
	public int getOffsetX() {
		return offsetX;
	}

	/**
	 * Sets the x offset that is added to the calculated position.
	 *
	 * @param offsetX the x offset
	 */
	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	/**
	 * Returns the y offset that is added to the calculated position.
	 *
	 * @return the y offset
	 */
	public int getOffsetY() {
		return offsetY;
	}

	/**
	 * Sets the y offset that is added to the calculated position.
	 *
	 * @param offsetY the x offset
	 */
	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	@Override
	public Point2D calculatePosition(Scene scene, Popover popover) {
		if (scene.getRoot() == null) {
			return Point2D.ORIGIN;
		}

		final int anchorX = anchor.toGlobalX(0);
		final int anchorY = anchor.toGlobalY(0);
		final int anchorWidth = anchor.getWidth();
		final int anchorHeight = anchor.getHeight();

		final int popoverWidth = popover.getWidth();
		final int popoverHeight = popover.getHeight();

		final int sceneRootWidth = scene.getRoot().getWidth();
		final int sceneRootHeight = scene.getRoot().getHeight();

		final Side finalSide = resolveSide(
				anchorX, anchorY, anchorWidth, anchorHeight,
				popoverWidth, popoverHeight,
				sceneRootWidth, sceneRootHeight
		);

		int finalX = anchorX;
		int finalY = anchorY;

		switch (finalSide) {
			case BOTTOM -> {
				finalX = align(anchorX, anchorWidth, popoverWidth) + offsetX;
				finalY = anchorY + anchorHeight + offsetY;
			}
			case TOP -> {
				finalX = align(anchorX, anchorWidth, popoverWidth) + offsetX;
				finalY = anchorY - popoverHeight - offsetY;
			}
			case RIGHT -> {
				finalX = anchorX + anchorWidth + offsetX;
				finalY = align(anchorY, anchorHeight, popoverHeight) + offsetY;
			}
			case LEFT -> {
				finalX = anchorX - popoverWidth - offsetX;
				finalY = align(anchorY, anchorHeight, popoverHeight) + offsetY;
			}
		}

		return new Point2D(finalX, finalY);
	}

	private Side resolveSide(
			int anchorX, int anchorY, int anchorWidth, int anchorHeight,
			int popoverWidth, int popoverHeight,
			int sceneWidth, int sceneHeight
	) {
		return switch (this.side) {
			case BOTTOM -> {
				final boolean fitsBottom = (anchorY + anchorHeight + popoverHeight + offsetY) <= sceneHeight;
				final int spaceAbove = anchorY;
				final int spaceBelow = sceneHeight - (anchorY + anchorHeight);

				if (!fitsBottom && spaceAbove > spaceBelow) {
					yield Side.TOP;
				}
				yield Side.BOTTOM;
			}
			case TOP -> {
				final boolean fitsTop = (anchorY - popoverHeight - offsetY) >= 0;
				final int spaceAbove = anchorY;
				final int spaceBelow = sceneHeight - (anchorY + anchorHeight);

				if (!fitsTop && spaceBelow > spaceAbove) {
					yield Side.BOTTOM;
				}
				yield Side.TOP;
			}
			case RIGHT -> {
				final boolean fitsRight = (anchorX + anchorWidth + popoverWidth + offsetX) <= sceneWidth;
				final int spaceLeft = anchorX;
				final int spaceRight = sceneWidth - (anchorX + anchorWidth);

				if (!fitsRight && spaceLeft > spaceRight) {
					yield Side.LEFT;
				}
				yield Side.RIGHT;
			}
			case LEFT -> {
				final boolean fitsLeft = (anchorX - popoverWidth - offsetX) >= 0;
				final int spaceLeft = anchorX;
				final int spaceRight = sceneWidth - (anchorX + anchorWidth);

				if (!fitsLeft && spaceRight > spaceLeft) {
					yield Side.RIGHT;
				}
				yield Side.LEFT;
			}
		};
	}

	private int align(int anchorPosition, int anchorSize, int popoverSize) {
		return switch (this.alignment) {
			case START -> anchorPosition;
			case CENTER -> anchorPosition + (anchorSize - popoverSize) / 2;
			case END -> anchorPosition + anchorSize - popoverSize;
		};
	}

}
