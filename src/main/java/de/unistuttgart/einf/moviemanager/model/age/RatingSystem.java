package de.unistuttgart.einf.moviemanager.model.age;

/**
 * The rating systems.
 */
public enum RatingSystem {

	/**
	 * British Board of Film Classification, used in the United Kingdom.
	 */
	BBFC(BbfcRating.values()),
	/**
	 * Freiwillige Selbstkontrolle der Filmwirtschaft, used in Germany.
	 */
	FSK(FskRating.values()),
	/**
	 * Motion Picture Association of America, used in the United States.
	 */
	MPA(MpaRating.values());

	private final AgeRating[] ratings;

	RatingSystem(AgeRating[] ratings) {
		this.ratings = ratings;
	}

	/**
	 * Returns the available ratings in this system.
	 *
	 * @return the available ratings
	 */
	public AgeRating[] getRatings() {
		return ratings;
	}

	/**
	 * Returns the rating with the given name.
	 *
	 * @param name the name
	 * @return the rating, or null if no rating with the given name exists
	 */
	public AgeRating getRating(String name) {
		if (name == null) {
			return null;
		}
		for (AgeRating rating : ratings) {
			if (!(rating instanceof Enum<?> ratingAsEnum)) {
				continue;
			}
			if (ratingAsEnum.name().equals(name)) {
				return rating;
			}
		}
		return null;
	}

	/**
	 * Returns an estimated rating for the given age.
	 *
	 * @param age the age
	 * @return the estimation
	 * @see #getEstimation(AgeRating)
	 */
	public AgeRating getEstimation(int age) {
		// Assuming enum is ordered
		if (values().length == 0) {
			return null;
		}
		AgeRating previousRating = null;
		for (AgeRating rating : ratings) {
			if (rating.getMinimumAge() >= age) {
				if (rating.getMinimumAge() == age) {
					return rating;
				}
				if (previousRating == null) {
					return rating;
				}
				final int toPreviousRating = age - previousRating.getMinimumAge();
				if (toPreviousRating > 1) {
					return rating;
				}
				final int toRating = rating.getMinimumAge() - age;
				return toPreviousRating <= toRating ? previousRating : rating;
			}
			previousRating = rating;
		}
		return previousRating;
	}

	/**
	 * Returns an estimated rating for the given age rating.
	 * If the rating is already from this system, it is returned as is.
	 *
	 * @param ageRating the age rating
	 * @return the estimation
	 * @see #getEstimation(int)
	 */
	public AgeRating getEstimation(AgeRating ageRating) {
		if (ageRating.getSystem() == this) {
			return ageRating;
		}
		return getEstimation(ageRating.getMinimumAge());
	}

	public static RatingSystem fromName(String name) {
		if (name == null) {
			return null;
		}
		for (RatingSystem system : values()) {
			if (system.name().equals(name)) {
				return system;
			}
		}
		return null;
	}

}
