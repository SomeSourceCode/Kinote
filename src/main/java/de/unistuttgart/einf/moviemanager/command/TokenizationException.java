package de.unistuttgart.einf.moviemanager.command;

/**
 * Exception thrown when an input cannot be tokenized properly.
 */
public class TokenizationException extends RuntimeException {

	/**
	 * Constructs a new TokenizationException with the specified message.
	 *
	 * @param message the message
	 */
	public TokenizationException(String message) {
		super(message);
	}

}
