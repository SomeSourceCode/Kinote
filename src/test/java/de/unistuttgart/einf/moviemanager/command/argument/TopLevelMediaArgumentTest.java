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

class TopLevelMediaArgumentTest extends ArgumentTestBase {

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
	@DisplayName("Should correctly match media titles")
	void testMatches() {
		final TopLevelMediaArgument argument = TopLevelMediaArgument.create("media", mediaService)
				.build();

		assertTrue(argument.matches(createToken("Inception")), "Input 'Inception' should match existing media");
		assertTrue(argument.matches(createToken("\"Breaking Bad\"")), "Input '\"Breaking Bad\"' should match existing media");

		assertFalse(argument.matches(createToken("Nonexistent Movie")), "Input 'Nonexistent Movie' should not match any media");
		assertFalse(argument.matches(createToken("Unknown Series")), "Input 'Unknown Series' should not match any media");
	}

	@Test
	@DisplayName("Should correctly validate inputs")
	void testIsValid() {
		final TopLevelMediaArgument argument = TopLevelMediaArgument.create("media", mediaService)
				.build();

		assertTrue(argument.isValid(createToken("Inception")), "Input 'Inception' should be valid");
		assertTrue(argument.isValid(createToken("\"Breaking Bad\"")), "Input '\"Breaking Bad\"' should be valid");

		assertFalse(argument.isValid(createToken("Nonexistent Movie")), "Input 'Nonexistent Movie' should be invalid (not existing)");
		assertFalse(argument.isValid(createToken("Unknown Series")), "Input 'Unknown Series' should be invalid (not existing)");
	}

	@Test
	@DisplayName("Should correctly parse valid media titles")
	void testParseValid() {
		final TopLevelMediaArgument argument = TopLevelMediaArgument.create("media", mediaService)
				.build();

		final TopLevelMedia media1 = argument.parse(createToken("Inception"));
		assertNotNull(media1, "Parsed media should not be null");
		assertEquals("Inception", media1.getTitle(), "Parsed media title should be 'Inception'");

		final TopLevelMedia media2 = argument.parse(createToken("\"Breaking Bad\""));
		assertNotNull(media2, "Parsed media should not be null");
		assertEquals("Breaking Bad", media2.getTitle(), "Parsed media title should be 'Breaking Bad'");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid media titles")
	void testParseInvalidFormat() {
		final TopLevelMediaArgument argument = TopLevelMediaArgument.create("media", mediaService)
				.build();

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
		final TopLevelMediaArgument argument = TopLevelMediaArgument.create("media", mediaService)
				.build();

		testDefaultSuggestions(argument);
	}

}
