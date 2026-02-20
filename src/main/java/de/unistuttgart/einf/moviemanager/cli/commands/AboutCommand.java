package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;

/**
 * The command to show information about the application.
 */
public class AboutCommand {

	private static final String aboutText = """
			Kinote (MoVim) Version 1.0.0
			Developed as the final project for the course "Einführung in die Informatik (EInf)" at the University of Stuttgart.
			
			Imports are supported via TMDb (www.themoviedb.org). This application uses the TMDb APIs but is not endorsed certified, or otherwise approved by TMDb.
			""";

	/**
	 * Registers the "about" command on the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("about")
				.executes(_ -> {
					cli.showInfoDialog(aboutText);
				}));
	}

}
