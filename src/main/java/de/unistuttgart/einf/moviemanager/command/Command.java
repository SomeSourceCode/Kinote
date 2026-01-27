package de.unistuttgart.einf.moviemanager.command;

import java.util.List;

/**
 * The top level command node representing a command.
 */
public class Command extends CommandNode {

	/**
	 * Constructs a new command from the given builder
	 *
	 * @param builder the builder
	 */
	protected Command(Builder builder) {
		super(builder);
	}

	@Override
	public boolean matches(Token token) {
		return getName().equalsIgnoreCase(token.value());
	}

	@Override
	public String parse(Token token) {
		return token.value();
	}

	@Override
	public List<String> getSuggestions() {
		return List.of(getName());
	}

	/**
	 * Creates a new command builder with the given name.
	 *
	 * @param name the name of the command
	 * @return the command builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	/**
	 * The builder for commands.
	 */
	public static class Builder extends CommandNode.Builder<Command> {

		/**
		 * Constructs a new command builder with the given name.
		 *
		 * @param name the name of the command
		 */
		protected Builder(String name) {
			super(name);
		}

		@Override
		public Command build() {
			return new Command(this);
		}

	}

}
