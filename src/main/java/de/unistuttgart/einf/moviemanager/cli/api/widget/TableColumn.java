package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.TextAlignment;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A column of a table.
 *
 * @param <T> the type of data the table shows
 * @see TableView#addColumn(TableColumn)
 */
public class TableColumn<T> {

	private final String header;
	private final int fixedWidth;
	private final int weight;

	private int padding;
	private TextAlignment alignment;

	private final Function<T, String> valueProvider;
	private BiConsumer<T, Integer> onAction;

	private TableColumn(String header, int fixedWidth, int weight, Function<T, String> valueProvider) {
		if (valueProvider == null) {
			throw new IllegalArgumentException("valueProvider must be non-null");
		}
		this.header = header;
		this.fixedWidth = fixedWidth;
		this.weight = weight;
		this.valueProvider = valueProvider;
	}

	/**
	 * Constructs a new column with a fixed content width. The fixed width is a constant number
	 * representing the width of the column excluding padding.
	 * <p>
	 * The value provider is used to display the values.
	 *
	 * @param header the header
	 * @param width the width
	 * @param valueProvider the provider
	 * @return the column
	 * @param <T> the type of data the table shows
	 * @throws IllegalArgumentException if valueProvider is null or width is negative
	 */
	public static <T> TableColumn<T> fixed(String header, int width, Function<T, String> valueProvider) {
		if (width < 0) {
			throw new IllegalArgumentException("width must be non-negative");
		}
		return new TableColumn<>(header, width, -1, valueProvider);
	}

	/**
	 * Constructs a new column with a weighted width. The weighted width will determine the columns
	 * with (excluding padding) based on the available space and the total weight of all weighted columns.
	 * Note that the available space is the total width of the table minus the fixed widths and padding of
	 * all columns.
	 * <p>
	 * The value provider is used to display the values.
	 *
	 * @param header the header
	 * @param weight the weight
	 * @param valueProvider the provider
	 * @return the column
	 * @param <T> the type of data the table shows
	 * @throws IllegalArgumentException valueProvider is null or weight is negative
	 */
	public static <T> TableColumn<T> weighted(String header, int weight, Function<T, String> valueProvider) {
		if (weight < 0) {
			throw new IllegalArgumentException("weight must be non-negative");
		}
		return new TableColumn<>(header, -1, weight, valueProvider);
	}

	/**
	 * Returns the header.
	 *
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Returns the fixed width or -1 if this column is weighted.
	 *
	 * @return the fixed width
	 */
	public int getFixedWidth() {
		return fixedWidth;
	}

	/**
	 * Returns the weight or -1 if this column is fixed.
	 *
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Returns the padding that is applied to the left
	 * and right of the column.
	 *
	 * @return the padding
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * Sets the padding that is applied to the left
	 * and right of the column.
	 *
	 * @param padding the padding
	 */
	public void setPadding(int padding) {
		this.padding = padding;
	}

	/**
	 * Returns the alignment of the text in this column.
	 *
	 * @return the alignment
	 */
	public TextAlignment getAlignment() {
		return alignment == null ? TextAlignment.LEFT : alignment;
	}

	/**
	 * Sets the alignment of the text in this column.
	 *
	 * @param alignment the alignment
	 */
	public void setAlignment(TextAlignment alignment) {
		this.alignment = alignment;
	}

	/**
	 * Returns the value to display for the given item.
	 *
	 * @param item the item
	 * @return the value
	 */
	public String getValue(T item) {
		return valueProvider.apply(item);
	}

	/**
	 * Returns the consumer that is called when a cell of this
	 * column is selected.
	 *
	 * @return the consumer
	 */
	public BiConsumer<T, Integer> getOnAction() {
		return onAction;
	}

	/**
	 * Sets the consumer that is called when a cell of this
	 * column is selected.
	 *
	 * @param onAction the consumer
	 */
	public void setOnAction(BiConsumer<T, Integer> onAction) {
		this.onAction = onAction;
	}

	/**
	 * Fires the consumer set by {@link #setOnAction(BiConsumer)}.
	 *
	 * @param item the item
	 * @param row the row
	 */
	public void fireOnAction(T item, int row) {
		if (onAction == null) {
			return;
		}
		onAction.accept(item, row);
	}

}
