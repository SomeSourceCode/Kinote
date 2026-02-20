package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;

/**
 * The command to refresh data manually.
 */
public class RefreshCommand {

	/**
	 * Registers the "refresh" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("refresh")
				.executes(context -> {
					cli.refresh();
					cli.getOverviewPage().resetSearchQuery();
					cli.getOverviewPage().resetFilter();
				}));
	}

}
