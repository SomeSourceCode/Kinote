package de.unistuttgart.einf.moviemanager.model.age;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper for multiple age ratings from different systems.
 */
public class AgeRatings {

	private final Map<RatingSystem, AgeRating> systemToRating = new HashMap<>();

	/**
	 * Sets the age rating for the given rating system.
	 *
	 * @param rating the rating
	 */
	public void set(AgeRating rating) {
		systemToRating.put(rating.getSystem(), rating);
	}

	/**
	 * Returns the age rating for the given rating system.
	 * If no rating is set for the system, an estimation based
	 * on the available ratings is returned.
	 *
	 * @param system the system
	 * @return the age rating, and estimation or null, if no ratings are available
	 */
	public AgeRating get(RatingSystem system) {
		if (isEmpty()) {
			return null;
		}
		final AgeRating rating = systemToRating.get(system);
		if (rating != null) {
			return rating;
		}
		final int averageMinAge = (int) systemToRating.values().stream()
				.mapToInt(AgeRating::getMinimumAge)
				.average()
				.orElse(0);
		return system.getEstimation(averageMinAge);
	}

	/**
	 * Returns whether a rating for the given system is set.
	 *
	 * @param system the system
	 * @return whether a rating is set
	 */
	public boolean contains(RatingSystem system) {
		return systemToRating.containsKey(system);
	}

	/**
	 * Removes the age rating for the given rating system.
	 *
	 * @param system the system
	 */
	public void remove(RatingSystem system) {
		systemToRating.remove(system);
	}

	/**
	 * Clears all age ratings.
	 */
	public void clear() {
		systemToRating.clear();
	}

	/**
	 * Returns whether no age ratings are set.
	 *
	 * @return true if no age ratings are set, false otherwise
	 */
	public boolean isEmpty() {
		return systemToRating.isEmpty();
	}

}
