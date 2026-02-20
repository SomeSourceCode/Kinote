package de.unistuttgart.einf.moviemanager.io;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.unistuttgart.einf.moviemanager.io.adapter.*;
import de.unistuttgart.einf.moviemanager.model.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * JSON-based implementation of the DataSerializer interface using Gson.
 *
 * This serializer is used to store and load a set of top-level media objects
 * (movies and series) as JSON.
 */
public class TopLevelMediaSetSerializer implements DataSerializer<Set<TopLevelMedia>> {

	private static final Type SET_TLM_TYPE = new TypeToken<Set<TopLevelMedia>>() {}.getType();
	private final Gson gson;

	public TopLevelMediaSetSerializer() {
		GsonBuilder builder = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(TopLevelMedia.class, new TopLevelMediaTypeAdapter())
				.registerTypeAdapter(Movie.class, new MovieAdapter())
				.registerTypeAdapter(Series.class, new SeriesAdapter())
				.registerTypeAdapter(Season.class, new SeasonAdapter())
				.registerTypeAdapter(Episode.class, new EpisodeAdapter())
				.addSerializationExclusionStrategy(skipParentField());

		this.gson = builder.create();
	}

	/**
	 * Writes the given media data as JSON to the provided output stream.
	 *
	 * @param outputStream the output stream to write to
	 * @param data the media data to serialize
	 * @throws RuntimeException if serialization fails
	 */
	@Override
	public void serialize(OutputStream outputStream, Set<TopLevelMedia> data) {
		try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			gson.toJson(data, SET_TLM_TYPE, writer);
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize JSON", e);
		}
	}

	/**
	 * Reads media data from the given input stream and deserializes it from JSON.
	 *
	 * @param data the input stream containing JSON data
	 * @return the loaded media data, or null if the input is empty
	 * @throws RuntimeException if deserialization fails
	 */
	@Override
	public Set<TopLevelMedia> deserialize(InputStream data) {
		try (Reader reader = new InputStreamReader(data, StandardCharsets.UTF_8)) {
			Set<TopLevelMedia> loaded = gson.fromJson(reader, SET_TLM_TYPE);
			if (loaded == null) return null;
			return new HashSet<>(loaded);

		} catch (IOException e) {
			throw new RuntimeException("Failed to deserialize JSON", e);
		}
	}

	/**
	 * Creates an exclusion strategy that skips all fields named "parent".
	 *
	 * This prevents cyclic references when serializing hierarchical
	 * media structures.
	 *
	 * @return an exclusion strategy that ignores parent fields
	 */
	private static ExclusionStrategy skipParentField() {
		return new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return f.getName().equals("parent");
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		};
	}


}
