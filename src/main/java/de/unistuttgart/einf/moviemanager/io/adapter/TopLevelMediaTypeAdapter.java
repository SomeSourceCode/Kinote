package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.*;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import java.lang.reflect.Type;

public class TopLevelMediaTypeAdapter implements JsonDeserializer<TopLevelMedia> {

	/**
	 * Deserializes a top-level media object based on the "type" field.
	 *
	 * @param json the JSON element to deserialize
	 * @param typeOfT the target type
	 * @param context the JSON deserialization context
	 * @return the deserialized media object
	 * @throws JsonParseException if the "type" field is missing or unknown
	 */
	@Override
	public TopLevelMedia deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		JsonObject object = json.getAsJsonObject();
		JsonElement typeElement = object.get("type");

		if (typeElement == null) {
			throw new JsonParseException("Missing field 'type' for TopLevelMedia");
		}

		String type = typeElement.getAsString();
		return switch (type) {
			case "movie" -> context.deserialize(object, Movie.class);
			case "series" -> context.deserialize(object, Series.class);
			default -> throw new JsonParseException("Unknown TopLevelMedia type: " + type);
		};
	}
}
