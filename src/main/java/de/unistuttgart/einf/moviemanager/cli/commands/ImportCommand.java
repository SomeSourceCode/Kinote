package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.Insets;
import de.unistuttgart.einf.moviemanager.cli.api.popover.CenteredPlacement;
import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;
import de.unistuttgart.einf.moviemanager.cli.api.util.TextUtils;
import de.unistuttgart.einf.moviemanager.cli.api.widget.ListView;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.CommandExecutionException;
import de.unistuttgart.einf.moviemanager.command.ExecutionContext;
import de.unistuttgart.einf.moviemanager.command.argument.*;
import de.unistuttgart.einf.moviemanager.dbimport.*;
import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.Series;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
				.then(Arguments.tmdbUrl("tmdb-url")
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
														.executes(context -> executeImportEpisode(context, cli)))))))
				// import [movie|series] search <query> [<language>]
				.then(LiteralArgument.create("search")
						// search <query> [<language>]
						.then(StringArgument.create("query")
								.executes(context -> executeSearch(context, cli))
								.then(EnumArgument.create("language", Language.class)
										.executes(context -> executeSearch(context, cli))))
						// search movie <query> [<language>]
						.then(LiteralArgument.create("movie")
								.then(StringArgument.create("query")
										.executes(context -> executeSearchMovies(context, cli))
										.then(EnumArgument.create("language", Language.class)
												.executes(context -> executeSearchMovies(context, cli)))))
						// search series <query> [<language>]
						.then(LiteralArgument.create("series")
								.then(StringArgument.create("query")
										.executes(context -> executeSearchSeries(context, cli))
										.then(EnumArgument.create("language", Language.class)
												.executes(context -> executeSearchSeries(context, cli)))))));
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
		final MovieImporter importer = createMovieImporter(cli);
		importer.setLanguage(language);

		final Movie movie;
		try {
			movie = importer.create(tmdbId);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import movie with id '" + tmdbId + "'. Is it the correct ID? Is it a movie? Do you have a stable internet connection?");
		}

		cli.getMediaService().addMedia(movie);
		cli.refresh();
		cli.getOverviewPage().moveTo(movie);
		cli.navigateToOverview();
	}

	private static void performImportSeries(Cli cli, int tmdbId, Language language) {
		final SeriesImporter importer = createSeriesImporter(cli);
		importer.setLanguage(language);

		final Series series;
		try {
			series = importer.create(tmdbId);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import series with id '" + tmdbId + "'. Is it the correct ID? Is it a series? Do you have a stable internet connection?");
		}

		cli.getMediaService().addMedia(series);
		cli.refresh();
		cli.getOverviewPage().moveTo(series);
		cli.navigateToOverview();
	}

	private static void performImportSeason(Cli cli, Series series, int seasonNumber, int seriesTmdbId, Language language) {
		if (series.hasChild(seasonNumber)) {
			throw Command.fail("Season " + seasonNumber + " already exists in series '" + series.getTitle() + "'.");
		}

		final SeasonImporter importer = createSeasonImporter(cli);
		importer.setLanguage(language);

		final Season season;
		try {
			season = importer.create(seriesTmdbId, seasonNumber);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import season " + seasonNumber + " (Series ID: " + seriesTmdbId + ").");
		}

		series.addChild(season);
		cli.refresh();
	}

	private static void performImportEpisode(Cli cli, Series series, int seasonNumber, int episodeNumber, int seriesTmdbId, Language language) {
		final Season season = series.getChild(seasonNumber);
		if (season == null) {
			throw Command.fail("Season " + seasonNumber + " does not exist in series '" + series.getTitle() + "'. Please import the season first.");
		}

		if (season.hasChild(episodeNumber)) {
			throw Command.fail("Episode " + episodeNumber + " already exists in season " + seasonNumber + ".");
		}

		final EpisodeImporter importer = createEpisodeImporter(cli);
		importer.setLanguage(language);

		final Episode episode;
		try {
			episode = importer.create(seriesTmdbId, seasonNumber, episodeNumber);
		} catch (MediaImportException exception) {
			throw Command.fail("Failed to import episode " + episodeNumber + " (S" + seasonNumber + ", Series ID: " + seriesTmdbId + ").");
		}

		season.addChild(episode);
		cli.refresh();
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

	private static MovieImporter createMovieImporter(Cli cli) {
		final MovieImporter movieImporter = new MovieImporter(cli.getTmdbClient());
		movieImporter.include(ImportableAttribute.values());
		return movieImporter;
	}

	private static SeriesImporter createSeriesImporter(Cli cli) {
		final SeriesImporter seriesImporter = new SeriesImporter(cli.getTmdbClient());
		seriesImporter.include(ImportableAttribute.values());
		seriesImporter.setSeasonImporter(createSeasonImporter(cli));
		return seriesImporter;
	}

	private static SeasonImporter createSeasonImporter(Cli cli) {
		final SeasonImporter seasonImporter = new SeasonImporter(cli.getTmdbClient());
		seasonImporter.include(ImportableAttribute.values());
		seasonImporter.setEpisodeImporter(createEpisodeImporter(cli));
		return seasonImporter;
	}

	private static EpisodeImporter createEpisodeImporter(Cli cli) {
		final EpisodeImporter episodeImporter = new EpisodeImporter(cli.getTmdbClient());
		episodeImporter.include(ImportableAttribute.values());
		return episodeImporter;
	}

	@FunctionalInterface
	private interface SearchProvider {
		List<TmdbSearcher.SearchResult> search(TmdbSearcher searcher, String query, Language language) throws MediaImportException;
	}

	private static void executeGenericSearch(ExecutionContext context, Cli cli, SearchProvider searchProvider,
			Function<TmdbSearcher.SearchResult, String> displayFunction, BiConsumer<TmdbSearcher.SearchResult, Language> importAction, String errorMessage) {
		final String query = context.getString("query");
		final Language language = getLanguage(context, cli);

		final TmdbSearcher searcher = new TmdbSearcher(cli.getTmdbClient());
		final List<TmdbSearcher.SearchResult> results;
		try {
			results = searchProvider.search(searcher, query, language);
		} catch (MediaImportException exception) {
			throw Command.fail(errorMessage + " '" + query + "'. Do you have a stable internet connection? Is the TMDb API key set and valid?");
		}

		if (results.isEmpty()) {
			cli.showInfoDialog("No results found for query: " + query);
			return;
		}

		final ListView<TmdbSearcher.SearchResult> resultListView = new ListView<>(displayFunction);
		resultListView.setItems(results);
		resultListView.setPadding(new Insets(0, 1));

		final int maximumAllowedHeight = 15;
		final int calculatedHeight = Math.min(maximumAllowedHeight, results.size());
		resultListView.setHeight(calculatedHeight);

		final int maximumAllowedWidth = 80;
		int calculatedWidth = 20;
		for (TmdbSearcher.SearchResult result : results) {
			final String displayString = displayFunction.apply(result);
			if (displayString.length() > calculatedWidth) {
				calculatedWidth = displayString.length();
			}
		}
		calculatedWidth = Math.min(maximumAllowedWidth, calculatedWidth);
		resultListView.setWidth(calculatedWidth + 2);

		final CenteredPlacement placementStrategy = new CenteredPlacement();
		final Popover searchPopover = new Popover(resultListView, placementStrategy);
		searchPopover.setShowBorders(true);

		resultListView.setOnItemSelected((selectedResult, index) -> {
			if (resultListView.getScene() != null) {
				resultListView.getScene().attemptClosePopover();
			}
			try {
				importAction.accept(selectedResult, language);
			} catch (CommandExecutionException exception) {
				cli.showInfoDialog(exception.getMessage());
			}
		});

		cli.showPopover(searchPopover);
		resultListView.requestFocus();
	}

	private static void executeSearch(ExecutionContext context, Cli cli) {
		executeGenericSearch(
				context,
				cli,
				TmdbSearcher::searchMedia,
				result -> {
					final String prefix = switch (result.type()) {
						case MOVIE -> "[M]";
						case SERIES -> "[S]";
						default -> "[?]";
					};
					return prefix + " " + result.title() + (result.releaseDate() == null ? "" : " (" + result.releaseDate().getYear() + ")") + " [id: " + result.id() + "]";
				},
				(selectedResult, language) -> {
					switch (selectedResult.type()) {
						case MOVIE -> performImportMovie(cli, selectedResult.id(), language);
						case SERIES -> performImportSeries(cli, selectedResult.id(), language);
					}
				},
				"Failed to search for media with query"
		);
	}

	private static void executeSearchMovies(ExecutionContext context, Cli cli) {
		executeGenericSearch(
				context,
				cli,
				TmdbSearcher::searchMovies,
				result -> result.title() + (result.releaseDate() == null ? "" : " (" + result.releaseDate().getYear() + ")") + " [id: " + result.id() + "]",
				(selectedResult, language) -> performImportMovie(cli, selectedResult.id(), language),
				"Failed to search for movies with query"
		);
	}

	private static void executeSearchSeries(ExecutionContext context, Cli cli) {
		executeGenericSearch(
				context,
				cli,
				TmdbSearcher::searchSeries,
				result -> result.title() + (result.releaseDate() == null ? "" : " (" + result.releaseDate().getYear() + ")") + " [id: " + result.id() + "]",
				(selectedResult, language) -> performImportSeries(cli, selectedResult.id(), language),
				"Failed to search for series with query"
		);
	}

}
