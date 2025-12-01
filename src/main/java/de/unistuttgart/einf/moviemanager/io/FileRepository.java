package de.unistuttgart.einf.moviemanager.io;

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
		this.path = path;
		this.serializer = serializer;
	}

	/**
	 * Saves the data to the previously specified file.
	 *
	 * @param data the data
	 */
	public void save(T data) {
		// TODO: implement save logic
	}

	/**
	 * Loads the data from the previously specified file.
	 *
	 * @return the data
	 */
	public T load() {
		return null;
	}

}
