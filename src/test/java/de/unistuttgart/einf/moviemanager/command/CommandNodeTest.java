package de.unistuttgart.einf.moviemanager.command;

import de.unistuttgart.einf.moviemanager.command.argument.BooleanArgument;
import de.unistuttgart.einf.moviemanager.command.argument.LiteralArgument;
import de.unistuttgart.einf.moviemanager.command.argument.MultiLiteralArgument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandNodeTest {

	@Test
	@DisplayName("Should throw IllegalArgumentException when adding duplicate literals")
	void testThenThrowsOnDuplicateLiterals() {
		final var builder = Command.create("root")
				.then(LiteralArgument.create("literal"));

		assertThrows(IllegalArgumentException.class, () -> {
			builder.then(LiteralArgument.create("literal"));
		}, "Expected IllegalArgumentException due to duplicate literal");
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when adding overlapping multi-literals")
	void testThenThrowsOnOverlappingMultiLiterals() {
		final var builder = Command.create("root")
				.then(MultiLiteralArgument.create("multi-literal")
						.withOptions("option1", "option2"));

		assertThrows(IllegalArgumentException.class, () -> {
			builder.then(MultiLiteralArgument.create("multi-literal-2")
					.withOptions("option2", "option3"));
		}, "Expected IllegalArgumentException due to overlapping options");
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when adding multi-literal clashing with existing literal")
	void testThenThrowsOnMultiLiteralClashingWithLiteral() {
		final var builder = Command.create("root")
				.then(LiteralArgument.create("literal"));

		assertThrows(IllegalArgumentException.class, () -> {
			builder.then(MultiLiteralArgument.create("multi-literal")
					.withOptions("literal", "option2"));
		}, "Expected IllegalArgumentException due to clashing literal");
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when adding literal clashing with existing multi-literal")
	void testThenThrowsOnLiteralClashingWithMultiLiteral() {
		final var builder = Command.create("root")
				.then(MultiLiteralArgument.create("multi-literal")
						.withOptions("option1", "option2"));

		assertThrows(IllegalArgumentException.class, () -> {
			builder.then(LiteralArgument.create("option1"));
		}, "Expected IllegalArgumentException due to clashing literal");
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when adding child resulting path with duplicate child names")
	void testThenThrowsOnDuplicateChildNames() {
		final var builder = Command.create("root");

		assertThrows(IllegalArgumentException.class, () -> {
			builder.then(MultiLiteralArgument.create("child")
					.then(LiteralArgument.create("child")));
		}, "Expected IllegalArgumentException due to duplicate child names");
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when adding arguments with clashing types")
	void testThenThrowsOnClashingArgumentTypes() {
		final var builder = Command.create("root")
				.then(BooleanArgument.create("boolean"));

		assertThrows(IllegalArgumentException.class, () -> {
			builder.then(BooleanArgument.create("another-boolean"));
		}, "Expected IllegalArgumentException due to clashing argument types");
	}

}
