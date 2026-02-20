package de.unistuttgart.einf.moviemanager.model;

import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

/**
 * A media item that does not have any children.
 */
public interface LeafMedia extends Media {

	/**
	 * Sets whether the media has been watched.
	 *
	 * @param watched whether the media has been watched
	 */
	void setWatched(boolean watched);

	/**
	 * Sets the rating of the media. A valid rating is between 0 and 100,
	 * or -1 if it does not have a rating.
	 *
	 * @param rating the rating
	 * @throws IllegalArgumentException if the rating is invalid
	 */
	void setRating(int rating);

	/**
	 * Sets the runtime of the media in minutes. It must be positive or -1 if it does not have a runtime.
	 *
	 * @param runtime the runtime
	 * @throws IllegalArgumentException if the runtime is less than -1
	 */
	void setRuntime(int runtime);

	/**
	 * Sets the given rating. This overrides the current
	 * rating of the current rating system.
	 *
	 * @param rating the rating
	 * @throws IllegalArgumentException if rating is null
	 */
	void setAgeRating(AgeRating rating);

	/**
	 * Unsets the age rating for all rating systems.
	 */
	void unsetAgeRating();

	/**
	 * Unsets the age rating for the given rating system.
	 *
	 * @param system the rating system
	 */
	void unsetAgeRating(RatingSystem system);

}
