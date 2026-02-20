package de.unistuttgart.einf.moviemanager.command.argument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiLiteralArgumentTest extends ArgumentTestBase {

	@Test
	@DisplayName("Should correctly match options")
	void testMatches() {
		final MultiLiteralArgument argument = MultiLiteralArgument.create("multi-literal")
				.withOptions("option1", "option2", "option3")
				.build();

		assertTrue(argument.matches(createToken("option1")), "Input 'option1' should match (options: 'option1', 'option2', 'option3')");
		assertTrue(argument.matches(createToken("option2")), "Input 'option2' should match (options: 'option1', 'option2', 'option3')");
		assertTrue(argument.matches(createToken("option3")), "Input 'option3' should match (options: 'option1', 'option2', 'option3')");

		assertFalse(argument.matches(createToken("\"option1\"")), "Input '\"option1\"' should not match (quoted)");
		assertFalse(argument.matches(createToken("other")), "Input 'other' should not match (options: 'option1', 'option2', 'option3')");
	}

	@Test
	@DisplayName("Should correctly validate inputs")
	void testIsValid() {
		final MultiLiteralArgument argument = MultiLiteralArgument.create("multi-literal")
				.withOptions("option1", "option2", "option3")
				.build();

		assertTrue(argument.isValid(createToken("option1")), "Input 'option1' should be valid (options: 'option1', 'option2', 'option3')");
		assertTrue(argument.isValid(createToken("option2")), "Input 'option2' should be valid (options: 'option1', 'option2', 'option3')");
		assertTrue(argument.isValid(createToken("option3")), "Input 'option3' should be valid (options: 'option1', 'option2', 'option3')");

		assertFalse(argument.isValid(createToken("other")), "Input 'other' should be invalid (options: 'option1', 'option2', 'option3')");
	}

	@Test
	@DisplayName("Should correctly parse valid options")
	void testParseValid() {
		final MultiLiteralArgument argument = MultiLiteralArgument.create("multi-literal")
				.withOptions("option1", "option2", "option3")
				.build();

		assertEquals("option1", argument.parse(createToken("option1")), "Parsed value should be 'option1'");
		assertEquals("option2", argument.parse(createToken("option2")), "Parsed value should be 'option2'");
		assertEquals("option3", argument.parse(createToken("option3")), "Parsed value should be 'option3'");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid input")
	void testParseInvalidFormat() {
		final MultiLiteralArgument argument = MultiLiteralArgument.create("multi-literal")
				.withOptions("option1", "option2", "option3")
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("other"));
		}, "Input 'other' should throw CommandParseException when parsed");
	}

	@Test
	@DisplayName("Should provide valid default suggestions")
	void testDefaultSuggestions() {
		final MultiLiteralArgument argument = MultiLiteralArgument.create("multi-literal")
				.withOptions("option1", "option2", "option3")
				.build();

		testDefaultSuggestions(argument);
	}

}
