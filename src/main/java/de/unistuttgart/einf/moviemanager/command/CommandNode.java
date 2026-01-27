package de.unistuttgart.einf.moviemanager.command;

import de.unistuttgart.einf.moviemanager.command.argument.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * The base class for commands and arguments.
 */
public abstract class CommandNode implements Comparable<CommandNode> {

	private final String name;
	private final List<CommandNode> children = new ArrayList<>();
	private final Executor executor;

	/**
	 * Constructs a new command node from the given builder
	 *
	 * @param builder the builder
	 */
	protected CommandNode(Builder<?> builder) {
		this.name = builder.name;
		this.children.addAll(builder.children);
		Collections.sort(this.children);
		this.executor = builder.executor;
	}

	/**
	 * Returns an unmodifiable list of child command nodes.
	 *
	 * @return the child command nodes
	 */
	public List<CommandNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Returns the name of this command node.
	 *
	 * @return the name of this command node
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the executor associated with this command node,
	 * or null if none is set.
	 *
	 * @return the executor of this command node
	 */
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * Returns whether the token matches the expected format
	 * for this command node.
	 *
	 * @param token the token
	 * @return whether the token matches
	 * @see #isValid(Token)
	 */
	public abstract boolean matches(Token token);

	/**
	 * Returns whether the token represents a valid value for this
	 * command node. Note that this can differ from {@link #matches(Token)} in some
	 * cases (e.g. for integer arguments with bounds).
	 *
	 * @param token the token
	 * @return whether the token is valid
	 * @see #matches(Token)
	 */
	public boolean isValid(Token token) {
		return matches(token);
	}

	/**
	 * Returns the parsed value of the given token.
	 *
	 * @param token the token
	 * @return the parsed value
	 */
	public abstract Object parse(Token token);

	/**
	 * Returns a list of suggestions for this command node.
	 *
	 * @return the suggestions
	 */
	public List<String> getSuggestions() {
		return Collections.emptyList();
	}

	private static final List<Class<? extends CommandNode>> nodePriority = List.of(
			Command.class,
			LiteralArgument.class,
			MultiLiteralArgument.class,
			StringArgument.class
	);

	@Override
	public int compareTo(CommandNode other) {
		if (other == null) {
			return 1;
		}
		int thisPriority = nodePriority.indexOf(this.getClass());
		int otherPriority = nodePriority.indexOf(other.getClass());
		if (thisPriority != otherPriority) {
			return Integer.compare(thisPriority, otherPriority);
		}
		return this.name.compareTo(other.name);
	}

	/**
	 * The builder for command nodes.
	 *
	 * @param <T> the type of command node
	 */
	public static abstract class Builder<T extends CommandNode> {

		private final String name;
		private final Set<CommandNode> children = new HashSet<>();
		private Executor executor;

		/**
		 * Constructs a new command node builder with the given name.
		 *
		 * @param name the name of the command node
		 */
		protected Builder(String name) {
			if (name == null || name.contains(" ")) {
				throw new IllegalArgumentException("Command node name cannot be null or contain spaces");
			}
			this.name = name;
		}

