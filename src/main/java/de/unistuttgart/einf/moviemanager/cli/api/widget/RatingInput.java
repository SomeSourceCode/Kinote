package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.cli.api.layout.HBox;

import java.util.function.Consumer;

/**
 * A widget to input a rating between 0 and 10.
 */
public class RatingInput extends Parent {

	private final HBox container;
	private final TextArea inputArea;

	private int rating = -1;

	private Consumer<Integer> onRatingChange;

	/**
	 * Constructs a new rating input with no initial rating.
	 */
	public RatingInput() {
		this(-1);
	}

	/**
	 * Constructs a new rating input.
	 *
	 * @param rating the initial rating, must be between 0 and 100 (or -1 for no rating)
	 * @throws IllegalArgumentException if rating is out of range
	 */
	public RatingInput(int rating) {
		container = new HBox();
		container.setAutoSizeWidth(true);
		container.setHeight(1);

		inputArea = new TextArea(formatValue(rating));
		inputArea.setAutoSizeHeight(false);
		inputArea.setWrapping(false);
		inputArea.setAutoSizeWidth(true);
		inputArea.setHeight(1);

		inputArea.setInputFilter(text -> {
			if (text.isEmpty() || text.equals("10")) {
				return true;
			}
			return text.matches("^[0-9](\\.[0-9]?)?$");
		});

		inputArea.setOnSubmit(text -> {
			if (text.isEmpty()) {
				setRating(-1);
				inputArea.setText(formatValue(this.rating));
				return;
			}
			try {
				final double value = Double.parseDouble(text);
				if (value < 0 || value > 10) {
					inputArea.setText(formatValue(this.rating));
					return;
				}
				setRating((int) Math.round(value * 10));
				inputArea.setText(formatValue(this.rating));
			} catch (NumberFormatException _) {
				inputArea.setText(formatValue(this.rating));
			}
		});

		container.addChild(inputArea);

		final Text suffix = new Text("/10");
		container.addChild(suffix);

		addChild(container);

		setRating(rating);
	}

	/**
	 * Returns the rating.
	 *
	 * @return the rating
	 */
	public int getRating() {
		return rating;
	}

	/**
	 * Sets the rating.
	 *
	 * @param rating the rating, must be between 0 and 100 (or -1 for no rating)
	 * @throws IllegalArgumentException if rating is out of range
	 */
	public void setRating(int rating) {
		if (rating == this.rating) {
			return;
		}
		if (rating < -1 || rating > 100) {
			throw new IllegalArgumentException("rating must be between 0 and 100 (or -1 for no rating)");
		}
		this.rating = rating;

		fireOnRatingChange(rating);
	}

	/**
	 * Returns the consumer that is called when the rating changes.
	 *
	 * @return the consumer
	 */
	public Consumer<Integer> getOnRatingChange() {
		return onRatingChange;
	}

	/**
	 * Sets the consumer that is called when the rating changes.
	 *
	 * @param onRatingChange the consumer
	 */
	public void setOnRatingChange(Consumer<Integer> onRatingChange) {
		this.onRatingChange = onRatingChange;
	}

	/**
	 * Fires the consumer set by {@link #setOnRatingChange(Consumer)}.
	 *
	 * @param rating the rating
	 */
	public void fireOnRatingChange(int rating) {
		if (onRatingChange == null) {
			return;
		}
		onRatingChange.accept(rating);
	}

	private String formatValue(int rating) {
		if (rating < 0) {
			return "";
		}
		if (rating % 10 == 0) {
			return String.valueOf(rating / 10);
		}
		return String.valueOf(rating / 10.0);
	}

	@Override
	protected void layoutChildren() {
		final int paddingTop = getPadding().getTop();
		final int paddingBottom = getPadding().getBottom();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getRight();

		container.setX(paddingLeft);
		container.setY(paddingTop);

		setWidth(container.getWidth() + paddingLeft + paddingRight);
		setHeight(container.getHeight() + paddingTop + paddingBottom);
	}

}
