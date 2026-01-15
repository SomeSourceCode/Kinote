package de.unistuttgart.einf.moviemanager.model;

import java.util.UUID;

/**
 * A movie.
 *
 * @see TopLevelMedia
 */
public final class Movie extends MediaBase implements TopLevelMedia, LeafMedia {

	private final UUID id;

	private boolean watched;
	private int rating = -1;
	private int duration;
	private Category category;
	
	/**
	 * Constructs a new Movie with the given title, description, watched status, duration and category.
	 * 
	 * @param id the uuid
	 * @param title the title
	 * @param description the description
	 * @param watched whether the movie has been watched
	 * @param duration the duration of the movie
	 * @param category the given category of the movie
	 *
	 */
	public Movie(UUID id, String title, String description, boolean watched, int duration, Category category) {
		setTitle(title);
		setDescription(description);
		this.watched = watched;
		setDuration(duration);
		setCategory(category);

		if (id == null)
			this.id = UUID.randomUUID();
		else
			this.id = id;
	}


	/**
	 * Constructs a new Movie with the given title and description.
	 *
	 * @param title the title
	 * @param description the description
	 */
	public Movie(String title, String description) {
		this(null, title, description, false, 0, null);
	}

	/**
	 * Constructs a new Movie with the given title.
	 *
	 * @param title the title
	 */
	public Movie(String title) {
		this(null, title, null, false, 0, null);
	}

	/**
	 * Constructs a new Movie with no title or description.
	 */
	public Movie() {
		this(null, null, null, false, 0, null);
	}

	public UUID getId() {
		return this.id;
	}

	@Override
	public void setWatched(boolean watched) {
		this.watched = watched;
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
	@Override
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
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public void setCategory(Category category) {
		this.category = category;
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
