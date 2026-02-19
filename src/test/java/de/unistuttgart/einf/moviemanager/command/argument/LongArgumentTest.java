package de.unistuttgart.einf.moviemanager.command.argument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongArgumentTest extends ArgumentTestBase {

	@Test
	@DisplayName("Should correctly match long inputs")
	void testMatches() {
		final LongArgument argument = LongArgument.create("long")
				.build();

		assertTrue(argument.matches(createToken("42")), "Input '42' should match expected format");
		assertTrue(argument.matches(createToken("-7")),"Input '-7' should match expected format");

		assertFalse(argument.matches(createToken("3.14")), "Input '3.14' should not match expected format (not an long)");
		assertFalse(argument.matches(createToken("not-a-number")), "Input 'not-a-number' should not match expected format (not an long)");
		assertFalse(argument.matches(createToken("\"42\"")), "Input '\"42\"' should not match expected format (quoted)");
	}

	@Test
	@DisplayName("Should correctly validate inputs")
	void testIsValid() {
		final LongArgument argument = LongArgument.create("long")
				.build();

		assertTrue(argument.isValid(createToken("99")), "Input '99' should be valid");
		assertTrue(argument.isValid(createToken("-7")),"Input '-7' should be valid");

		assertFalse(argument.isValid(createToken("2.72")), "Input '2.72' should be invalid (not an long)");
		assertFalse(argument.isValid(createToken("hello")), "Input 'hello' should be invalid");
	}

	@Test
	@DisplayName("Should correctly validate bounds")
	void testIsValidWithBounds() {
		final LongArgument argument = LongArgument.create("bounded-long")
				.withBounds(10, 100)
				.build();

		assertTrue(argument.isValid(createToken("50")), "Input '50' should be valid (within bounds)");
		assertTrue(argument.isValid(createToken("10")), "Input '10' should be valid (min: 10)");
		assertTrue(argument.isValid(createToken("100")), "Input '100' should be valid (max: 100)");

		assertFalse(argument.isValid(createToken("-5")), "Input '-5' should be invalid (min: 10)");
		assertFalse(argument.isValid(createToken("9")), "Input '9' should be invalid (min: 10)");
		assertFalse(argument.isValid(createToken("101")), "Input '101' should be invalid (max: 100)");
		assertFalse(argument.isValid(createToken("150")), "Input '150' should be invalid (max: 100)");
	}

	@Test
	@DisplayName("Should correctly parse valid long inputs")
	void testParseValid() {
		final LongArgument argument = LongArgument.create("long")
				.build();

		assertEquals(42, argument.parse(createToken("42")), "Parsed value should be 42");
		assertEquals(-15, argument.parse(createToken("-15")), "Parsed value should be -15");
	}

	@Test
	@DisplayName("Should throw exception when parsing invalid long inputs")
	void testParseInvalidFormat() {
		final LongArgument argument = LongArgument.create("long")
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("3.14"));
		}, "Input '3.14' should throw CommandParseException when parsed");
		assertThrowsParseException(() -> {
			argument.parse(createToken("not-a-number"));
		}, "Input 'not-a-number' should throw CommandParseException when parsed");
	}

	@Test
	@DisplayName("Should correctly parse long inputs within bounds")
	void testParseInBounds() {
		final LongArgument argument = LongArgument.create("long")
				.withBounds(-35, 42)
				.build();

		assertEquals(-35, argument.parse(createToken("-35")), "Parsed value should be -35");
		assertEquals(-10, argument.parse(createToken("-10")), "Parsed value should be -10");
		assertEquals(42, argument.parse(createToken("42")), "Parsed value should be 42");
	}

	@Test
	@DisplayName("Should throw exception when parsing long inputs out of bounds")
	void testParseOutOfBounds() {
		final LongArgument argument = LongArgument.create("long")
				.withBounds(0, 100)
				.build();

		assertThrowsParseException(() -> {
			argument.parse(createToken("-50"));
		}, "Input '-50' should throw CommandParseException (min: 0)");
		assertThrowsParseException(() -> {
			argument.parse(createToken("-1"));
		}, "Input '-1' should throw CommandParseException (min: 0)");
		assertThrowsParseException(() -> {
			argument.parse(createToken("101"));
		}, "Input '101' should throw CommandParseException (max: 100)");
		assertThrowsParseException(() -> {
			argument.parse(createToken("150"));
		}, "Input '150' should throw CommandParseException (max: 100)");
	}

}
