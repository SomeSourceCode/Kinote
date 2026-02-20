package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.argument.CommandArgument;

import java.util.HashMap;

/**
 * The command to show help for other commands
 */
public class HelpCommand {

	private static final HashMap<String, String> commandToHelpMessage = new HashMap<>();

	static {
		commandToHelpMessage.put("create", """
				This command is used to create new media manually. To create a new movie or series, simply do
				
				:create movie|series <title> [<description>]
				
				If you want to create a season or an episode, you must additionally provide the parent series (and season).
				
				:create season <series> <season-number>
				:create episode <series> <season-number> <episode number> [<description>]
				
				Sometimes the series can be inferred from context, in which case you can omit it (e.g. if it's selected or opened).
				
				To import media from TMDb use the 'import' command.
				""");

		commandToHelpMessage.put("import", """
				Imports media from The Movie Database (TMDb). You can either provide a direct URL or an ID.
				
				1. Using a URL (easiest):
				:import <tmdb-url> [<language>]
				Example: :import https://www.themoviedb.org/movie/603-the-matrix
				
				2. Using an ID:
				:import movie|series <tmdb-id> [<language>]
				Example: :import movie 603
				
				3. Importing Seasons/Episodes:
				:import season [<series>] <season-number> <series-tmdb-id>
				:import episode [<series>] <season-number> <episode-number> <series-tmdb-id>
				
				If you are already inside a series/season, the <series> argument can be omitted.
				The optional <language> argument (e.g. DE, EN) overrides the default import language.
				""");

		commandToHelpMessage.put("smart-fill", """
				Updates existing media with data from TMDb. This is useful if you created an entry manually or want to refresh data.
				
				Usage:
				:smart-fill [<media>] <tmdb-url> [<language>]
				
				Examples:
				:smart-fill https://www.themoviedb.org/movie/603-the-matrix
				(Updates the currently selected or opened media)
				
				:smart-fill "My Movie" https://www.themoviedb.org/movie/603
				(Updates the specific movie "My Movie")
				""");

		commandToHelpMessage.put("filter", """
				Filters the media overview using a powerful query syntax.
				
				Basic Filters:
				:filter watched - Shows only watched items)
				:filter unwatched - Shows only unwatched items)
				:filter movie - Shows only movies)
				:filter series - Shows only series)
				:filter [Action] - Shows items with genre 'Action')
				:filter Matrix - Searches for 'Matrix' in the title)
				
				Comparisons:
				:filter rating >= 8 - Rating is 8 or higher)
				:filter age <= 12 - Age rating is 12 or lower)
				
				Combinations (AND, OR, NOT, Parentheses):
				:filter movie && watched
				:filter (movie && rating >= 8) || (series && unwatched)
				:filter !watched && [Sci-Fi]
				
				To clear the filter:
				:filter reset
				""");
	}

	private static final String MISSING_ENTRY_MESSAGE = """
			This command does not have a help entry.
			""";

	/**
	 * Register the "help" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("help")
				.then(CommandArgument.create("command", dispatcher)
						.executes(context -> {
							final Command command = context.get("command", Command.class);
							cli.showInfoDialog(createHelpMessage(command.getName()));
						})));
	}

	private static String createHelpMessage(String command) {
		final String message = commandToHelpMessage.get(command);
		return message == null ? MISSING_ENTRY_MESSAGE : message;
	}

}
