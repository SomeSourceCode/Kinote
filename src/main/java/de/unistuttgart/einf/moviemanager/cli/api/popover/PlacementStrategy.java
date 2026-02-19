package de.unistuttgart.einf.moviemanager.cli.api.popover;

import de.unistuttgart.einf.moviemanager.cli.api.Point2D;
import de.unistuttgart.einf.moviemanager.cli.api.Scene;

/**
 * A placement strategy for popovers.
 */
public interface PlacementStrategy {

	/**
	 * Calculatest the position for the given popover based on the
	 * specified scene.
	 *
	 * @param scene the scene
	 * @param popover the popover
	 * @return the position
	 */
	Point2D calculatePosition(Scene scene, Popover popover);

}
