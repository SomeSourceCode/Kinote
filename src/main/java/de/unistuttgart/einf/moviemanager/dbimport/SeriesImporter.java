package de.unistuttgart.einf.moviemanager.dbimport;

import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.model.tv.core.TvSeason;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;

import java.util.List;
import java.util.Set;

public class SeriesImporter extends MediaImporter {

	private SeasonImporter seasonImporter;

	/**
	 * Creates a new SeriesImporter with the given TMDb API key.
	 *
	 * @param apiKey the TMDb API key
	 */
	public SeriesImporter(String apiKey) {
		super(apiKey);
	}

	/**
	 * Creates a new SeriesImporter with the given TMDb API key
	 * and also sets the configuration for season imports.
	 *
	 * @param apiKey the TMDb API key
	 * @param seasonImporter the configuration used for season imports
	 */
	public SeriesImporter(String apiKey, SeasonImporter seasonImporter) {
		super(apiKey);
		this.seasonImporter = seasonImporter;
	}

	/**
	 * Returns the SeasonImporter used for importing Seasons.
	 *
	 * @return the SeasonImporter
	 */
	public SeasonImporter getSeasonImporter() {
		return seasonImporter;
	}

	/**
	 * Sets the SeasonImporter used for importing Seasons.
	 * If it is null, no season will be imported.
	 *
	 * @param seasonImporter the SeasonImporter to set
	 */
	public void setSeasonImporter(SeasonImporter seasonImporter) {
		this.seasonImporter = seasonImporter;
	}

	/**
	 * Runs the import with the current configuration for the given series and TMDB ID.
	 *
	 * @param series the series to import data into
	 * @param seriesId the TMDB ID of the series
	 */
	public void importData(Series series, int seriesId) {
		final TmdbTvSeries tmdbTvSeries = tmdbApi.getTvSeries();

		try {
			TvSeriesDb tmdbSeries = tmdbTvSeries.getDetails(seriesId, language.getCode());

			included.forEach(attribute -> {
				switch (attribute) {
					case TITLE -> {
						String tmdbTitle = tmdbSeries.getName();
						String title = series.getTitle();
						
						if (tmdbTitle != null && (overridden.contains(attribute) || title == null || title.isBlank())) {
							series.setTitle(tmdbTitle);
						}
					}
					case DESCRIPTION -> {
						String tmdbDescription = tmdbSeries.getOverview();
						String description = series.getDescription();
						
						if (tmdbDescription != null && (overridden.contains(attribute) || description == null ||description.isBlank())) {
							series.setDescription(tmdbDescription);
						}
					}
					
					// todo: case CATEGORY ->

				}
			});

			if (seasonImporter == null) {
				return;
			}

			List<TvSeason> seasons = tmdbSeries.getSeasons();
			Set<AgeRating> ageRatings = fetchAgeRatingsFromSeries(seriesId);
			for (TvSeason tmdbSeason : seasons) {
				if (tmdbSeason.getSeasonNumber() < 1) {
					continue;
				}
				int seasonNumber = tmdbSeason.getSeasonNumber();
				Season season = series.getChild(seasonNumber);
				if (season == null) {
					season = new Season(seasonNumber);
					series.addChild(season);
				}
				seasonImporter.importData(season, seriesId, seasonNumber, ageRatings);
			}

		} catch (TmdbException e) {
			throw new RuntimeException(e);
		}

	}

}
