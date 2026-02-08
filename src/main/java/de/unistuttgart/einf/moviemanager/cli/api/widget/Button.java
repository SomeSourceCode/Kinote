package de.unistuttgart.einf.moviemanager.cli.api.widget;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.unistuttgart.einf.moviemanager.cli.CliMode;
import de.unistuttgart.einf.moviemanager.cli.api.InputResult;
import de.unistuttgart.einf.moviemanager.cli.api.InteractableBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;

import java.util.function.Function;

/**
 * A button.
 */
public class Button extends InteractableBase {

	private String label = "";
	private Runnable onAction;

	private Function<KeyStroke, InputResult> inputHandler;

	/**
	 * Constructs a new button without a label.
	 */
	public Button() {
		this("");
	}

	/**
	 * Constructs a new button with the given label.
	 *
	 * @param label the label
	 */
	public Button(String label) {
		setLabel(label);
	}

	/**
	 * Returns the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the label
	 */
	public void setLabel(String label) {
		this.label = label != null ? label : "";
	}

	/**
	 * Returns the runnable that is called when the button is
	 * used.
	 *
	 * @return the runnable
	 */
	public Runnable getOnAction() {
		return onAction;
	}

	/**
	 * Sets the runnable that is called when the button is
	 * used.
	 *
	 * @param onAction the runnable
	 */
	public void setOnAction(Runnable onAction) {
		this.onAction = onAction;
	}

	/**
	 * Fires the runnable set by {@link #setOnAction(Runnable)}.
	 */
	public void fireOnAction() {
		if (onAction != null) {
			onAction.run();
		}
	}

	@Override
	public void layout() {
		setWidth(Math.max(1 ,this.label.length()) + getPadding().getLeft() + getPadding().getRight());
		setHeight(1 + getPadding().getTop() + getPadding().getBottom());
	}

	@Override
	protected void drawContent(Painter painter) {
		final int x = toGlobalX(getPadding().getLeft());
		final int y = toGlobalY(getPadding().getTop());

		final int width = getInnerWidth();

		painter.drawString(
				x,
				y,
				width,
				label,
				isFocused() ? TextColor.ANSI.BLACK_BRIGHT : TextColor.ANSI.WHITE,
				isFocused() ? TextColor.ANSI.CYAN : TextColor.ANSI.DEFAULT
		);
	}

	/**
	 * Sets an input handler that is processed before the default button
	 * actions.
	 *
	 * @param inputHandler the input handler
	 */
	public void setInputHandler(Function<KeyStroke, InputResult> inputHandler) {
		this.inputHandler = inputHandler;
	}

	@Override
	public InputResult handleInput(KeyStroke key, CliMode mode) {
		if (mode != CliMode.NAVIGATION) {
			return InputResult.UNHANDLED;
		}

		if (inputHandler != null) {
			final InputResult result = inputHandler.apply(key);
			if (result != InputResult.UNHANDLED && result != null) {
				return result;
			}
		}

		if (key.getKeyType() == KeyType.Enter || (key.getKeyType() == KeyType.Character && key.getCharacter() == ' ')) {
			fireOnAction();
			return InputResult.HANDLED;
		}
		return InputResult.UNHANDLED;
	}

}
