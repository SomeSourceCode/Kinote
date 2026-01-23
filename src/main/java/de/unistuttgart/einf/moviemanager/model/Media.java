package de.unistuttgart.einf.moviemanager.model;

import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

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

	/**
	 * Returns the age rating associated with the given system.
	 * <p>
	 * If no rating has been set for the system, but ratings in other
	 * systems are available, the rating is estimated based on those.
	 * If this is a parent media, its rating is determined by the
	 * highest rated child.
	 *
	 * @param system the rating system
	 * @return the age rating
	 */
	AgeRating getAgeRating(RatingSystem system);

	/**
	 * Returns whether this has any age rating.
	 *
	 * @return true if a rating of any system is set, false otherwise
	 */
	boolean hasAgeRating();

	/**
	 * Returns whether this has an age rating for the given system.
	 *
	 * @param system the rating system
	 * @return true if a rating for the given system is set, false otherwise
	 */
	boolean hasAgeRating(RatingSystem system);

}
