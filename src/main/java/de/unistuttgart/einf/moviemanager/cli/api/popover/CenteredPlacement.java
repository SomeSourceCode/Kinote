package de.unistuttgart.einf.moviemanager.cli.api.popover;

import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.cli.api.Point2D;
import de.unistuttgart.einf.moviemanager.cli.api.Scene;

/**
 * Places the popover centered relative to the scene.
 */
public class CenteredPlacement implements PlacementStrategy {

	@Override
	public Point2D calculatePosition(Scene scene, Popover popover) {
		final Parent root = scene.getRoot();
		if (root == null) {
			return Point2D.ORIGIN;
		}

		return new Point2D(
				(root.getWidth() - popover.getWidth()) / 2,
				(root.getHeight() - popover.getHeight()) / 2
		);
	}

}
