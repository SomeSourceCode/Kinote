package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.Parent;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A widget to input a runtime in minutes, supporting the following formats:
 * <ul>
 *     <li>Number of minutes (e.g. "90")</li>
 *     <li>Hours and minutes (e.g. "1h 30m", "1h", "30m")</li>
 *     <li>Hours and minutes with colon (e.g. "1:30")</li>
 * </ul>
 */
public class RuntimeInput extends Parent {

	private final TextArea inputArea;

	private int runtime = -1;
	private Consumer<Integer> onRuntimeChange;

	/**
	 * Constructs a new runtime input with no initial runtime.
	 */
	public RuntimeInput() {
		this(-1);
	}

	/**
	 * Constructs a new runtime input.
	 *
	 * @param runtime the initial runtime in minutes, must be non-negative or -1 for no runtime
	 * @throws IllegalArgumentException if runtime is negative and not -1
	 */
	public RuntimeInput(int runtime) {
		inputArea = new TextArea(formatRuntime(runtime));
		inputArea.setAutoSizeHeight(false);
		inputArea.setWrapping(false);
		inputArea.setAutoSizeWidth(true);
		inputArea.setHeight(1);

		inputArea.setInputFilter(text -> text.matches("^[0-9hHm :]*$"));

		inputArea.setOnSubmit(text -> {
			if (text.isBlank()) {
				setRuntime(-1);
				inputArea.setText(formatRuntime(-1));
				return;
			}

			try {
				final int parsed = parseRuntime(text);
				setRuntime(parsed);
				inputArea.setText(formatRuntime(parsed));
			} catch (IllegalArgumentException _) {
				inputArea.setText(formatRuntime(this.runtime));
			}
		});

		addChild(inputArea);

		setRuntime(runtime);
	}

	/**
	 * Returns the runtime.
	 *
	 * @return the runtime
	 */
	public int getRuntime() {
		return runtime;
	}

	/**
	 * Sets the runtime.
	 *
	 * @param runtime the runtime, must be non-negative or -1 for no runtime
	 * @throws IllegalArgumentException if runtime is negative and not -1
	 */
	public void setRuntime(int runtime) {
		if (runtime == this.runtime) {
			return;
		}
		if (runtime < -1) {
			throw new IllegalArgumentException("Runtime must be non-negative or -1 for no runtime");
		}
		this.runtime = runtime;

		fireOnRuntimeChange(runtime);
	}

	/**
	 * Returns the consumer that is called when the runtime changes.
	 *
	 * @return the consumer
	 */
	public Consumer<Integer> getOnRuntimeChange() {
		return onRuntimeChange;
	}

	/**
	 * Sets the consumer that is called when the runtime changes.
	 *
	 * @param onRuntimeChange the consumer
	 */
	public void setOnRuntimeChange(Consumer<Integer> onRuntimeChange) {
		this.onRuntimeChange = onRuntimeChange;
	}

	/**
	 * Fires the consumer set by {@link #setOnRuntimeChange(Consumer)}.
	 *
	 * @param runtime the runtime
	 */
	public void fireOnRuntimeChange(int runtime) {
		if (onRuntimeChange == null) {
			return;
		}
		onRuntimeChange.accept(runtime);
	}

	private String formatRuntime(int minutes) {
		if (minutes <= 0) {
			return "";
		}
		int h = minutes / 60;
		int m = minutes % 60;
		if (h > 0) {
			return String.format("%dh %02dm", h, m);
		}
		return m + "m";
	}

	private int parseRuntime(String text) {
		text = text.toLowerCase().trim();

		// number (90)
		if (text.matches("^\\d+$")) {
			return Integer.parseInt(text);
		}

		// hours minutes ("1h 30m", "1h", "30m")
		if (text.contains("h") || text.contains("m")) {
			int total = 0;

			final Matcher hMatcher = Pattern.compile("(\\d+)\\s*h").matcher(text);
			if (hMatcher.find()) {
				total += Integer.parseInt(hMatcher.group(1)) * 60;
			}

			final Matcher mMatcher = Pattern.compile("(\\d+)\\s*m").matcher(text);
			if (mMatcher.find()) {
				total += Integer.parseInt(mMatcher.group(1));
			}

			return total > 0 ? total : -2;
		}

		// hours:minutes (1:30)
		if (text.contains(":")) {
			String[] parts = text.split(":");
			if (parts.length == 2) {
				try {
					int h = Integer.parseInt(parts[0].trim());
					int m = Integer.parseInt(parts[1].trim());
					return h * 60 + m;
				} catch (NumberFormatException _) {

				}
			}
		}

		throw new IllegalArgumentException("Invalid runtime format: " + text);
	}

	@Override
	protected void layoutChildren() {
		final int paddingLeft = getPadding().getLeft();
		final int paddingTop = getPadding().getTop();

		inputArea.setX(paddingLeft);
		inputArea.setY(paddingTop);

		setWidth(inputArea.getWidth() + paddingLeft + getPadding().getRight());
		setHeight(inputArea.getHeight() + paddingTop + getPadding().getBottom());
	}

}
