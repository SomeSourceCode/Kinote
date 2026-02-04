package de.unistuttgart.einf.moviemanager.cli.api;

import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The container for all components in the scene graph.
 */
public class Scene {

	private Parent root;
	private final FocusManager focusManager;
	private final List<Popover> popovers = new ArrayList<>();

	/**
	 * Constructs a new scene.
	 */
	public Scene() {
		focusManager = new FocusManager(this);
	}

	/**
	 * Returns the root of the scene graph.
	 *
	 * @return the root
	 */
	public Parent getRoot() {
		return root;
	}

	/**
	 * Sets the root of the scene graph. The root must not be a
	 * {@link Popover}.
	 *
	 * @param root the root
	 * @see #showPopover(Popover)
	 */
	public void setRoot(Parent root) {
		if (Objects.equals(this.root, root)) {
			return;
		}

		if (root instanceof Popover) {
			throw new IllegalArgumentException("scene root cannot be a popover");
		}

		if (this.root != null) {
			final Parent oldRoot = this.root;
			this.root = null;
			oldRoot.setScene(null);
		}

		this.root = root;

		if (root != null) {
			root.setScene(this);
		}
	}

	/**
	 * Returns the focus manager.
	 *
	 * @return the focus manager
	 */
	public FocusManager getFocusManager() {
		return focusManager;
	}

	/* *************************************************************** *
	 *                            Popovers                             *
	 * *************************************************************** */

	/**
	 * Shows the popover. If the popover was already added to the scene,
	 * it is moved to the front.
	 *
	 * @param popover the popover
	 * @throws IllegalArgumentException if popover is null
	 */
	public void showPopover(Popover popover) {
		if (popover == null) {
			throw new IllegalArgumentException("popover must be non-null");
		}
		popovers.remove(popover);
		popovers.add(popover);
		popover.setScene(this);
	}

	/**
	 * Closes the given popover. If the popover was not shown in this scene,
	 * this method does nothing.
	 *
	 * @param popover the popover
	 */
	public void closePopover(Popover popover) {
		if (!popovers.remove(popover)) {
			return;
		}
		popover.setScene(null);
	}

	/**
	 * Closes all popovers.
	 */
	public void closeAllPopovers() {
		popovers.forEach(p -> p.setScene(null));
		popovers.clear();
	}

	/**
	 * Returns whether any popovers are shown.
	 *
	 * @return whether any popovers are shown
	 */
	public boolean hasPopovers() {
		return !popovers.isEmpty();
	}

	/**
	 * Returns an unmodifiable view of all active popovers.
	 *
	 * @return the popovers
	 */
	public List<Popover> getPopovers() {
		return Collections.unmodifiableList(popovers);
	}

	/**
	 * Attempts to close the active (focused) popover. If a popover was
	 * closed, true is returned, false otherwise.
	 *
	 * @return whether a popover was closed
	 */
	public boolean attemptClosePopover() {
		if (!hasPopovers()) {
			return false;
		}

		final Interactable focused = focusManager.getCurrentFocus();
		if (focused != null) {
			for (Popover popover : new ArrayList<>(popovers)) {
				if (popover.hasFocusedChild()) {
					closePopover(popover);
					popover.fireOnClosed();
					return true;
				}
			}
		}
		return false;
	}

	/* *************************************************************** *
	 *                            Rendering                            *
	 * *************************************************************** */

	/**
	 * Updates the screen. This includes layout out the components
	 * and rendering to the screen with the given painter.
	 *
	 * @param width the width
	 * @param height the height
	 * @param painter the painter
	 */
	public void update(int width, int height, Painter painter) {
		if (root == null) {
			return;
		}

		getFocusManager().ensureValidFocus();

		root.setX(0);
		root.setY(0);
		root.setWidth(width);
		root.setHeight(height);
		root.layout();
		root.draw(painter);

		for (Popover popover : new ArrayList<>(popovers)) {
			popover.layout();
			popover.draw(painter);
		}
	}

}
