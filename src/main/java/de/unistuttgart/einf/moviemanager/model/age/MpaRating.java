package de.unistuttgart.einf.moviemanager.model.age;

/**
 * The MPA (Motion Picture Association) age ratings
 * used in the United States.
 */
public enum MpaRating implements AgeRating {

	/**
	 * G - General Audiences, suitable for all ages.
	 */
	G("G", 0),
	/**
	 * PG - Parental Guidance Suggested, suitable for ages 6 and above (approximation).
	 */
	PG("PG", 6),
	/**
	 * PG-13 - Parents Strongly Cautioned, suitable for ages 13 and above.
	 */
	PG_13("PG-13", 13),
	/**
	 * R - Restricted, suitable for ages 17 and above.
	 */
	R("R", 17),
	/**
	 * NC-17 - No One 17 and Under Admitted, suitable for ages 18 and above.
	 */
	NC_17("NC-17", 18);

	private final String label;
	private final int minimumAge;

	MpaRating(String label, int minimumAge) {
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
		return RatingSystem.MPA;
	}

}
