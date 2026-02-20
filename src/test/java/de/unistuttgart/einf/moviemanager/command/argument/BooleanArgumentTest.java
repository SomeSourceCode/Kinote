package de.unistuttgart.einf.moviemanager.command.argument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanArgumentTest extends ArgumentTestBase {

	@Test
	@DisplayName("Should correctly match boolean inputs")
	void testMatch() {
		final BooleanArgument argument = BooleanArgument.create("boolean")
				.build();

		assertTrue(argument.matches(createToken("true")), "Input 'true' should match expected format");
		assertTrue(argument.matches(createToken("TRUE")), "Input 'TRUE' should match expected format");
		assertTrue(argument.matches(createToken("tRUe")), "Input 'tRUe' should match expected format");

		assertTrue(argument.matches(createToken("false")), "Input 'false' should match expected format");
		assertTrue(argument.matches(createToken("FALSE")), "Input 'FALSE' should match expected format");
		assertTrue(argument.matches(createToken("FaLSe")), "Input 'FaLSe' should match expected format");

		assertFalse(argument.matches(createToken("\"true\"")), "Input '\"true\"' should not match expected format (quoted)");
		assertFalse(argument.matches(createToken("\"false\"")), "Input '\"false\"' should not match expected format (quoted)");
		assertFalse(argument.matches(createToken("not-a-boolean")), "Input 'not-a-boolean' should not match expected format");
	}

	@Test
	@DisplayName("Should correctly validate boolean inputs")
	void testIsValid() {
		final BooleanArgument argument = BooleanArgument.create("boolean")
				.build();

		assertTrue(argument.isValid(createToken("true")), "Input 'true' should be valid");
		assertTrue(argument.isValid(createToken("TRUE")), "Input 'TRUE' should be valid");
		assertTrue(argument.isValid(createToken("tRUe")), "Input 'tRUe' should be valid");

		assertTrue(argument.isValid(createToken("false")), "Input 'false' should be valid");
		assertTrue(argument.isValid(createToken("FALSE")), "Input 'FALSE' should be valid");
		assertTrue(argument.isValid(createToken("FaLSe")), "Input 'FaLSe' should be valid");

		assertFalse(argument.isValid(createToken("not-a-boolean")), "Input 'not-a-boolean' should be invalid (not a boolean)");
	}

	@Test
	@DisplayName("Should correctly parse valid boolean inputs")
	void testParseValid() {
		final BooleanArgument argument = BooleanArgument.create("boolean")
				.build();

		assertEquals(true, argument.parse(createToken("true")), "Parsed value should be true");
		assertEquals(true, argument.parse(createToken("TRUE")), "Parsed value should be true");
		assertEquals(true, argument.parse(createToken("tRUe")), "Parsed value should be true");

		assertEquals(false, argument.parse(createToken("false")), "Parsed value should be false");
		assertEquals(false, argument.parse(createToken("FALSE")), "Parsed value should be false");
		assertEquals(false, argument.parse(createToken("FaLSe")), "Parsed value should be false");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid boolean inputs")
	void testParseInvalidFormat() {
		final BooleanArgument argument = BooleanArgument.create("boolean")
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("not-a-boolean"));
		}, "Input 'not-a-boolean' should throw CommandParseException when parsed");
	}

	@Test
	@DisplayName("Should provide valid default suggestions")
	void testGetDefaultSuggestions() {
		final BooleanArgument argument = BooleanArgument.create("boolean")
				.build();

		testDefaultSuggestions(argument);
	}

}
