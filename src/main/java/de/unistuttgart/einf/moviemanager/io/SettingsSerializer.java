package de.unistuttgart.einf.moviemanager.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.unistuttgart.einf.moviemanager.model.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class is responsible for serializing and deserializing the settings of the application to and from JSON format.
 */
public class SettingsSerializer implements DataSerializer<Settings> {

	private final Gson gson;

	/**
	 * Creates a new SettingsSerializer.
	 */
	public SettingsSerializer() {
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		gson = gsonBuilder.create();
	}

	/**
	 * Serializes the given settings to the output stream in JSON format. The output will be pretty printed.
	 *
	 * @param outputStream the output stream
	 * @param settings the data
	 */
	@Override
	public void serialize(OutputStream outputStream, Settings settings) {
		try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			gson.toJson(settings, Settings.class, writer);
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
	public Settings deserialize(InputStream data) {
		Reader reader = new InputStreamReader(data, StandardCharsets.UTF_8);
		return gson.fromJson(reader, Settings.class);
	}

}
