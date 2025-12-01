package de.unistuttgart.einf.moviemanager.model;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A helper class for managing child media items within a {@link ParentMedia}.
 *
 * @param <P> the type of the parent media
 * @param <C> the type of the child media
 */
public class MediaContainer<P extends ParentMedia<C>, C extends ChildMedia<P, C>> implements Iterable<C> {

	private final P parent;
	private final NavigableMap<Integer, C> children = new TreeMap<>();

	/**
	 * Constructs a new MediaContainer for the given parent.
	 *
	 * @param parent the parent media
	 * @throws IllegalArgumentException if the parent is null
	 */
	protected MediaContainer(P parent) {
		if (parent == null) {
			throw new IllegalArgumentException("Parent cannot be null");
		}
		this.parent = parent;
	}

	/**
	 * Returns an immutable list of all children in this container.
	 *
	 * @return the list of children
	 */
	public List<C> getChildren() {
		return List.copyOf(children.values());
	}

	/**
	 * Adds a child to this container.
	 *
	 * @param child the child to add
	 * @throws IllegalArgumentException if a child with the same number already exists in the container
	 */
	public void addChild(C child) {
		if (child == null) {
			return;
		}
		if (children.containsKey(child.getNumber())) {
			throw new IllegalArgumentException("Child with number " + child.getNumber() + " already exists in container " + parent.getTitle());
		}
		if (child.getParent() != null && child.getParent() != parent) {
			child.getParent().removeChild(child.getNumber());
		}
		children.put(child.getNumber(), child);
		child.setParent(parent);
	}

	/**
	 * Removes a child from this container by its number.
	 * If no child with the given number exists, this is a no-op.
	 *
	 * @param number the number of the child to remove
	 */
	public void removeChild(int number) {
		final ChildMedia<P, C> child = children.remove(number);
		if (child == null) {
			return;
		}
		child.setParent(null);
	}

	/**
	 * Returns the child with the given number.
	 *
	 * @param number the number of the child
	 * @return the child, or null if no child with the given number exists
	 */
	public C getChild(int number) {
		return children.get(number);
	}

	@Override
	public Iterator<C> iterator() {
		return getChildren().iterator();
	}

}
