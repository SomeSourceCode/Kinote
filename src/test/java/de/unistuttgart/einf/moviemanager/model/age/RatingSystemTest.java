package de.unistuttgart.einf.moviemanager.model.age;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class RatingSystemTest {

	@ParameterizedTest
	@EnumSource(RatingSystem.class)
	void testEstimationExactMatch(RatingSystem system) {
		for (AgeRating rating : system.getRatings()) {
			final AgeRating estimated = system.getEstimation(rating.getMinimumAge());

			assertEquals(rating, estimated, "System " + system + " failed to map age " + rating.getMinimumAge() + " back to " + rating);
		}
	}

	@ParameterizedTest
	@EnumSource(RatingSystem.class)
	void testNegativeAges(RatingSystem system) {
		final AgeRating rating = system.getEstimation(-5);

		assertNotNull(rating, "System " + system + " returned null for negative age");
		assertEquals(0, rating.getMinimumAge(), "System " + system + " returned rating " + rating + " for negative age, expected minimum age of 0");
	}

	@ParameterizedTest
	@EnumSource(RatingSystem.class)
	void testEstimationMonotonicity(RatingSystem system) {
		AgeRating previous = null;
		for (int age = 0; age <= 30; age++) {
			final AgeRating rating = system.getEstimation(age);

			assertNotNull(rating, "System " + system + " returned null for age " + age);

			if (previous != null) {
				assertTrue(
						rating.getMinimumAge() >= previous.getMinimumAge(),
						"System " + system + " is not monotonic: age " + age + " mapped to " + rating + " which is less than previous rating" + previous + ")"
				);
			}

			previous = rating;
		}
	}

}
