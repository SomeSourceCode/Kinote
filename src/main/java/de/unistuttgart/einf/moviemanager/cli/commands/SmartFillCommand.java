package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.util.TextUtils;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.*;
import de.unistuttgart.einf.moviemanager.dbimport.*;
import de.unistuttgart.einf.moviemanager.model.*;

/**
 * The command to complete existing media with data from TMDb.
 */
public class SmartFillCommand {

	/**
	 * Registers the "smart-fill" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("smart-fill")
				// smart-fill <tmdb-url> [<language>]
				.then(Arguments.tmdbUrl("tmdb-url")
						.executes(context -> executeImportUrl(context, cli))
						.then(EnumArgument.create("language", Language.class)
								.executes(context -> executeImportUrl(context, cli))))
				// smart-fill <movie-or-series> <tmdb-url> [<language>]
				.then(TopLevelMediaArgument.create("movie-or-series", cli.getMediaService())
						.then(Arguments.tmdbUrl("tmdb-url")
								.executes(context -> executeImportUrl(context, cli))
								.then(EnumArgument.create("language", Language.class)
										.executes(context -> executeImportUrl(context, cli))))));
	}

	private static String getApiKey(Cli cli) {
		return cli.getSettingsService().getSettings().getTmdbApiKey();
	}

	private static Language getLanguage(ExecutionContext context, Cli cli) {
		return context.getOptional("language", Language.class)
				.orElseGet(() -> cli.getSettingsService().getSettings().getImportLanguage());
	}

	private static void executeImportUrl(ExecutionContext context, Cli cli) {
		final String url = context.getString("tmdb-url");
		if (!UrlParser.isValid(url)) {
			throw Command.fail("Invalid TMDb URL: " + TextUtils.abbreviate(url, 20)
					+ ". Copy the URL from the address bar of the TMDb page.");
		}

		final MediaType type = UrlParser.getMediaType(url);
		final Language language = getLanguage(context, cli);

		if (type == null) {
			throw Command.fail("Could not determine media type from URL.");
		}

		switch (type) {
			case MOVIE -> {
				final TopLevelMedia topLevelMedia = context.has("movie-or-series")
						? context.get("movie-or-series", TopLevelMedia.class)
						: cli.getActiveMovie();
				if (topLevelMedia == null) {
					throw Command.fail("Target movie can only be omitted if it's clear from context (e.g. opened)");
				}

				if (!(topLevelMedia instanceof Movie movie)) {
					throw Command.fail("Unexpected media url. Excepted movie.");
				}
				performSmartFillMovie(cli, movie, UrlParser.getMovieTmdbId(url), language);
			}
			case SERIES -> {
				final TopLevelMedia topLevelMedia = context.has("movie-or-series")
						? context.get("movie-or-series", TopLevelMedia.class)
						: cli.getActiveSeries();
				if (topLevelMedia == null) {
					throw Command.fail("Target series can only be omitted if it's clear from context (e.g. opened)");
				}

				if (!(topLevelMedia instanceof Series series)) {
					throw Command.fail("Unexpected media url. Excepted series.");
				}
				performSmartFillSeries(cli, series, UrlParser.getSeriesTmdbId(url), language);
			}
			case SEASON -> {
				final TopLevelMedia topLevelMedia = context.has("movie-or-series")
						? context.get("movie-or-series", TopLevelMedia.class)
						: cli.getActiveSeries();
				if (topLevelMedia == null) {
					throw Command.fail("Target series can only be omitted if it's clear from context (e.g. opened)");
				}

				if (!(topLevelMedia instanceof Series series)) {
					throw Command.fail("Movies do not have seasons. Please provide a series as target.");
				}
				performSmartFillSeason(cli, series, UrlParser.getSeasonNumber(url), UrlParser.getSeriesTmdbId(url), language);
			}
			case EPISODE -> {
				final TopLevelMedia topLevelMedia = context.has("movie-or-series")
						? context.get("movie-or-series", TopLevelMedia.class)
						: cli.getActiveSeries();
				if (topLevelMedia == null) {
					throw Command.fail("Target series can only be omitted if it's clear from context (e.g. opened)");
				}

				if (!(topLevelMedia instanceof Series series)) {
					throw Command.fail("Movies do not have episodes. Please provide a series as target.");
				}
				performSmartFillEpisode(cli, series, UrlParser.getSeasonNumber(url), UrlParser.getEpisodeNumber(url), UrlParser.getSeriesTmdbId(url), language);
			}
		}
	}

	private static void performSmartFillMovie(Cli cli, Movie movie, int tmdbId, Language language) {
		final MovieImporter movieImporter = createMovieImporter(getApiKey(cli));
		movieImporter.setLanguage(language);

		try {
			movieImporter.importData(movie, tmdbId);
		} catch (MediaImportException e) {
			throw Command.fail("Failed to fill movie '" + movie.getTitle() + "' from TMDb (id " + tmdbId + "). Is it the correct ID? Is it a movie? Do you have a stable internet connection?");
		}

		cli.refresh();
	}

	private static void performSmartFillSeries(Cli cli, Series series, int tmdbId, Language language) {
		final SeriesImporter seriesImporter = createSeriesImporter(getApiKey(cli));
		seriesImporter.setLanguage(language);

		try {
			seriesImporter.importData(series, tmdbId);
		} catch (MediaImportException e) {
			throw Command.fail("Failed to fill series '" + series.getTitle() + "' from TMDb (id " + tmdbId + "). Is it the correct ID? Is it a series? Do you have a stable internet connection?");
		}

		cli.refresh();
	}

	private static void performSmartFillSeason(Cli cli, Series series, int seasonNumber, int tmdbId, Language language) {
		final Season season = series.getChild(seasonNumber);
		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in series '" + series.getTitle() + "'.");
		}

		final SeasonImporter seasonImporter = createSeasonImporter(getApiKey(cli));
		seasonImporter.setLanguage(language);

		try {
			seasonImporter.importData(season, tmdbId, seasonNumber);
		} catch (MediaImportException e) {
			throw Command.fail("Failed to fill season " + seasonNumber + " of series '" + series.getTitle() + "' from TMDb (id " + tmdbId + "). Is it the correct ID? Do you have a stable internet connection?");
		}

		cli.refresh();
	}

	private static void performSmartFillEpisode(Cli cli, Series series, int seasonNumber, int episodeNumber, int tmdbId, Language language) {
		final Season season = series.getChild(seasonNumber);
		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in series '" + series.getTitle() + "'.");
		}

		final Episode episode = season.getChild(episodeNumber);
		if (episode == null) {
			throw Command.fail("Episode " + episodeNumber + " of season " + seasonNumber + " does not exist in series '" + series.getTitle() + "'.");
		}

		final EpisodeImporter episodeImporter = createEpisodeImporter(getApiKey(cli));
		episodeImporter.setLanguage(language);

		try {
			episodeImporter.importData(episode, tmdbId, seasonNumber, episodeNumber);
		} catch (MediaImportException e) {
			throw Command.fail("Failed to fill episode " + episodeNumber + " of season " + seasonNumber + " of series '" + series.getTitle() + "' from TMDb (id " + tmdbId + "). Is it the correct ID? Do you have a stable internet connection?");
		}

		cli.refresh();
	}

	private static MovieImporter createMovieImporter(String apiKey) {
		final MovieImporter movieImporter = new MovieImporter(apiKey);
		movieImporter.include(ImportableAttribute.values());
		return movieImporter;
	}

	private static SeriesImporter createSeriesImporter(String apiKey) {
		final SeriesImporter seriesImporter = new SeriesImporter(apiKey);
		seriesImporter.include(ImportableAttribute.values());
		seriesImporter.setSeasonImporter(createSeasonImporter(apiKey));
		return seriesImporter;
	}

	private static SeasonImporter createSeasonImporter(String apiKey) {
		final SeasonImporter seasonImporter = new SeasonImporter(apiKey);
		seasonImporter.include(ImportableAttribute.values());
		seasonImporter.setEpisodeImporter(createEpisodeImporter(apiKey));
		return seasonImporter;
	}

	private static EpisodeImporter createEpisodeImporter(String apiKey) {
		final EpisodeImporter episodeImporter = new EpisodeImporter(apiKey);
		episodeImporter.include(ImportableAttribute.values());
		return episodeImporter;
	}

}
