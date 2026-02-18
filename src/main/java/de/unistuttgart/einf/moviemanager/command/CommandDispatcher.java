package de.unistuttgart.einf.moviemanager.command;

import de.unistuttgart.einf.moviemanager.command.argument.LiteralArgument;
import de.unistuttgart.einf.moviemanager.command.argument.MultiLiteralArgument;

import java.util.*;
import java.util.stream.Stream;

/**
 * Dispatches and manages commands, handling their registration, execution, and suggestions.
 */
public class CommandDispatcher {

	private final List<Command> commands = new ArrayList<>();

	/**
	 * Returns an unmodifiable list of all registered commands.
	 *
	 * @return the list of registered commands
	 */
	public List<Command> getRegisteredCommands() {
		return Collections.unmodifiableList(commands);
	}

	/**
	 * Returns a list of all registered command names.
	 *
	 * @return the list of command names
	 */
	public List<String> getRegisteredCommandNames() {
		return commands.stream().map(Command::getName).toList();
	}

	/**
	 * Registers the given command to be considered in execution
	 * and suggestion generation.
	 *
	 * @param command the command to register
	 * @see #register(CommandNode.Builder)
	 */
	public void register(Command command) {
		if (isRegistered(command.getName())) {
			throw new IllegalArgumentException("Command with name '" + command.getName() + "' is already registered.");
		}
		commands.add(command);
	}

	/**
	 * Registers a command built from the given builder.
	 *
	 * @param builder the command builder
	 * @see #register(Command)
	 */
	public void register(CommandNode.Builder<Command> builder) {
		register(builder.build());
	}

	/**
	 * Unregisters the command with the given name
	 *
	 * @param commandName the command name
	 */
	public void unregister(String commandName) {
		commands.removeIf(command -> command.getName().equals(commandName));
	}

