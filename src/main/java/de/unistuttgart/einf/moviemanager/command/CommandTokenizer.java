package de.unistuttgart.einf.moviemanager.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tokenizer for command input strings.
 */
public class CommandTokenizer {

	private static final Pattern VALID_UNQUOTED_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-.:]+$");

	/**
	 * Returns a list of tokens parsed from the given input string,
	 * respecting quoted strings and escape sequences.
	 *
	 * @param input the input
	 * @return the tokens
	 */
	public static List<Token> tokenize(String input) {
		if (input == null) {
			throw new IllegalArgumentException("input must be non-null");
		}
		final List<Token> tokens = new ArrayList<>();
		final int length = input.length();
		int inputPointer = 0;

		while (inputPointer < length) {
			final char currentChar = input.charAt(inputPointer);

			if (currentChar == ' ') {
				inputPointer++;
				continue;
			}

			if (currentChar == '"') {
				inputPointer = parseQuotedToken(input, inputPointer, tokens);
			} else {
				inputPointer = parseUnquotedToken(input, inputPointer, tokens);
			}
		}

		return tokens;
	}

	private static int parseQuotedToken(String input, int startIndex, List<Token> tokens) {
		final StringBuilder builder = new StringBuilder();
		boolean escaped = false;

		int inputPointer = startIndex + 1;
		boolean closed = false;

		while (inputPointer < input.length()) {
			char currentChar = input.charAt(inputPointer);

			if (escaped) {
				builder.append(currentChar);
				escaped = false;
				inputPointer++;
				continue;
			}

			if (currentChar == '\\') {
				escaped = true;
				inputPointer++;
				continue;
			}

			if (currentChar == '"') {
				closed = true;
				inputPointer++;
				break;
			}

			builder.append(currentChar);
			inputPointer++;
		}

		if (!closed) {
			throw new TokenizationException("Syntax error: Unclosed quotation mark starting at index " + startIndex);
		}

		if (inputPointer < input.length() && input.charAt(inputPointer) != ' ') {
			throw new TokenizationException("Syntax error: No space between quoted string and following text at index " + inputPointer);
		}

		final String value = builder.toString();
		final String raw = input.substring(startIndex, inputPointer);
		tokens.add(new Token(value, raw, true, startIndex, inputPointer));
		return inputPointer;
	}

	private static int parseUnquotedToken(String input, int startIndex, List<Token> tokens) {
		final StringBuilder builder = new StringBuilder();
		int inputPointer = startIndex;

		while (inputPointer < input.length()) {
			char currentChar = input.charAt(inputPointer);
			if (currentChar == ' ') {
				break;
			}

			if (currentChar == '"') {
				throw new TokenizationException("Syntax error: Unexpected quote in the middle of an unquoted token at index " + inputPointer);
			}

			builder.append(currentChar);
			inputPointer++;
		}

		final String value = builder.toString();

		if (!VALID_UNQUOTED_PATTERN.matcher(value).matches()) {
			throw new TokenizationException("Syntax error: Invalid character in unquoted token: '" + value + "'");
		}

		tokens.add(new Token(value, value, false, startIndex, inputPointer));
		return inputPointer;
	}

}
