package de.unistuttgart.einf.moviemanager.dbimport;

import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import info.movito.themoviedbapi.TmdbTvSeasons;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.model.tv.season.TvSeasonDb;
import info.movito.themoviedbapi.model.tv.season.TvSeasonEpisode;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class SeasonImporter extends MediaImporter {

	private EpisodeImporter episodeImporter;
	private static final Pattern BAD_TITLE_PATTERN = Pattern.compile("((Season)|(Staffel)) \\d+[:\\-\\s]*");

	/**
	 * Creates a new SeasonImporter with the given TMDb API key.
	 *
	 * @param apiKey the TMDb API key
	 */
	public SeasonImporter(String apiKey) {
		super(apiKey);
	}

	/**
	 * Creates a new SeasonImporter with the given TMDb API key
	 * and also sets the configuration for episode imports.
	 *
	 * @param apiKey the TMDb API key
	 * @param episodeImporter the configuration used for episode imports
	 */
	public SeasonImporter(String apiKey, EpisodeImporter episodeImporter) {
		super(apiKey);
		this.episodeImporter = episodeImporter;
	}

	/**
	 * Returns the EpisodeImporter used for importing Episodes.
	 *
	 * @return the EpisodeImporter
	 */
	public EpisodeImporter getEpisodeImporter() {
		return episodeImporter;
	}

	/**
	 * Sets the EpisodeImporter used for importing Episodes.
	 * If it is null, no episode will be imported.
	 *
	 * @param episodeImporter the EpisodeImporter to set
	 */
	public void setEpisodeImporter(EpisodeImporter episodeImporter) {
		this.episodeImporter = episodeImporter;
	}

	/**
	 * Runs the import with the current configuration for the given season and TMDB series ID.
	 *
	 * @param season the season to import data into
	 * @param seriesId the TMDB ID of the series
	 * @param seasonNumber the season number
	 */
	public void importData(Season season, int seriesId, int seasonNumber) {
		TmdbTvSeries tmdbTvSeries = tmdbApi.getTvSeries();

		try {
			TvSeriesDb tmdbSeries =  tmdbTvSeries.getDetails(seriesId, language.getCode());
			importData(season, seriesId, seasonNumber, getAgeRatingsFromSeries(seriesId));
		} catch (TmdbException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Runs the import with the current configuration for the given season, TMDB series ID and age ratings.
	 *
	 * @param season the season to import data into
	 * @param seriesId the TMDB ID of the series
	 * @param seasonNumber the season number
	 * @param ageRatings the age ratings of the series
	 */
	protected void importData(Season season, int seriesId, int seasonNumber, Set<AgeRating> ageRatings) {
		final TmdbTvSeasons tmdbSeasons = tmdbApi.getTvSeasons();

		try {
			TvSeasonDb tmdbSeason = tmdbSeasons.getDetails(seriesId, seasonNumber, language.getCode());

			included.forEach(attribute -> {
				switch (attribute) {
					case TITLE -> {
						String tmdbTitle = tmdbSeason.getName();
						if (tmdbTitle == null) {
							break;
						}
						tmdbTitle = BAD_TITLE_PATTERN.matcher(tmdbTitle).replaceFirst("");
						String title = season.getTitle();

						if (!tmdbTitle.isBlank() && (overridden.contains(attribute) || title == null || title.isBlank())) {
							season.setTitle(tmdbTitle);
						}
					}
					case DESCRIPTION -> {
						String tmdbDescription = tmdbSeason.getOverview();
						String description = season.getDescription();

						if (tmdbDescription != null && (overridden.contains(attribute) || description == null || description.isBlank())) {
							season.setDescription(tmdbDescription);
						}
					}
				}
			});

			if (episodeImporter != null) {
				List<TvSeasonEpisode> tmdbEpisodes = tmdbSeason.getEpisodes();
				for (TvSeasonEpisode tmdbEpisode : tmdbEpisodes) {
					if (tmdbEpisode.getEpisodeNumber() < 1) {
						continue;
					}
					int episodeNumber = tmdbEpisode.getEpisodeNumber();
					Episode episode = season.getChild(episodeNumber);
					if (episode == null) {
						episode = new Episode(episodeNumber);
						season.addChild(episode);
					}
					episodeImporter.importData(episode, tmdbEpisode, ageRatings);
				}
			}
			
		} catch (TmdbException e) {
			throw new RuntimeException(e);
		}
	}

}
