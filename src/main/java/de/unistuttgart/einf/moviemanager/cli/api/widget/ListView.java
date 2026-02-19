package de.unistuttgart.einf.moviemanager.cli.api.widget;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;
import de.unistuttgart.einf.moviemanager.cli.api.InputResult;
import de.unistuttgart.einf.moviemanager.cli.api.InteractableBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A vertical scrollable list of items.
 *
 * @param <T> the type of items displayed
 */
public class ListView<T> extends InteractableBase {

	private final List<T> items = new ArrayList<>();

	private final Function<T, String> valueProvider;
	private BiConsumer<T, Integer> onItemSelected;

	private int selectedIndex = 0;
	private int scrollOffset = 0;

	private boolean alwaysShowSelection = false;

	/**
	 * Constructs a new list view with a {@link String#valueOf(Object)}
	 * provider.
	 */
	public ListView() {
		this(String::valueOf);
	}

	/**
	 * Constructs a new list view with using the given provider
	 * to display the items.
	 *
	 * @param valueProvider the provider
	 * @throws IllegalArgumentException if valueProvider is null
	 */
	public ListView(Function<T, String> valueProvider) {
		if (valueProvider == null) {
			throw new IllegalArgumentException("valueProvider must not be null");
		}
		this.valueProvider = valueProvider;
	}

