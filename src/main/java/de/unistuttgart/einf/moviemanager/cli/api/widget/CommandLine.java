package de.unistuttgart.einf.moviemanager.cli.api.widget;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;
import de.unistuttgart.einf.moviemanager.cli.api.InputResult;
import de.unistuttgart.einf.moviemanager.cli.api.InteractableBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.Scene;
import de.unistuttgart.einf.moviemanager.cli.api.popover.AnchoredPlacement;
import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.CommandNode;
import de.unistuttgart.einf.moviemanager.command.CommandTokenizer;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A command line with syntax highlighting and suggestions.
 */
public class CommandLine extends InteractableBase {

	private final CommandDispatcher dispatcher;

	private String prefix;

	private String content = "";
	private int cursorPos = 0;
	private int contentStart = 0;

	private final Popover suggestionsPopover;
	private final AnchoredPlacement popoverPlacement;
	private final ListView<String> suggestionsListView;

	private Consumer<String> onExecute;
	private Consumer<String> onFail;
	private Consumer<String> onSuccess;

	/**
	 * Constructs a new command line with the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @throws IllegalArgumentException if dispatcher is null
	 */
	public CommandLine(CommandDispatcher dispatcher) {
		if (dispatcher == null) {
			throw new IllegalArgumentException("dispatcher must be non-null");
		}
		this.dispatcher = dispatcher;

		suggestionsListView = new ListView<>();
		suggestionsListView.setFocusable(false);
		suggestionsListView.setAlwaysShowSelection(true);

		popoverPlacement = new AnchoredPlacement(
				this,
				AnchoredPlacement.Side.TOP,
				AnchoredPlacement.Alignment.START
		);

		suggestionsPopover = new Popover(suggestionsListView, popoverPlacement);
		suggestionsPopover.setShowBorders(true);
	}

	/**
	 * Returns the prefix, displayed at the start of the line.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the prefix, displayed at the start of the line.
	 *
	 * @param prefix the prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	private int getContentWidth() {
		return Math.max(0, getInnerWidth() - getPadding().getLeft() - getPadding().getRight() - (prefix == null ? 0 : prefix.length()));
	}

	@Override
	protected void drawContent(Painter painter) {
		final int topPadding = getPadding().getTop();
		final int leftPadding = getPadding().getLeft();
		final int bottomPadding = getPadding().getBottom();
		final int rightPadding = getPadding().getRight();

		final int height = getInnerHeight() - topPadding - bottomPadding;
		final int width = getInnerWidth() - leftPadding - rightPadding;

		if (height <= 0 || width <= 0) {
			return;
		}

		final int prefixWidth = prefix == null ? 0 : prefix.length();
		final int contentStartX = leftPadding + prefixWidth;
		final int contentWidth = width - prefixWidth;

		if (prefixWidth > 0) {
			painter.drawString(toGlobalX(leftPadding), toGlobalY(topPadding), prefixWidth, prefix);
		}

		if (contentWidth <= 0) {
			return;
		}

		final List<ColoredString> highlightedParts = highlight(content);
		int drawPos = 0;

		for (ColoredString part : highlightedParts) {
			final String partContent = part.content();
			final TextColor partColor = part.textColor();

			final int partStartInView = Math.max(0, contentStart - drawPos);
			final int partEndInView = Math.min(partContent.length(), contentStart + contentWidth - drawPos);

			if (partStartInView >= partEndInView) {
				drawPos += partContent.length();
				continue;
			}

			final String visiblePart = partContent.substring(partStartInView, partEndInView);
			painter.drawString(
					toGlobalX(contentStartX + drawPos - contentStart + partStartInView),
					toGlobalY(topPadding),
					contentWidth - (drawPos - contentStart + partStartInView),
					visiblePart,
					partColor
			);

			drawPos += partContent.length();
		}

		final char highlightedChar = cursorPos < content.length() ? content.charAt(cursorPos) : ' ';
		final int cursorScreenPos = cursorPos - contentStart;
		if (cursorScreenPos >= 0 && cursorScreenPos < contentWidth) {
			painter.drawChar(
					toGlobalX(contentStartX + cursorScreenPos),
					toGlobalY(topPadding),
					highlightedChar,
					TextColor.ANSI.BLACK_BRIGHT,
					TextColor.ANSI.WHITE
			);
		}

		if (cursorPos == content.length()) {
			final int maxLength = Math.max(0, contentWidth - cursorScreenPos - 1);
			final String tailTip = dispatcher.getTailTip(content, maxLength);
			if (tailTip != null && !tailTip.isBlank()) {
				painter.drawString(
						toGlobalX(contentStartX + cursorScreenPos),
						toGlobalY(0),
						maxLength,
						tailTip,
						TextColor.ANSI.BLACK_BRIGHT
				);
				painter.drawChar(
						toGlobalX(contentStartX + cursorScreenPos),
						toGlobalY(topPadding),
						tailTip.charAt(0),
						TextColor.ANSI.BLACK_BRIGHT,
						TextColor.ANSI.WHITE_BRIGHT
				);
			}
		}
	}

	/* *************************************************************** *
	 *                         Command Suggestions 					   *
	 * *************************************************************** */

