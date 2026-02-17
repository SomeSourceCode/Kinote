package de.unistuttgart.einf.moviemanager.cli.api.widget;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import de.unistuttgart.einf.moviemanager.cli.CliMode;
import de.unistuttgart.einf.moviemanager.cli.api.InputResult;
import de.unistuttgart.einf.moviemanager.cli.api.InteractableBase;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.TextAlignment;
import de.unistuttgart.einf.moviemanager.cli.api.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A table view to display a list of items in a tabular format.
 *
 * @param <T> the type of items displayed
 */
public class TableView<T> extends InteractableBase {

	private final List<TableColumn<T>> columns = new ArrayList<>();
	private final List<T> items = new ArrayList<>();

	private boolean showHeader = true;
	private boolean showVerticalSeparators = false;

	private int selectedRowIndex = 0;
	private int selectedColumnIndex = 0;
	private int scrollOffset = 0;

	private BiConsumer<T, Integer> defaultRowAction;

	/**
	 * Adds a column.
	 *
	 * @param column the column
	 */
	public void addColumn(TableColumn<T> column) {
		columns.add(column);
	}

	/**
	 * Sets the columns, overriding the current ones.
	 *
	 * @param columns the columns
	 */
	public void setColumns(List<TableColumn<T>> columns) {
		this.columns.clear();
		if (columns != null) {
			this.columns.addAll(columns);
		}
		ensureValidSelection();
	}

	/**
	 * Sets the columns, overriding the current ones.
	 *
	 * @param columns the columns
	 */
	@SafeVarargs
	public final void setColumns(TableColumn<T>... columns) {
		setColumns(List.of(columns));
	}

	/**
	 * Removes a column.
	 *
	 * @param column the column
	 */
	public void removeColumn(TableColumn<T> column) {
		this.columns.remove(column);
		ensureValidSelection();
	}

	/**
	 * Adds an item.
	 *
	 * @param item the item
	 */
	public void addItem(T item) {
		items.add(item);
	}

	/**
	 * Sets the items, overriding the current ones.
	 *
	 * @param items the items
	 */
	public void setItems(List<T> items) {
		this.items.clear();
		if (items != null) {
			this.items.addAll(items);
		}
		resetSelection();
	}

	/**
	 * Removes an item.
	 *
	 * @param item the item
	 */
	public void removeItem(T item) {
		items.remove(item);
	}

	/**
	 * Returns whether to show the header.
	 *
	 * @return whether to show the header
	 */
	public boolean isShowHeader() {
		return showHeader;
	}

	/**
	 * Sets whether to show the header.
	 *
	 * @param showHeader whether to show the header
	 */
	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	/**
	 * Returns whether this table renders vertical separators between columns.
	 *
	 * @return whether this table renders vertical separators between columns
	 */
	public boolean isShowVerticalSeparators() {
		return showVerticalSeparators;
	}

	/**
	 * Sets whether this table should render vertical separators between columns.
	 *
	 * @param showVerticalSeparators whether this table should render vertical
	 * separators between columns
	 */
	public void setShowVerticalSeparators(boolean showVerticalSeparators) {
		this.showVerticalSeparators = showVerticalSeparators;
	}

	/**
	 * Returns the action for columns that don't have a defined interaction.
	 *
	 * @return the default action
	 */
	public BiConsumer<T, Integer> getDefaultRowAction() {
		return defaultRowAction;
	}

	/**
	 * Sets the action for columns that don't have a defined interaction. It takes
	 * the selected item and the row index as parameters.
	 *
	 * @param defaultRowAction the default action
	 */
	public void setDefaultRowAction(BiConsumer<T, Integer> defaultRowAction) {
		this.defaultRowAction = defaultRowAction;
	}

	/**
	 * Fires the consumer set by {@link #setDefaultRowAction(BiConsumer)}.
	 *
	 * @param item the item
	 * @param row the row
	 */
	public void fireDefaultRowAction(T item, int row) {
		if (defaultRowAction == null) {
			return;
		}
		defaultRowAction.accept(item, row);
	}

	/* *************************************************************** *
	 *                           Navigation                            *
	 * *************************************************************** */

	private void ensureValidSelection() {
		if (items.isEmpty()) {
			selectedRowIndex = 0;
			selectedColumnIndex = 0;
			return;
		}

		if (selectedRowIndex >= items.size()) {
			selectedRowIndex = items.size() - 1;
		}
		if (selectedColumnIndex >= columns.size()) {
			selectedColumnIndex = columns.size() - 1;
		}
	}

	private void ensureSelectionVisible() {
		int height = getInnerHeight();
		if (selectedRowIndex < scrollOffset) {
			scrollOffset = selectedRowIndex;
		} else if (selectedRowIndex >= scrollOffset + height - (showHeader ? 2 : 0)) {
			scrollOffset = selectedRowIndex - (height - (showHeader ? 2 : 0)) + 1;
		}
	}

	/**
	 * Returns the currently selected item, or null if there are no items.
	 *
	 * @return the currently selected item
	 */
	public T getSelectedItem() {
		if (items.isEmpty() || selectedRowIndex >= items.size()) {
			return null;
		}
		return items.get(selectedRowIndex);
	}

