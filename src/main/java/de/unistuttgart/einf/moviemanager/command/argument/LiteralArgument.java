package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.List;

/**
 * A literal argument.
 */
public class LiteralArgument extends Argument<String> {

	/**
	 * Constructs a new literal argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected LiteralArgument(Builder builder) {
		super(builder);
	}

	@Override
	public boolean matches(Token token) {
		return !token.isQuoted() && getName().equals(token.value());
	}

	@Override
	public String parse(Token token) {
		if (!getName().equals(token.value())) {
			throw new CommandParseException("Invalid literal: " + token.value() + ", expected: " + getName());
		}
		return token.value();
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return List.of(getName());
	}

	/**
	 * Creates a new literal argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	/**
	 * The builder for literal arguments.
	 */
	public static class Builder extends Argument.Builder<LiteralArgument> {

		/**
		 * Constructs a new literal argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name) {
			super(name);
		}

		@Override
		public LiteralArgument build() {
			return new LiteralArgument(this);
		}

	}

}
