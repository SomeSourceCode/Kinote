package de.unistuttgart.einf.moviemanager.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A serializer.
 *
 * @param <T> the data to serialize
 */
public interface DataSerializer<T> {

	/**
	 * Writes the data to the given output stream.
	 *
	 * @param outputStream the output stream
	 * @param data the data
	 */
	void serialize(OutputStream outputStream, T data);

	/**
	 * Deserializes the data from a given input stream.
	 *
	 * @param data the data
	 * @return the input stream
	 */
	T deserialize(InputStream data);

}
