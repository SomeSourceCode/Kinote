package de.unistuttgart.einf.moviemanager.model.age;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class AgeRatingsTest {

	@Test
	void testGetEmpty() {
		final AgeRatings ageRatings = new AgeRatings();

		for (RatingSystem system : RatingSystem.values()) {
			assertNull(ageRatings.get(system), "Expected null for empty ratings in system " + system);
		}
	}

	@Test
	void testGetNullSystem() {
		final AgeRatings ageRatings = new AgeRatings();

		assertNull(ageRatings.get(null), "Expected null for null rating system");
	}

	@Test
	void testGetNullAfterRemove() {
		final AgeRatings ageRatings = new AgeRatings();
		ageRatings.set(BbfcRating.BBFC_18);
		ageRatings.remove(RatingSystem.BBFC);

		assertNull(ageRatings.get(RatingSystem.BBFC), "Expected null after removing rating");
	}

	@Test
	void testGetNotNullAfterSet() {
		final AgeRatings ageRatings = new AgeRatings();
		ageRatings.set(FskRating.FSK_16);

		for (RatingSystem system : RatingSystem.values()) {
			final AgeRating rating = ageRatings.get(system);

			assertNotNull(rating, "Expected non-null rating for system " + system + " after setting FSK_16");
		}
	}

	@ParameterizedTest
	@EnumSource(RatingSystem.class)
	void testGetExactMatch(RatingSystem system) {
		for (AgeRating rating : system.getRatings()) {
			final AgeRatings ageRatings = new AgeRatings();
			ageRatings.set(rating);

			assertEquals(
					rating,
					ageRatings.get(system),
					"Failed to retrieve exact match for " + rating
			);
		}
	}

}
