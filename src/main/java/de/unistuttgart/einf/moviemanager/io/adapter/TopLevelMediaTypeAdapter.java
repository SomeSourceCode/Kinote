package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.*;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import java.lang.reflect.Type;

public class TopLevelMediaTypeAdapter implements JsonSerializer<TopLevelMedia>, JsonDeserializer<TopLevelMedia> {

	/**
	 * Serializes a top-level media object and adds a "type" field
	 * to indicate the concrete implementation.
	 *
	 * @param src the media object to serialize
	 * @param typeOfSrc the type of the source object
	 * @param context the JSON serialization context
	 * @return the serialized JSON element
	 */
	@Override
	public JsonElement serialize(TopLevelMedia src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = context.serialize(src, src.getClass()).getAsJsonObject();
		if (src instanceof Movie) {
			object.addProperty("type", "movie");
		}
		return object;
	}

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
