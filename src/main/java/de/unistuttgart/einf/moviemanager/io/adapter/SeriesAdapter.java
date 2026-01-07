package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.*;
import de.unistuttgart.einf.moviemanager.model.*;

import java.lang.reflect.Type;

import static de.unistuttgart.einf.moviemanager.io.adapter.Converter.getAsStringOrNull;

public class SeriesAdapter implements JsonSerializer<Series>, JsonDeserializer<Series> {

	/**
	 * Serializes a series object to JSON.
	 *
	 * The resulting JSON contains the properties,
	 * and a list of seasons.
	 *
	 * @param src the series to serialize
	 * @param typeOfSrc the type of the source object
	 * @param context the JSON serialization context
	 * @return the serialized JSON element
	 */
	@Override
	public JsonElement serialize(Series src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("type", "series");
		object.addProperty("title", src.getTitle());
		object.addProperty("description", src.getDescription());

		JsonArray seasons = new JsonArray();
		for (Season s : src.getChildren()) {
			seasons.add(context.serialize(s, Season.class));
		}
		object.add("seasons", seasons);
		return object;
	}

	/**
	 * Deserializes a series object from JSON.
	 *
	 * A new Series instance is created and all contained seasons
	 * are added using addChild to correctly restore parent relations.
	 *
	 * @param json the JSON element to deserialize
	 * @param typeOfT the target type
	 * @param context the JSON deserialization context
	 * @return the deserialized series
	 * @throws JsonParseException if the JSON structure is invalid
	 */
	@Override
	public Series deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		Series series = new Series(
				getAsStringOrNull(object, "title"),
				getAsStringOrNull(object, "description")
		);

		JsonArray seasons = object.has("seasons") && object.get("seasons").isJsonArray()
				? object.getAsJsonArray("seasons")
				: new JsonArray();

		for (JsonElement el : seasons) {
			Season season = context.deserialize(el, Season.class);
			series.addChild(season);
		}
		return series;
	}

}
