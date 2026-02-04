package de.unistuttgart.einf.moviemanager.cli.api;

import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;

/**
 * An interactable component.
 */
public interface Interactable extends Component {

	/**
	 * Attempts to set the focus on this component.
	 *
	 * @see Scene#getFocusManager()
	 */
	void requestFocus();

	/**
	 * Returns whether this component is focused.
	 *
	 * @return whether this component is focused
	 */
	boolean isFocused();

	/**
	 * Indicates whether this component can receive the input focus.
	 * <p>
	 * If false, this component will be excluded from focus traversal
	 * (e.g. tab cycling), but may still be focused programmatically.
	 *
	 * @return whether this component can receive focus
	 */
	boolean isFocusable();

	/**
	 * Sets whether this component should receive focus. Note that
	 * {@link #isFocusable()} maybe also consider other factors, such as
	 * whether this component is currently visible.
	 *
	 * @param focusable whether this component should receive focus
	 */
	void setFocusable(boolean focusable);

	/**
	 * Returns the runnable that is called when this component gains focus.
	 *
	 * @return the runnable
	 */
	Runnable getOnFocusGained();

	/**
	 * Sets the runnable that is called when this component gains focus.
	 *
	 * @param onFocusGained the runnable
	 */
	void setOnFocusGained(Runnable onFocusGained);

	/**
	 * Returns the runnable that is called when this component looses focus.
	 *
	 * @return the runnable
	 */
	Runnable getOnFocusLost();

	/**
	 * Sets the runnable that is called when this component looses focus.
	 *
	 * @param onFocusLost the runnable
	 */
	void setOnFocusLost(Runnable onFocusLost);

	/**
	 * Fires the runnable set by {@link #setOnFocusGained(Runnable)}.
	 */
	void fireOnFocusGained();

	/**
	 * Fires the runnable set by {@link #setOnFocusLost(Runnable)}
	 */
	void fireOnFocusLost();

	/**
	 * Returns whether this component is editable, indicating special
	 * behavior during edit mode.
	 *
	 * @return whether this component is editable
	 */
	boolean isEditable();

	/**
	 * Handles the given keystroke considering the given cli mode.
	 * If this method requires a specific action, it should indicate so by
	 * returning the respective result.
	 *
	 * @param keyStroke the keystroke
	 * @param mode the cli mode
	 * @return the result indicating the status and an optional action
	 */
	InputResult handleInput(KeyStroke keyStroke, CliMode mode);

}
