package de.unistuttgart.einf.moviemanager.cli.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A component that contains other components.
 */
public abstract class Parent extends ComponentBase {

	private final List<Component> children = new ArrayList<>();

	/**
	 * Adds the given component to its children.
	 *
	 * @throws IllegalArgumentException if component is null
	 * @throws IllegalStateException if the component is already added to another parent
	 * @param component the component
	 */
	protected void addChild(Component component) {
		if (component == null) {
			throw new IllegalArgumentException("component must be non-null");
		}
		if (component.getParent() != null) {
			throw new IllegalStateException("component can't be added as child because it already has a parent");
		}

		children.add(component);

		try {
			component.setParent(this);
		} catch (RuntimeException exception) {
			children.remove(component);
			throw exception;
		}
	}

	/**
	 * Removes the component from this parent. If the given component
	 * is null, this method does nothing.
	 *
	 * @param component the component to remove
	 */
	protected void removeChild(Component component) {
		if (component == null || !children.contains(component)) {
			return;
		}
		children.remove(component);
		component.setParent(null);
		component.setScene(null);
	}

	/**
	 * Returns an unmodifiable view of this parent's children.
	 *
	 * @return the children
	 */
	public List<Component> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	protected void drawContent(Painter painter) {
		for (Component child : children) {
			child.draw(painter);
		}
	}

	@Override
	public void layout() {
		for (Component child : children) {
			child.layout();
		}
		layoutChildren();
	}

	/**
	 * Lays out the children. This method should be overridden by subclasses
	 * to achieve the specific visual arrangement of the child components.
	 * <p>
	 * The default implementation does nothing, which allows for manual positioning
	 * of children if not overridden.
	 */
	protected void layoutChildren() {

	}

	/**
	 * Returns whether this component or any of its children are currently
	 * focussed.
	 *
	 * @return whether any child is focussed
	 * @see Interactable#isFocused()
	 */
	public boolean hasFocusedChild() {
		if (this instanceof Interactable asInteractable && asInteractable.isFocused()) {
			return true;
		}
		for (Component child : getChildren()) {
			switch (child) {
				case Parent parent -> {
					if (parent.hasFocusedChild()) {
						return true;
					}
				}
				case Interactable interactable -> {
					if (interactable.isFocused()) {
						return true;
					}
				}
				default -> {}
			}
		}
		return false;
	}

}
