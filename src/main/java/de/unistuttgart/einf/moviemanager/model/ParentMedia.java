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
	 * Returns the child with the given index.
	 *
	 * @param index the index of the child
	 * @return the child, or null if no child with the given index exists
	 */
	C getChild(int index);

	/**
	 * Returns whether this media item has a child with the given index.
	 *
	 * @param index the index of the child
	 * @return true if a child with the given index exists, false otherwise
	 */
	default boolean hasChild(int index) {
		return getChild(index) != null;
	}

	/**
	 * Adds a child to this media item.
	 *
	 * @param child the child to add
	 * @throws IllegalArgumentException if a child with the same index already exists
	 */
	void addChild(C child);

	/**
	 * Removes the child with the given index from this media item.
	 *
	 * @param index the index of the child to remove
	 */
	void removeChild(int index);

}
