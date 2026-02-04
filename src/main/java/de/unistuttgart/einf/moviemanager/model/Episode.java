package de.unistuttgart.einf.moviemanager.model;

import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.AgeRatings;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.EnumSet;
import java.util.Set;

/**
 * An episode of a season in a series.
 */
public class Episode extends ChildMedia<Season, Episode> implements LeafMedia {

	private boolean watched;
	private int rating = -1;
	private int duration;
	private final AgeRatings ageRatings = new AgeRatings();

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
	public Status getStatus() {
		return watched ? Status.WATCHED : Status.UNWATCHED;
	}

	@Override
	public int getRating() {
		return rating;
	}

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
	public Set<Genre> getGenres() {
		final Media parent = getParent();
		return parent == null ? EnumSet.noneOf(Genre.class) : parent.getGenres();
	}

	@Override
	public boolean hasGenre(Genre genre) {
		final Media parent = getParent();
		return parent != null && parent.hasGenre(genre);
	}

	@Override
	public void setAgeRating(AgeRating rating) {
		ageRatings.set(rating);
	}

	@Override
	public void unsetAgeRating() {
		ageRatings.clear();
	}

	@Override
	public void unsetAgeRating(RatingSystem system) {
		ageRatings.remove(system);
	}

	@Override
	public AgeRating getAgeRating(RatingSystem system) {
		return ageRatings.get(system);
	}

	@Override
	public boolean hasAgeRating() {
		return !ageRatings.isEmpty();
	}

	@Override
	public boolean hasAgeRating(RatingSystem system) {
		return ageRatings.contains(system);
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
