package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.io.FileRepository;

import java.util.LinkedList;
import java.util.List;

/**
 * The service managing the persistent command history.
 */
public class CommandHistoryService {

	private static final int MAX_HISTORY_SIZE = 100;

	private final FileRepository<List<String>> repository;
	private List<String> commandHistory;

	/**
	 * Constructs a new command history service with the given repository.
	 *
	 * @param repository the repository
	 */
	public CommandHistoryService(FileRepository<List<String>> repository) {
		if (repository == null) {
			throw new IllegalArgumentException("repository must not be null");
		}
		this.repository = repository;
		reload();
	}

	/**
	 * Reloads the list from the repository. If there is no data in the repository,
	 * the command history is reset to an empty list.
	 */
	public void reload() {
		final List<String> loadedHistory = repository.load();
		if (loadedHistory == null) {
			commandHistory = new LinkedList<>();
			return;
		}
		commandHistory = new LinkedList<>(loadedHistory);
	}

	/**
	 * Saves the command history to the repository.
	 */
	public void save() {
		repository.save(commandHistory);
	}

	/**
	 * Returns an unmodifiable copy of the command history.
	 *
	 * @return the command history
	 */
	public List<String> getCommandHistory() {
		return List.copyOf(commandHistory);
	}

	/**
	 * Adds a command to the history. If the command is null, blank, or the same as the last command,
	 * it is not added. If adding the command exceeds the maximum history size, the oldest command is removed.
	 *
	 * @param command the command to add
	 */
	public void addCommand(String command) {
		if (command == null || command.isBlank()) {
			return;
		}
		if (!commandHistory.isEmpty() && commandHistory.getLast().equals(command)) {
			return;
		}

		commandHistory.add(command);
		if (commandHistory.size() > MAX_HISTORY_SIZE) {
			commandHistory.removeFirst();
		}
	}

}
