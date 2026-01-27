package de.unistuttgart.einf.moviemanager.command;

/**
 * A token parsed from a command input.
 *
 * @param value the value of the token
 * @param raw the raw representation of the token as in the input
 * @param isQuoted whether the token was quoted
 * @param startIndex the start index of the token in the input (inclusive)
 * @param endIndex the end index of the token in the input (exclusive)
 */
public record Token(String value, String raw, boolean isQuoted, int startIndex, int endIndex) {

	@Override
	public String toString() {
		return "Token{value='" + value + "', quoted=" + isQuoted + ", position=" + startIndex + "-" + endIndex + "}";
	}

}