	/**
	 * Returns an unmodifiable view of the list's items.
	 *
	 * @return the items
	 */
	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}

	/**
	 * Adds an item to the list.
	 *
	 * @param item the item
	 * @throws IllegalArgumentException if item is null
	 */
	public void addItem(T item) {
		if (item == null) {
			throw new IllegalArgumentException("item must not be null");
		}
		items.add(item);
	}

	/**
	 * Replaces the items with the given ones.
	 *
	 * @param items the new items
	 * @throws IllegalArgumentException if items is or contains null
	 */
	public void setItems(List<T> items) {
		this.items.clear();
		if (items == null) {
			throw new IllegalArgumentException("items must be non-null");
		}
		for (T item : items) {
			addItem(item);
		}
		if (selectedIndex != -1) {
			resetSelection();
		}
	}

	/**
	 * Empties the list.
	 */
	public void clear() {
		items.clear();
	}

	/**
	 * Returns the consumer that is called when an item is selected.
	 *
	 * @return the consumer
	 */
	public BiConsumer<T, Integer> getOnItemSelected() {
		return onItemSelected;
	}

	/**
	 * Sets the consumer that is called when an item is selected
	 *
	 * @param onItemSelected the consumer
	 */
	public void setOnItemSelected(BiConsumer<T, Integer> onItemSelected) {
		this.onItemSelected = onItemSelected;
	}

	/**
	 * Fires the consumer set by {@link #setOnItemSelected(BiConsumer)}
	 *
	 * @param item the item
	 * @param row the row
	 */
	public void fireOnItemSelected(T item, int row) {
		if (onItemSelected == null) {
			return;
		}
		onItemSelected.accept(item, row);
	}

	/**
	 * Returns whether to always highlight the selection, regardless of
	 * being focussed.
	 *
	 * @return whether to always highlight the selection
	 */
	public boolean isAlwaysShowSelection() {
		return alwaysShowSelection;
	}

	/**
	 * Sets whether to always highlight the selection, regardless of
	 * being focussed.
	 *
	 * @param alwaysShowSelection whether to always highlight the selection
	 */
	public void setAlwaysShowSelection(boolean alwaysShowSelection) {
		this.alwaysShowSelection = alwaysShowSelection;
	}

	/* *************************************************************** *
	 *                           Navigation                            *
	 * *************************************************************** */

	/**
	 * Returns the selected item.
	 *
	 * @return the selected item
	 */
	public T getSelectedItem() {
		if (selectedIndex >= 0 && selectedIndex < items.size()) {
			return items.get(selectedIndex);
		}
		return null;
	}

	/**
	 * Sets the selected index, clamping it to a valid value
	 * and scrolling to make the selection visible.
	 *
	 * @param index the index to select
	 * @return whether the selection has changed
	 */
	public boolean setSelectedIndex(int index) {
		index = Math.max(0, Math.min(index, items.size() - 1));
		final boolean selectionChanged = index != selectedIndex;
		selectedIndex = index;
		ensureSelectionVisible();
		return selectionChanged;
	}

	private void ensureSelectionVisible() {
		if (items.isEmpty()) {
			resetSelection();
		}

		final int height = getInnerHeight() - getPadding().getTop() - getPadding().getBottom();
		if (height <= 0) {
			return;
		}

		if (selectedIndex < scrollOffset) {
			scrollOffset = selectedIndex;
		} else if (selectedIndex >= scrollOffset + height) {
			scrollOffset = selectedIndex - height + 1;
		}

		if (scrollOffset < 0) {
			scrollOffset = 0;
		}
	}

	/**
	 * Removes the selection.
	 */
	public void removeSelection() {
		selectedIndex = -1;
		scrollOffset = 0;
	}

	/**
	 * Scroll back to the top of the list.
	 */
	public void resetSelection() {
		selectedIndex = 0;
		scrollOffset = 0;
	}

	/**
	 * Moves the selection downward.
	 *
	 * @return whether the selection changed
	 */
	public boolean moveDown() {
		return setSelectedIndex(selectedIndex + 1);
	}

	/**
	 * Moves the selection downward, or back to the top, if
	 * it can't go further down.
	 */
	public void cycleDown() {
		if (selectedIndex < items.size() - 1) {
			moveDown();
			return;
		}
		resetSelection();
	}

	/**
	 * Moves the selection upward.
	 *
	 * @return whether the selection changed
	 */
	public boolean moveUp() {
		return setSelectedIndex(selectedIndex - 1);
	}

	/**
	 * Moves the selection upward, or back to the top, if
	 * it can't go further up.
	 */
	public void cycleUp() {
		if (selectedIndex > 0) {
			moveUp();
			return;
		}
		setSelectedIndex(items.size() - 1);
		ensureSelectionVisible();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		ensureSelectionVisible();
	}

	/* *************************************************************** *
	 *                        Rendering / Input                        *
	 * *************************************************************** */

	@Override
	protected void drawContent(Painter painter) {
		final int topPadding = getPadding().getTop();
		final int bottomPadding = getPadding().getBottom();
		final int leftPadding = getPadding().getLeft();
		final int rightPadding = getPadding().getRight();

		final int width = getInnerWidth() - leftPadding - rightPadding;
		final int height = getInnerHeight() - topPadding - bottomPadding;

		if (height <= 0 || width <= 0) {
			return;
		}

		final boolean showSelection = alwaysShowSelection || isFocused();

		for (int i = 0; i < height; i++) {
			final int itemIndex = i + scrollOffset;
			if (itemIndex >= items.size()) {
				break;
			}
			final T item = items.get(itemIndex);
			final String itemValue = valueProvider.apply(item);
			final String displayValue = itemValue.length() > width ? itemValue.substring(0, width) : itemValue;

			TextColor.ANSI textColor = TextColor.ANSI.DEFAULT;
			TextColor.ANSI backgroundColor = null;

			final boolean isSelected = (itemIndex == selectedIndex);

			if (isSelected && showSelection)  {
				textColor = TextColor.ANSI.BLACK_BRIGHT;
				backgroundColor = TextColor.ANSI.CYAN;
			}

			painter.drawString(
					toGlobalX(leftPadding), toGlobalY(topPadding + i),
					width, displayValue,
					textColor, backgroundColor
			);
		}
	}

	@Override
	public InputResult handleInput(KeyStroke key, CliMode mode) {
		if (mode != CliMode.NAVIGATION) {
			return InputResult.UNHANDLED;
		}

		switch (key.getKeyType()) {
			case Enter -> {
				if (triggerSelection()) {
					return InputResult.HANDLED;
				}
			}
			case ArrowDown, Tab -> {
				if (moveDown()) {
					return InputResult.HANDLED;
				}
			}
			case ArrowUp, ReverseTab -> {
				if (moveUp()) {
					return InputResult.HANDLED;
				}
			}
			case Character -> {
				switch (key.getCharacter()) {
					case ' ' -> {
						if (triggerSelection()) {
							return InputResult.HANDLED;
						}
					}
					case 'j' -> {
						if (moveDown()) {
							return InputResult.HANDLED;
						}
					}
					case 'k' -> {
						if (moveUp()) {
							return InputResult.HANDLED;
						}
					}
				}
			}
		}
		return InputResult.UNHANDLED;
	}

	private boolean triggerSelection() {
		final T selectedItem = getSelectedItem();
		if (selectedItem == null) {
			return false;
		}
		fireOnItemSelected(selectedItem, selectedIndex);
		return true;
	}

}
