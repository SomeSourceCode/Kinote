package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An enum argument.
 *
 * @param <T> the enum type
 */
public class EnumArgument<T extends Enum<T>> extends Argument<T> {

	private final EnumSet<T> options;
	private final Map<String, T> lookupMap;

	/**
	 * Constructs a new enum argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected EnumArgument(Builder<T> builder) {
		super(builder);
		this.options = EnumSet.copyOf(builder.options);
		this.lookupMap = this.options.stream()
				.map(option -> Map.entry(option.toString().toLowerCase(), option))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Returns the available options.
	 *
	 * @return the available options
	 */
	public EnumSet<T> getOptions() {
		return options;
	}

	@Override
	public boolean matches(Token token) {
		return !token.isQuoted() && lookupMap.containsKey(token.value());
	}

	@Override
	public T parse(Token token) {
		T value = lookupMap.get(token.value().toLowerCase());
		if (value == null) {
			throw new CommandParseException("Invalid value");
		}
		return value;
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return options.stream()
				.map(option -> option.toString().toLowerCase())
				.toList();
	}

	/**
	 * Creates a new enum argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @param enumClass the enum class
	 * @return the argument builder
	 * @param <T> the enum type
	 */
	public static <T extends Enum<T>> Builder<T> create(String name, Class<T> enumClass) {
		return new Builder<>(name, enumClass);
	}

	/**
	 * The builder for enum arguments.
	 *
	 * @param <T> the enum type
	 */
	public static class Builder<T extends Enum<T>> extends Argument.Builder<EnumArgument<T>> {

		private final Class<T> enumClass;
		private EnumSet<T> options;

		/**
		 * Constructs a new enum argument builder with the given name
		 * and full set of options of the given enum class.
		 *
		 * @param name the name of the argument
		 * @param enumClass the enum class
		 */
		protected Builder(String name, Class<T> enumClass) {
			if (enumClass == null) {
				throw new IllegalArgumentException("enumClass must be non-null");
			}
			super(name);
			this.options = EnumSet.allOf(enumClass);
			this.enumClass = enumClass;
		}

		/**
		 * Excludes the given options from the current set of options.
		 *
		 * @param options the options to exclude
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if options is null
		 */
		@SafeVarargs
		public final Builder<T> exclude(T... options) {
			if (options == null) {
				throw new IllegalArgumentException("options must be non-null");
			}
			Arrays.asList(options).forEach(this.options::remove);
			return this;
		}

		/**
		 * Excludes the given options from the current set of options.
		 *
		 * @param options the options to exclude
		 * @return this builder for chaining
		  @throws IllegalArgumentException if options is null
		 */
		public Builder<T> exclude(EnumSet<T> options) {
			if (options == null) {
				throw new IllegalArgumentException("options must be non-null");
			}
			this.options.removeAll(options);
			return this;
		}

		/**
		 * Sets the available options to only the given ones.
		 *
		 * @param options the options
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if options is or contains null
		 */
		@SafeVarargs
		public final Builder<T> only(T... options) {
			if (options == null) {
				throw new IllegalArgumentException("options must be non-null");
			}
			if (Arrays.stream(options).anyMatch(Objects::isNull)) {
				throw new IllegalArgumentException("options cannot contain null values");
			}
			this.options = EnumSet.noneOf(enumClass);
			this.options.addAll(Arrays.asList(options));
			return this;
		}

		/**
		 * Sets the available options to only the given ones.
		 *
		 * @param options the options
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if options is or contains null
		 */
		public Builder<T> only(EnumSet<T> options) {
			if (options == null) {
				throw new IllegalArgumentException("options must be non-null");
			}
			this.options = EnumSet.copyOf(options);
			return this;
		}

		@Override
		public EnumArgument<T> build() {
			return new EnumArgument<>(this);
		}

	}

}
