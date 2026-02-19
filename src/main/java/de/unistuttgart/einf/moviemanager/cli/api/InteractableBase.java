package de.unistuttgart.einf.moviemanager.cli.api;

import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;

/**
 * The base implementation of the Interactable interface.
 */
public abstract class InteractableBase extends ComponentBase implements Interactable {

	private Runnable onFocusGained;
	private Runnable onFocusLost;

	private boolean focusable = true;

	@Override
	public void requestFocus() {
		final Scene scene = getScene();
		if (scene == null) {
			return;
		}
		scene.getFocusManager().requestFocus(this);
	}

	@Override
	public boolean isFocused() {
		final Scene scene = getScene();
		if (scene == null) {
			return false;
		}
		return scene.getFocusManager().isFocused(this);
	}

	@Override
	public boolean isFocusable() {
		return !isHidden() && focusable;
	}

	@Override
	public void setFocusable(boolean focusable) {
		this.focusable = focusable;
	}

	@Override
	public Runnable getOnFocusGained() {
		return onFocusGained;
	}

	@Override
	public void setOnFocusGained(Runnable runnable) {
		this.onFocusGained = runnable;
	}

	@Override
	public Runnable getOnFocusLost() {
		return onFocusLost;
	}

	@Override
	public void setOnFocusLost(Runnable runnable) {
		this.onFocusLost = runnable;
	}

	@Override
	public void fireOnFocusGained() {
		if (onFocusGained == null) {
			return;
		}
		onFocusGained.run();
	}

	@Override
	public void fireOnFocusLost() {
		if (onFocusLost == null) {
			return;
		}
		onFocusLost.run();
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public InputResult handleInput(KeyStroke keyStroke, CliMode mode) {
		return switch (keyStroke.getKeyType()) {
			case Tab -> InputResult.MOVE_FOCUS_NEXT;
			case ReverseTab -> InputResult.MOVE_FOCUS_PREVIOUS;
			case ArrowLeft -> InputResult.MOVE_FOCUS_LEFT;
			case ArrowRight -> InputResult.MOVE_FOCUS_RIGHT;
			case ArrowUp -> InputResult.MOVE_FOCUS_UP;
			case ArrowDown -> InputResult.MOVE_FOCUS_DOWN;
			default -> InputResult.UNHANDLED;
		};
	}

}
