package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import de.unistuttgart.einf.moviemanager.service.MediaService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SeriesArgumentTest extends ArgumentTestBase {

	private static MediaService mediaService;

	@BeforeAll
	static void setup() {
		final Set<TopLevelMedia> media = Set.of(
				new Series("Breaking Bad"),
				new Series("Stranger Things"),
				new Series("Dark"),
				new Movie("Inception"),
				new Movie("Interstellar")
		);

		mediaService = Mockito.mock(MediaService.class);
		when(mediaService.getAllMedia()).thenReturn(media);
	}

	@Test
	@DisplayName("Should correctly match series titles")
	void testMatches() {
		final SeriesArgument argument = SeriesArgument.create("series", mediaService)
				.build();

		assertTrue(argument.matches(createToken("\"Breaking Bad\"")), "Input '\"Breaking Bad\"' should match existing series");
		assertTrue(argument.matches(createToken("Dark")), "Input 'Dark' should match existing series");

		assertFalse(argument.matches(createToken("Inception")), "Input 'Inception' should not match (series)");
		assertFalse(argument.matches(createToken("Nonexistent Movie")), "Input 'Nonexistent Movie' should not match any movie");
		assertFalse(argument.matches(createToken("Unknown Series")), "Input 'Unknown Series' should not match any movie");
	}

	@Test
	@DisplayName("Should correctly validate inputs")
	void testIsValid() {
		final SeriesArgument argument = SeriesArgument.create("series", mediaService)
				.build();

		assertTrue(argument.isValid(createToken("\"Breaking Bad\"")), "Input '\"Breaking Bad\"' should be valid");
		assertTrue(argument.isValid(createToken("Dark")), "Input 'Dark' should be valid");

		assertFalse(argument.isValid(createToken("\"The Dark Knight\"")), "Input '\"The Dark Knight\"' should be invalid (series)");
		assertFalse(argument.isValid(createToken("Nonexistent Movie")), "Input 'Nonexistent Movie' should be invalid (not existing)");
		assertFalse(argument.isValid(createToken("Unknown Series")), "Input 'Unknown Series' should be invalid (not existing)");
	}

	@Test
	@DisplayName("Should correctly parse valid series titles")
	void testParseValid() {
		final SeriesArgument argument = SeriesArgument.create("series", mediaService)
				.build();

		final Series series1 = argument.parse(createToken("Dark"));
		assertNotNull(series1, "Parsed series should not be null");
		assertEquals("Dark", series1.getTitle(), "Parsed series title should be 'Dark'");

		final Series series2 = argument.parse(createToken("\"Breaking Bad\""));
		assertNotNull(series2, "Parsed series should not be null");
		assertEquals("Breaking Bad", series2.getTitle(), "Parsed series title should be 'Breaking Bad'");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid series titles")
	void testParseInvalidFormat() {
		final SeriesArgument argument = SeriesArgument.create("series", mediaService)
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("\"The Dark Knight\""));
		}, "Input '\"The Dark Knight\"' should throw CommandParseException when parsed (movie)");

		assertThrowsParseException(() -> {
			argument.parse(createToken("Nonexistent Movie"));
		}, "Input 'Nonexistent Movie' should throw CommandParseException when parsed");

		assertThrowsParseException(() -> {
			argument.parse(createToken("Unknown Series"));
		}, "Input 'Unknown Series' should throw CommandParseException when parsed");
	}

	@Test
	@DisplayName("Should provide valid default suggestions")
	void testGetDefaultSuggestions() {
		final SeriesArgument argument = SeriesArgument.create("series", mediaService)
				.build();

		testDefaultSuggestions(argument);
	}

}
