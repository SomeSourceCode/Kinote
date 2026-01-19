package de.unistuttgart.einf.moviemanager.model;

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
	 * Sets the duration of the media in minutes.
	 *
	 * @param duration the duration
	 */
	void setDuration(int duration);

	/**
	 * Sets the ageRestriction of the media. A valid restriction is between 0 and 18,
	 * or -1 if it does not have a restriction.
	 *
	 * @param ageRestriction the age restriction
	 * @throws IllegalArgumentException if the restriction is invalid
	 */
	void setAgeRestriction(int ageRestriction);

}
