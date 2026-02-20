package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.*;

/**
 * A multi-literal argument.
 */
public class MultiLiteralArgument extends Argument<String> {

	private final Set<String> options;

	/**
	 * Constructs a new multi-literal argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected MultiLiteralArgument(Builder builder) {
		super(builder);
		this.options = Set.copyOf(builder.options);
	}

	/**
	 * Returns the available options
	 *
	 * @return the available options
	 */
	public Set<String> getOptions() {
		return options;
	}

	@Override
	public boolean matches(Token token) {
		return !token.isQuoted() && options.stream().anyMatch(option -> option.equals(token.value()));
	}

	@Override
	public String parse(Token token) {
		if (!options.contains(token.value())) {
			throw new CommandParseException("Invalid option: " + token.value() + ". Valid options are: " + String.join(", ", options));
		}
		return token.value();
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return new ArrayList<>(options);
	}

	/**
	 * Creates a new multi-literal argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	public static class Builder extends Argument.Builder<MultiLiteralArgument> {

		private final Set<String> options = new HashSet<>();

		/**
		 * Constructs a new multi-literal argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		public Builder(String name) {
			super(name);
		}

		/**
		 * Adds an option to the multi-literal argument.
		 *
		 * @param option the option
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if option is null
		 */
		public Builder withOption(String option) {
			if (option == null) {
				throw new IllegalArgumentException("option must be non-null");
			}
			options.add(option);
			return this;
		}

		/**
		 * Adds the options the multi-literal argument.
		 *
		 * @param options the options
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if options is or contains null
		 */
		public Builder withOptions(List<String> options) {
			if (options == null) {
				throw new IllegalArgumentException("options must be non-null");
			}
			if (options.stream().anyMatch(Objects::isNull)) {
				throw new IllegalArgumentException("options must not contain null elements");
			}
			this.options.addAll(options);
			return this;
		}

		/**
		 * Adds the options the multi-literal argument.
		 *
		 * @param options the options
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if options is or contains null
		 */
		public Builder withOptions(String... options) {
			return withOptions(Arrays.asList(options));
		}

		@Override
		public MultiLiteralArgument build() {
			return new MultiLiteralArgument(this);
		}

	}

}