	private enum SuggestionState {
		HIDDEN,
		SHOWN,
		TEMPORARILY_DISABLED
	}

	private SuggestionState suggestionState = SuggestionState.HIDDEN;
	private String originalContent;

	private String lastCompletedContent;
	private List<String> cachedSuggestions = Collections.emptyList();

	private List<String> getSuggestions() {
		if (Objects.equals(content, lastCompletedContent)) {
			return cachedSuggestions;
		}

		if (content.endsWith("\"")) {
			return Collections.emptyList();
		}

		if (content.endsWith(" ")) {
			final List<String> suggestions = dispatcher.getSuggestions(content).stream()
					.sorted()
					.toList();

			lastCompletedContent = content;
			cachedSuggestions = suggestions;
			return suggestions;
		}

		final int lastSpaceIndex = content.lastIndexOf(' ');
		final String lastArg = lastSpaceIndex == -1
				? content
				: content.substring(lastSpaceIndex + 1);
		final String lowercaseLastArg = lastArg.toLowerCase();

		final List<String> suggestions = dispatcher.getSuggestions(content).stream()
				.filter(suggestion -> {
					final String lowercaseSuggestion = suggestion.toLowerCase();

					int currentIndex = 0;
					for (char c : lowercaseLastArg.toCharArray()) {
						currentIndex = lowercaseSuggestion.indexOf(c, currentIndex);
						if (currentIndex == -1) {
							return false;
						}
						currentIndex++;
					}
					return true;
				})
				.sorted((s1, s2) -> {
					final boolean s1StartMatches = s1.toLowerCase().startsWith(lowercaseLastArg);
					final boolean s2StartMatches = s2.toLowerCase().startsWith(lowercaseLastArg);
					if (s1StartMatches && !s2StartMatches) {
						return -1;
					}
					if (!s1StartMatches && s2StartMatches) {
						return 1;
					}
					return s1.compareTo(s2);
				})
				.toList();

		lastCompletedContent = content;
		cachedSuggestions = suggestions;
		return suggestions;
	}

	private void showSuggestions() {
		final SuggestionState previousState = suggestionState;

		final Scene scene = getScene();
		if (scene == null) {
			return;
		}

		final List<String> suggestions = getSuggestions();
		if (suggestions.isEmpty()) {
			scene.closePopover(suggestionsPopover);
			return;
		}

		suggestionState = SuggestionState.SHOWN;

		suggestionsListView.setItems(suggestions);
		if (previousState == SuggestionState.HIDDEN) {
			suggestionsListView.removeSelection();
		}
		suggestionsListView.setWidth(suggestions.stream().mapToInt(String::length).max().orElse(10));
		suggestionsListView.setHeight(Math.min(suggestions.size(), 8));

		final String baseContent = originalContent == null ? content : originalContent;
		final int lastArgLength = baseContent.endsWith(" ") ? 0 : content.length() - baseContent.lastIndexOf(' ') - 1;
		final int cursorScreenPos = cursorPos - contentStart + getPadding().getLeft() + (prefix == null ? 0 : prefix.length());
		popoverPlacement.setOffsetX(cursorScreenPos - lastArgLength - 1); // -1 for border
		scene.showPopover(suggestionsPopover);
	}

