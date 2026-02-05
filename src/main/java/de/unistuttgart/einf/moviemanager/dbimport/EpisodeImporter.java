package de.unistuttgart.einf.moviemanager.dbimport;

import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;
import info.movito.themoviedbapi.TmdbTvEpisodes;
import info.movito.themoviedbapi.model.tv.episode.TvEpisodeDb;
import info.movito.themoviedbapi.model.tv.season.TvSeasonEpisode;
import info.movito.themoviedbapi.tools.TmdbException;

import java.util.Set;
import java.util.regex.Pattern;

public class EpisodeImporter extends MediaImporter {

	private static final Pattern TITLE_PREFIX_PATTERN = Pattern.compile("((Episode)|(Folge)) \\d+[:\\-\\s]*");

	/**
	 * Creates a new EpisodeImporter with the given TMDb API key.
	 *
	 * @param apiKey the TMDb API key
	 */
	public EpisodeImporter(String apiKey) {
		super(apiKey);
	}

	/**
	 * Runs the import with the current configuration for the given episode and TMDB episode data.
	 *
	 * @param episode the episode to import data into
	 * @param seriesId the TMDB ID of the series
	 * @param seasonNumber the season number
	 * @param episodeNumber the episode number
	 * @throws MediaImportException if the import fails due to a TMDb API error
	 */
	public void importData(Episode episode, int seriesId, int seasonNumber, int episodeNumber) throws MediaImportException {
		importData(episode, seriesId, seasonNumber, episodeNumber, fetchAgeRatingsFromSeries(seriesId));
	}

	/**
	 * Runs the import with the current configuration for the given episode and TMDB episode data.
	 *
	 * @param episode the episode to import data into
	 * @param tmdbEpisode the TMDB episode data
	 * @param ageRatings the age ratings of the series
	 */
	protected void importData(Episode episode, TvSeasonEpisode tmdbEpisode, Set<AgeRating> ageRatings) {
		included.forEach(attribute -> {
			switch (attribute) {
				case TITLE -> {
					String tmdbTitle = tmdbEpisode.getName();
					if (tmdbTitle == null) {
						break;
					}
					tmdbTitle = TITLE_PREFIX_PATTERN.matcher(tmdbTitle).replaceFirst("");
					String title = episode.getTitle();

					if (!tmdbTitle.isBlank() && (overridden.contains(attribute) || title == null || title.isBlank())) {
						episode.setTitle(tmdbTitle);
					}
				}
				case DESCRIPTION -> {
					String tmdbDescription = tmdbEpisode.getOverview();
					String description = episode.getDescription();
					if (tmdbDescription != null && (overridden.contains(attribute) || description == null || description.isBlank())) {
						episode.setDescription(tmdbEpisode.getOverview());
					}
				}
				case RUNTIME -> {
					Integer tmdbRuntime = tmdbEpisode.getRuntime();
					int runtime = episode.getDuration();
					if (tmdbRuntime != null && (overridden.contains(attribute) || episode.getDuration() == -1)) {
						episode.setDuration(tmdbEpisode.getRuntime());
					}
				}
				case AGE_RATING -> {
					for (RatingSystem ratingSystem : RatingSystem.values()) {
						if (!overridden.contains(attribute) && episode.getAgeRating(ratingSystem) != null) {
							continue;
						}
						AgeRating rating = AgeRating.max(getAgeRatingsBySystem(ageRatings, ratingSystem));
						episode.setAgeRating(rating);
					}
				}
			}
		});
	}

	private void importData(Episode episode, int seriesId, int seasonNumber, int episodeNumber, Set<AgeRating> ageRatings) throws MediaImportException {
		final TmdbTvEpisodes tmdbEpisodes = tmdbApi.getTvEpisodes();

		try {
			TvEpisodeDb tmdbEpisode = tmdbEpisodes.getDetails(seriesId, seasonNumber, episodeNumber, language.getCode());

			included.forEach(attribute -> {
				switch (attribute) {
					case TITLE -> {
						String tmdbTitle = tmdbEpisode.getName();
						if (tmdbTitle == null) {
							break;
						}
						tmdbTitle = TITLE_PREFIX_PATTERN.matcher(tmdbTitle).replaceFirst("");
						String title = episode.getTitle();

						if (!tmdbTitle.isBlank() && (overridden.contains(attribute) || title == null || title.isBlank())) {
							episode.setTitle(tmdbTitle);
						}
					}
					case DESCRIPTION -> {
						String tmdbDescription = tmdbEpisode.getOverview();
						String description = episode.getDescription();

						if (tmdbDescription != null && (overridden.contains(attribute) || description == null || description.isBlank())) {
							episode.setDescription(tmdbDescription);
						}
					}
					case RUNTIME -> {
						Integer tmdbRuntime = tmdbEpisode.getRuntime();
						int runtime = episode.getDuration();

						if (tmdbRuntime != null && (overridden.contains(attribute) || episode.getDuration() == -1)) {
							episode.setDuration(tmdbRuntime);
						}
					}
					case AGE_RATING -> {
						for (RatingSystem ratingSystem : RatingSystem.values()) {
							if (!overridden.contains(attribute) && episode.getAgeRating(ratingSystem) != null) {
								continue;
							}
							AgeRating rating = AgeRating.max(getAgeRatingsBySystem(ageRatings, ratingSystem));
							episode.setAgeRating(rating);
						}
					}
				}
			});

		} catch (TmdbException e) {
			throw new MediaImportException(e.getMessage(), e.getCause());
		}

	}

}
