package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.StringArgument;

import java.util.Objects;

/**
 * The command to search the overview for media.
 */
public class SearchCommand {

	/**
	 * Registers the "search" command on the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("search")
				.then(StringArgument.create("query")
						.executes(context -> {
							if (Objects.equals(cli.getPage(), cli.getOverviewPage())) {
								executeSearch(context, cli);
								return;
							}
							cli.showConfirmationDialog("Search is only available on the overview page. Do you want to navigate there now?", () -> {
								cli.navigateToOverview();
								executeSearch(context, cli);
							});
						})));
	}

	private static void executeSearch(ExecutionContext context, Cli cli) {
		final String query = context.getString("query");
		cli.getOverviewPage().setSearchQuery(query);
	}

}
