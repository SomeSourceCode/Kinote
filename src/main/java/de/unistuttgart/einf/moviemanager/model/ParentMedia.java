package de.unistuttgart.einf.moviemanager.model;

import java.util.List;

/**
 * A media item that can have child media items.
 *
 * @param <C>
 * @see ChildMedia
 */
public interface ParentMedia<C extends ChildMedia<?, C>> extends Media, Iterable<C> {

	/**
	 * Returns an immutable list of all children of this media item.
	 *
	 * @return the list of children
	 */
	List<C> getChildren();

	/**
	 * Returns the child with the given number.
	 *
	 * @param number the number of the child
	 * @return the child, or null if no child with the given number exists
	 */
	C getChild(int number);

	/**
	 * Returns whether this media item has a child with the given number.
	 *
	 * @param number the number of the child
	 * @return true if a child with the given number exists, false otherwise
	 */
	default boolean hasChild(int number) {
		return getChild(number) != null;
	}

	/**
	 * Adds a child to this media item.
	 *
	 * @param child the child to add
	 * @throws IllegalArgumentException if a child with the same number already exists
	 */
	void addChild(C child);

	/**
	 * Removes the child with the given number from this media item.
	 *
	 * @param number the number of the child to remove
	 */
	void removeChild(int number);

}
