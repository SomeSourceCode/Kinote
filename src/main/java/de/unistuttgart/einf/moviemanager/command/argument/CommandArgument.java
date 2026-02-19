package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.List;

public class CommandArgument extends Argument<Command> {

	private final CommandDispatcher dispatcher;

	protected CommandArgument(Builder builder) {
		super(builder);
		this.dispatcher = builder.dispatcher;
	}

	@Override
	public boolean matches(Token token) {
		return dispatcher.getRegisteredCommands().stream()
				.anyMatch(command -> command.matches(token));
	}

	@Override
	public Command parse(Token token) {
		return dispatcher.getRegisteredCommands().stream()
				.filter(cmd -> cmd.matches(token))
				.findFirst()
				.orElseThrow(() -> new CommandParseException("Invalid command name"));
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return dispatcher.getRegisteredCommandNames();
	}

	public static Builder create(String name, CommandDispatcher dispatcher) {
		return new Builder(name, dispatcher);
	}

	public static class Builder extends Argument.Builder<CommandArgument> {

		private final CommandDispatcher dispatcher;

		public Builder(String name, CommandDispatcher dispatcher) {
			super(name);
			if (dispatcher == null) {
				throw new IllegalArgumentException("CommandDispatcher cannot be null");
			}
			this.dispatcher = dispatcher;
		}

		@Override
		public CommandArgument build() {
			return new CommandArgument(this);
		}

	}

}
