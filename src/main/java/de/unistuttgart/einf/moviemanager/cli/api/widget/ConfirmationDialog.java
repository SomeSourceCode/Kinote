package de.unistuttgart.einf.moviemanager.cli.api.widget;

import de.unistuttgart.einf.moviemanager.cli.api.Insets;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.cli.api.Scene;
import de.unistuttgart.einf.moviemanager.cli.api.popover.CenteredPlacement;
import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;

/**
 * A reusable confirmation dialog that displays a message and asks the user
 * to confirm or execute an action.
 * <p>
 * It automatically wraps text if the dialog would exceed the terminal width
 * (minus the defined screen margin).
 */
public class ConfirmationDialog extends Popover {

	private final DialogPane pane;
	private int screenMargin = 3;

	private Runnable onConfirm;
	private Runnable onCancel;

	/**
	 * Constructs a new confirmation dialog.
	 *
	 * @param message the message to display
	 */
	public ConfirmationDialog(String message) {
		this(message, null, null);
	}

	/**
	 * Constructs a new confirmation dialog.
	 *
	 * @param message the message to display
	 * @param onConfirm the action to execute when the user confirms
	 */
	public ConfirmationDialog(String message, Runnable onConfirm) {
		this(message, onConfirm, null);
	}

	/**
	 * Constructs a new confirmation dialog.
	 *
	 * @param message the message to display
	 * @param onConfirm the action to execute when the user confirms
	 * @param onCancel the action to execute when the user cancels
	 */
	public ConfirmationDialog(String message, Runnable onConfirm, Runnable onCancel) {
		final DialogPane thePane = new DialogPane(message);
		super(thePane, new CenteredPlacement());
		this.pane = thePane;
		this.pane.setOwner(this);

		this.onConfirm = onConfirm;
		this.onCancel = onCancel;

		pane.confirmButton.setOnAction(() -> {
			close();
			fireOnConfirm();
		});

		pane.cancelButton.setOnAction(() -> {
			close();
			fireOnCancel();
		});

		setShowBorders(true);
		setPadding(new Insets(1));
	}

	/**
	 * Returns the runnable that is called when the dialog is confirmed.
	 *
	 * @return the runnable
	 */
	public Runnable getOnConfirm() {
		return onConfirm;
	}

	/**
	 * Sets the runnable that is called when the dialog is confirmed.
	 *
	 * @param onConfirm the runnable
	 */
	public void setOnConfirm(Runnable onConfirm) {
		this.onConfirm = onConfirm;
	}

	/**
	 * Fires the runnable set by {@link #setOnConfirm(Runnable)}.
	 */
	public void fireOnConfirm() {
		if (onConfirm == null) {
			return;
		}
		onConfirm.run();
	}

	/**
	 * Returns the runnable that is called when the dialog is canceled.
	 *
	 * @return the runnable
	 */
	public Runnable getOnCancel() {
		return onCancel;
	}

	/**
	 * Sets the runnable that is called when the dialog is canceled.
	 *
	 * @param onCancel the runnable
	 */
	public void setOnCancel(Runnable onCancel) {
		this.onCancel = onCancel;
	}

	/**
	 * Fires the runnable set by {@link #setOnCancel(Runnable)}.
	 */
	public void fireOnCancel() {
		if (onCancel == null) {
			return;
		}
		onCancel.run();
	}

	@Override
	public void fireOnClosed() {
		fireOnCancel();
		super.fireOnClosed();
	}

	/**
	 * Returns the margin to keep clear on the sides of the terminal.
	 *
	 * @return the margin
	 */
	public int getScreenMargin() {
		return screenMargin;
	}

	/**
	 * Sets the margin to keep clear on the sides of the terminal (default: 3).
	 *
	 * @param screenMargin the margin
	 */
	public void setScreenMargin(int screenMargin) {
		this.screenMargin = screenMargin;
	}

	/**
	 * Returns the label for the confirmation button.
	 *
	 * @return the label
	 */
	public String getConfirmLabel() {
		return pane.confirmButton.getLabel();
	}

	/**
	 * Sets the label for the confirmation button (default: "Yes").
	 *
	 * @param label the label
	 */
	public void setConfirmLabel(String label) {
		pane.confirmButton.setLabel(label);
	}

