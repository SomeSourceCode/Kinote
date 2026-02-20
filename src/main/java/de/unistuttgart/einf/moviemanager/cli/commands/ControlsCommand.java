package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;

/**
 * The command to show the controls.
 */
public class ControlsCommand {

	private static final String controlsText = """
			Use arrow keys or TAB to navigate, ENTER/SPACE to select.
			You can often use vim-like keybindings, such as j/k, h/l to move, or 'A', 'I', 'O'/'o' etc. inside text areas.
			For command help, use 'help <command>'.
			""";

	/**
	 * Registers the "controls" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("controls")
				.executes(_ -> cli.showInfoDialog(controlsText)));
	}

}
