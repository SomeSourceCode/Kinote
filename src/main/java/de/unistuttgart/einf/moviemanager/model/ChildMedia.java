package de.unistuttgart.einf.moviemanager.model;

/**
 * Media that can be the child of a {@link ParentMedia}.
 *
 * @param <P> the parent media type
 * @param <S> the self type (every class extending ChildMedia must provide itself as the second type parameter)
 * @see ParentMedia
 */
public abstract class ChildMedia<P extends ParentMedia<S>, S extends ChildMedia<P, S>> extends MediaBase {

	private P parent;
	private int index;

	/**
	 * Returns the parent of this child.
	 * If this child has no parent, it returns null.
	 *
	 * @return the parent
	 */
	protected P getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this child.
	 * <p>
	 * This method should only be used internally by the parent media when adding or removing children.
	 *
	 * @param parent the parent
	 */
	protected void setParent(P parent) {
		this.parent = parent;
	}

	/**
	 * Returns the index of this child inside its parent.
	 *
	 * @return the index
	 */
	protected int getIndex() {
		return index;
	}

	/**
	 * Sets the index of this child inside its parent.
	 * This also updates the parent's child mapping accordingly.
	 *
	 * @param index the index
	 * @throws IllegalArgumentException if the index is less than 1 or if the parent already has a child with the given index
	 */
	@SuppressWarnings("unchecked")
	protected void setIndex(int index) {
		if (index < 1) {
			throw new IllegalArgumentException("Index must be at least 1");
		}
		if (this.index == index) {
			return;
		}
		if (this.parent != null && this.parent.getChild(index) != null) {
			throw new IllegalArgumentException("Child with index " + index + " already exists in parent " + this.parent.getTitle());
		}
		if (this.parent != null) {
			final P parent = this.parent;
			parent.removeChild(this.index);
			this.index = index;
			parent.addChild((S) this);
		} else {
			this.index = index;
		}
	}

}
