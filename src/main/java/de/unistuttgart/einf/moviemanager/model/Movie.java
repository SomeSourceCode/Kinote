package de.unistuttgart.einf.moviemanager.model;

import java.util.UUID;

/**
 * A movie.
 *
 * @see TopLevelMedia
 */
public final class Movie extends MediaBase implements TopLevelMedia {

	private boolean watched;
	private final UUID id;

	/**
	 * Constructs a new Movie with the given UUID, title, description and watched status.
	 *
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
	 * Constructs a new Movie with the given title, description and watched status.
	 *
	 * @param title the title
	 * @param description the description
	 * @param watched whether the movie has been watched
	 */
	public Movie(String title, String description, boolean watched) {
		this(null, title, description, watched);
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

	public UUID getId() {
		return this.id;

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
	public String toString() {
		final String title = getTitle();
		if (title != null && !title.isBlank()) {
			return "Movie: " + title;
		}
		return "Movie";
	}

}
