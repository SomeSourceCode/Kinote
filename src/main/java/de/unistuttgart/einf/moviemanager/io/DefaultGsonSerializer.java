package de.unistuttgart.einf.moviemanager.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A generic data serializer for converting objects to and from JSON format.
 *
 * @param <T> the type of the data to serialize/deserialize
 */
public class DefaultGsonSerializer<T> implements DataSerializer<T> {

	private final TypeToken<T> typeToken;
	private final Gson gson;

	private DefaultGsonSerializer(TypeToken<T> typeToken) {
		this.typeToken = typeToken;
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
	}

	/**
	 * Creates a new serializer for a generic type using a TypeToken.
	 *
	 * @param typeToken the type token representing the generic type
	 * @param <T> the type of the data
	 * @return the new serializer
	 */
	public static <T> DefaultGsonSerializer<T> create(TypeToken<T> typeToken) {
		return new DefaultGsonSerializer<>(typeToken);
	}

	/**
	 * Creates a new serializer for a simple class type.
	 *
	 * @param typeClass the class of the type
	 * @param <T> the type of the data
	 * @return the new serializer
	 */
	public static <T> DefaultGsonSerializer<T> createFor(Class<T> typeClass) {
		return new DefaultGsonSerializer<>(TypeToken.get(typeClass));
	}

	/**
	 * Creates a new serializer for a list of a specific element type.
	 *
	 * @param elementClass the class of the list elements
	 * @return the new serializer
	 * @param <E> the type of the list elements
	 */
	@SuppressWarnings("unchecked")
	public static <E> DefaultGsonSerializer<List<E>> createForList(Class<E> elementClass) {
		return new DefaultGsonSerializer<>((TypeToken<List<E>>) TypeToken.getParameterized(List.class, elementClass));
	}

	/**
	 * Creates a new serializer for a map with specific key and value types.
	 *
	 * @param keyClass the class of the map keys
	 * @param valueClass the class of the map values
	 * @return the new serializer
	 * @param <K> the type of the map keys
	 * @param <V> the type of the map values
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> DefaultGsonSerializer<Map<K, V>> createForMap(Class<K> keyClass, Class<V> valueClass) {
		return new DefaultGsonSerializer<>((TypeToken<Map<K, V>>) TypeToken.getParameterized(Map.class, keyClass, valueClass));
	}

	@Override
	public void serialize(OutputStream outputStream, T data) {
		try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			gson.toJson(data, typeToken.getType(), writer);
		} catch (IOException exception) {
			throw new RuntimeException("Failed to serialize JSON", exception);
		}
	}

	@Override
	public T deserialize(InputStream inputStream) {
		try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			return gson.fromJson(reader, typeToken.getType());
		} catch (IOException exception) {
			throw new RuntimeException("Failed to deserialize JSON", exception);
		}
	}

}
