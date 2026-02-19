package de.unistuttgart.einf.moviemanager.cli.api;

import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;

import java.util.List;

/**
 * The result of handling an input.
 *
 * @see Interactable#handleInput(KeyStroke, CliMode)
 */
public enum InputResult {

	UNHANDLED,
	HANDLED,

	MOVE_FOCUS_NEXT,
	MOVE_FOCUS_PREVIOUS,
	MOVE_FOCUS_LEFT,
	MOVE_FOCUS_RIGHT,
	MOVE_FOCUS_UP,
	MOVE_FOCUS_DOWN,

	LEAVE_COMMAND_MODE,
	LEAVE_EDIT_MODE,
	ENTER_EDIT_MODE;

	/**
	 * Returns whether the result is considered handled.
	 *
	 * @return whether the result is considered handled
	 */
	public boolean isHandled() {
		return this == HANDLED;
	}

	/**
	 * Returns whether the result expects the focus to move.
	 *
	 * @return whether the result expects the focus to move
	 */
	public boolean shouldMoveFocus() {
		return List.of(
				MOVE_FOCUS_NEXT,
				MOVE_FOCUS_PREVIOUS,
				MOVE_FOCUS_LEFT,
				MOVE_FOCUS_RIGHT,
				MOVE_FOCUS_UP,
				MOVE_FOCUS_DOWN
		).contains(this);
	}

	/**
	 * Returns whether the result expects the cli mode to change.
	 *
	 * @return whether the result expects the cli mode to change
	 */
	public boolean shouldSwitchMode() {
		return this == LEAVE_COMMAND_MODE || this == LEAVE_EDIT_MODE || this == ENTER_EDIT_MODE;
	}

}
