package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Used to import season data from TMDb.
 */
public class SeasonImporter extends MediaImporter<Season> {

	private EpisodeImporter episodeImporter;
	private static final Pattern TITLE_PREFIX_PATTERN = Pattern.compile("((Season)|(Staffel)) \\d+[:\\-\\s]*");

	/**
	 * Constructs a new season importer with the given TMDb client.
	 *
	 * @param client the TMDb client to use for API requests.
	 */
	public SeasonImporter(TmdbClient client) {
		super(client);
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

	@Override
	protected void initializeStrategy() {
		strategies.put(ImportableAttribute.TITLE, createStringStrategy("name", Season::getTitle, Season::setTitle, title -> TITLE_PREFIX_PATTERN.matcher(title).replaceFirst("")));
		strategies.put(ImportableAttribute.DESCRIPTION, createStringStrategy("overview", Season::getDescription, Season::setDescription));
	}

	protected void fill(Season season, JsonObject json, ImportContext context) throws MediaImportException {
		if (episodeImporter == null || context == null) {
			applyAttributes(season, json, context);
			return;
		}

		json = client.get("tv/" + context.seriesId() + "/season/" + season.getNumber(), language, "content_ratings");

		final Set<AgeRating> seriesAgeRatings;
		if (context.seriesAgeRatings() == null) {
			seriesAgeRatings = fetchSeriesAgeRatings(context.seriesId());
		} else {
			seriesAgeRatings = context.seriesAgeRatings();
		}

		context = new ImportContext(context.seriesId(), seriesAgeRatings);

		applyAttributes(season, json, context);
		if (!json.has("episodes")) {
			return;
		}

		final JsonArray episodesArray = json.getAsJsonArray("episodes");
		for (JsonElement episodeElement : episodesArray) {
			final JsonObject episodeObject = episodeElement.getAsJsonObject();
			final int episodeNumber = episodeObject.get("episode_number").getAsInt();

			if (episodeNumber < 1) {
				continue;
			}

			Episode episode = season.getChild(episodeNumber);
			if (episode == null) {
				episode = new Episode(episodeNumber);
				season.addChild(episode);
			}
			episodeImporter.fill(episode, episodeObject, context);
		}
	}

	/**
	 * Fills the given season with data from TMDb for the given series ID and season number.
	 *
	 * @param season the season to fill
	 * @param seriesTmdbId the TMDb ID of the parent series
	 * @param seasonNumber the season number
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public void fill(Season season, int seriesTmdbId, int seasonNumber) throws MediaImportException {
		final JsonObject json = client.get("tv/" + seriesTmdbId + "/season/" + seasonNumber, language, "content_ratings");
		final Set<AgeRating> ageRatings = fetchSeriesAgeRatings(seriesTmdbId);
		final ImportContext context = new ImportContext(seriesTmdbId, ageRatings);
		fill(season, json, context);
	}

	/**
	 * Creates a new season and fills it with data from TMDb for the given series ID and season number.
	 *
	 * @param seriesTmdbId the TMDb ID of the parent series
	 * @param seasonNumber the season number
	 * @return the newly created season
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public Season create(int seriesTmdbId, int seasonNumber) throws MediaImportException {
		final Season season = new Season(seasonNumber);
		fill(season, seriesTmdbId, seasonNumber);
		return season;
	}

}
