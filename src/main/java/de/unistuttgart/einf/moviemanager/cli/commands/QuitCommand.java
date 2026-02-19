package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;

/**
 * The command to quit the cli.
 */
public class QuitCommand {

	/**
	 * Registers the "quit" command on the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("quit")
				.executes(context -> cli.stop()));
	}

}
