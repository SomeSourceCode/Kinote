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

class MovieArgumentTest extends ArgumentTestBase {

	private static MediaService mediaService;

	@BeforeAll
	static void setup() {
		final Set<TopLevelMedia> media = Set.of(
				new Movie("Interstellar"),
				new Movie("Inception"),
				new Movie("The Dark Knight"),
				new Series("Breaking Bad"),
				new Series("Stranger Things")
		);

		mediaService = Mockito.mock(MediaService.class);
		when(mediaService.getAllMedia()).thenReturn(media);
	}

	@Test
	@DisplayName("Should correctly match movie titles")
	void testMatches() {
		final MovieArgument argument = MovieArgument.create("movie", mediaService)
				.build();

		assertTrue(argument.matches(createToken("Inception")), "Input 'Inception' should match existing movie");
		assertTrue(argument.matches(createToken("\"The Dark Knight\"")), "Input '\"The Dark Knight\"' should match existing movie");

		assertFalse(argument.matches(createToken("\"Breaking Bad\"")), "Input '\"Breaking Bad\"' should not match (series)");
		assertFalse(argument.matches(createToken("Nonexistent Movie")), "Input 'Nonexistent Movie' should not match any movie");
		assertFalse(argument.matches(createToken("Unknown Series")), "Input 'Unknown Series' should not match any movie");
	}

	@Test
	@DisplayName("Should correctly validate inputs")
	void testIsValid() {
		final MovieArgument argument = MovieArgument.create("movie", mediaService)
				.build();

		assertTrue(argument.isValid(createToken("Inception")), "Input 'Inception' should be valid");
		assertTrue(argument.isValid(createToken("\"The Dark Knight\"")), "Input '\"The Dark Knight\"' should be valid");

		assertFalse(argument.isValid(createToken("\"Breaking Bad\"")), "Input '\"Breaking Bad\"' should be invalid (series)");
		assertFalse(argument.isValid(createToken("Nonexistent Movie")), "Input 'Nonexistent Movie' should be invalid (not existing)");
		assertFalse(argument.isValid(createToken("Unknown Series")), "Input 'Unknown Series' should be invalid (not existing)");
	}

	@Test
	@DisplayName("Should correctly parse valid movie titles")
	void testParseValid() {
		final MovieArgument argument = MovieArgument.create("movie", mediaService)
				.build();

		final Movie movie1 = argument.parse(createToken("Inception"));
		assertNotNull(movie1, "Parsed movie should not be null");
		assertEquals("Inception", movie1.getTitle(), "Parsed movie title should be 'Inception'");

		final Movie movie2 = argument.parse(createToken("\"The Dark Knight\""));
		assertNotNull(movie2, "Parsed movie should not be null");
		assertEquals("The Dark Knight", movie2.getTitle(), "Parsed movie title should be 'The Dark Knight'");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid movie titles")
	void testParseInvalidFormat() {
		final MovieArgument argument = MovieArgument.create("movie", mediaService)
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("\"Breaking Bad\""));
		}, "Input '\"Breaking Bad\"' should throw CommandParseException when parsed (series)");

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
		final MovieArgument argument = MovieArgument.create("movie", mediaService)
				.build();

		testDefaultSuggestions(argument);
	}

}