	/**
	 * Selects the row of the first occurrence of the given item, if it exists in the table.
	 *
	 * @param item the item
	 */
	public void moveToRow(T item) {
		int index = items.indexOf(item);
		if (index >= 0) {
			moveToRow(index);
		}
	}

	/**
	 * Selects the row with the given index, clamping it to the valid range.
	 *
	 * @param rowIndex the row index
	 */
	public void moveToRow(int rowIndex) {
		selectedRowIndex = Math.max(0, Math.min(rowIndex, items.size() - 1));
		ensureValidSelection();
		ensureSelectionVisible();
	}

	/**
	 * Selects the column with the given index, clamping it to the valid range.
	 *
	 * @param columnIndex the column index
	 */
	public void moveToColumn(int columnIndex) {
		selectedColumnIndex = Math.max(0, Math.min(columnIndex, columns.size() - 1));
		ensureValidSelection();
	}

	/**
	 * Selects the cell at the given row and column indices, clamping them to the valid range.
	 *
	 * @param rowIndex the row index
	 * @param columnIndex the column index
	 */
	public void moveToCell(int rowIndex, int columnIndex) {
		moveToRow(rowIndex);
		moveToColumn(columnIndex);
	}

	/**
	 * Resets the selection to the top-left cell.
	 */
	public void resetSelection() {
		selectedRowIndex = 0;
		selectedColumnIndex = 0;
		scrollOffset = 0;
	}

	/**
	 * Moves the selection down by one row, if possible.
	 *
	 * @return whether the selection changed
	 */
	public boolean moveSelectionDown() {
		if (selectedRowIndex >= items.size() - 1) {
			return false;
		}
		selectedRowIndex++;
		ensureSelectionVisible();
		return true;
	}

	/**
	 * Moves the selection up by one row, if possible.
	 *
	 * @return whether the selection changed
	 */
	public boolean moveSelectionUp() {
		if (selectedRowIndex <= 0) {
			return false;
		}
		selectedRowIndex--;
		ensureSelectionVisible();
		return true;
	}

	/**
	 * Moves the selection one column to the left, if possible.
	 *
	 * @return whether the selection changed
	 */
	public boolean moveSelectionLeft() {
		if (selectedColumnIndex <= 0) {
			return false;
		}
		selectedColumnIndex--;
		return true;
	}

	/**
	 * Moves the selection one column to the left, if possible.
	 *
	 * @return whether the selection changed
	 */
	public boolean moveSelectionRight() {
		if (selectedColumnIndex >= columns.size() - 1) {
			return false;
		}
		selectedColumnIndex++;
		return true;
	}

	@Override
	public InputResult handleInput(KeyStroke key, CliMode mode) {
		if (mode != CliMode.NAVIGATION) {
			return InputResult.UNHANDLED;
		}

		switch (key.getKeyType()) {
			case Enter -> triggerSelection();
			case ArrowUp -> {
				if (!moveSelectionUp()) {
					return InputResult.UNHANDLED;
				}
			}
			case ArrowDown -> {
				if (!moveSelectionDown()) {
					return InputResult.UNHANDLED;
				}
			}
			case ArrowLeft -> {
				if (!moveSelectionLeft()) {
					return InputResult.UNHANDLED;
				}
			}
			case ArrowRight -> {
				if (!moveSelectionRight()) {
					return InputResult.UNHANDLED;
				}
			}
			case Tab -> {
				if (moveSelectionRight() || moveSelectionDown()) {
					return InputResult.HANDLED;
				}
				return InputResult.UNHANDLED;
			}
			case ReverseTab -> {
				if (moveSelectionLeft() || moveSelectionUp()) {
					return InputResult.HANDLED;
				}
				return InputResult.UNHANDLED;
			}
			case Character -> {
				switch (key.getCharacter()) {
					case 'k' -> {
						if (!moveSelectionUp()) {
							return InputResult.UNHANDLED;
						}
					}
					case 'j' -> {
						if (!moveSelectionDown()) {
							return InputResult.UNHANDLED;
						}
					}
					case 'h' -> {
						if (!moveSelectionLeft()) {
							return InputResult.UNHANDLED;
						}
					}
					case 'l' -> {
						if (!moveSelectionRight()) {
							return InputResult.UNHANDLED;
						}
					}
					default -> {
						return InputResult.UNHANDLED;
					}
				}
			}
			default -> {
				return InputResult.UNHANDLED;
			}
		}
		return InputResult.HANDLED;
	}

	private void triggerSelection() {
		if (items.isEmpty() || columns.isEmpty()) {
			return;
		}

		final T selectedItem = items.get(selectedRowIndex);
		final TableColumn<T> selectedColumn = columns.get(selectedColumnIndex);

		if (selectedColumn.getOnAction() != null) {
			selectedColumn.fireOnAction(selectedItem, selectedRowIndex);
			return;
		}
		fireDefaultRowAction(selectedItem, selectedRowIndex);
	}

