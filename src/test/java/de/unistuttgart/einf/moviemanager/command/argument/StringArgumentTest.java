package de.unistuttgart.einf.moviemanager.command.argument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class StringArgumentTest extends ArgumentTestBase {

	@Test
	@DisplayName("Should match any string")
	void testMatches() {
		final StringArgument argument = StringArgument.create("string")
				.build();

		assertTrue(argument.matches(createToken("HelloWorld")), "Input 'Hello World' should match expected format");
		assertTrue(argument.matches(createToken("\"This is a quote\"")), "Input 'Hello World' should match expected format");
		assertTrue(argument.matches(createToken("3.14")), "Input 'Hello World' should match expected format");
	}

	@Test
	@DisplayName("Should validate any string input to be valid")
	void testIsValid() {
		final StringArgument argument = StringArgument.create("string")
				.build();

		assertTrue(argument.isValid(createToken("HelloWorld")), "Input 'Hello World' should be valid");
		assertTrue(argument.isValid(createToken("\"This is a quote\"")), "Input '\"This is a quote\"' should be valid");
		assertTrue(argument.isValid(createToken("3.14")), "Input '3.14' should be valid");
	}

	@Test
	@DisplayName("Should correctly validate pattern")
	void testIsValidWithPattern() {
		final StringArgument argument = StringArgument.create("string")
				.withPattern(Pattern.compile("^[a-zA-Z]+$"))
				.build();

		assertTrue(argument.isValid(createToken("Hello")), "Input 'Hello' should be valid (matches pattern)");
		assertTrue(argument.isValid(createToken("World")), "Input 'World' should be valid (matches pattern)");

		assertFalse(argument.isValid(createToken("Hello123")), "Input 'Hello123' should be invalid (contains digits)");
		assertFalse(argument.isValid(createToken("123")), "Input '123' should be invalid (only digits)");
		assertFalse(argument.isValid(createToken("\"Hello World\"")), "Input 'Hello World' should be invalid (contains space)");
	}

	@Test
	@DisplayName("Should correctly parse valid string inputs")
	void testParseValid() {
		final StringArgument argument = StringArgument.create("string")
				.build();

		assertEquals("Hello World", argument.parse(createToken("\"Hello World\"")), "Parsed value should be 'This is a quote'");
		assertEquals("3.14", argument.parse(createToken("3.14")));
	}

	@Test
	@DisplayName("Should correctly parse integer inputs matching pattern")
	void testParsePatternValid() {
		final StringArgument argument = StringArgument.create("string")
				.withPattern(Pattern.compile("^[a-zA-Z]+$"))
				.build();

		assertEquals("Hello", argument.parse(createToken("Hello")), "Parsed value should be 'Hello'");
		assertEquals("World", argument.parse(createToken("World")), "Parsed value should be 'World'");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid string inputs")
	void testParseInvalidPattern() {
		final StringArgument argument = StringArgument.create("string")
				.withPattern(Pattern.compile("^[a-zA-Z]+$"))
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("Hello123"));
		}, "Input 'Hello123' should throw CommandParseException when parsed (contains digits)");

		assertThrowsParseException(() -> {
			argument.parse(createToken("123"));
		}, "Input '123' should throw CommandParseException when parsed (only digits)");

		assertThrowsParseException(() -> {
			argument.parse(createToken("\"Hello World\""));
		}, "Input 'Hello World' should throw CommandParseException when parsed (contains space)");
	}

}
