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
	private int number;

	/**
	 * Returns the parent of this child.
	 * If this child has no parent, it returns null.
	 *
	 * @return the parent
	 */
	public P getParent() {
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
	 * Returns the number of this child inside its parent.
	 *
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the number of this child inside its parent.
	 * This also updates the parent's child mapping accordingly.
	 *
	 * @param number the number
	 * @throws IllegalArgumentException if the number is less than 1 or if the parent already has a child with the given number
	 */
	@SuppressWarnings("unchecked")
	public void setNumber(int number) {
		if (number < 1) {
			throw new IllegalArgumentException("number must be at least 1");
		}
		if (this.number == number) {
			return;
		}
		if (this.parent != null && this.parent.getChild(number) != null) {
			throw new IllegalArgumentException("Child with number " + number + " already exists in parent " + this.parent.getTitle());
		}
		if (this.parent != null) {
			final P parent = this.parent;
			parent.removeChild(this.number);
			this.number = number;
			parent.addChild((S) this);
		} else {
			this.number = number;
		}
	}

}
