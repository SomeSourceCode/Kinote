package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.*;
import de.unistuttgart.einf.moviemanager.model.*;
import java.lang.reflect.Type;

import static de.unistuttgart.einf.moviemanager.io.adapter.Converter.getAsIntOrElse;
import static de.unistuttgart.einf.moviemanager.io.adapter.SerializationHelper.*;

public class SeasonAdapter implements JsonSerializer<Season>, JsonDeserializer<Season> {

	/**
	 * Serializes a season object to JSON.
	 *
	 * The resulting JSON contains the properties,
	 * and a list of episodes.
	 *
	 * @param src the season to serialize
	 * @param typeOfSrc the type of the source object
	 * @param context the JSON serialization context
	 * @return the serialized JSON element
	 */
	@Override
	public JsonElement serialize(Season src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();

		object.addProperty(Keys.NUMBER, src.getNumber());
		setMediaAttributesOnJson(object, src, context);

		JsonArray episodeList = new JsonArray();
		for (Episode e : src.getChildren()) {
			episodeList.add(context.serialize(e, Episode.class));
		}

		object.add(Keys.EPISODES, episodeList);
		return object;
	}

	/**
	 * Deserializes a season object from JSON.
	 *
	 * A new Season instance is created and all contained episodes
	 * are added using addChild to correctly restore parent relations.
	 *
	 * @param json the JSON element to deserialize
	 * @param typeOfT the target type
	 * @param context the JSON deserialization context
	 * @return the deserialized season
	 * @throws JsonParseException if the JSON structure is invalid
	 */
	@Override
	public Season deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();

		final Season season = new Season(getAsIntOrElse(object, Keys.NUMBER, 1));

		setMediaAttributesFromJson(season, object, context);

		JsonArray episodes = object.has("episodes") && object.get("episodes").isJsonArray()
				? object.getAsJsonArray("episodes") : new JsonArray();

		for (JsonElement element : episodes) {
			Episode ep = context.deserialize(element, Episode.class);
			season.addChild(ep);
		}
		return season;
	}


}