	/**
	 * Returns the command with the given name or null, if no such
	 * command is registered.
	 *
	 * @param commandName the command name
	 * @return the command with the given name
	 */
	public Command getCommand(String commandName) {
		return commands.stream()
				.filter(command -> command.getName().equals(commandName))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Returns whether a command with the given name is registered.
	 *
	 * @param commandName the command name
	 * @return true if a command with the given name is registered, false otherwise
	 */
	public boolean isRegistered(String commandName) {
		return commands.stream().anyMatch(command -> command.getName().equals(commandName));
	}

	public record ExecutionResult(boolean success, String message) {}

	public record MatchResult(int depth, CommandNode node, SequencedMap<CommandNode, Token> argsMap) {}

	/**
	 * Tries to the command represented by the given input string.
	 * If a valid command path is found, it will be executed.
	 * <p>
	 * The result indicates whether the execution was successful or
	 * not, along with an optional message providing additional information.
	 *
	 * @param input the command input string
	 * @return the execution result
	 */
	public ExecutionResult execute(String input) {
		if (input == null || input.isBlank()) {
			return new ExecutionResult(true, null);
		}
		List<Token> tokens;
		try {
			tokens = CommandTokenizer.tokenize(input);
		} catch (TokenizationException exception) {
			final String errorMessage = "Failed to parse command input: " + exception.getMessage();
			return new ExecutionResult(false, errorMessage);
		}
		if (tokens.isEmpty()) {
			return new ExecutionResult(true, null);
		}

		MatchResult bestMatch = null;

		for (Command command : commands) {
			if (command.matches(tokens.getFirst())) {
				final SequencedMap<CommandNode, Token> argsMap = new LinkedHashMap<>();
				argsMap.put(command, tokens.getFirst());
				final MatchResult result = findDeepestMatch(command, tokens, 1, argsMap);

				if (result.depth() == tokens.size() && result.node().getExecutor() != null) {
					return executeSuccessfulMatch(tokens, result.node(), result.argsMap());
				}

				if (bestMatch == null || result.depth() > bestMatch.depth()) {
					bestMatch = result;
				}
			}
		}

		if (bestMatch != null) {
			return executeSuccessfulMatch(tokens, bestMatch.node(), bestMatch.argsMap());
		} else {
			final String errorMessage = "Unknown command: " + tokens.getFirst().value();
			return new ExecutionResult(false, errorMessage);
		}
	}

	private MatchResult findDeepestMatch(CommandNode node, List<Token> tokens, int tokenIndex, SequencedMap<CommandNode, Token> argsMap) {
		MatchResult currentBest = new MatchResult(tokenIndex, node, new LinkedHashMap<>(argsMap));

		if (tokenIndex >= tokens.size()) {
			return currentBest;
		}

		final Token currentToken = tokens.get(tokenIndex);

		boolean foundValidChild = false;
		for (CommandNode child : node.getChildren()) {
			if (child.matches(currentToken)) {
				foundValidChild = true;
				argsMap.put(child, currentToken);
				MatchResult branchResult = findDeepestMatch(child, tokens, tokenIndex + 1, argsMap);
				argsMap.remove(child);

				if (branchResult.depth() > currentBest.depth()) {
					currentBest = branchResult;
				}
				if (currentBest.depth() == tokens.size()) {
					break;
				}
			}
		}

		if (!foundValidChild && !node.getChildren().isEmpty()) {
			final CommandNode firstChild = node.getChildren().getFirst();
			argsMap.put(firstChild, currentToken);
			return new MatchResult(tokenIndex + 1, firstChild, new LinkedHashMap<>(argsMap));
		}

		return currentBest;
	}

	public MatchResult findDeepestMatch(List<Token> tokens) {
		if (tokens.isEmpty()) {
			return null;
		}

		MatchResult bestMatch = null;

		for (Command command : commands) {
			if (command.matches(tokens.getFirst())) {
				final SequencedMap<CommandNode, Token> argsMap = new LinkedHashMap<>();
				argsMap.put(command, tokens.getFirst());
				MatchResult result = findDeepestMatch(command, tokens, 1, argsMap);

				if (bestMatch == null || result.depth() > bestMatch.depth()) {
					bestMatch = result;
				}
			}
		}

		return bestMatch;
	}

	private ExecutionResult executeSuccessfulMatch(List<Token> tokens, CommandNode node, SequencedMap<CommandNode, Token> argsMap) {
		final Map<String, Object> parsedArgsMap = new HashMap<>();

		final StringBuilder commandPathBuilder = new StringBuilder();

		for (Map.Entry<CommandNode, Token> entry : argsMap.entrySet()) {
			final CommandNode argument = entry.getKey();
			final Token token = entry.getValue();

			if (!commandPathBuilder.isEmpty()) {
				commandPathBuilder.append(" ");
			}
			commandPathBuilder.append(token.raw());

			try {
				parsedArgsMap.put(argument.getName(), argument.parse(token));
			} catch (CommandParseException exception) {
				final String errorMessage = buildErrorMessage("Invalid argument for command. " + exception.getMessage() + ":", commandPathBuilder.toString());
				return new ExecutionResult(false, errorMessage);
			}
		}

		final Executor executor = node.getExecutor();

		if (executor == null && !node.getChildren().isEmpty()) {
			final String errorMessage = buildErrorMessage("Missing arguments for command:", commandPathBuilder.toString(), false);

			return new ExecutionResult(false, errorMessage);
		}
		if (tokens.size() > argsMap.size()) {
			commandPathBuilder.append(" ").append(tokens.get(argsMap.size()).raw());
			final String errorMessage = buildErrorMessage("Too many arguments for command:", commandPathBuilder.toString());
			return new ExecutionResult(false, errorMessage);
		}

		if (executor == null) {
			return new ExecutionResult(false, "No executor found.");
		}

		try {
			executor.execute(new ExecutionContext(commandPathBuilder.toString(), parsedArgsMap));
			return new ExecutionResult(true, null);
		} catch (CommandExecutionException exception) {
			return new ExecutionResult(false, exception.getMessage());
		} catch (Exception exception) {
			return new ExecutionResult(false, "An error occurred during command execution: " + exception.getMessage());
		}
	}

	private String buildErrorMessage(String message, String command) {
		return buildErrorMessage(message, command, true);
	}

	private String buildErrorMessage(String message, String command, boolean inArg) {
		final StringBuilder errorBuilder = new StringBuilder(message).append(" ");
		final int commandLength = command.length();
		final boolean trim = commandLength > 16;
		if (trim) {
			errorBuilder.append("...");
		}
		errorBuilder.append(command, Math.max(0, commandLength - (trim ? 13 : 16)), commandLength);
		if (inArg) {
			errorBuilder.append("<--[HERE]");
		} else {
			errorBuilder.append(" <--[HERE]");
		}
		return errorBuilder.toString();
	}

	public Set<String> getSuggestions(String input) {
		final Set<String> suggestions = new HashSet<>();
		if (input == null) return suggestions;
		List<Token> tokens;
		try {
			tokens = CommandTokenizer.tokenize(input);
		} catch (TokenizationException exception) {
			return suggestions;
		}

		final boolean inArg = !(input.endsWith(" ") || input.endsWith("\t"));
		if (tokens.isEmpty() || (tokens.size() == 1 && inArg)) {
			suggestions.addAll(commands.stream()
					.map(Command::getName)
					.toList());
			return suggestions;
		}

		for (Command command : commands) {
			if (command.matches(tokens.getFirst())) {
				collectSuggestions(command, tokens, 1, suggestions, inArg);
			}
		}
		return suggestions;
	}

	private void collectSuggestions(CommandNode node, List<Token> tokens, int tokenIndex, Set<String> suggestions, boolean inArg) {
		if (!node.matches(tokens.get(tokenIndex - 1))) return;

		if ((tokenIndex == tokens.size() - 1 && inArg) || tokenIndex >= tokens.size()) {
			for (CommandNode child : node.getChildren()) {
				suggestions.addAll(child.getSuggestions());
			}
			return;
		}

		final Token currentToken = tokens.get(tokenIndex);
		for (CommandNode child : node.getChildren()) {
			if (child.matches(currentToken)) {
				collectSuggestions(child, tokens, tokenIndex + 1, suggestions, inArg);
			}
		}
	}

	private record NodeInfo(CommandNode node, int depth) {}

	/**
	 * Returns a tail tip for the given input, representing the next possible arguments
	 * or literals that can follow the current input.
	 *
	 * @param input the input
	 * @param maxLength the maximum length of the tail tip
	 * @return the tail tip
	 */
	public String getTailTip(String input, int maxLength) {
		if (maxLength < 0) {
			throw new IllegalArgumentException("maxLength must be non-negative");
		}
		if (maxLength == 0) {
			return "";
		}

		final boolean inArg = !input.endsWith(" ");
		if (inArg) {
			return "";
		}

		List<Token> tokens;
		try {
			tokens = CommandTokenizer.tokenize(input);
		} catch (TokenizationException exception) {
			return "";
		}

		if (tokens.isEmpty()) {
			return "";
		}

		final List<CommandNode> matchingTails = new ArrayList<>();
		final Stack<NodeInfo> nodesToCheck = new Stack<>();
		for (Command command : commands) {
			if (command.matches(tokens.getFirst())) {
				nodesToCheck.push(new NodeInfo(command, 1));
				break;
			}
		}

		while (!nodesToCheck.isEmpty()) {
			final NodeInfo info = nodesToCheck.pop();
			final CommandNode node = info.node();
			final int depth = info.depth();

			final Token currentToken = tokens.get(depth - 1);
			if (!node.isValid(currentToken)) {
				continue;
			}

			if (depth == tokens.size()) {
				matchingTails.add(node);
				continue;
			}

			for (CommandNode child : node.getChildren()) {
				nodesToCheck.push(new NodeInfo(child, depth + 1));
			}
		}

		if (matchingTails.isEmpty()) {
			return "";
		}

		final boolean isTipOptional = matchingTails.stream()
				.anyMatch(tail -> tail.getExecutor() != null);
		final List<CommandNode> followingNodes = matchingTails.stream()
				.flatMap(tail -> tail.getChildren().stream())
				.toList();

		if (followingNodes.isEmpty()) {
			return "⏎";
		}

		final List<String> nonLiterals = followingNodes.stream()
				.filter(node -> !(node instanceof LiteralArgument) && !(node instanceof MultiLiteralArgument))
				.map(node -> "<" + node.getName() + ">")
				.distinct()
				.toList();
		final List<String> literals = followingNodes.stream()
				.flatMap(node -> switch (node) {
					case LiteralArgument literalArgument -> Stream.of(literalArgument.getName());
					case MultiLiteralArgument multiLiteralArgument -> multiLiteralArgument.getOptions().stream();
					default -> Stream.empty();
				})
				.distinct()
				.toList();

		final StringBuilder tipBuilder = new StringBuilder();
		if (isTipOptional) {
			tipBuilder.append("[");
		}

		for (String tip : Stream.concat(nonLiterals.stream(), literals.stream()).toList()) {
			if (tipBuilder.length() > (isTipOptional ? 1 : 0)) {
				tipBuilder.append("|");
			}
			tipBuilder.append(tip, 0, Math.min(tip.length(), Math.max(0, maxLength - tipBuilder.length() - 3)));
			if (tipBuilder.length() >= maxLength - 3) {
				return tipBuilder + "...";
			}
		}

		if (isTipOptional) {
			tipBuilder.append("]");
		}
		return tipBuilder.toString();
	}

}
