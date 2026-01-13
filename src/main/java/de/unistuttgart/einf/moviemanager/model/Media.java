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
	 * Sets whether the media has been watched.
	 * If this is a parent media (like {@link Series} or {@link Season}), this method
	 * sets the watched status for all child media as well.
	 *
	 * @param watched whether the media has been watched
	 */
	void setWatched(boolean watched);

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
	 * Sets the duration.
	 * 
	 * @param duration the duration
	 */
	void setDuration(int duration);

	/**
	 * Returns the duration
	 * 
	 * @return the duration
	*/
	int getDuration();

	/**
	 * Sets the category
	 * 
	 * @param Category the category
	*/
	void setCategory(Category category);

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
}
