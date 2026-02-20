package de.unistuttgart.einf.moviemanager.model.age;

import java.util.List;

/**
 * A specific age rating from a rating system.
 */
public sealed interface AgeRating permits BbfcRating, FskRating, MpaRating {

	/**
	 * Returns the string representation of this age rating.
	 *
	 * @return the label
	 */
	String getLabel();

	/**
	 * Returns the minimum age for this age rating.
	 *
	 * @return the minimum age
	 */
	int getMinimumAge();

	/**
	 * Returns the system this rating is a part of.
	 *
	 * @return the rating system
	 */
	RatingSystem getSystem();

	/**
	 * Compares two age ratings by their minimum age. If the minimum ages are equal,
	 * the rating systems' ordinals are compared.
	 *
	 * @param a the first age rating
	 * @param b the second age rating
	 * @return a negative integer, zero, or a positive integer if the first argument is
	 * less than, equal to, or greater than the second, respectively
	 */
	static int compare(AgeRating a, AgeRating b) {
		final int comparedMinAge = Integer.compare(
				a.getMinimumAge(),
				b.getMinimumAge()
		);
		if (comparedMinAge != 0) {
			return comparedMinAge;
		}
		return Integer.compare(
				a.getSystem().ordinal(),
				b.getSystem().ordinal()
		);
	}

	/**
	 * Returns the maximum age rating from the given list,
	 * according to {@link #compare(AgeRating, AgeRating)}.
	 *
	 * @param ratings the list of age ratings
	 * @return the maximum age rating, or null if the list is empty
	 * or contains only nulls
	 */
	static AgeRating max(List<AgeRating> ratings) {
		return ratings.stream()
				.filter(rating -> rating != null)
				.max(AgeRating::compare)
				.orElse(null);
	}

}
