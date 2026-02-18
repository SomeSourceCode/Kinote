package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;

/**
 * A helper class to create a command dispatcher with all commands registered.
 */
public class Commands {

	/**
	 * A helper to create a command dispatcher with all commands registered.
	 *
	 * @param cli the cli
	 * @return the command dispatcher
	 * @throws IllegalArgumentException if cli is null
	 */
	public static CommandDispatcher createDispatcher(Cli cli) {
		if (cli == null) {
			throw new IllegalArgumentException("cli must be non-null");
		}

		final CommandDispatcher dispatcher = new CommandDispatcher();

		QuitCommand.register(dispatcher, cli);
		HomeCommand.register(dispatcher, cli);
		AboutCommand.register(dispatcher, cli);
		SettingsCommand.register(dispatcher, cli);

		ControlsCommand.register(dispatcher, cli);

		FindCommand.register(dispatcher, cli);
		SearchCommand.register(dispatcher, cli);
		FilterCommand.register(dispatcher, cli);

		CreateCommand.register(dispatcher, cli);
		DeleteCommand.register(dispatcher, cli);

		ImportCommand.register(dispatcher, cli);
		SmartFillCommand.register(dispatcher, cli);

		DebugCommand.register(dispatcher, cli);

		return dispatcher;
	}

}