	private void restoreOriginalContent() {
		hideSuggestions();
		if (originalContent == null) {
			return;
		}
		content = originalContent;
		cursorPos = content.length();
		originalContent = null;
	}

	private void hideSuggestions() {
		suggestionState = SuggestionState.HIDDEN;
		final Scene scene = getScene();
		if (scene != null) {
			scene.closePopover(suggestionsPopover);
		}
	}

	private void cycleSuggestion(boolean forward) {
		if (suggestionState != SuggestionState.SHOWN) {
			return;
		}
		if (forward) {
			suggestionsListView.cycleDown();
		} else {
			suggestionsListView.cycleUp();
		}
		final String selectedSuggestion = suggestionsListView.getSelectedItem();
		if (selectedSuggestion == null) {
			return;
		}

		final int lastOriginalSpaceIndex = originalContent.lastIndexOf(' ');
		if (lastOriginalSpaceIndex == -1) {
			content = selectedSuggestion;
		} else {
			content = content.substring(0, lastOriginalSpaceIndex + 1) + selectedSuggestion;
		}
		cursorPos = content.length(); // TODO: cursorPos setter that adjusts contentStart
		contentStart = Math.max(0, cursorPos - getContentWidth() + 1);
	}

	private void nextSuggestion() {
		cycleSuggestion(true);
	}

	private void previousSuggestion() {
		cycleSuggestion(false);
	}

	private void showSuggestionsOrNext() {
		if (suggestionState != SuggestionState.SHOWN) {
			originalContent = content;
			showSuggestions();
		} else {
			nextSuggestion();
		}
	}

	private void showSuggestionsOrPrevious() {
		if (suggestionState != SuggestionState.SHOWN) {
			originalContent = content;
			showSuggestions();
		} else {
			previousSuggestion();
		}
	}

	/* *************************************************************** *
	 *                          Command History                        *
	 * *************************************************************** */

	private final List<String> history = new ArrayList<>();
	private final Map<Integer, String> modifiedHistory = new HashMap<>();
	private int historyPos = -1;

	private void moveHistory(int index) {
		index = Math.max(-1, Math.min(index, history.size() - 1));
		if (historyPos == index) {
			return;
		}

		hideSuggestions();

		if (content != null) {
			modifiedHistory.put(historyPos, content);
		}
		historyPos = index;
		content = modifiedHistory.getOrDefault(historyPos, historyPos == -1 ? "" : history.get(historyPos));
		if (content == null) {
			content = "";
		}

		cursorPos = content.length();
	}

	private void moveHistoryUp() {
		if (historyPos == -1) {
			moveHistory(history.size() - 1);
			return;
		}
		moveHistory(Math.max(0, historyPos - 1));
	}

	private void moveHistoryDown() {
		if (historyPos == -1) {
			return;
		}
		if (historyPos == history.size() - 1) {
			moveHistory(-1);
			return;
		}
		moveHistory(historyPos + 1);
	}

	/**
	 * Adds the given command to the history. This method does not add empty or null commands,
	 * and does not add duplicate consecutive entries.
	 *
	 * @param command the command to add
	 */
	public void addToHistory(String command) {
		if (command == null || command.isBlank()) {
			return;
		}
		if (!history.isEmpty() && history.getLast().equals(command)) {
			return;
		}
		history.add(command);
	}

