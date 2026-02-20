package de.unistuttgart.einf.moviemanager.cli.api.widget;

import com.googlecode.lanterna.input.KeyType;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Interactable;
import de.unistuttgart.einf.moviemanager.cli.api.InputResult;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.cli.api.layout.FlowPane;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A menu to select and display multiple selections from
 * a set of options.
 *
 * @param <T> the type of the options
 */
public class ChipFlow<T> extends Parent {

	private final Function<T, String> valueProvider;

	private final List<T> options = new ArrayList<>();
	private final Set<T> selected = new HashSet<>();

	private final FlowPane flowPane;
	private final DropdownMenu<T> addChipDropdown;

	private Consumer<T> onOptionSelected;
	private Consumer<T> onOptionDeselected;

	/**
	 * Constructs a new chip flow with the given value provider.
	 *
	 * @param valueProvider the provider
	 * @throws IllegalArgumentException if valueProvider is null
	 */
	public ChipFlow(Function<T, String> valueProvider) {
		if (valueProvider == null) {
			throw new IllegalArgumentException("valueProvider must be non-null");
		}
		this.valueProvider = valueProvider;

		flowPane = new FlowPane();
		flowPane.setAutoAdjustHeight(true);
		flowPane.setHGap(1);

		addChipDropdown = new DropdownMenu<>(valueProvider);
		addChipDropdown.setPlaceholder("[+]");
		addChipDropdown.setSelectionMode(DropdownMenu.SelectionMode.ACTION);

		addChipDropdown.setOnSelect(this::select);

		addChild(flowPane);
		rebuildUI();
	}

	/**
	 * Selects the given option. If it's not already part of the options
	 * list, it is automatically appended.
	 *
	 * @param option the option
	 */
	public void select(T option) {
		if (option == null) {
			return;
		}
		if (!options.contains(option)) {
			options.add(option);
		}
		if (selected.contains(option)) {
			return;
		}

		final T focusedOption = getFocusedOption();

		selected.add(option);
		fireOnOptionSelected(option);

		rebuildUI();

		if (focusedOption != null) {
			focusChip(focusedOption);
		}
	}

	/**
	 * Deselects the option.
	 *
	 * @param option the option
	 */
	public void deselect(T option) {
		if (option == null || !selected.contains(option)) {
			return;
		}

		final T focusedOption = getFocusedOption();
		final boolean isFocusedOption = Objects.equals(focusedOption, option);
		final int focusedIndex = isFocusedOption ? getSelectedOptions().indexOf(option) : -1;

		selected.remove(option);
		fireOnOptionDeselected(option);

		rebuildUI();

		if (!isFocusedOption) {
			if (focusedOption != null) {
				focusChip(focusedOption);
			}
			return;
		}

		final int targetIndex = Math.max(0, focusedIndex - 1);

		if (targetIndex < selected.size()) {
			((Button) flowPane.getChildren().get(targetIndex)).requestFocus();
			return;
		}
		addChipDropdown.requestButtonFocus();
	}

	private void rebuildUI() {
		new ArrayList<>(flowPane.getChildren()).forEach(flowPane::removeChild);

		for (T option : options) {
			if (selected.contains(option)) {
				flowPane.addChild(createChip(option));
			}
		}

		final List<T> availableOptions = options.stream()
				.filter(option -> !selected.contains(option))
				.toList();
		addChipDropdown.setOptions(availableOptions);

		flowPane.addChild(addChipDropdown);
	}

	private Button createChip(T option) {
		final String label = "[" + valueProvider.apply(option) + "]";
		final Button chip = new Button(label);

		chip.setInputHandler(key -> {
			if (key.getKeyType() == KeyType.Backspace || key.getKeyType() == KeyType.Delete) {
				deselect(option);
				return InputResult.HANDLED;
			}
			return InputResult.UNHANDLED;
		});

		return chip;
	}

	@Override
	public void layoutChildren() {
		final int paddingTop = getPadding().getTop();
		final int paddingBottom = getPadding().getBottom();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getRight();

		final int width = getInnerWidth() - paddingLeft - paddingRight;

		flowPane.setX(paddingLeft);
		flowPane.setY(paddingTop);
		flowPane.setWidth(width);

		this.setHeight(flowPane.getHeight() + paddingTop + paddingBottom + (isShowBorders() ? 2 : 0));
	}

	/**
	 * Returns an unmodifiable view of the options.
	 *
	 * @return the options
	 */
	public List<T> getOptions() {
		return Collections.unmodifiableList(options);
	}

	/**
	 * Returns the selected options.
	 *
	 * @return the selected options
	 */
	public List<T> getSelectedOptions() {
		return this.options.stream()
				.filter(selected::contains)
				.toList();
	}

	/**
	 * Adds the option. If the options is already present, this
	 * method does nothing.
	 *
	 * @param option the option
	 */
	public void addOption(T option) {
		if (options.contains(option)) {
			return;
		}
		options.add(option);
		rebuildUI();
	}

	/**
	 * Sets the options, overriding the current ones
	 *
	 * @param options the options
	 */
	public void setOptions(Collection<T> options) {
		this.options.clear();
		for (T option : options) {
			if (!this.options.contains(option)) {
				this.options.add(option);
			}
		}
		selected.retainAll(options);
		rebuildUI();
	}

	/**
	 * Returns the consumer that is called when an option is selected.
	 *
	 * @return the consumer
	 */
	public Consumer<T> getOnOptionSelected() {
		return onOptionSelected;
	}

	/**
	 * Sets the consumer that is called when an option is selected.
	 *
	 * @param onOptionSelected the consumer
	 */
	public void setOnOptionSelected(Consumer<T> onOptionSelected) {
		this.onOptionSelected = onOptionSelected;
	}

	/**
	 * Fires the consumer set by {@link #setOnOptionSelected(Consumer)}.
	 *
	 * @param option the option selected
	 */
	public void fireOnOptionSelected(T option) {
		if (onOptionSelected == null) {
			return;
		}
		onOptionSelected.accept(option);
	}

	/**
	 * Returns the consumer that is called when an option is deselected.
	 *
	 * @return the consumer
	 */
	public Consumer<T> getOnOptionDeselected() {
		return onOptionDeselected;
	}

	/**
	 * Sets the consumer that is called when an option is deselected.
	 *
	 * @param onOptionDeselected the consumer
	 */
	public void setOnOptionDeselected(Consumer<T> onOptionDeselected) {
		this.onOptionDeselected = onOptionDeselected;
	}

	/**
	 * Fires the consumer set by {@link #setOnOptionDeselected(Consumer)}.
	 *
	 * @param option the option deselected
	 */
	public void fireOnOptionDeselected(T option) {
		if (onOptionDeselected == null) {
			return;
		}
		onOptionDeselected.accept(option);
	}

	private T getFocusedOption() {
		final List<T> currentSelection = getSelectedOptions();
		final List<Component> children = flowPane.getChildren();

		for (int i = 0; i < currentSelection.size(); i++) {
			if (i >= children.size()) {
				return null;
			}
			final Component component = children.get(i);
			if (component instanceof Interactable interactable && interactable.isFocused()) {
				return currentSelection.get(i);
			}
		}
		return null;
	}

	private void focusChip(T option) {
		final int optionIndex = getSelectedOptions().indexOf(option);
		final List<Component> children = flowPane.getChildren();
		if (optionIndex >= 0 && optionIndex < children.size()) {
			((Interactable) children.get(optionIndex)).requestFocus();
		}
	}

}
