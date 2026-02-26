package de.unistuttgart.einf.moviemanager.dbimport;

import de.unistuttgart.einf.moviemanager.model.age.AgeRating;

import java.util.Set;

/**
 * The import context used to pass information between importes in order to reduce
 * the number of API requests.
 *
 * @param seriesId the TMDb ID of the series being imported, used by season and episode importers
 * @param seriesAgeRatings the age ratings of the series being imported, used by season and episode importers
 */
public record ImportContext(int seriesId, Set<AgeRating> seriesAgeRatings) {

}
