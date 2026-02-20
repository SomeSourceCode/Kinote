package de.unistuttgart.einf.moviemanager.service.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextNormalizerTest {

	@ParameterizedTest
	@MethodSource("normalizeTestCases")
	@DisplayName("normalize() should clean and normalize search input")
	void normalize_shouldNormalizeText(String input, String expected) {
		String result = TextNormalizer.normalize(input);
		assertEquals(expected, result);
	}

	static Stream<Arguments> normalizeTestCases() {
		return Stream.of(
				org.junit.jupiter.params.provider.Arguments.of(
						"Star Wars: Episode IV – A New Hope",
						"star wars episode iv a new hope"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"Spider-Man: No Way Home!",
						"spider man no way home"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"Fack ju Göhte",
						"fack ju göhte"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"Game of Thrones S02E03",
						"game of thrones s02e03"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"Avengers: Endgame 💥🔥",
						"avengers endgame"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"   The   Lord   of   the   Rings   ",
						"the lord of the rings"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"!!!@@@###",
						""
				),
				org.junit.jupiter.params.provider.Arguments.of(
						"",
						""
				),
				org.junit.jupiter.params.provider.Arguments.of(
						null,
						""
				)
		);
	}

	@Test
	@DisplayName("normalize() should be idempotent")
	void normalize_shouldBeIdempotent() {
		String input = "Star Wars: Episode IV";
		String once = TextNormalizer.normalize(input);
		String twice = TextNormalizer.normalize(once);

		assertEquals(once, twice);
	}
}