		/**
		 * Adds a child command node to this command node.
		 * <p>
		 * If a child of the same type already exists on this node, an
		 * {@link IllegalArgumentException} is thrown. This excludes
		 * {@link LiteralArgument} and {@link MultiLiteralArgument}, which
		 * are checked for ambiguous names or options instead.
		 * <p>
		 * If a resulting command path would contain multiple arguments with the
		 * same name, an {@link IllegalArgumentException} is also thrown.
		 *
		 * @param child the child node
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if adding the child results in ambiguities
		 * or child is null
		 */
		public Builder<T> then(CommandNode child) {
			if (child == null) {
				throw new IllegalArgumentException("Child command node cannot be null");
			}
			final Stack<CommandNode> childrenToCheck = new Stack<>();
			while (!childrenToCheck.isEmpty()) {
				final CommandNode childToCheck = childrenToCheck.pop();
				if (childToCheck.getName().equals(name)) {
					throw new IllegalArgumentException("Adding child node '" + child.getName() + "' would create duplicate command path on node '" + this.name + "'");
				}
				childrenToCheck.addAll(childToCheck.getChildren());
			}
			if (child instanceof LiteralArgument || child instanceof MultiLiteralArgument) {
				final Stream<String> takenLiteralsStream = children.stream()
						.filter(c -> c instanceof LiteralArgument || c instanceof MultiLiteralArgument)
						.flatMap(argument -> switch (argument) {
							case LiteralArgument literalArgument -> Stream.of(literalArgument.getName());
							case MultiLiteralArgument multiLiteralArgument -> multiLiteralArgument.getOptions().stream();
							default -> Stream.empty();
						});
				switch (child) {
					case LiteralArgument literalArgument -> {
						if (takenLiteralsStream.anyMatch(literal -> literal.equalsIgnoreCase(literalArgument.getName()))) {
							throw new IllegalArgumentException("Literal " + literalArgument.getName() + " is ambiguous on node " + this.name);
						}
					}
					case MultiLiteralArgument multiLiteralArgument -> {
						if (takenLiteralsStream.anyMatch(literal -> multiLiteralArgument.getOptions().stream().anyMatch(option -> option.equalsIgnoreCase(literal)))) {
							throw new IllegalArgumentException("One of the literals " + multiLiteralArgument.getOptions() + " is ambiguous on node " + this.name);
						}
					}
					default -> {}
				}
			} else if (children.stream().anyMatch(node -> node.getClass().isInstance(child) || child.getClass().isInstance(node))) {
				throw new IllegalArgumentException("A child of type '" + child.getClass().getSimpleName() + "' already exists on node '" + this.name + "'");
			}
			children.add(child);
			return this;
		}

		/**
		 * Adds the child command node built by the given builder to this command node.
		 * <p>
		 * See {@link #then(CommandNode)} for details on ambiguity checks.
		 *
		 * @param childBuilder the child node builder
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if adding the child results in ambiguities
		 * or childBuilder is null
		 */
		public Builder<T> then(Builder<?> childBuilder) {
			if (childBuilder == null) {
				throw new IllegalArgumentException("Child command node builder cannot be null");
			}
			return then(childBuilder.build());
		}

		/**
		 * A shorthand for nested {@link #then(Builder)} calls. For example
		 * <pre>
		 * Command.create("command")
		 *         .thenNested(
		 *             IntegerArgument.create("arg1")
		 *             BooleanArgument.create("arg2")
		 *             TopLevelMediaArgument.create("arg3")
		 *         );
		 * </pre>
		 * achieves the same tree structure as
		 * <pre>
		 * Command.create("command")
		 *         .then(IntegerArgument.create("arg1")
		 *                 .then(BooleanArgument.create("arg2")
		 *                         .then(TopLevelMediaArgument.create("arg3"))));
		 * </pre>
		 *
		 * @param childBuilders the child node builders
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if adding any of the children results in ambiguities
		 * or childBuilders is or contains null
		 */
		public Builder<T> thenNested(Builder<?>... childBuilders) {
			if (childBuilders == null) {
				throw new IllegalArgumentException("Child command node builders cannot be null");
			}

			final List<Builder<?>> reversed = new ArrayList<>(Arrays.asList(childBuilders)).reversed();
			reversed.add(this);

			Builder<?> currentBuilder = null;
			for (Builder<?> childBuilder : reversed) {
				if (childBuilder == null) {
					throw new IllegalArgumentException("Child command node builder cannot be null");
				}
				if (currentBuilder != null) {
					childBuilder.then(currentBuilder);
				}
				currentBuilder = childBuilder;
			}
			return this;
		}

		/**
		 * Sets the executor for this command node.
		 *
		 * @param executor the executor
		 * @return this builder for chaining
		 */
		public Builder<T> executes(Executor executor) {
			this.executor = executor;
			return this;
		}

		/**
		 * Builds the command node.
		 *
		 * @return the built command node
		 */
		public abstract T build();

	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(getClass().getSimpleName())
				.append("('").append(name).append("')");

		if (children.isEmpty()) {
			return builder.toString();
		}

		builder.append(" {\n");
		for (CommandNode child : children) {
			final String[] childLines = child.toString().split("\n");
			for (String line : childLines) {
				builder.append("  ").append(line).append("\n");
			}
		}

		return builder.append("}").toString();
	}

}
