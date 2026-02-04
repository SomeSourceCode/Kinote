package de.unistuttgart.einf.moviemanager.cli.api;

import java.util.*;
import java.util.function.BiFunction;

/**
 * The manager responsible for selecting and moving focus
 * inside a scene.
 */
public class FocusManager {

	private final Scene scene;
	private Interactable currentFocus = null;

	private BiFunction<Interactable, Interactable, Boolean> onFocusChange;

	/**
	 * Constructs a new FocusManager for the given scene.
	 *
	 * @param scene the scene
	 */
	public FocusManager(Scene scene) {
		if (scene == null) {
			throw new IllegalArgumentException("Scene must be non-null");
		}
		this.scene = scene;
	}

	/**
	 * Returns the scene this manager belongs to.
	 *
	 * @return the scene
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * Returns whether the given interactable is currently focused
	 * in this manager's scene.
	 *
	 * @param interactable the interactable
	 * @return whether the interactable is focused
	 * @see Interactable#isFocused()
	 */
	public boolean isFocused(Interactable interactable) {
		return Objects.equals(currentFocus, interactable);
	}

	/**
	 * Returns the interactable currently focused by this
	 * manager's scene.
	 *
	 * @return the current focus
	 */
	public Interactable getCurrentFocus() {
		return currentFocus;
	}

	/**
	 * Switches focus to the given interactable.
	 *
	 * @param toFocus the interactable to move the focus to
	 */
	public void requestFocus(Interactable toFocus) {
		if (Objects.equals(currentFocus, toFocus)) {
			return;
		}

		if (!fireOnFocusChange(currentFocus, toFocus)) {
			return;
		}

		if (currentFocus != null) {
			currentFocus.fireOnFocusLost();
		}
		currentFocus = toFocus;
		if (currentFocus != null) {
			currentFocus.fireOnFocusGained();
		}
	}

	/**
	 * Returns the function that is called when the focus changes. The function
	 * should return whether the focus switch should be performed.
	 *
	 * @return the function
	 */
	public BiFunction<Interactable, Interactable, Boolean> getOnFocusChange() {
		return onFocusChange;
	}

	/**
	 * Sets the function that is called when the focus changes. The function
	 * should return whether the focus switch should be performed.
	 *
	 * @param onFocusChange the function
	 */
	public void setOnFocusChange(BiFunction<Interactable, Interactable, Boolean> onFocusChange) {
		this.onFocusChange = onFocusChange;
	}

	/**
	 * Fires the function set by {@link #setOnFocusChange(BiFunction)} with the given
	 * old and new focus. The return value indicates whether the switch should be
	 * performed.
	 *
	 * @param oldFocus the old focus
	 * @param newFocus the new focus
	 * @return whether the change is permitted
	 */
	public boolean fireOnFocusChange(Interactable oldFocus, Interactable newFocus) {
		if (onFocusChange ==  null) {
			return true;
		}
		final Boolean allowed = onFocusChange.apply(oldFocus, newFocus);
		return allowed == null || allowed;
	}

	private List<Interactable> getFocusableComponents() {
		Parent root = null;
		if (scene.hasPopovers()) {
			root = scene.getPopovers().stream()
					.filter(Parent::hasFocusedChild)
					.findFirst()
					.orElse(null);
		}

		if (root == null) {
			root = scene.getRoot();
		}

		if (root == null) {
			return Collections.emptyList();
		}

		final Stack<Parent> layoutsToProcesses = new Stack<>();
		layoutsToProcesses.push(root);

		final List<Interactable> focusableComponents = new ArrayList<>();

		while (!layoutsToProcesses.isEmpty()) {
			final Parent currentParent = layoutsToProcesses.pop();

			for (Component child : currentParent.getChildren()) {
				if (child instanceof Parent) {
					layoutsToProcesses.push((Parent) child);
				} else if (child instanceof final Interactable interactableChild) {
					if (interactableChild.isFocusable()) {
						focusableComponents.add(interactableChild);
					}
				}
			}
		}
		return focusableComponents;
	}

	/**
	 * Ensures the current focus is valid. If it isn't it selects a valid interactable
	 * to focus.
	 */
	public void ensureValidFocus() {
		final List<Interactable> focusableComponents = getFocusableComponents();
		if (currentFocus != null && focusableComponents.contains(currentFocus)) {
			return;
		}
		currentFocus = focusableComponents.isEmpty() ? null : focusableComponents.getFirst();
	}

