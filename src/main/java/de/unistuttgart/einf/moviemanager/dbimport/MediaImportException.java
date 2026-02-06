package de.unistuttgart.einf.moviemanager.dbimport;

/**
 * Exception thrown when an error occurs during media imports from TMDb.
 */
public class MediaImportException extends Exception {

	/**
	 * Creates a new MediaImportException with the given error message.
	 *
	 * @param message the error message describing the reason for the exception
	 */
	public MediaImportException(String message) {
		super(message);
	}

	/**
	 * Creates a new MediaImportException with the given error message and cause.
	 *
	 * @param message the error message describing the reason for the exception
	 * @param error the underlying root cause of the exception, which may be null
	 */
	public MediaImportException(String message, Throwable error) {
		super(message, error);
	}

}
