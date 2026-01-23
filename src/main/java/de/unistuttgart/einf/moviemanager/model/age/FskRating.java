package de.unistuttgart.einf.moviemanager.model.age;

/**
 * The FSK (Freiwillige Selbstkontrolle der Filmwirtschaft) age ratings
 * used in Germany.
 */
public enum FskRating implements AgeRating {

	/**
	 * FSK 0 - Suitable for all ages.
	 */
	FSK_0("FSK 0", 0),
	/**
	 * FSK 6 - Suitable for ages 6 and above.
	 */
	FSK_6("FSK 6", 6),
	/**
	 * FSK 12 - Suitable for ages 12 and above.
	 */
	FSK_12("FSK 12", 12),
	/**
	 * FSK 16 - Suitable for ages 16 and above.
	 */
	FSK_16("FSK 16", 16),
	/**
	 * FSK 18 - Suitable for ages 18 and above.
	 */
	FSK_18("FSK 18", 18);

	private final String label;
	private final int minimumAge;

	FskRating(String label, int minimumAge) {
		this.label = label;
		this.minimumAge = minimumAge;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int getMinimumAge() {
		return minimumAge;
	}

	@Override
	public RatingSystem getSystem() {
		return RatingSystem.FSK;
	}

}
