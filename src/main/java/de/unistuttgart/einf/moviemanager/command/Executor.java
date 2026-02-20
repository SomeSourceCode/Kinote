package de.unistuttgart.einf.moviemanager.command;

/**
 * Executor for a command.
 */
@FunctionalInterface
public interface Executor {

	/**
	 * Executes the command with the given context.
	 *
	 * @param context the context
	 */
	void execute(ExecutionContext context);

}
