package de.unistuttgart.einf.moviemanager.command;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Context for command execution.
 */
public class ExecutionContext {

	private final String rawInput;
	private final Map<String, Object> arguments;

	/**
	 * Constructs a new ExecutionContext.
	 *
	 * @param rawInput the raw input
	 * @param arguments the parsed arguments
	 */
	public ExecutionContext(String rawInput, Map<String, Object> arguments) {
		this.rawInput = rawInput;
		this.arguments = Map.copyOf(arguments);
	}

	/**
	 * Returns the raw input this context was created from.
	 *
	 * @return the raw input
	 */
	public String getRawInput() {
		return rawInput;
	}

	/**
	 * Returns true if this context contains an argument with the given name.
	 *
	 * @param name the name of the argument
	 * @return true if this context contains an argument with the given name
	 */
	public boolean has(String name) {
		return arguments.containsKey(name);
	}

	/**
	 * Returns the value of the argument with the given name,
	 * or null if it does not exist.
	 *
	 * @param name the name of the argument
	 * @return the value of the argument
	 * @see #get(String, Class)
	 */
	public Object get(String name) {
		return arguments.get(name);
	}

	/**
	 * Returns an Optional containing the value of the argument
	 * with the given name, or an empty Optional if it does not exist.
	 *
	 * @param name the name of the argument
	 * @return an Optional containing the value of the argument
	 * @see #getOptional(String, Class)
	 */
	public Optional<Object> getOptional(String name) {
		if (!arguments.containsKey(name)) {
			return Optional.empty();
		}
		return Optional.ofNullable(arguments.get(name));
	}

	/**
	 * Returns the value of the argument with the given name
	 * cast to the given type, or null if it does not exist.
	 *
	 * @param name the name of the argument
	 * @param type the type to cast to
	 * @param <T> the type to cast to
	 * @return the value of the argument
	 * @throws IllegalArgumentException if type is null
	 * @throws ClassCastException if the value cannot be cast to the given type
	 */
	public <T> T get(String name, Class<T> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		return type.cast(arguments.get(name));
	}

	/**
	 * Returns an Optional containing the value of the argument
	 * with the given name cast to the given type, or an empty Optional
	 * if it does not exist.
	 *
	 * @param name the name of the argument
	 * @param type the type to cast to
	 * @param <T> the type to cast to
	 * @return an Optional containing the value of the argument
	 * @throws IllegalArgumentException if type is null
	 * @throws ClassCastException if the value cannot be cast to the given type
	 */
	public <T> Optional<T> getOptional(String name, Class<T> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		if (!arguments.containsKey(name)) {
			return Optional.empty();
		}
		return Optional.ofNullable(type.cast(arguments.get(name)));
	}

	/**
	 * A shorthand for {@link #get(String, Class) get(name, Integer.class)}.
	 *
	 * @param name the name of the argument
	 * @return the value of the argument
	 */
	public int getInt(String name) {
		return get(name, Integer.class);
	}

	/**
	 * A shorthand for {@link #get(String, Class) get(name, Long.class)}.
	 *
	 * @param name the name of the argument
	 * @return the value of the argument
	 */
	public long getLong(String name) {
		return get(name, Long.class);
	}

	/**
	 * A shorthand for {@link #get(String, Class) get(name, Float.class)}.
	 *
	 * @param name the name of the argument
	 * @return the value of the argument
	 */
	public float getFloat(String name) {
		return get(name, Float.class);
	}

	/**
	 * A shorthand for {@link #get(String, Class) get(name, Double.class)}.
	 *
	 * @param name the name of the argument
	 * @return the value of the argument
	 */
	public double getDouble(String name) {
		return get(name, Double.class);
	}

	/**
	 * A shorthand for {@link #get(String, Class) get(name, Boolean.class)}.
	 *
	 * @param name the name of the argument
	 * @return the value of the argument
	 */
	public boolean getBoolean(String name) {
		return get(name, Boolean.class);
	}

	/**
	 * Returns the string representation of the argument with the given name.
	 *
	 * @param name the name of the argument
	 * @return the string representation of the argument
	 */
	public String getString(String name) {
		return Objects.toString(get(name));
	}

}
