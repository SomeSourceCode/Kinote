package de.unistuttgart.einf.moviemanager.model;

/**
 * The base for all media classes.
 */
public interface Media {

	/**
	 * Returns the title.
	 *
	 * @return the title
	 */
	String getTitle();

	/**
	 * Sets the title.
	 *
	 * @param title the title
	 */
	void setTitle(String title);

	/**
	 * Returns the description.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Sets the description.
	 *
	 * @param description the description
	 */
	void setDescription(String description);

	/**
	 * Returns whether the media has been watched.
	 * If this is a parent media (like {@link Series} or {@link Season}), this method
	 * returns true only if all child media have been watched.
	 *
	 * @return whether the media has been watched
	 */
	boolean isWatched();

	/**
	 * Returns the status of the media.
	 * The status is derived from the watched status.
	 *
	 * @return the status
	 */
	Status getStatus();

	/**
	 * Returns the duration
	 * 
	 * @return the duration
	*/
	int getDuration();

	/**
	 * Returns the Category
	 * 
	 * @return the Category 
	 */
	Category getCategory();

	/**
	 * Returns the rating between 0 and 100 or -1 if it does not have a rating.
	 * If this is not a leaf node, the rating is the average rating of its children.
	 *
	 * @return the rating
	 */
	int getRating();

	/**
	 * Returns whether this has a rating.
	 *
	 * @return true if it has a rating, false otherwise
	 */
	boolean hasRating();

}
