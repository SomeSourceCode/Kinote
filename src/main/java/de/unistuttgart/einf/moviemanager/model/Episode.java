package de.unistuttgart.einf.moviemanager.model;

/**
 * An episode of a season in a series.
 */
public class Episode extends ChildMedia<Season, Episode> {

	private boolean watched;
	private int rating = -1;

	/**
	 * Constructs a new Episode with the given episode number, title, description and watched status.
	 *
	 * @param episodeNumber the episode number (must be at least 1)
	 * @param title the title
	 * @param description the description
	 * @param watched whether the episode has been watched
	 * @param duration the duration
	 * @throws IllegalArgumentException if episodeNumber is less than 1
	 */
	public Episode(int episodeNumber, String title, String description, boolean watched, int duration) {
		if (episodeNumber < 1) {
			throw new IllegalArgumentException("Episode number must be at least 1");
		}
		setNumber(episodeNumber);
		setTitle(title);
		setDescription(description);
		this.watched = watched;
		setDuration(duration);
	}

	/**
	 * Constructs a new Episode with the given episode number, title and description.
	 *
	 * @param episodeNumber the episode number (must be at least 1)
	 * @param title the title
	 * @param description the description
	 * @throws IllegalArgumentException if episodeNumber is less than 1
	 */
	public Episode(int episodeNumber, String title, String description) {
		this(episodeNumber, title, description, false, 0);
	}

	/**
	 * Constructs a new Episode with the given episode number and title.
	 *
	 * @param episodeNumber the episode number (must be at least 1)
	 * @param title the title
	 * @throws IllegalArgumentException if episodeNumber is less than 1
	 */
	public Episode(int episodeNumber, String title) {
		this(episodeNumber, title, null, false, 0);
	}

	/**
	 * Constructs a new Episode with the given episode number.
	 *
	 * @param episodeNumber the episode number (must be at least 1)
	 * @throws IllegalArgumentException if episodeNumber is less than 1
	 */
	public Episode(int episodeNumber) {
		this(episodeNumber, null, null, false,0);
	}

	/**
	 * Returns the season this episode belongs to.
	 *
	 * @return the season
	 */
	public Season getSeason() {
		return getParent();
	}

	/**
	 * Returns the episode number.
	 *
	 * @return the episode number
	 */
	public int getEpisodeNumber() {
		return getNumber();
	}

	/**
	 * Sets the episode number.
	 * This also updates the parent season's episode mapping accordingly.
	 *
	 * @param episodeNumber the episode number
	 */
	public void setEpisodeNumber(int episodeNumber) {
		setNumber(episodeNumber);
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
		return watched ? Status.WATCHED : Status.UNWATCHED;
	}

	@Override
	public int getRating() {
		return rating;
	}

	/**
	 * Sets the rating of the episode.
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
			return "Episode " + getEpisodeNumber() + ": " + title;
		}
		return "Episode " + getEpisodeNumber();
	}

}
