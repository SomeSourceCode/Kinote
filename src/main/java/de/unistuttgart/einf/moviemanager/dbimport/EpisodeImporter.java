package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonObject;
import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Used to import episode data from TMDb.
 */
public class EpisodeImporter extends MediaImporter<Episode> {

	private static final Pattern TITLE_PREFIX_PATTERN = Pattern.compile("((Episode)|(Folge)) \\d+[:\\-\\s]*");

	/**
	 * Constructs a new episode importer with the given TMDb client.
	 *
	 * @param client the TMDb client to use for API requests
	 */
	public EpisodeImporter(TmdbClient client) {
		super(client);
	}

	@Override
	protected void initializeStrategy() {
		strategies.put(ImportableAttribute.TITLE, createStringStrategy("name", Episode::getTitle, Episode::setTitle, title -> TITLE_PREFIX_PATTERN.matcher(title).replaceFirst("")));
		strategies.put(ImportableAttribute.DESCRIPTION, createStringStrategy("overview", Episode::getDescription, Episode::setDescription));
		strategies.put(ImportableAttribute.RUNTIME, createRuntimeStrategy("runtime", Episode::hasRuntime, Episode::setRuntime));

		strategies.put(ImportableAttribute.AGE_RATING, (episode, json, context, override) -> {
			if (context == null || context.seriesAgeRatings() == null) {
				return;
			}
			final Set<AgeRating> ageRatings = context.seriesAgeRatings();

			if (ageRatings.isEmpty()) {
				return;
			}

			for (RatingSystem ratingSystem : RatingSystem.values()) {
				if (!override && episode.hasAgeRating(ratingSystem)) {
					continue;
				}
				final AgeRating rating = AgeRating.max(getAgeRatingsBySystem(ageRatings, ratingSystem));
				if (rating == null) {
					continue;
				}
				episode.setAgeRating(rating);
			}
		});
	}

	protected void fill(Episode episode, JsonObject json, ImportContext context) {
		applyAttributes(episode, json, context);
	}

	/**
	 * Fills the given episode with data from TMDb for the given series ID, season number and episode number.
	 *
	 * @param episode the episode to fill
	 * @param seriesId the TMDb ID of the parent series
	 * @param seasonNumber the season number
	 * @param episodeNumber the episode number
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public void fill(Episode episode, int seriesId, int seasonNumber, int episodeNumber) throws MediaImportException {
		final JsonObject json = client.get("tv/" + seriesId + "/season/" + seasonNumber + "/episode/" + episodeNumber, language);
		final Set<AgeRating> ageRatings = fetchSeriesAgeRatings(seriesId);
		final ImportContext context = new ImportContext(seriesId, ageRatings);
		applyAttributes(episode, json, context);
	}

	/**
	 * Creates a new episode and fills it with data from TMDb for the given series ID, season number and episode number.
	 *
	 * @param seriesTmdbId the TMDb ID of the parent series
	 * @param seasonNumber the season number
	 * @param episodeNumber the episode number
	 * @return the newly created episode
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public Episode create(int seriesTmdbId, int seasonNumber, int episodeNumber) throws MediaImportException {
		final Episode episode = new Episode(episodeNumber);
		fill(episode, seriesTmdbId, seasonNumber, episodeNumber);
		return episode;
	}

}
