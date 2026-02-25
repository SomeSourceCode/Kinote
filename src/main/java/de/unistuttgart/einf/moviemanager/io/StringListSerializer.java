package de.unistuttgart.einf.moviemanager.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A data serializer for lists of strings to JSON format.
 */
public class StringListSerializer implements DataSerializer<List<String>> {

	private static final TypeToken<List<String>> STRING_LIST_TYPE = new TypeToken<>() {};

	private final Gson gson;

	/**
	 * Creates a new string list serializer.
	 */
	public StringListSerializer() {
		gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
	}

	/**
	 * Serializes the given list to the output stream in JSON format.
	 *
	 * @param outputStream the output stream
	 * @param list the data
	 */
	@Override
	public void serialize(OutputStream outputStream, List<String> list) {
		try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			gson.toJson(list, writer);
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize JSON", e);
		}
	}

	/**
	 * Deserializes the settings from the input stream. The input is expected to be in JSON format.
	 *
	 * @param data the data
	 * @return the deserialized settings
	 */
	@Override
	public List<String> deserialize(InputStream data) {
		final Reader reader = new InputStreamReader(data, StandardCharsets.UTF_8);
		return gson.fromJson(reader, STRING_LIST_TYPE);
	}

}