	/**
	 * Adds the given commands to the history. This method does not add empty or null commands,
	 * and does not add duplicate consecutive entries.
	 *
	 * @param commands the commands to add
	 */
	public void addToHistory(Collection<String> commands) {
		for (String command : commands) {
			addToHistory(command);
		}
	}

	/* *************************************************************** *
	 *                         Syntax Highlighting                     *
	 * *************************************************************** */

	private record ColoredString(String content, TextColor textColor) {}

	private static final TextColor COMMAND_TEXT_COLOR = TextColor.ANSI.WHITE_BRIGHT;
	private static final TextColor[] ARGUMENT_TEXT_COLORS = {
			TextColor.ANSI.CYAN,
			TextColor.ANSI.GREEN,
			TextColor.ANSI.YELLOW,
	};
	private static final TextColor ERROR_TEXT_COLOR = TextColor.ANSI.RED_BRIGHT;

	private String lastHighlightedContent;
	private List<ColoredString> cachedHighlightedContent;

	private List<ColoredString> highlight(String content) {
		if (content == null) {
			throw new IllegalArgumentException("content must be non-null");
		}

		if (content.equals(lastHighlightedContent)) {
			return cachedHighlightedContent;
		}

		final List<ColoredString> parts = new ArrayList<>();

		List<Token> tokens;
		try {
			tokens = CommandTokenizer.tokenize(content);
		} catch (Exception e) {
			parts.add(new ColoredString(content, ERROR_TEXT_COLOR));

			lastHighlightedContent = content;
			cachedHighlightedContent = parts;
			return parts;
		}

		final CommandDispatcher.MatchResult deepestMatch = dispatcher.findDeepestMatch(tokens);
		if (deepestMatch == null) {
			parts.add(new ColoredString(content, ERROR_TEXT_COLOR));

			lastHighlightedContent = content;
			cachedHighlightedContent = parts;
			return parts;
		}

		final Map<Token, CommandNode> tokenToNode = deepestMatch.argsMap().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		int startIndex = 0;
		for (int i = 0; i < tokens.size(); i++) {
			final Token token = tokens.get(i);
			final CommandNode node = tokenToNode.get(token);

			final TextColor color;
			if (node == null || !node.isValid(token)) {
				parts.add(new ColoredString(content.substring(startIndex), ERROR_TEXT_COLOR));
				return parts;
			} else if (i == 0) {
				color = COMMAND_TEXT_COLOR;
			} else {
				final int styleIndex = (i - 1) % ARGUMENT_TEXT_COLORS.length;
				color = ARGUMENT_TEXT_COLORS[styleIndex];
			}

			parts.add(new ColoredString(content.substring(startIndex, token.endIndex()), color));
			startIndex = token.endIndex();
		}

		lastHighlightedContent = content;
		cachedHighlightedContent = parts;
		return parts;
	}

	/* *************************************************************** *
	 *                       Input / Navigation                        *
	 * *************************************************************** */

	/**
	 * Reset the command line's state and clears the buffer.
	 */
	public void clear() {
		content = "";
		originalContent = "";

		cursorPos = 0;
		contentStart = 0;

		historyPos = -1;
		modifiedHistory.clear();

		hideSuggestions();
	}

