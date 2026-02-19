package de.unistuttgart.einf.moviemanager.command;

/**
 * Exception thrown when a command fails to parse during evaluation.
 */
public class CommandParseException extends RuntimeException {

	/**
	 * Constructs a new CommandParseException with the specified message.
	 *
	 * @param message the message
	 */
	public CommandParseException(String message) {
		super(message);
	}

}
