package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonObject;
import de.unistuttgart.einf.moviemanager.model.Media;

/**
 * A strategy to import media attributes from TMDb.
 *
 * @param <T> the type of media to import attributes for
 */
@FunctionalInterface
public interface ImportStrategy<T extends Media> {

	void apply(T media, JsonObject json, ImportContext context, boolean override);

}
