package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.Insets;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.cli.api.Scene;
import de.unistuttgart.einf.moviemanager.cli.api.popover.AnchoredPlacement;
import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A menu for selecting from a set of options.
 *
 * @param <T> the type of the options
 */
public class DropdownMenu<T> extends Parent {

	/**
	 * Defines how to handle the selection of an item.
	 */
	public enum SelectionMode {

		/**
		 * Keeps the selection.
		 */
		PERSISTENT,
		/**
		 * Keeps the label static, forgets the selection.
		 */
		ACTION

	}

	private T selectedOption;
	private SelectionMode selectionMode = SelectionMode.PERSISTENT;

	private final Function<T, String> valueProvider;
	private Consumer<? super T> onSelect;

	private final List<T> options = new ArrayList<>();

	private String placeholder;

	private final Button button;
	private final Popover popover;
	private final ListView<T> listView;

	/**
	 * Constructs a new dropdown menu with the given value provider
	 * to display the items.
	 *
	 * @param valueProvider the provider
	 * @throws IllegalArgumentException if valueProvider is null
	 */
	public DropdownMenu(Function<T, String> valueProvider) {
		this(valueProvider, null);
	}

	/**
	 * Constructs a new dropdown menu with the given placeholder and
	 * value provider to display the items.
	 *
	 * @param valueProvider the provider
	 * @throws IllegalArgumentException if valueProvider is null
	 */
	public DropdownMenu(Function<T, String> valueProvider, String placeholder) {
		if (valueProvider == null) {
			throw new IllegalArgumentException("valueProvider must be non-null");
		}
		this.valueProvider = valueProvider;

		button = new Button();
		listView = new ListView<>(valueProvider);

		button.setOnAction(this::showDropdown);

		listView.setOnItemSelected((value, _) -> {
			setSelectedOption(value);
			hideDropdown();
			button.requestFocus();
		});

		final AnchoredPlacement placement = new AnchoredPlacement(this);
		popover = new Popover(listView, placement);
		popover.setShowBorders(true);
		popover.setOnClosed(button::requestFocus);

		setPlaceholder(placeholder);
		addChild(button);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		button.setWidth(width - (isShowBorders() ? 2 : 0));
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		button.setHeight(height - (isShowBorders() ? 2 : 0));
	}

	@Override
	public void setPadding(Insets padding) {
		button.setPadding(padding);
	}

	@Override
	public void layoutChildren() {
		setWidth(button.getWidth() + (isShowBorders() ? 2 : 0));
		setHeight(button.getHeight() + (isShowBorders() ? 2 : 0));
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
	 * Sets options, overriding the current ones
	 *
	 * @param options the options
	 */
	public void setOptions(Collection<T> options) {
		this.options.clear();

		for (T option : options) {
			if (option != null && !this.options.contains(option)) {
				this.options.add(option);
			}
		}
		if (selectedOption != null && !this.options.contains(selectedOption)) {
			selectedOption = null;
		}

		updateListItems();
	}

	/**
	 * Adds the option. If the option was already available, this
	 * method does nothing.
	 *
	 * @param option the option
	 */
	public void addOption(T option) {
		if (this.options.contains(option)) {
			return;
		}
		options.add(option);
		updateListItems();
	}

	/**
	 * Removes the given option
	 *
	 * @param option the option
	 */
	public void removeOption(T option) {
		if (!options.remove(option)) {
			return;
		}
		if (selectedOption == option) {
			selectedOption = null;
		}
		updateListItems();
	}

	private void updateListItems() {
		listView.setItems(options);
		final Scene scene = getScene();
		if (scene == null) {
			return;
		}
		if (options.isEmpty()) {
			hideDropdown();
			return;
		}
		if (scene.getPopovers().contains(popover)) {
			resizeListView();
		}
	}

	private void resizeListView() {
		listView.setWidth(Math.max(
				this.options.stream()
						.mapToInt(item -> valueProvider.apply(item).length())
						.max()
						.orElse(0),
				button.getWidth()
		));
		listView.setHeight(Math.min(5, options.size()));
	}

	private void showDropdown() {
		final Scene scene = getScene();
		if (scene == null) {
			return;
		}

		if (options.isEmpty()) {
			return;
		}

		resizeListView();
		scene.showPopover(popover);

		if (selectionMode == SelectionMode.PERSISTENT && selectedOption != null) {
			final int selectedIndex = Math.max(0, options.indexOf(selectedOption));
			listView.setSelectedIndex(selectedIndex);
		} else {
			listView.resetSelection();
		}
		listView.requestFocus();
	}

	private void hideDropdown() {
		final Scene scene = getScene();
		if (scene != null) {
			scene.closePopover(popover);
		}
	}

	/**
	 * Attempts to set the focus to this menu's button if
	 * the dropdown is not already focused.
	 */
	public void requestButtonFocus() {
		if (listView.isFocused()) {
			return;
		}
		button.requestFocus();
	}

	/**
	 * Returns the selection mode.
	 *
	 * @return the selection mode
	 */
	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	/**
	 * Sets the selection mode.
	 *
	 * @param selectionMode the selection mode
	 * @throws IllegalArgumentException if selectionMode is null
	 */
	public void setSelectionMode(SelectionMode selectionMode) {
		if (selectionMode == null) {
			throw new IllegalArgumentException("selectionMode must be non-null");
		}
		if (this.selectionMode == selectionMode) {
			return;
		}
		this.selectionMode = selectionMode;
		setSelectedOption(selectedOption);
	}

	/**
	 * Returns the placeholder that is displayed when no option
	 * is selected or if the selection mode is {@link SelectionMode#ACTION}.
	 *
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * Sets the placeholder that is displayed when no option
	 * is selected or if the selection mode is {@link SelectionMode#ACTION}.
	 *
	 * @param placeholder the placeholder
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		if (selectedOption == null || selectionMode == SelectionMode.ACTION) {
			button.setLabel(placeholder);
		}
	}

	/**
	 * Returns the selected option.
	 *
	 * @return the selected option
	 */
	public T getSelectedOption() {
		return selectedOption;
	}

	/**
	 * Selects the given option.
	 *
	 * @param option the option
	 */
	public void setSelectedOption(T option) {
		if (selectionMode == SelectionMode.PERSISTENT && selectedOption == option) {
			return;
		}

		this.selectedOption = option;
		if (option != null) {
			fireOnSelect(this.selectedOption);
		}

		if (selectionMode == SelectionMode.ACTION) {
			this.selectedOption = null;
			button.setLabel(placeholder);
		} else {
			button.setLabel(option == null ? placeholder : valueProvider.apply(option));
		}
	}

	/**
	 * Returns the consumer that is called when an option
	 * is selected.
	 *
	 * @return the consumer
	 */
	public Consumer<? super T> getOnSelect() {
		return onSelect;
	}

	/**
	 * Sets the consumer that is called when an option
	 * is selected.
	 *
	 * @param onSelect the consumer
	 */
	public void setOnSelect(Consumer<? super T> onSelect) {
		this.onSelect = onSelect;
	}

	/**
	 * Fires the consumer set by {@link #setOnSelect(Consumer)}.
	 *
	 * @param option the selected option
	 */
	public void fireOnSelect(T option) {
		if (onSelect == null) {
			return;
		}
		onSelect.accept(selectedOption);
	}

}
