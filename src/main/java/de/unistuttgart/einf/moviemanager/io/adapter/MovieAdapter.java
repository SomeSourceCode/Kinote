package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.*;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.age.*;

import java.lang.reflect.Type;

import static de.unistuttgart.einf.moviemanager.io.adapter.Converter.*;
import static de.unistuttgart.einf.moviemanager.io.adapter.SerializationHelper.*;

public class MovieAdapter implements JsonSerializer<Movie>, JsonDeserializer<Movie> {

	/**
	 * Serializes a movie object to JSON.
	 *
	 * The resulting JSON contains the properties
	 *
	 * @param src the movie to serialize
	 * @param typeOfSrc the type of the source object
	 * @param context the JSON serialization context
	 * @return the serialized JSON element
	 */
	@Override
	public JsonElement serialize(Movie src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();

		setMediaAttributesOnJson(object, src, context);
		setTopLevelMediaAttributesOnJson(object, src, context);
		setLeafMediaAttributesOnJson(object, src, context);

		return object;
	}

	/**
	 * Deserializes a movie object from JSON.
	 * A new Movie instance is created
	 *
	 * @param json the JSON element to deserialize
	 * @param typeOfT the target type
	 * @param context the JSON deserialization context
	 * @return the deserialized season
	 * @throws JsonParseException if the JSON structure is invalid
	 */
	@Override
	public Movie deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();

		final Movie movie = new Movie(getAsUUIDOrNull(object, Keys.ID));

		setMediaAttributesFromJson(movie, object, context);
		setTopLevelMediaAttributesFromJson(movie, object, context);
		setLeafMediaAttributesFromJson(movie, object, context);

		return movie;
	}
}
