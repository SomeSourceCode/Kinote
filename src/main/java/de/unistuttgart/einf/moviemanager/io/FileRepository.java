package de.unistuttgart.einf.moviemanager.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A repository to handle read and writing operations from a file.
 *
 * @param <T> the type of data to store and load
 */
public class FileRepository<T> {

	private final Path path;
	private final DataSerializer<T> serializer;

	/**
	 * Constructs a new FileRepository with the given path serializer
	 *
	 * @param path the path
	 * @param serializer the serializer
	 */
	public FileRepository(Path path, DataSerializer<T> serializer) {
		if (path == null) {
			throw new IllegalArgumentException("path must not be null");
		}
		if (serializer == null) {
			throw new IllegalArgumentException("serializer must not be null");
		}

		this.path = path;
		this.serializer = serializer;
	}

	/**
	 * Saves the data to the previously specified file.
	 *
	 * @param data the data
	 */
	public void save(T data) {
		try {
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}
			try (OutputStream out = Files.newOutputStream(path)) {
				serializer.serialize(out, data);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save data to: " + path, e);
		}
	}

	/**
	 * Loads the data from the previously specified file.
	 *
	 * @return the data
	 */
	public T load() {
		if (!Files.exists(path)) {
			return null;
		}
		try (InputStream in = Files.newInputStream(path)) {
			return serializer.deserialize(in);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load data from: " + path, e);
		}
	}

}
