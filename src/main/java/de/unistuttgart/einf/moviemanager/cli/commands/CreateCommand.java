package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.IntegerArgument;
import de.unistuttgart.einf.moviemanager.command.argument.LiteralArgument;
import de.unistuttgart.einf.moviemanager.command.argument.SeriesArgument;
import de.unistuttgart.einf.moviemanager.command.argument.StringArgument;
import de.unistuttgart.einf.moviemanager.model.*;

import java.util.Optional;

/**
 * The command to create new media items.
 */
public class CreateCommand {

	/**
	 * Registers the "create" command on the given dispatcher
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("create")
				.then(LiteralArgument.create("movie")
						.then(StringArgument.create("title")
								.executes(context -> executeCreateMovie(context, cli))
								.then(StringArgument.create("description")
										.executes(context -> executeCreateMovie(context, cli)))))
				.then(LiteralArgument.create("series")
						.then(StringArgument.create("title")
								.executes(context -> executeCreateSeries(context, cli))
								.then(StringArgument.create("description")
										.executes(context -> executeCreateSeries(context, cli)))))
				.then(LiteralArgument.create("season")
						.then(SeriesArgument.create("parent-series", cli.getMediaService())
								.then(IntegerArgument.create("number")
										.withMin(1)
										.executes(context -> executeCreateSeason(context, cli))))
						.then(IntegerArgument.create("number")
								.withMin(1)
								.executes(context -> executeCreateSeason(context, cli))))
				.then(LiteralArgument.create("episode")
						.then(SeriesArgument.create("parent-series", cli.getMediaService())
								.then(IntegerArgument.create("season-number")
										.withMin(1)
										.then(IntegerArgument.create("number")
												.withMin(1)
												.then(StringArgument.create("title")
														.executes(context -> executeCreateEpisode(context, cli))
														.then(StringArgument.create("description")
												.executes(context -> executeCreateEpisode(context, cli)))))))
						.then(IntegerArgument.create("season-number")
								.withMin(1)
								.then(IntegerArgument.create("number")
										.withMin(1)
										.then(StringArgument.create("title")
												.executes(context -> executeCreateEpisode(context, cli))
												.then(StringArgument.create("description")
														.executes(context -> executeCreateEpisode(context, cli))))))));
	}

	private static void executeCreateMovie(ExecutionContext context, Cli cli) {
		final String title = context.getString("title");
		final Optional<String> description = context.getOptional("description", String.class);

		final Movie movie = new Movie(title);
		description.ifPresent(movie::setDescription);

		cli.getMediaService().addMedia(movie);
		cli.getOverviewPage().refreshItems();
		cli.getOverviewPage().moveTo(movie);
		cli.navigateToOverview();
	}

	private static void executeCreateSeries(ExecutionContext context, Cli cli) {
		final String title = context.getString("title");
		final Optional<String> description = context.getOptional("description", String.class);

		final Series series = new Series(title);
		description.ifPresent(series::setDescription);

		cli.getMediaService().addMedia(series);
		cli.getOverviewPage().refreshItems();
		cli.getOverviewPage().moveTo(series);
		cli.navigateToOverview();
	}

	private static Series getParentSeries(ExecutionContext context, Cli cli) {
		Series parentSeries = context.get("parent-series", Series.class);
		if (parentSeries == null) {
			final Media activeMedia = cli.getPage().getActiveMedia();
			if (activeMedia == null) {
				throw Command.fail("Parent series can only be omitted if it's clearly identifiable from the context (e.g. opened)");
			}
			if (!(activeMedia instanceof Series series)) {
				throw Command.fail("Active media" + (activeMedia.getTitle() != null ? " ('" + activeMedia.getTitle() + "') " : " ")
						+ "item is not a series, so parent series must be specified explicitly");
			}
			parentSeries = series;
		}
		return parentSeries;
	}

	private static void executeCreateSeason(ExecutionContext context, Cli cli) {
		final Series parentSeries = getParentSeries(context, cli);

		final int number = context.getInt("number");
		if (parentSeries.hasChild(number)) {
			throw Command.fail("Season " + number + " already exists in series '" + parentSeries.getTitle() + "'");
		}

		final Season season = new Season(number);
		parentSeries.addChild(season);

		cli.getOverviewPage().moveTo(parentSeries);
	}

	private static void executeCreateEpisode(ExecutionContext context, Cli cli) {
		final Series parentSeries = getParentSeries(context, cli);

		final int seasonNumber = context.getInt("season-number");
		final Season season = parentSeries.getChild(seasonNumber);
		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in series '" + parentSeries.getTitle() + "'");
		}

		final int episodeNumber = context.getInt("number");
		if (season.hasChild(episodeNumber)) {
			throw Command.fail("Episode " + episodeNumber + " already exists in season " + seasonNumber + " of series '" + parentSeries.getTitle() + "'");
		}

		final String title = context.getString("title");
		final String description = context.getOptional("description", String.class).orElse("");

		final Episode episode = new Episode(episodeNumber, title);
		episode.setDescription(description);
		season.addChild(episode);

		cli.getOverviewPage().moveTo(parentSeries);
	}

}
