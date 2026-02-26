package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.Series;

/**
 * Used to import series data from TMDb.
 */
public class SeriesImporter extends MediaImporter<Series> {

	private SeasonImporter seasonImporter;

	/**
	 * Constructs a new series importer with the given TMDb client.
	 *
	 * @param client the TMDb client to use for API requests
	 */
	public SeriesImporter(TmdbClient client) {
		super(client);
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

	@Override
	protected void initializeStrategy() {
		strategies.put(ImportableAttribute.TITLE, createStringStrategy("name", Series::getTitle, Series::setTitle));
		strategies.put(ImportableAttribute.DESCRIPTION, createStringStrategy("overview", Series::getDescription, Series::setDescription));
		strategies.put(ImportableAttribute.GENRE, createGenreStrategy("genres", Series::addGenre));
	}

	/**
	 * Fills the given series with data from TMDb for the given series ID.
	 *
	 * @param series the series to fill
	 * @param tmdbId the TMDb ID of the series to import
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public void fill(Series series, int tmdbId) throws MediaImportException {
		final JsonObject json = client.get("tv/" + tmdbId, language, "content_ratings");
		final JsonObject contentRatings = json.getAsJsonObject("content_ratings");
		final ImportContext context = new ImportContext(tmdbId, parseSeriesAgeRatings(contentRatings));
		applyAttributes(series, json, context);

		if (seasonImporter == null || !json.has("seasons")) {
			return;
		}

		final JsonArray seasonsArray = json.getAsJsonArray("seasons");
		for (JsonElement seasonElement : seasonsArray) {
			final JsonObject seasonObject = seasonElement.getAsJsonObject();
			final int seasonNumber = seasonObject.get("season_number").getAsInt();

			if (seasonNumber < 1) {
				continue;
			}

			Season season = series.getChild(seasonNumber);
			if (season == null) {
				season = new Season(seasonNumber);
				series.addChild(season);
			}
			seasonImporter.fill(season, seasonObject, context);
		}
	}

	/**
	 * Creates a new series and fills it with data from TMDb for the given TMDb ID.
	 *
	 * @param tmdbId the TMDb ID of the series to import
	 * @return the newly create series
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public Series create(int tmdbId) throws MediaImportException {
		final Series series = new Series();
		fill(series, tmdbId);
		return series;
	}

}
