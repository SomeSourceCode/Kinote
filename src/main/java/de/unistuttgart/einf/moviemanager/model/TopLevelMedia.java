package de.unistuttgart.einf.moviemanager.model;

import java.util.UUID;

/**
 * A top-level media item.
 *
 * @see Movie
 * @see Series
 */
public sealed interface TopLevelMedia extends Media permits Movie, Series {

	/**
	 * Adds the genre to the media.
	 *
	 * @param genre the genre
	 */
	void addGenre(Genre genre);

	/**
	 * Removes the genre from the media.
	 *
	 * @param genre the genre
	 */
	void removeGenre(Genre genre);

	/**
	 * Returns the UUID of the media
	 *
	 * @return the uuid
	 */
	UUID getId();

}
