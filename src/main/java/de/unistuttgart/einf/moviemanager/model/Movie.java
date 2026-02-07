package de.unistuttgart.einf.moviemanager.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.AgeRatings;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

/**
 * A movie.
 *
 * @see TopLevelMedia
 */
public final class Movie extends MediaBase implements TopLevelMedia, LeafMedia {

	private final UUID id;

	private boolean watched;
	private int rating = -1;
	private int runtime = -1;
	private final Set<Genre> genres = EnumSet.noneOf(Genre.class);
	private final AgeRatings ageRatings = new AgeRatings();

	/**
	 * Constructs a new Movie with the given id.
	 *
	 * @param id the uuid
	 */
	public Movie(UUID id) {
		this.id = id != null ? id : UUID.randomUUID();
	}

	/**
	 * Constructs a new Movie with the given id, title, description and watched status.
	 *
	 * @param id the uuid
	 * @param title the title
	 * @param description the description
	 * @param watched whether the movie has been watched
	 */
	public Movie(UUID id, String title, String description, boolean watched) {
		setTitle(title);
		setDescription(description);
		this.watched = watched;

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
		this(null, title, description, false);
	}

	/**
	 * Constructs a new Movie with the given title.
	 *
	 * @param title the title
	 */
	public Movie(String title) {
		this(null, title, null, false);
	}

	/**
	 * Constructs a new Movie with no title or description.
	 */
	public Movie() {
		this(null, null, null, false);
	}

	@Override
	public UUID getId() {
		return this.id;
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
	public int getRuntime() {
		return runtime;
	}

	@Override
	public boolean hasRuntime() {
		return runtime != -1;
	}

	@Override
	public void setRuntime(int runtime) {
		if (runtime < -1) {
			throw new IllegalArgumentException("The runtime can not be less than -1");
		}
		this.runtime = runtime;
	}

	@Override
	public Set<Genre> getGenres() {
		return EnumSet.copyOf(genres);
	}

	@Override
	public boolean hasGenre(Genre genre) {
		return genres.contains(genre);
	}

	@Override
	public void addGenre(Genre genre) {
		genres.add(genre);
	}

	@Override
	public void removeGenre(Genre genre) {
		genres.remove(genre);
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
			return "Movie: " + title;
		}
		return "Movie";
	}

}
