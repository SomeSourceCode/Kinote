package de.unistuttgart.einf.moviemanager.cli.api;

/**
 * Represents the distance from the edges of a container to its content.
 */
public class Insets {

	/**
	 * Empty insets (0 on all sides)
	 */
	public static final Insets NONE = new Insets(0);

	final private int top;
	final private int right;
	final private int bottom;
	final private int left;

	/**
	 * Constructs insets with the given offsets.
	 *
	 * @param top the top inset
	 * @param right the right inset
	 * @param bottom the bottom inset
	 * @param left the left inset
	 */
	public Insets(int top, int right, int bottom, int left) {
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.left = left;
	}

	/**
	 * Constructs insets with the given offsets.
	 *
	 * @param topBottom the top and bottom insets
	 * @param leftRight the left and right insets
	 */
	public Insets(int topBottom, int leftRight) {
		this.top = topBottom;
		this.right = leftRight;
		this.bottom = topBottom;
		this.left = leftRight;
	}

	/**
	 * Constructs insets with the given offsets.
	 *
	 * @param allSides the insets for all sides
	 */
	public Insets(int allSides) {
		this.top = allSides;
		this.right = allSides;
		this.bottom = allSides;
		this.left = allSides;
	}

	/**
	 * Returns the top inset.
	 *
	 * @return the top inset
	 */
	public int getTop() {
		return top;
	}

	/**
	 * Returns the right inset.
	 *
	 * @return the right inset
	 */
	public int getRight() {
		return right;
	}

	/**
	 * Returns the bottom inset.
	 *
	 * @return the bottom inset
	 */
	public int getBottom() {
		return bottom;
	}

	/**
	 * Returns the left inset.
	 *
	 * @return the left inset
	 */
	public int getLeft() {
		return left;
	}

}
