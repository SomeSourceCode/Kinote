package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

/**
 * A long argument.
 */
public class LongArgument extends Argument<Long> {

	private final long min;
	private final long max;

	/**
	 * Constructs a new long argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected LongArgument(Builder builder) {
		super(builder);
		this.min = builder.min;
		this.max = builder.max;
	}

	/**
	 * Returns the minimum value.
	 *
	 * @return the minimum value (inclusive)
	 */
	public long getMin() {
		return min;
	}

	/**
	 * Returns the maximum value.
	 *
	 * @return the maximum value (inclusive)
	 */
	public long getMax() {
		return max;
	}

	@Override
	public boolean matches(Token token) {
		if (token.isQuoted()) {
			return false;
		}
		try {
			Long.parseLong(token.value());
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
	public Long parse(Token token) {
		try {
			final long value = Integer.parseInt(token.value());
			if (value < min) {
				throw new CommandParseException("Value must be at least " + min);
			}
			if (value > max) {
				throw new CommandParseException("Value must be at most " + max);
			}
			return value;
		} catch (NumberFormatException exception) {
			throw new CommandParseException("Invalid long value");
		}
	}

	/**
	 * Creates a new long argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	/**
	 * The builder for long arguments.
	 */
	public static class Builder extends Argument.Builder<LongArgument> {

		private long min = Long.MIN_VALUE;
		private long max = Long.MAX_VALUE;

		/**
		 * Constructs a new long argument builder with the given name.
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
		public Builder withMin(long min) {
			this.min = min;
			return this;
		}

		/**
		 * Sets the maximum required value for the argument.
		 *
		 * @param max the maximum value (inclusive)
		 * @return this builder for chaining
		 */
		public Builder withMax(long max) {
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
		public Builder withBounds(long min, long max) {
			this.min = min;
			this.max = max;
			return this;
		}

		@Override
		public LongArgument build() {
			return new LongArgument(this);
		}

	}

}
