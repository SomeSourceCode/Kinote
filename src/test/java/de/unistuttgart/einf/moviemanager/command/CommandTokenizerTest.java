package de.unistuttgart.einf.moviemanager.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandTokenizerTest {

	static Stream<Arguments> provideValidTokens() {
		return Stream.of(
				Arguments.of("hello world", new String[]{"hello", "world"}),
				Arguments.of("one   two", new String[]{"one", "two"}),
				Arguments.of("", new String[]{}),

				Arguments.of("user-name_123:key.value", new String[]{"user-name_123:key.value"}),

				Arguments.of("\"hello world\"", new String[]{"hello world"}),
				Arguments.of("key \"value space\"", new String[]{"key", "value space"}),
				Arguments.of("\"multiple\" \"quotes\"", new String[]{"multiple", "quotes"}),

				Arguments.of("\"He said \\\"Hi\\\"\"", new String[]{"He said \"Hi\""}),
				Arguments.of("\"C:\\\\Path\"", new String[]{"C:\\Path"}),
				Arguments.of("\"Backslash \\\"\"", new String[]{"Backslash \""})
		);
	}

	@ParameterizedTest(name = "[{index}] Input: {0}, expected tokens: {1}")
	@MethodSource("provideValidTokens")
	@DisplayName("Should tokenize valid command strings correctly")
	void testTokenizeValid(String input, String[] expectedValues) {
		final List<Token> tokens = CommandTokenizer.tokenize(input);
		assertEquals(expectedValues.length, tokens.size(), "Token count mismatch");

		for (int i = 0; i < expectedValues.length; i++) {
			final String value = tokens.get(i).value();
			assertEquals(expectedValues[i], value, "Token " + i + "(" + value + ")" + "should be '" + expectedValues[i] + "'");
		}
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException for null input")
	void testTokenizeNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			CommandTokenizer.tokenize(null);
		});
	}

	@Test
	@DisplayName("Should return no tokens for empty or blank input")
	void testTokenizeEmptyOrBlank() {
		final List<Token> tokensEmpty = CommandTokenizer.tokenize("");
		assertTrue(tokensEmpty.isEmpty(), "Expected no tokens for empty input");

		final List<Token> tokensBlank = CommandTokenizer.tokenize("    ");
		assertTrue(tokensBlank.isEmpty(), "Expected no tokens for blank input");
	}

	@ParameterizedTest(name = "[{index}] Input: {0}")
	@ValueSource(strings = {
			"command \"unclosed string",
			"another \"test with unclosed quote",
			"\"starts with quote but no end",
			"close \"quotes\" here \"and unclosed",
	})
	@DisplayName("Should throw TokenizationException for unclosed quotes")
	void testTokenizeUnclosedQuotes(String input) {
		assertThrows(TokenizationException.class, () -> {
			CommandTokenizer.tokenize(input);
		}, "Expected TokenizationException for unclosed quotes in input: '" + input + "'");
	}

	@ParameterizedTest(name = "[{index}] Input: {0}")
	@ValueSource(strings = {
			"quote in\"side word",
			"mid\"quote",

			"\"missing\"space after",
			"again no\"space\" but before",
			"quotes\"without\"any space"
	})
	@DisplayName("Should throw TokenizationException for misplaced quotes")
	void testTokenizeMisplacedQuotes(String input) {
		assertThrows(TokenizationException.class, () -> {
			CommandTokenizer.tokenize(input);
		}, "Expected TokenizationException for misplaced quotes in input: '" + input + "'");
	}

	@ParameterizedTest(name = "[{index}] Input: {0}")
	@ValueSource(strings = {
			"invalid$char",
			"price%value",
			"wrong|pipe",
			"hash#tag",
			"user@domain"
	})
	@DisplayName("Should throw TokenizationException for invalid characters")
	void testTokenizeInvalidChar(String input) {
		assertThrows(TokenizationException.class, () -> {
			CommandTokenizer.tokenize(input);
		}, "Expected TokenizationException for invalid character in input: '" + input + "'");
	}

}
