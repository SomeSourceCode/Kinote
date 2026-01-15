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
	 * Sets the category of the media.
	 *
	 * @param category the category
	 */
	void setCategory(Category category);

	/**
	 * Returns the UUID of the media
	 *
	 * @return the uuid
	 */
	UUID getId();
}
