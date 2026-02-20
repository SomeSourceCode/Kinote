package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.*;
import de.unistuttgart.einf.moviemanager.model.Episode;

import java.lang.reflect.Type;

import static de.unistuttgart.einf.moviemanager.io.adapter.Converter.*;
import static de.unistuttgart.einf.moviemanager.io.adapter.SerializationHelper.*;

public class EpisodeAdapter implements JsonSerializer<Episode>, JsonDeserializer<Episode> {

	/**
	 * Serializes an episode object to JSON.
	 *
	 * The resulting JSON contains the properties
	 *
	 * @param src the episode to serialize
	 * @param typeOfSrc the type of the source object
	 * @param context the JSON serialization context
	 * @return the serialized JSON element
	 */
	@Override
	public JsonElement serialize(Episode src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();

		object.addProperty(Keys.NUMBER, src.getNumber());
		setMediaAttributesOnJson(object, src, context);
		setLeafMediaAttributesOnJson(object, src, context);

		return object;
	}

	/**
	 * Deserializes an episode object from JSON.
	 * A new Episode instance is created
	 *
	 * @param json the JSON element to deserialize
	 * @param typeOfT the target type
	 * @param context the JSON deserialization context
	 * @return the deserialized season
	 * @throws JsonParseException if the JSON structure is invalid
	 */
	@Override
	public Episode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();

		final Episode episode = new Episode(getAsIntOrElse(object, Keys.NUMBER, 1));

		setMediaAttributesFromJson(episode, object, context);
		setLeafMediaAttributesFromJson(episode, object, context);

		return episode;
	}
}
