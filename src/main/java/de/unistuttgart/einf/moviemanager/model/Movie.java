package de.unistuttgart.einf.moviemanager.model;

/**
 * A movie.
 *
 * @see TopLevelMedia
 */
public final class Movie extends MediaBase implements TopLevelMedia {

	private boolean watched;
	private int rating = -1;

	/**
	 * Constructs a new Movie with the given title, description, watched status, duration and category.
	 *
	 * @param title the title
	 * @param description the description
	 * @param watched whether the movie has been watched
	 * @param duration the duration of the movie
	 * @param category the given category of the movie
	 *
	 */
	public Movie(String title, String description, boolean watched, int duration, Category category ) {
		setTitle(title);
		setDescription(description);
		this.watched = watched;
		setDuration(duration);
		setCategory(category);
	}

	/**
	 * Constructs a new Movie with the given title and description.
	 *
	 * @param title the title
	 * @param description the description
	 */
	public Movie(String title, String description) {
		this(title, description, false, 0, null);
	}

	/**
	 * Constructs a new Movie with the given title.
	 *
	 * @param title the title
	 */
	public Movie(String title) {
		this(title, null, false, 0, null);
	}

	/**
	 * Constructs a new Movie with no title or description.
	 */
	public Movie() {
		this(null, null, false, 0, null);
	}

	@Override
	public void setWatched(boolean watched) {
		this.watched = watched;
	}

	@Override
	public boolean isWatched() {
		return watched;
	}

	@Override
	public Status getStatus() {
		return watched ? Status.UNWATCHED : Status.WATCHED;
	}

	@Override
	public int getRating() {
		return rating;
	}

	/**
	 * Sets the rating of the movie.
	 * It must be between 0 and 100 or -1 if it does not have a rating.
	 *
	 * @param rating an int between 0 and 100 or -1 if it does not have a rating
	 * @throws IllegalArgumentException if the rating is not within -1 to 100.
	 */
	public void setRating(int rating) {
		if (rating < -1 || rating > 100) {
			throw new IllegalArgumentException("The rating must be between -1 and 100.");
		}
		this.rating = rating;
	}

	@Override
	public boolean hasRating() {
		return rating != -1;
	}

	@Override
	public String toString() {
		final String title = getTitle();
		if (title != null && !title.isBlank()) {
			return "Movie: " + title;
		}
		return "Movie";
	}

}