	/**
	 * Moves the cursor to the left.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveCursorLeft() {
		if (cursorPos == 0) {
			return false;
		}
		cursorPos--;
		final int cursorScreenPos = cursorPos - contentStart;
		if (cursorScreenPos < 0) {
			contentStart -= getContentWidth() / 3;
			contentStart = Math.max(0, contentStart);
		}
		return true;
	}

	/**
	 * Moves the cursor to the right.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveCursorRight() {
		if (cursorPos == content.length()) {
			return false;
		}
		cursorPos++;
		final int cursorScreenPos = cursorPos - contentStart;
		if (cursorScreenPos >= getContentWidth()) {
			contentStart++;
		}
		return true;
	}

	/**
	 * Moves the cursor to the start.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveCursorStart() {
		if (cursorPos == 0) {
			return false;
		}
		cursorPos = 0;
		contentStart = 0;
		return true;
	}

	/**
	 * Moves the cursor to the end.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveCursorEnd() {
		final int endPos = content.length();
		if (cursorPos == endPos) {
			return false;
		}
		cursorPos = endPos;
		contentStart = Math.max(0, content.length() - getContentWidth() + 1);
		return true;
	}

	/**
	 * Moves the cursor one word to the right.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveWordRight() {
		boolean posChanged = false;
		while (cursorPos < content.length() && content.charAt(cursorPos) == ' ') {
			posChanged |= moveCursorRight();
		}
		while (cursorPos < content.length() && content.charAt(cursorPos) != ' ') {
			posChanged |= moveCursorRight();
		}
		return posChanged;
	}

	/**
	 * Moves the cursor one word to the left.
	 *
	 * @return whether the cursor position changed
	 */
	public boolean moveWordLeft() {
		boolean posChanged = false;
		while (cursorPos > 0 && content.charAt(cursorPos - 1) == ' ') {
			posChanged |= moveCursorLeft();
		}
		while (cursorPos > 0 && content.charAt(cursorPos - 1) != ' ') {
			posChanged |= moveCursorLeft();
		}
		return posChanged;
	}

	/**
	 * Insets the given character at the current cursor position.
	 *
	 * @param character the character
	 */
	public void insertCharacter(char character) {
		if (cursorPos == content.length()) {
			content += character;
		} else {
			content = content.substring(0, cursorPos) + character + content.substring(cursorPos);
		}
		moveCursorRight();
	}

	/**
	 * Deletes the character to the left of the cursor.
	 */
	public void delete() {
		if (cursorPos == 0) {
			return;
		}
		if (cursorPos == content.length()) {
			content = content.substring(0, content.length() - 1);
		} else {
			content = content.substring(0, cursorPos - 1) + content.substring(cursorPos);
		}
		moveCursorLeft();
	}

	/**
	 * Deletes the character to the right of the cursor.
	 */
	public void deleteRight() {
		if (cursorPos == content.length()) {
			return;
		}
		if (cursorPos == content.length() - 1) {
			content = content.substring(0, content.length() - 1);
		} else {
			content = content.substring(0, cursorPos) + content.substring(cursorPos + 1);
		}
	}

	/**
	 * Returns the consumer that is called when a command is executed. It takes the executed command as parameter.
	 *
	 * @return the consumer
	 */
	public Consumer<String> getOnExecute() {
		return onExecute;
	}

	/**
	 * Sets the consumer that is called when a command is executed. It takes the executed command as parameter.
	 *
	 * @param onExecute the consumer
	 */
	public void setOnExecute(Consumer<String> onExecute) {
		this.onExecute = onExecute;
	}

	/**
	 * Fires the consumer set by {@link #setOnExecute(Consumer)}.
	 *
	 * @param command the executed command
	 */
	public void fireOnExecute(String command) {
		if (onExecute == null) {
			return;
		}
		onExecute.accept(command);
	}

	/**
	 * Returns the consumer that is called when a command
	 * execution fails.
	 *
	 * @return the consumer
	 */
	public Consumer<String> getOnFail() {
		return onFail;
	}

	/**
	 * Sets the consumer that is called when a command
	 * execution fails. It takes an error message as parameter.
	 *
	 * @param onFail the consumer.
	 */
	public void setOnFail(Consumer<String> onFail) {
		this.onFail = onFail;
	}

	/**
	 * Fires the consumer set by {@link #setOnFail(Consumer)}.
	 *
	 * @param message the error message
	 */
	public void fireOnFail(String message) {
		if (onFail == null) {
			return;
		}
		onFail.accept(message);
	}