	/* *************************************************************** *
	 *                            Rendering                            *
	 * *************************************************************** */

	@Override
	protected void drawContent(Painter painter) {
		final int topPadding = getPadding().getTop();
		final int bottomPadding = getPadding().getBottom();
		final int leftPadding = getPadding().getLeft();
		final int rightPadding = getPadding().getRight();

		final int width = getInnerWidth() - leftPadding - rightPadding;
		final int height = getInnerHeight() - topPadding - bottomPadding;

		final int contentWidth = width - columns.size() + 1;
		final int[] columnWidths = calculateColumnWidths(contentWidth);

		// header
		if (showHeader) {
			int x = 0;
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
				final TableColumn<T> column = columns.get(columnIndex);
				if (column.getHeader() == null) {
					x += columnWidths[columnIndex] + 1;
					continue;
				}

				final int maxHeaderLength = columnWidths[columnIndex] - (column.getPadding() * 2);
				String headerText = TextUtils.abbreviate(column.getHeader(), maxHeaderLength, "");
				headerText = TextUtils.pad(headerText, maxHeaderLength, column.getAlignment());

				painter.drawString(
						toGlobalX(x + column.getPadding()),
						toGlobalY(0),
						columnWidths[columnIndex],
						headerText
				);
				x += columnWidths[columnIndex] + 1;
			}
			painter.drawSmartHorizontalLine(toGlobalX(0), toGlobalY(1), width, TextColor.ANSI.DEFAULT, null);
		}

		// rows
		final int headerHeight = showHeader ? 2 : 0;
		for (int y = 0; y < height - headerHeight; y++) {
			int dataIndex = y + scrollOffset;
			if (dataIndex >= items.size()) {
				break;
			}

			final T item = items.get(dataIndex);
			final boolean isRowSelected = (dataIndex == selectedRowIndex);

			int x = 0;
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
				final TableColumn<T> column = columns.get(columnIndex);
				final boolean isCellSelected = isRowSelected && (columnIndex == selectedColumnIndex);

				final int maxTextWidth = columnWidths[columnIndex] - (column.getPadding() * 2);

				final String text = column.getValue(item);
				if (text == null) {
					x += columnWidths[columnIndex] + 1;
					return;
				}

				final TextAlignment alignment = column.getAlignment();

				final String ellipsis = isRowSelected ? "" : "...";
				String displayText = (alignment == TextAlignment.RIGHT)
						? TextUtils.abbreviateStart(text, maxTextWidth, ellipsis)
						: TextUtils.abbreviate(text, maxTextWidth, ellipsis);
				displayText = TextUtils.pad(displayText, maxTextWidth, alignment);

				TextColor backgroundColor = TextColor.ANSI.DEFAULT;
				TextColor textColor = TextColor.ANSI.WHITE;

				if (isCellSelected && isFocused()) {
					backgroundColor = TextColor.ANSI.CYAN;
					textColor = TextColor.ANSI.BLACK;
				} else if (isRowSelected) {
					backgroundColor = TextColor.ANSI.BLACK_BRIGHT;
				}

				painter.drawString(
						toGlobalX(x + columns.get(columnIndex).getPadding()),
						toGlobalY(y + headerHeight),
						maxTextWidth,
						displayText,
						textColor,
						backgroundColor
				);
				x += columnWidths[columnIndex] + 1;
			}
		}

		// vertical separators
		if (showVerticalSeparators) {
			int x = 0;
			for (int columnIndex = 0; columnIndex < columns.size() - 1; columnIndex++) {
				x += columnWidths[columnIndex];
				painter.drawSmartVerticalLine(toGlobalX(x), toGlobalY(0), height, TextColor.ANSI.DEFAULT, null);
				x++;
			}
		}
	}

	private int[] calculateColumnWidths(int width) {
		final int[] columnWidths = new int[columns.size()];
		int totalFixedWidth = 0;
		int totalWeight = 0;

		// fixed widths and total weight
		for (int i = 0; i < columns.size(); i++) {
			final TableColumn<T> column = columns.get(i);
			if (column.getFixedWidth() > 0) {
				final int columnWidth = column.getFixedWidth() + column.getPadding() * 2;
				columnWidths[i] = columnWidth;
				totalFixedWidth += columnWidth;
			} else if (column.getWeight() > 0) {
				totalWeight += column.getWeight();
			}
		}

		// remaining width for weighted columns
		int remainingWidth = width - totalFixedWidth;
		if (remainingWidth < 0) {
			remainingWidth = 0;
		}
		for (final TableColumn<T> column : columns) {
			if (column.getFixedWidth() <= 0 && column.getWeight() > 0) {
				remainingWidth -= column.getPadding() * 2;
			}
		}

		// widths for weighted columns
		for (int i = 0; i < columns.size(); i++) {
			final TableColumn<T> column = columns.get(i);
			if (column.getFixedWidth() <= 0 && column.getWeight() > 0) {
				columnWidths[i] = (column.getWeight() * remainingWidth) / totalWeight + column.getPadding() * 2;
			}
		}

		return columnWidths;
	}

}
