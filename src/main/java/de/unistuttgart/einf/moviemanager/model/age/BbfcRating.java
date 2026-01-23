package de.unistuttgart.einf.moviemanager.model.age;

/**
 * The BBFC (British Board of Film Classification) age ratings
 * used in the United Kingdom.
 */
public enum BbfcRating implements AgeRating {

	/**
	 * U - Universal, suitable for all ages.
	 */
	BBFC_U("U", 0),
	/**
	 * PG - Parental Guidance, suitable for ages 6 and above (approximation).
	 */
	BBFC_PG("PG", 6),
	/**
	 * 12 - Suitable for ages 12 and above.
	 */
	BBFC_12("12", 12),
	/**
	 * 15 - Suitable for ages 15 and above.
	 */
	BBFC_15("15", 15),
	/**
	 * 18 - Suitable for ages 18 and above.
	 */
	BBFC_18("18", 18);

	private final String label;
	private final int minimumAge;

	BbfcRating(String label, int minimumAge) {
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
		return RatingSystem.BBFC;
	}

}
