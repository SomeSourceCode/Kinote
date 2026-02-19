package de.unistuttgart.einf.moviemanager.command;

public class CommandExecutionException extends RuntimeException {

	private final String message;

	public CommandExecutionException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
