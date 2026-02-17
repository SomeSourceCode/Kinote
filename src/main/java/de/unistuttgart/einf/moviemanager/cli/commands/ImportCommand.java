package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.util.TextUtils;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.*;
import de.unistuttgart.einf.moviemanager.dbimport.*;
import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.Series;

import java.util.regex.Pattern;

/**
 * The command to import media from TMDb.
 */
public class ImportCommand {

	/**
	 * Registers the "import" command on the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("import")
				// import <tmdb-url> [<language>]
				.then(StringArgument.create("tmdb-url")
						.withPattern(Pattern.compile("(https?://)?(www\\.)?themoviedb\\.org/((movie)|(tv))/\\d+-.*"))
						.executes(context -> executeImportUrl(context, cli))
						.then(EnumArgument.create("language", Language.class)
								.executes(context -> executeImportUrl(context, cli))))
				// import movie <tmdb-id> [<language>]
				.then(LiteralArgument.create("movie")
						.then(IntegerArgument.create("tmdb-id")
								.withMin(0)
								.executes(context -> executeImportMovie(context, cli))
								.then(EnumArgument.create("language", Language.class)
										.executes(context -> executeImportMovie(context, cli)))))
				// import series <tmdb-id> [<language>]
				.then(LiteralArgument.create("series")
						.then(IntegerArgument.create("tmdb-id")
								.withMin(0)
								.executes(context -> executeImportSeries(context, cli))
								.then(EnumArgument.create("language", Language.class)
										.executes(context -> executeImportSeries(context, cli)))))
				// import season [<series>] <season-number> <series-tmdb-id> [<language>]
				.then(LiteralArgument.create("season")
						// season <series> <season-number> <series-tmdb-id> [<language>]
						.then(SeriesArgument.create("series", cli.getMediaService())
								.then(IntegerArgument.create("season-number")
										.withMin(0)
										.then(IntegerArgument.create("series-tmdb-id")
												.withMin(0)
												.executes(context -> executeImportSeason(context, cli))
												.then(EnumArgument.create("language", Language.class)
														.executes(context -> executeImportSeason(context, cli))))))
						// season <season-number> <series-tmdb-id> [<language>]
						.then(IntegerArgument.create("season-number").withMin(0)
								.then(IntegerArgument.create("series-tmdb-id").withMin(0)
										.executes(context -> executeImportSeason(context, cli))
										.then(EnumArgument.create("language", Language.class)
												.executes(context -> executeImportSeason(context, cli))))))

				// import episode [<series>] <season-number> <episode-number> <series-tmdb-id> [<language>]
				.then(LiteralArgument.create("episode")
						// episode <series> <season-number> <episode-number> <series-tmdb-id> [<language>]
						.then(SeriesArgument.create("series", cli.getMediaService())
								.then(IntegerArgument.create("season-number").withMin(0)
										.then(IntegerArgument.create("episode-number").withMin(1)
												.then(IntegerArgument.create("series-tmdb-id").withMin(0)
														.executes(context -> executeImportEpisode(context, cli))
														.then(EnumArgument.create("language", Language.class)
																.executes(context -> executeImportEpisode(context, cli)))))))
						// episode <season-number> <episode-number> <series-tmdb-id> [<language>]
						.then(IntegerArgument.create("season-number").withMin(0)
								.then(IntegerArgument.create("episode-number").withMin(1)
										.then(IntegerArgument.create("series-tmdb-id").withMin(0)
												.executes(context -> executeImportEpisode(context, cli))
												.then(EnumArgument.create("language", Language.class)
														.executes(context -> executeImportEpisode(context, cli))))))));
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
			case MOVIE -> performImportMovie(cli, UrlParser.getMovieTmdbId(url), language);
			case SERIES -> performImportSeries(cli, UrlParser.getSeriesTmdbId(url), language);
			case SEASON -> {
				final Series series = resolveTargetSeries(context, cli);
				performImportSeason(cli, series, UrlParser.getSeasonNumber(url), UrlParser.getSeriesTmdbId(url), language);
			}
			case EPISODE -> {
				final Series series = resolveTargetSeries(context, cli);
				performImportEpisode(cli, series, UrlParser.getSeasonNumber(url), UrlParser.getEpisodeNumber(url), UrlParser.getSeriesTmdbId(url), language);
			}
		}
	}

	private static void executeImportMovie(ExecutionContext context, Cli cli) {
		performImportMovie(cli, context.getInt("tmdb-id"), getLanguage(context, cli));
	}

	private static void executeImportSeries(ExecutionContext context, Cli cli) {
		performImportSeries(cli, context.getInt("tmdb-id"), getLanguage(context, cli));
	}

	private static void executeImportSeason(ExecutionContext context, Cli cli) {
		final Series series = resolveTargetSeries(context, cli);
		final int seasonNumber = context.getInt("season-number");
		final int seriesTmdbId = context.getInt("series-tmdb-id");
		performImportSeason(cli, series, seasonNumber, seriesTmdbId, getLanguage(context, cli));
	}

	private static void executeImportEpisode(ExecutionContext context, Cli cli) {
		final Series series = resolveTargetSeries(context, cli);
		final int seasonNumber = context.getInt("season-number");
		final int episodeNumber = context.getInt("episode-number");
		final int seriesTmdbId = context.getInt("series-tmdb-id");
		performImportEpisode(cli, series, seasonNumber, episodeNumber, seriesTmdbId, getLanguage(context, cli));
	}

	private static void performImportMovie(Cli cli, int tmdbId, Language language) {
		final MovieImporter importer = createMovieImporter(getApiKey(cli));
		importer.setLanguage(language);

		final Movie movie = new Movie();
		try {
			importer.importData(movie, tmdbId);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import movie with id '" + tmdbId + "'. Is it the correct ID? Is it a movie? Do you have a stable internet connection?");
		}

		cli.getMediaService().addMedia(movie);
		cli.getOverviewPage().refreshItems();
		cli.getOverviewPage().moveTo(movie);
		cli.navigateToOverview();
	}

	private static void performImportSeries(Cli cli, int tmdbId, Language language) {
		final SeriesImporter importer = createSeriesImporter(getApiKey(cli));
		importer.setLanguage(language);

		final Series series = new Series();
		try {
			importer.importData(series, tmdbId);
			cli.getMediaService().save();
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import series with id '" + tmdbId + "'. Is it the correct ID? Is it a series? Do you have a stable internet connection?");
		}

		cli.getMediaService().addMedia(series);
		cli.getOverviewPage().refreshItems();
		cli.getOverviewPage().moveTo(series);
		cli.navigateToOverview();
	}

	private static void performImportSeason(Cli cli, Series series, int seasonNumber, int seriesTmdbId, Language language) {
		if (series.hasChild(seasonNumber)) {
			throw Command.fail("Season " + seasonNumber + " already exists in series '" + series.getTitle() + "'.");
		}

		final SeasonImporter importer = createSeasonImporter(getApiKey(cli));
		importer.setLanguage(language);

		final Season season = new Season(seasonNumber);
		try {
			importer.importData(season, seriesTmdbId, seasonNumber);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import season " + seasonNumber + " (Series ID: " + seriesTmdbId + ").");
		}

		series.addChild(season);
		cli.getOverviewPage().refreshItems();
	}

	private static void performImportEpisode(Cli cli, Series series, int seasonNumber, int episodeNumber, int seriesTmdbId, Language language) {
		final Season season = series.getChild(seasonNumber);
		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in series '" + series.getTitle() + "'. Please import the season first.");
		}

		if (season.hasChild(episodeNumber)) {
			throw Command.fail("Episode " + episodeNumber + " already exists in season " + seasonNumber + ".");
		}

		final EpisodeImporter importer = createEpisodeImporter(getApiKey(cli));
		importer.setLanguage(language);

		final Episode episode = new Episode(episodeNumber);
		try {
			importer.importData(episode, seriesTmdbId, seasonNumber, episodeNumber);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import episode " + episodeNumber + " (S" + seasonNumber + ", Series ID: " + seriesTmdbId + ").");
		}

		season.addChild(episode);
		cli.getOverviewPage().refreshItems();
	}

	private static String getApiKey(Cli cli) {
		return cli.getSettingsService().getSettings().getTmdbApiKey();
	}

	private static Language getLanguage(ExecutionContext context, Cli cli) {
		return context.getOptional("language", Language.class)
				.orElse(cli.getSettingsService().getSettings().getImportLanguage());
	}

	private static Series resolveTargetSeries(ExecutionContext context, Cli cli) {
		final Series explicitSeries = context.get("series", Series.class);
		if (explicitSeries != null) {
			return explicitSeries;
		}

		final Series activeSeries = cli.getActiveSeries();
		if (activeSeries != null) {
			return activeSeries;
		}

		throw Command.fail("Target series could not be determined. Either specify it explicitly as an argument or open a series/season/episode to provide context.");
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
