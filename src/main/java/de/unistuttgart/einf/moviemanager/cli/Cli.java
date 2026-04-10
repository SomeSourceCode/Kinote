package de.unistuttgart.einf.moviemanager.cli;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TabBehaviour;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import de.unistuttgart.einf.moviemanager.cli.api.*;
import de.unistuttgart.einf.moviemanager.cli.api.layout.HorizontalBorderPane;
import de.unistuttgart.einf.moviemanager.cli.api.layout.VerticalBorderPane;
import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;
import de.unistuttgart.einf.moviemanager.cli.api.widget.CommandLine;
import de.unistuttgart.einf.moviemanager.cli.api.widget.ConfirmationDialog;
import de.unistuttgart.einf.moviemanager.cli.api.widget.Text;
import de.unistuttgart.einf.moviemanager.cli.commands.Commands;
import de.unistuttgart.einf.moviemanager.cli.page.DetailsPage;
import de.unistuttgart.einf.moviemanager.cli.page.OverviewPage;
import de.unistuttgart.einf.moviemanager.cli.page.Page;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.dbimport.TmdbClient;
import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.service.CommandHistoryService;
import de.unistuttgart.einf.moviemanager.service.MediaService;
import de.unistuttgart.einf.moviemanager.service.SettingsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class Cli {

	private static final int FRAME_TIME_MS = 100;
	private static final int INPUT_POLL_DELAY_MS = 10;

	private volatile boolean running = false;
	private CliMode mode = CliMode.NAVIGATION;

	private final Scene scene;

	// services
	private final MediaService mediaService;
	private final SettingsService settingsService;
	private final CommandHistoryService commandHistoryService;

	private final TmdbClient tmdbClient;

	// components
	private final VerticalBorderPane mainContainer;
	private final Text dateDisplay;
	private final Text navigationBar;
	private final CommandLine commandLine;

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	// pages
	private final OverviewPage overviewPage;

	private Page currentPage;
	private final Deque<Page> pageStack = new ArrayDeque<>();

	/**
	 * Constructs a new Cli for the given services and tmdb client.
	 *
	 * @param mediaService the media service
	 * @param settingsService the settings service
	 * @param commandHistoryService the command history service
	 * @throws IllegalArgumentException if any service or the tmdb client is null
	 */
	public Cli(MediaService mediaService, SettingsService settingsService, CommandHistoryService commandHistoryService, TmdbClient tmdbClient) {
		if (mediaService == null) {
			throw new IllegalArgumentException("mediaService must be non-null");
		}
		this.mediaService = mediaService;
		if (settingsService == null) {
			throw new IllegalArgumentException("settingsService must be non-null");
		}
		this.settingsService = settingsService;
		if (commandHistoryService == null) {
			throw new IllegalArgumentException("commandHistoryService must be non-null");
		}
		this.commandHistoryService = commandHistoryService;
		if (tmdbClient == null) {
			throw new IllegalArgumentException("tmdbClient must be non-null");
		}
		this.tmdbClient = tmdbClient;

		scene = new Scene();

		// layout
		final VerticalBorderPane rootLayout = new VerticalBorderPane();
		scene.setRoot(rootLayout);

		mainContainer = new VerticalBorderPane();
		mainContainer.setShowBorders(true);
		mainContainer.setShowSeparators(true);
		rootLayout.setCenter(mainContainer);

		// title bar (title and date)
		final HorizontalBorderPane titleBar = new HorizontalBorderPane();
		titleBar.setPadding(new Insets(0, 1));
		titleBar.setHeight(1);
		mainContainer.setTop(titleBar);

		final Text titleDisplay = new Text("Kinote (MoVim)");
		titleBar.setLeft(titleDisplay);

		dateDisplay = new Text();
		titleBar.setRight(dateDisplay);

		// navigation bar (displays current mode and hints)
		navigationBar = new Text(mode.name());
		navigationBar.setWrapping(true);
		navigationBar.setPadding(new Insets(0, 1));
		mainContainer.setBottom(navigationBar);

		// pages (the main ui part)
		overviewPage = new OverviewPage(this, mediaService);
		overviewPage.setOnMediaSelected((media, _) -> navigateTo(new DetailsPage(this, media)));
		mainContainer.setCenter(overviewPage);
		currentPage = overviewPage;

		// info bar hook
		scene.getFocusManager().setOnFocusChange((_, newFocus) -> {
			updateInfoBar(newFocus);
			return true;
		});

		// command line
		final CommandDispatcher dispatcher = Commands.createDispatcher(this);

		commandLine = new CommandLine(dispatcher);
		commandLine.setHidden(true);
		commandLine.setHeight(1);
		commandLine.setPrefix(":");
		commandLine.setPadding(new Insets(0, 2));

		commandLine.addToHistory(commandHistoryService.getCommandHistory());
		commandLine.setOnExecute(commandHistoryService::addCommand);
		commandLine.setOnFail(this::showInfoDialog);

		rootLayout.setBottom(commandLine);

		// initial state
		scene.getFocusManager().ensureValidFocus();
		updateInfoBar(scene.getFocusManager().getCurrentFocus());
	}

	/**
	 * Returns whether the cli is running.
	 *
	 * @return whether the cli is running
	 * @see #run()
	 * @see #stop()
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Stops the cli.
	 */
	public void stop() {
		running = false;
	}

	/**
	 * Starts the cli. This method blocks until the cli is stopped.
	 */
	public void run() {
		running = true;

		try (
				Terminal terminal = new DefaultTerminalFactory().createTerminal();
				Screen screen = new TerminalScreen(terminal)
		) {
			screen.setTabBehaviour(TabBehaviour.CONVERT_TO_ONE_SPACE);
			screen.setCursorPosition(null);
			screen.startScreen();

			final Painter painter = new Painter(screen);
			long time = System.currentTimeMillis();

			while (running) {
				KeyStroke key;
				boolean keyHandled = false;

				while ((key = terminal.pollInput()) != null) {
					keyHandled |= handleGlobalInput(key);
				}

				if (screen.doResizeIfNecessary() != null || System.currentTimeMillis() - time > FRAME_TIME_MS || keyHandled) {
					time = System.currentTimeMillis();

					updateDateText();

					screen.clear();
					scene.update(screen.getTerminalSize().getColumns(), screen.getTerminalSize().getRows(), painter);
					screen.refresh();
				}

				try {
					Thread.sleep(INPUT_POLL_DELAY_MS);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void updateDateText() {
		final String dateString = LocalDateTime.now().format(dateTimeFormatter);
		dateDisplay.setText(dateString);
	}

	private void updateInfoBar(Interactable focused) {
		navigationBar.setText(switch (mode) {
			case COMMAND -> "COMMAND - Enter to submit, Esc to exit";
			case NAVIGATION -> {
				if (focused == null || !focused.isEditable()) {
					yield "NAVIGATION - j/k, h/l to navigate, : to enter command mode";
				}
				yield "NAVIGATION - j/k, h/l to navigate, e to edit, : to enter command mode";
			}
			case EDIT -> "EDIT - Type to edit, Esc to exit";
		});
	}

	/**
	 * Returns the media service.
	 *
	 * @return the media service
	 */
	public MediaService getMediaService() {
		return mediaService;
	}

	/**
	 * Returns the settings service.
	 *
	 * @return the settings service
	 */
	public SettingsService getSettingsService() {
		return settingsService;
	}

	/**
	 * Returns the tmdb client.
	 *
	 * @return the tmdb client
	 */
	public TmdbClient getTmdbClient() {
		return tmdbClient;
	}

	/**
	 * Returns the focus manager.
	 *
	 * @return the focus manager.
	 */
	public FocusManager getFocusManager() {
		return scene.getFocusManager();
	}

	/* *************************************************************** *
	 *                       State / Navigation                        *
	 * *************************************************************** */

	/**
	 * Returns the current mode of the cli.
	 *
	 * @return the cli mode
	 */
	public CliMode getMode() {
		return mode;
	}

	public void setMode(CliMode mode) {
		if (Objects.equals(this.mode, mode)) {
			return;
		}
		this.mode = mode;

		final Interactable currentFocus = scene.getFocusManager().getCurrentFocus();
		updateInfoBar(currentFocus);

		if (this.mode == CliMode.COMMAND) {
			if (currentPage.isChild(currentFocus)) {
				currentPage.setFocusCache(currentFocus);
			}

			commandLine.setHidden(false);
			commandLine.requestFocus();
		} else {
			commandLine.setHidden(true);
			if (scene.getFocusManager().getCurrentFocus() == commandLine) {
				if (currentPage.isChild(currentPage.getFocusCache())) {
					currentPage.getFocusCache().requestFocus();
				} else {
					scene.getFocusManager().ensureValidFocus();
				}
			}
		}
	}

	/**
	 * Returns the current page.
	 *
	 * @return the current page
	 */
	public Page getPage() {
		return currentPage;
	}

	/**
	 * Returns the overview page.
	 *
	 * @return the overview page
	 */
	public OverviewPage getOverviewPage() {
		return overviewPage;
	}

	/**
	 * Clears history and navigates to the overview page.
	 */
	public void navigateToOverview() {
		pageStack.clear();
		navigateTo(overviewPage, false);
	}

	/**
	 * Navigates to the given page.
	 *
	 * @param page the page
	 */
	public void navigateTo(Page page) {
		navigateTo(page, true);
	}

	private void navigateTo(Page page, boolean addToHistory) {
		if (page.getCli() != this) {
			throw new IllegalArgumentException("page does not belong to this cli");
		}
		if (currentPage == page) {
			return;
		}

		if (currentPage != null) {
			final Interactable currentFocus = scene.getFocusManager().getCurrentFocus();
			if (currentPage.isChild(currentFocus)) {
				currentPage.setFocusCache(currentFocus);
			}
			if (addToHistory) {
				pageStack.push(currentPage);
			}
		}

		currentPage = page;
		mainContainer.setCenter(page);

		page.refresh();

		if (page.isChild(page.getFocusCache())) {
			page.getFocusCache().requestFocus();
		} else {
			scene.getFocusManager().ensureValidFocus();
		}
	}

	/**
	 * Navigates back to the previous page.
	 *
	 * @return whether there was a previous page to navigate to
	 */
	public boolean navigateBack() {
		if (pageStack.isEmpty()) {
			return false;
		}
		final Page previousPage = pageStack.pop();

		currentPage = previousPage;
		mainContainer.setCenter(previousPage);

		previousPage.refresh();

		if (previousPage.isChild(previousPage.getFocusCache())) {
			previousPage.getFocusCache().requestFocus();
		} else {
			scene.getFocusManager().ensureValidFocus();
		}

		return true;
	}

	/**
	 * Shows an info dialog.
	 */
	public void showInfoDialog(String info) {
		final ConfirmationDialog dialog = new ConfirmationDialog(info);
		dialog.setConfirmLabel("Ok");
		dialog.setCancelButtonVisible(false);

		final Interactable currentFocus = scene.getFocusManager().getCurrentFocus();
		if (currentPage.isChild(currentFocus)) {
			currentPage.setFocusCache(currentFocus);
		}

		dialog.setOnClosed(() -> {
			if (currentPage.isChild(currentPage.getFocusCache())) {
				currentPage.getFocusCache().requestFocus();
			}
		});

		dialog.show(scene);
	}

	/**
	 * Shows a confirmation dialog with the given text.
	 *
	 * @param text the text to display
	 * @param onConfirm the action to execute if the user confirms
	 */
	public void showConfirmationDialog(String text, Runnable onConfirm) {
		final ConfirmationDialog dialog = new ConfirmationDialog(text);
		dialog.setConfirmLabel("Yes");
		dialog.setCancelLabel("No");
		dialog.setOnConfirm(onConfirm);

		final Interactable currentFocus = scene.getFocusManager().getCurrentFocus();
		if (currentPage.isChild(currentFocus)) {
			currentPage.setFocusCache(currentFocus);
		}

		dialog.setOnClosed(() -> {
			if (currentPage.isChild(currentPage.getFocusCache())) {
				currentPage.getFocusCache().requestFocus();
			}
		});

		dialog.show(scene);
	}

	/**
	 * Shows the given popover.
	 *
	 * @param popover the popover to show
	 */
	public void showPopover(Popover popover) {
		final Interactable currentFocus = scene.getFocusManager().getCurrentFocus();
		if (currentPage.isChild(currentFocus)) {
			currentPage.setFocusCache(currentFocus);
		}

		final Runnable existingOnClosed = popover.getOnClosed();

		popover.setOnClosed(() -> {
			if (existingOnClosed != null) {
				existingOnClosed.run();
			}

			if (currentPage.isChild(currentPage.getFocusCache())) {
				currentPage.getFocusCache().requestFocus();
			}
		});

		scene.showPopover(popover);
	}

	/* *************************************************************** *
	 *                             Context                             *
	 * *************************************************************** */

	/**
	 * Returns the active media item (opened, selected, etc.), or null if no
	 * media item is active.
	 *
	 * @return the active media item
	 */
	public Media getActiveMedia() {
		final Page page = getPage();
		return page == null ? null : page.getActiveMedia();
	}

	/**
	 * Returns the active movie, or null if no movie is active.
	 *
	 * @return the active movie
	 */
	public Movie getActiveMovie() {
		final Media activeMedia = getActiveMedia();
		return activeMedia instanceof Movie movie ? movie : null;
	}

	/**
	 * Returns the active series, or null if no series is active.
	 * Tries to infer the active series from the active media item,
	 * e.g. if a season or episode is active, their parent series is returned.
	 *
	 * @return the active series
	 */
	public Series getActiveSeries() {
		final Media activeMedia = getActiveMedia();
		return switch (activeMedia) {
			case Series series -> series;
			case Season season -> season.getParent();
			case Episode episode -> {
				final Season parentSeason = episode.getParent();
				yield parentSeason == null ? null : parentSeason.getParent();
			}
			default -> null;
		};
	}

	/**
	 * Returns the active season, or null if no season is active.
	 * Tries to infer the active season from the active media item,
	 * e.g. if an episode is active, its parent season is returned.
	 *
	 * @return the active season
	 */
	public Season getActiveSeason() {
		final Media activeMedia = getActiveMedia();
		return switch (activeMedia) {
			case Season season -> season;
			case Episode episode -> episode.getParent();
			default -> null;
		};
	}

	/**
	 * Returns the active episode, or null if no episode is active.
	 *
	 * @return the active episode
	 */
	public Episode getActiveEpisode() {
		final Media activeMedia = getActiveMedia();
		return activeMedia instanceof Episode episode ? episode : null;
	}

	/**
	 * Refreshes the data for the current pages and all the ones in history.
	 */
	public void refresh() {
		currentPage.refresh();
		pageStack.forEach(Page::refresh);
	}

	/* *************************************************************** *
	 *                              Input                              *
	 * *************************************************************** */

	public boolean handleGlobalInput(KeyStroke key) {
		if (key == null) {
			return false;
		}

		// command mode has top priority
		if (mode == CliMode.COMMAND) {
			final InputResult result = commandLine.handleInput(key, mode);
			if (result.shouldMoveFocus() || result == InputResult.LEAVE_COMMAND_MODE) {
				setMode(CliMode.NAVIGATION);
				return true;
			}
			return result != InputResult.UNHANDLED;
		}

		// pass input to focus
		final FocusManager focusManager = scene.getFocusManager();
		final Interactable focused = focusManager.getCurrentFocus();

		if (focused != null) {
			final InputResult result = focused.handleInput(key, mode);

			if (result.shouldMoveFocus()) {
				switch (result) {
					case MOVE_FOCUS_NEXT -> focusManager.focusNext();
					case MOVE_FOCUS_PREVIOUS -> focusManager.focusPrevious();
					case MOVE_FOCUS_UP -> focusManager.moveFocus(Direction.UP);
					case MOVE_FOCUS_DOWN -> focusManager.moveFocus(Direction.DOWN);
					case MOVE_FOCUS_LEFT -> focusManager.moveFocus(Direction.LEFT);
					case MOVE_FOCUS_RIGHT -> focusManager.moveFocus(Direction.RIGHT);
				}
				return true;
			}

			if (result == InputResult.ENTER_EDIT_MODE) {
				setMode(CliMode.EDIT);
				return true;
			}
			if (result == InputResult.LEAVE_EDIT_MODE) {
				setMode(CliMode.NAVIGATION);
				return true;
			}

			if (result.isHandled()) {
				return true;
			}
		}

		// default navigation hotkeys
		if (mode == CliMode.NAVIGATION) {
			switch (key.getKeyType()) {
				case Character -> {
					switch (key.getCharacter()) {
						case 'q' -> stop();
						case 'e' -> {
							if (focused != null && focused.isEditable()) {
								setMode(CliMode.EDIT);
								focused.requestFocus();
							}
						}
						case ':' -> {
							if (scene.getPopovers().stream().anyMatch(Parent::hasFocusedChild)) {
								return false;
							}
							setMode(CliMode.COMMAND);
						}
						case 'K', 'k' -> focusManager.moveFocus(Direction.UP);
						case 'J', 'j' -> focusManager.moveFocus(Direction.DOWN);
						case 'H', 'h' -> focusManager.moveFocus(Direction.LEFT);
						case 'L', 'l' -> focusManager.moveFocus(Direction.RIGHT);
						default -> {
							return false;
						}
					}
				}
				case Escape -> {
					if (scene.attemptClosePopover()) {
						return true;
					}
					return navigateBack();
				}
				case Tab -> focusManager.focusNext();
				case ReverseTab -> focusManager.focusPrevious();
				case ArrowUp -> focusManager.moveFocus(Direction.UP);
				case ArrowDown -> focusManager.moveFocus(Direction.DOWN);
				case ArrowLeft -> focusManager.moveFocus(Direction.LEFT);
				case ArrowRight -> focusManager.moveFocus(Direction.RIGHT);
				default -> {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
