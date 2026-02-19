package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.LiteralArgument;
import de.unistuttgart.einf.moviemanager.command.argument.MediaFilterArgument;
import de.unistuttgart.einf.moviemanager.service.MediaFilter;

/**
 * The command to filter the overview.
 */
public class FilterCommand {

	/**
	 * Register the "filter" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("filter")
				.then(MediaFilterArgument.create("query", cli.getSettingsService())
						.executes(context -> executeFilter(context, cli)))
				.then(LiteralArgument.create("reset")
						.executes(context -> executeFilter(context, cli))));
	}

	private static void executeFilter(ExecutionContext context, Cli cli) {
		final MediaFilter filter = context.getOptional("query", MediaFilter.class).orElse(null);
		if (cli.getPage() == cli.getOverviewPage()) {
			cli.getOverviewPage().setFilter(filter);
			return;
		}
		cli.showConfirmationDialog("Filters are only available on the overview page. Do you want to navigate there now?", () -> {
			cli.navigateToOverview();
			cli.getOverviewPage().setFilter(filter);
		});
	}

}