	/**
	 * Returns the label for the cancel button.
	 *
	 * @return the label
	 */
	public String getCancelLabel() {
		return pane.cancelButton.getLabel();
	}

	/**
	 * Sets the label for the cancel button (default: "No").
	 *
	 * @param label the label
	 */
	public void setCancelLabel(String label) {
		pane.cancelButton.setLabel(label);
	}

	/**
	 * Sets whether the cancel button is visible.
	 *
	 * @param visible true to show, false to hide
	 */
	public void setCancelButtonVisible(boolean visible) {
		pane.setCancelVisible(visible);
	}

	/**
	 * Shows this confirmation dialog on the given scene.
	 *
	 * @param scene the scene
	 * @throws IllegalArgumentException if scene is null
	 */
	public void show(Scene scene) {
		if (scene == null) {
			throw new IllegalArgumentException("scene must be non-null");
		}
		scene.showPopover(this);
		pane.confirmButton.requestFocus();
	}

	/**
	 * Closes this confirmation dialog.
	 */
	public void close() {
		final Scene scene = getScene();
		if (scene != null) {
			scene.closePopover(this);
		}
	}

	private static class DialogPane extends Parent {

		private final Text textComponent;
		private final Button confirmButton;
		private final Button cancelButton;

		private boolean showCancel = true;
		private ConfirmationDialog owner;

		public DialogPane(String message) {
			this.textComponent = new Text(message);
			this.confirmButton = new Button("Yes");
			this.cancelButton = new Button("No");

			addChild(textComponent);
			addChild(confirmButton);
			addChild(cancelButton);
		}

		private void setOwner(ConfirmationDialog owner) {
			this.owner = owner;
		}

		private void setCancelVisible(boolean visible) {
			if (this.showCancel == visible) {
				return;
			}
			this.showCancel = visible;

			if (visible) {
				addChild(cancelButton);
			} else {
				removeChild(cancelButton);
			}
		}

		@Override
		public void layoutChildren() {
			final Scene scene = getScene();
			if (owner == null || scene == null) {
				return;
			}
			final Parent root = scene.getRoot();
			if (root == null) {
				return;
			}

			final int paddingTop = getPadding().getTop();
			final int paddingBottom = getPadding().getBottom();
			final int paddingLeft = getPadding().getLeft();
			final int paddingRight = getPadding().getRight();

			final int maxPopoverWidth = Math.max(10, root.getWidth() - (owner.screenMargin * 2));

			final int borderWidth = owner.isShowBorders() ? 2 : 0;
			final int maxContentWidth = Math.max(10, maxPopoverWidth - borderWidth - paddingLeft - paddingRight);

			final int gap = 2;
			final int verticalSpacing = 1;

			final int buttonsWidth;
			final int buttonHeight;

			if (showCancel) {
				buttonsWidth = confirmButton.getWidth() + gap + cancelButton.getWidth();
				buttonHeight = Math.max(confirmButton.getHeight(), cancelButton.getHeight());
			} else {
				buttonsWidth = confirmButton.getWidth();
				buttonHeight = confirmButton.getHeight();
			}

			textComponent.setWrapping(false);
			textComponent.layout();

			if (textComponent.getWidth() > maxContentWidth) {
				textComponent.setWrapping(true);
				textComponent.setWidth(maxContentWidth);
				textComponent.layout();
			}

			final int textWidth = textComponent.getWidth();
			final int textHeight = textComponent.getHeight();

			final int contentWidth = Math.max(textWidth, buttonsWidth);
			final int contentHeight = textHeight + verticalSpacing + buttonHeight;

			setWidth(contentWidth);
			setHeight(contentHeight);

			textComponent.setX((contentWidth - textWidth) / 2);
			textComponent.setY(0);

			final int startX = (contentWidth - buttonsWidth) / 2;
			final int buttonY = textHeight + verticalSpacing;

			confirmButton.setX(startX);
			confirmButton.setY(buttonY);

			if (showCancel) {
				cancelButton.setX(startX + confirmButton.getWidth() + gap);
				cancelButton.setY(buttonY);
			}
		}

	}

}
