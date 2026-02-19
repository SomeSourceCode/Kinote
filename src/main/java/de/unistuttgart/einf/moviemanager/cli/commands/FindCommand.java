package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.argument.TopLevelMediaArgument;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

/**
 * The command to find media in the UI.
 */
public class FindCommand {

	/**
	 * Registers the "find" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("find")
				.then(TopLevelMediaArgument.create("media", cli.getMediaService())
						.executes(context -> {
							final TopLevelMedia media = context.get("media", TopLevelMedia.class);
							cli.getOverviewPage().moveTo(media);
							cli.navigateToOverview();
						})));
	}

}
