package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.argument.LiteralArgument;

/**
 * The command to debug the application.
 */
public class DebugCommand {

	/**
	 * Registers the debug command.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("debug")
				.then(LiteralArgument.create("context")
						.executes(_ -> {
							final String context = """
									Active Media: %s
									Active Movie: %s
									Active Series: %s
									Active Season: %s
									Active Episode: %s
									""";
							cli.showInfoDialog(String.format(
									context,
									cli.getActiveMedia(),
									cli.getActiveMovie(),
									cli.getActiveSeries(),
									cli.getActiveSeason(),
									cli.getActiveEpisode()
							));
						})));
	}

}
