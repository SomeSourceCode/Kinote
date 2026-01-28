package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

/**
 * An integer argument.
 */
public class IntegerArgument extends Argument<Integer> {

	private final int min;
	private final int max;

	/**
	 * Constructs a new integer argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected IntegerArgument(Builder builder) {
		super(builder);
		this.min = builder.min;
		this.max = builder.max;
	}

	/**
	 * Returns the minimum value.
	 *
	 * @return the minimum value (inclusive)
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Returns the maximum value.
	 *
	 * @return the maximum value (inclusive)
	 */
	public int getMax() {
		return max;
	}

	@Override
	public boolean matches(Token token) {
		if (token.isQuoted()) {
			return false;
		}
		try {
			Integer.parseInt(token.value());
			return true;
		} catch (NumberFormatException exception) {
			return false;
		}
	}

	@Override
	public boolean isValid(Token token) {
		try {
			final int value = Integer.parseInt(token.value());
			return value >= min && value <= max;
		} catch (NumberFormatException exception) {
			return false;
		}
	}

	@Override
	public Integer parse(Token token) {
		try {
			final int value = Integer.parseInt(token.value());
			if (value < min) {
				throw new CommandParseException("Value must be at least " + min);
			}
			if (value > max) {
				throw new CommandParseException("Value must be at most " + max);
			}
			return value;
		} catch (NumberFormatException exception) {
			throw new CommandParseException("Invalid integer value");
		}
	}

	/**
	 * Creates a new integer argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	/**
	 * The builder for integer arguments.
	 */
	public static class Builder extends Argument.Builder<IntegerArgument> {

		private int min = Integer.MIN_VALUE;
		private int max = Integer.MAX_VALUE;

		/**
		 * Constructs a new integer argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name) {
			super(name);
		}

		/**
		 * Sets the minimum required value for the argument.
		 *
		 * @param min the minimum value (inclusive)
		 * @return this builder for chaining
		 */
		public Builder withMin(int min) {
			this.min = min;
			return this;
		}

		/**
		 * Sets the maximum required value for the argument.
		 *
		 * @param max the maximum value (inclusive)
		 * @return this builder for chaining
		 */
		public Builder withMax(int max) {
			this.max = max;
			return this;
		}

		/**
		 * Sets the minimum and maximum required values for the argument.
		 *
		 * @param min the minimum value (inclusive)
		 * @param max the maximum value (inclusive)
		 * @return this builder for chaining
		 */
		public Builder withBounds(int min, int max) {
			this.min = min;
			this.max = max;
			return this;
		}

		@Override
		public IntegerArgument build() {
			return new IntegerArgument(this);
		}

	}

}
