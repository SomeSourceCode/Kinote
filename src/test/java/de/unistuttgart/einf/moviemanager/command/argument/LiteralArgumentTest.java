package de.unistuttgart.einf.moviemanager.command.argument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiteralArgumentTest extends ArgumentTestBase {

	@Test
	@DisplayName("Should correctly match literal")
	void testMatches() {
		final LiteralArgument argument = LiteralArgument.create("literal")
				.build();

		assertTrue(argument.matches(createToken("literal")), "Input 'literal' should match");

		assertFalse(argument.matches(createToken("\"literal\"")), "Input '\"literal\"' should not match (quoted)");
		assertFalse(argument.matches(createToken("other")), "Input 'other' should not match (literal: 'literal')");
	}

	@Test
	@DisplayName("Should correctly validate literal")
	void testIsValid() {
		final LiteralArgument argument = LiteralArgument.create("literal")
				.build();

		assertTrue(argument.isValid(createToken("literal")), "Input 'literal' should be valid");

		assertFalse(argument.isValid(createToken("other")), "Input 'other' should be invalid (literal: 'literal')");
	}

	@Test
	@DisplayName("Should correctly parse valid literal")
	void testParseValid() {
		final LiteralArgument argument = LiteralArgument.create("literal")
				.build();

		assertEquals("literal", argument.parse(createToken("literal")), "Parsed value should be 42");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid input")
	void testParseInvalidFormat() {
		final LiteralArgument argument = LiteralArgument.create("literal")
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("other"));
		}, "Input 'other' should throw CommandParseException when parsed");
	}

	@Test
	@DisplayName("Should provide valid default suggestions")
	void testDefaultSuggestions() {
		final LiteralArgument argument = LiteralArgument.create("literal")
				.build();

		testDefaultSuggestions(argument);
	}

}