	/**
	 * Moves focus to the next interactable.
	 */
	public void focusNext() {
		final List<Interactable> focusableComponents = getFocusableComponents();

		if (currentFocus == null) {
			if (!focusableComponents.isEmpty()) {
				requestFocus(focusableComponents.getFirst());
			}
			return;
		}

		final int currentIndex = focusableComponents.indexOf(currentFocus);
		if (currentIndex == -1) {
			if (!focusableComponents.isEmpty()) {
				requestFocus(focusableComponents.getFirst());
			}
		}

		final int nextIndex = (currentIndex + 1) % focusableComponents.size();
		requestFocus(focusableComponents.get(nextIndex));
	}

	/**
	 * Moves focus to the previous interactable.
	 */
	public void focusPrevious() {
		final List<Interactable> focusableComponents = getFocusableComponents();

		if (currentFocus == null) {
			if (!focusableComponents.isEmpty()) {
				requestFocus(focusableComponents.getFirst());
			}
			return;
		}

		final int currentIndex = focusableComponents.indexOf(currentFocus);
		if (currentIndex == -1) {
			if (!focusableComponents.isEmpty()) {
				requestFocus(focusableComponents.getFirst());
			}
		}

		final int previousIndex = (currentIndex - 1 + focusableComponents.size()) % focusableComponents.size();
		requestFocus(focusableComponents.get(previousIndex));
	}

	/**
	 * Moves focus in the given direction.
	 *
	 * @param direction the direction
	 * @return whether the focus was moved
	 */
	public boolean moveFocus(Direction direction) {
		if (direction == null) {
			return false;
		}

		final List<Interactable> focusableComponents = getFocusableComponents();

		if (currentFocus == null) {
			if (!focusableComponents.isEmpty()) {
				requestFocus(focusableComponents.getFirst());
				return true;
			}
			return false;
		}

		final int currentX = currentFocus.toGlobalX(0);
		final int currentY = currentFocus.toGlobalY(0);
		final int currentW = currentFocus.getWidth();
		final int currentH = currentFocus.getHeight();
		final int currentRight = currentX + currentW;
		final int currentBottom = currentY + currentH;

		Interactable bestCandidate = null;
		long minScore = Long.MAX_VALUE;

		for (Interactable candidate : focusableComponents) {
			if (candidate == currentFocus) {
				continue;
			}

			final int candidateX = candidate.toGlobalX(0);
			final int candidateY = candidate.toGlobalY(0);
			final int candidateWidth = candidate.getWidth();
			final int candidateHeight = candidate.getHeight();
			final int candidateRight = candidateX + candidateWidth;
			final int candidateBottom = candidateY + candidateHeight;

			// check direction strictly
			boolean isValidDirection = switch (direction) {
				case UP -> candidateBottom <= currentY;
				case DOWN -> candidateY >= currentBottom;
				case LEFT -> candidateRight <= currentX;
				case RIGHT -> candidateX >= currentRight;
			};

			// check direction using the center points
			if (!isValidDirection) {
				final int currentCenterX = currentX + currentW / 2;
				final int currentCenterY = currentY + currentH / 2;
				final int candidateCenterX = candidateX + candidateWidth / 2;
				final int candidateCenterY = candidateY + candidateHeight / 2;

				isValidDirection = switch (direction) {
					case UP -> candidateCenterY < currentCenterY;
					case DOWN -> candidateCenterY > currentCenterY;
					case LEFT -> candidateCenterX < currentCenterX;
					case RIGHT -> candidateCenterX > currentCenterX;
				};
			}

			if (!isValidDirection) {
				continue;
			}

			long primaryDist;
			long crossDist;

			if (direction == Direction.UP || direction == Direction.DOWN) {
				if (direction == Direction.DOWN) {
					primaryDist = candidateY - currentBottom;
				} else {
					primaryDist = currentY - candidateBottom;
				}

				final int overlapStart = Math.max(currentX, candidateX);
				final int overlapEnd = Math.min(currentRight, candidateRight);

				if (overlapStart < overlapEnd) {
					crossDist = 0;
				} else {
					crossDist = overlapStart - overlapEnd;
				}
			} else {
				if (direction == Direction.RIGHT) {
					primaryDist = candidateX - currentRight;
				} else {
					primaryDist = currentX - candidateRight;
				}

				final int overlapStart = Math.max(currentY, candidateY);
				final int overlapEnd = Math.min(currentBottom, candidateBottom);

				if (overlapStart < overlapEnd) {
					crossDist = 0;
				} else {
					crossDist = overlapStart - overlapEnd;
				}
			}

			final long score = (primaryDist * primaryDist) + (5 * crossDist * crossDist);

			if (score < minScore) {
				minScore = score;
				bestCandidate = candidate;
			}
		}

		if (bestCandidate != null) {
			requestFocus(bestCandidate);
			return true;
		}
		return false;
	}

}
