package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.IntegerArgument;
import de.unistuttgart.einf.moviemanager.command.argument.MovieArgument;
import de.unistuttgart.einf.moviemanager.command.argument.SeriesArgument;
import de.unistuttgart.einf.moviemanager.model.*;

/**
 * The command to delete media items.
 */
public class DeleteCommand {

	/**
	 * Registers the "delete" command on the given dispatcher
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("delete")
				.executes(_ -> executeDeleteActiveMedia(cli))
				.then(MovieArgument.create("movie", cli.getMediaService())
						.executes(context -> executeDeleteMovie(context, cli)))
				.then(SeriesArgument.create("series", cli.getMediaService())
						.executes(context -> executeDeleteSeries(context, cli))
						.then(IntegerArgument.create("season-number")
								.withMin(1)
								.executes(context -> executeDeleteSeason(context, cli))
								.then(IntegerArgument.create("episode-number")
										.withMin(1)
										.executes(context -> executeDeleteEpisode(context, cli)))))
				.then(IntegerArgument.create("season-number")
						.withMin(1)
						.executes(context -> executeDeleteSeason(context, cli))
						.then(IntegerArgument.create("episode-number")
								.withMin(1)
								.executes(context -> executeDeleteEpisode(context, cli)))));
	}

	private static void executeDeleteActiveMedia(Cli cli) {
		final Media media = cli.getActiveMedia();
		switch (media) {
			case null -> {
				throw Command.fail("Delete only works without arguments if the subject is clear from the context.");
			}
			case TopLevelMedia asTopLevelMedia -> confirmAndExecute(
					cli,
					"You are about to delete '" + asTopLevelMedia.getTitle() + "'. Are you sure?",
					() -> {
						cli.getMediaService().removeMedia(asTopLevelMedia);
						cli.navigateBack();
						cli.getOverviewPage().refreshItems();
					}
			);
			case Season season -> {
				if (season.getParent() == null) {
					return;
				}
				confirmAndExecute(
						cli,
						"You are about to delete Season " + season.getNumber() + " of " + season.getParent().getTitle() + ". Are you sure?",
						() -> {
							season.getParent().removeChild(season.getNumber());
							cli.navigateBack();
						}
				);
			}
			case Episode episode -> {
				if (episode.getParent() == null) {
					return;
				}
				confirmAndExecute(
						cli,
						"You are about to delete 'Episode " + episode.getNumber() + ": " + episode.getTitle() + "'. Are you sure?",
						() -> {
							episode.getParent().removeChild(episode.getNumber());
							cli.navigateBack();
						}
				);
			}
			default -> {}
		}

	}

	private static void executeDeleteMovie(ExecutionContext context, Cli cli) {
		final Movie media = context.get("movie", Movie.class);
		confirmAndExecute(cli, "You are about to delete '" + media.getTitle() + "'. Are you sure?", () -> {
			cli.getMediaService().removeMedia(media);
			cli.navigateBack();
			cli.getOverviewPage().refreshItems();
		});
	}

	private static void executeDeleteSeries(ExecutionContext context, Cli cli) {
		final Series media = context.get("series", Series.class);
		confirmAndExecute(cli, "You are about to delete '" + media.getTitle() + "'. Are you sure?", () -> {
			cli.getMediaService().removeMedia(media);
			cli.navigateBack();
			cli.getOverviewPage().refreshItems();
		});
	}

	private static void executeDeleteSeason(ExecutionContext context, Cli cli) {
		final Series media = resolveSeries(context, cli);
		final int seasonNumber = context.getInt("season-number");
		final Season season = media.getChild(seasonNumber);

		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in '" + media.getTitle() + "'.");
		}

		confirmAndExecute(cli, "You are about to delete Season " + seasonNumber + " of '" + media.getTitle() + "'. Are you sure?", () -> {
			media.removeChild(seasonNumber);
			cli.navigateBack();
		});
	}

	private static void executeDeleteEpisode(ExecutionContext context, Cli cli) {
		final Series media = resolveSeries(context, cli);
		final int seasonNumber = context.getInt("season-number");
		final Season season = media.getChild(seasonNumber);

		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in '" + media.getTitle() + "'.");
		}

		final int episodeNumber = context.getInt("episode-number");
		final Episode episode = season.getChild(episodeNumber);

		if (episode == null) {
			throw Command.fail("Episode " + episodeNumber + " does not exist in Season " + seasonNumber + " of '" + media.getTitle() + "'.");
		}

		confirmAndExecute(cli, "You are about to delete Episode " + episodeNumber + " of Season " + seasonNumber + " of '" + media.getTitle() + "'. Are you sure?", () -> {
			season.removeChild(episodeNumber);
			cli.navigateBack();
		});
	}

	private static Series resolveSeries(ExecutionContext context, Cli cli) {
		final Series series = context.get("series", Series.class);
		if (series != null) {
			return series;
		}

		final Series activeMedia = cli.getActiveSeries();

		if (activeMedia == null) {
			throw Command.fail("No series specified and the subject is not clear from the context.");
		}

		return activeMedia;
	}

	private static void confirmAndExecute(Cli cli, String message, Runnable action) {
		cli.showConfirmationDialog(message, action);
	}

}
