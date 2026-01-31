package de.unistuttgart.einf.moviemanager;

import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.model.age.FskRating;
import de.unistuttgart.einf.moviemanager.model.age.MpaRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;
import de.unistuttgart.einf.moviemanager.service.MediaFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MediaFilterServiceTest {

	@Test
	public void testIsSeries() {
		MediaFilter filter = MediaFilter.isSeries();
		List<TopLevelMedia> list = List.of(new Movie("movie", "peter"));

		assertFalse(list.stream().anyMatch(filter::test));

		list = List.of(new Series("film", "hallo"));
		assertTrue(list.stream().anyMatch(filter::test));
	}

	@Test
	public void testIsMovie() {
		MediaFilter filter = MediaFilter.isMovie();
		List<TopLevelMedia> list = List.of(new Movie("movie", "peter"));

		assertTrue(list.stream().anyMatch(filter::test));

		list = List.of(new Series("film", "hallo"));
		assertFalse(list.stream().anyMatch(filter::test));

	}

	@Test
	public void testCategory() {
		MediaFilter filter = MediaFilter.isMovie()
				.and(MediaFilter.maxRating(70))
				.and(MediaFilter.isCategory(Category.CRIME))
				.and(MediaFilter.isAgeRatingAtLeast(FskRating.FSK_16))
				.and(MediaFilter.hasStatus(Status.UNWATCHED));
		Movie movie = new Movie(UUID.randomUUID(), "movie", "peter", false, 720, Category.CRIME);
		movie.setRating(70);
		movie.setAgeRating(FskRating.FSK_16);
		List<TopLevelMedia> list = List.of(movie);
		assertTrue(list.stream().anyMatch(filter::test));
	}




}
