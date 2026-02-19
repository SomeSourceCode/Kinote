package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.List;

/**
 * A boolean argument.
 */
public class BooleanArgument extends Argument<Boolean> {

	/**
	 * Constructs a new boolean argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected BooleanArgument(Builder builder) {
		super(builder);
	}

	@Override
	public boolean matches(Token token) {
		if (token.isQuoted()) {
			return false;
		}
		final String value = token.value().toLowerCase();
		return value.equals("true") || value.equals("false");
	}

	@Override
	public Boolean parse(Token token) {
		final String value = token.value().toLowerCase();
		if (value.equals("true")) {
			return true;
		}
		if (value.equals("false")) {
			return false;
		}
		throw new CommandParseException("Invalid boolean value");
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return List.of("true", "false");
	}

	/**
	 * Creates a new boolean argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	/**
	 * The builder for boolean arguments.
	 */
	public static class Builder extends Argument.Builder<BooleanArgument> {

		/**
		 * Constructs a new boolean argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name) {
			super(name);
		}

		@Override
		public BooleanArgument build() {
			return new BooleanArgument(this);
		}

	}

}