	/**
	 * Returns the consumer that is called when a command
	 * executes successfully.
	 *
	 * @return the consumer
	 */
	public Consumer<String> getOnSuccess() {
		return onSuccess;
	}

	/**
	 * Sets the consumer that is called when a command
	 * executes successfully. It takes an optional message as parameter.
	 *
	 * @param onSuccess the consumer
	 */
	public void setOnSuccess(Consumer<String> onSuccess) {
		this.onSuccess = onSuccess;
	}

	/**
	 * Fires the consumer set by {@link #setOnSuccess(Consumer)}.
	 *
	 * @param message the consumer
	 */
	public void fireOnSuccess(String message) {
		if (onSuccess == null) {
			return;
		}
		onSuccess.accept(message);
	}

	private void executeCurrentCommand() {
		final CommandDispatcher.ExecutionResult result = dispatcher.execute(content);

		fireOnExecute(content);
		if (result.success()) {
			fireOnSuccess(result.message());
		} else {
			fireOnFail(result.message());
		}

		addToHistory(content);
		clear();
	}

	@Override
	public InputResult handleInput(KeyStroke key, CliMode mode) {
		if (mode != CliMode.COMMAND) {
			return InputResult.UNHANDLED;
		}

		switch (key.getKeyType()) {
			case Escape -> {
				if (suggestionState == SuggestionState.SHOWN) {
					restoreOriginalContent();
					suggestionState = SuggestionState.TEMPORARILY_DISABLED;
					suggestionsListView.removeSelection();
					return InputResult.HANDLED;
				}
				clear();
				return InputResult.LEAVE_COMMAND_MODE;
			}
			case ArrowLeft -> {
				if (moveCursorLeft()) {
					hideSuggestions();
				}
			}
			case ArrowRight -> {
				if (moveCursorRight()) {
					hideSuggestions();
				}
			}
			case Home -> {
				if (moveCursorStart()) {
					hideSuggestions();
				}
			}
			case End -> {
				if (moveCursorEnd()) {
					hideSuggestions();
				}
			}
			case Backspace -> {
				delete();
				if (suggestionState != SuggestionState.TEMPORARILY_DISABLED || content.isEmpty() || content.endsWith(" ")) {
					hideSuggestions();
				}
			}
			case Delete -> {
				deleteRight();
				hideSuggestions();
			}
			case Enter -> {
				if (suggestionState == SuggestionState.SHOWN && !suggestionsListView.getItems().isEmpty()) {
					hideSuggestions();
					originalContent = null;
					return InputResult.HANDLED;
				}
				executeCurrentCommand();
				return InputResult.LEAVE_COMMAND_MODE;
			}
			case Tab -> {
				showSuggestionsOrNext();
			}
			case ArrowDown -> {
				if (suggestionState == SuggestionState.SHOWN) {
					nextSuggestion();
					return InputResult.HANDLED;
				}
				moveHistoryDown();
			}
			case ReverseTab -> {
				showSuggestionsOrPrevious();
			}
			case ArrowUp -> {
				if (suggestionState == SuggestionState.SHOWN) {
					previousSuggestion();
					return InputResult.HANDLED;
				}
				moveHistoryUp();
			}
			case Character -> {
				final char character = key.getCharacter();
				final boolean isAltDown = key.isAltDown();
				if (isAltDown) {
					switch (character) {
						case 'b' -> { // alt + left arrow
							if (moveWordLeft()) {
								hideSuggestions();
							}
						}
						case 'f' -> { // alt + right arrow
							if (moveWordRight()) {
								hideSuggestions();
							}
						}
					}
					return InputResult.HANDLED;
				}

				insertCharacter(character);

				if (character == ' ') {
					originalContent = null;
					hideSuggestions();
				} else {
					originalContent = content;
					if (suggestionState != SuggestionState.TEMPORARILY_DISABLED) {
						showSuggestions();
					}
				}
			}
			default -> {
				return InputResult.UNHANDLED;
			}
		}
		return InputResult.HANDLED;
	}

}
