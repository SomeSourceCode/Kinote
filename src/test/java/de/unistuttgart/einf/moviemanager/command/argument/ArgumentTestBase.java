package de.unistuttgart.einf.moviemanager.command.argument;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.CommandTokenizer;
import de.unistuttgart.einf.moviemanager.command.Token;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ArgumentTestBase {

	protected Token createToken(String input) {
		return CommandTokenizer.tokenize(input).getFirst();
	}

	@CanIgnoreReturnValue
	protected CommandParseException assertThrowsParseException(Executable executable, String message) {
		final CommandParseException exception = assertThrows(CommandParseException.class, executable, message);

		assertNotNull(exception.getMessage(), "Exception message should not be null");
		assertFalse(exception.getMessage().isBlank(), "Exception message should not be blank");

		return exception;
	}

	protected void testDefaultSuggestions(Argument<?> argument) {
		assertDefaultSuggestionsAreNotEmpty(argument);
		assertDefaultSuggestionsAreMatching(argument);
		assertDefaultSuggestionsAreValid(argument);
	}

	protected void assertDefaultSuggestionsAreNotEmpty(Argument<?> argument) {
		assertNotNull(argument.getDefaultSuggestions(), "Default suggestions should not be null");
		assertFalse(argument.getDefaultSuggestions().isEmpty(), "Default suggestions should not be empty");
	}

	protected void assertDefaultSuggestionsAreMatching(Argument<?> argument) {
		for (String suggestion : argument.getDefaultSuggestions()) {
			final Token token = createToken(suggestion);
			assertTrue(argument.matches(token), "Default suggestion '" + suggestion + "' should match argument format");
		}
	}

	protected void assertDefaultSuggestionsAreValid(Argument<?> argument) {
		for (String suggestion : argument.getDefaultSuggestions()) {
			final Token token = createToken(suggestion);
			assertTrue(argument.isValid(token), "Default suggestion '" + suggestion + "' should be valid for argument");
		}
	}

}
