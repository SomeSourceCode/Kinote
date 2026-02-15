package de.unistuttgart.einf.moviemanager.cli.page;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.Insets;
import de.unistuttgart.einf.moviemanager.cli.api.layout.VerticalBorderPane;
import de.unistuttgart.einf.moviemanager.cli.api.util.MediaFormatter;
import de.unistuttgart.einf.moviemanager.cli.api.widget.TableColumn;
import de.unistuttgart.einf.moviemanager.cli.api.widget.TableView;
import de.unistuttgart.einf.moviemanager.cli.api.widget.Text;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import de.unistuttgart.einf.moviemanager.service.MediaService;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * The overview page. Displays a table of all media items that
 * can be filtered.
 */
public class OverviewPage extends Page {

	private final MediaService mediaService;

	private final Text filterText;
	private final TableView<TopLevelMedia> tableView;

	private Filter filter;

	/**
	 * Constructs a new overview page for the given media service.
	 *
	 * @param cli the cli
	 * @param mediaService the media service
	 * @throws IllegalArgumentException if mediaService is null
	 */
	public OverviewPage(Cli cli, MediaService mediaService) {
		if (mediaService == null) {
			throw new IllegalArgumentException("mediaService must not be null");
		}
		this.mediaService = mediaService;

		final VerticalBorderPane container = new VerticalBorderPane();
		super(cli, container);
		container.setShowSeparators(true);

		filterText = new Text();
		filterText.setPadding(new Insets(0, 1));
		filterText.setHidden(true);
		container.setTop(filterText);

		tableView = new TableView<>();
		tableView.setHeight(6);
		tableView.setShowVerticalSeparators(true);

		final TableColumn<TopLevelMedia> typeColumn = TableColumn.fixed("Type", 6, MediaFormatter::type);
		typeColumn.setPadding(1);
		final TableColumn<TopLevelMedia> titleColumn = TableColumn.weighted("Title", 1, TopLevelMedia::getTitle);
		titleColumn.setPadding(1);
		final TableColumn<TopLevelMedia> ratingColumn = TableColumn.fixed("Rating", 6, MediaFormatter::rating);
		ratingColumn.setPadding(1);
		final TableColumn<TopLevelMedia> statusColumn = TableColumn.fixed("Status", 11, MediaFormatter::status);
		statusColumn.setPadding(1);

		tableView.setColumns(typeColumn, titleColumn, ratingColumn, statusColumn);
		container.setCenter(tableView);

		refreshItems();
		addChild(container);
	}

	/**
	 * Returns the consumer that is called when a media item is selected.
	 *
	 * @return the consumer
	 */
	public BiConsumer<TopLevelMedia, Integer> getOnMediaSelected() {
		return tableView.getDefaultRowAction();
	}

	/**
	 * Sets the consumer that is called when a media item is selected. The first parameter
	 * is the selected media item, the second parameter is the index of the selected item
	 * in the currently displayed list.
	 *
	 * @param onMediaSelected the consumer
	 */
	public void setOnMediaSelected(BiConsumer<TopLevelMedia, Integer> onMediaSelected) {
		tableView.setDefaultRowAction(onMediaSelected);
	}

	/**
	 * Returns the filter.
	 *
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 */
	public void setFilter(Filter filter) {
		if (Objects.equals(this.filter, filter)) {
			return;
		}
		this.filter = filter;
		filterText.setHidden(filter == null);
		if (filter != null) {
			filterText.setText(filter.name());
		}
		refreshItems();
	}

	/**
	 * Resets the filter.
	 */
	public void resetFilter() {
		setFilter(null);
	}

	/**
	 * Refreshes the displayed items.
	 */
	public void refreshItems() {
		tableView.setItems(mediaService.getAllMedia().stream()
				.filter(media -> filter == null || filter.predicate().test(media))
				.sorted((media1, media2) -> {
					final String title1 = media1.getTitle() != null ? media1.getTitle() : "";
					final String title2 = media2.getTitle() != null ? media2.getTitle() : "";
					final int comparedTitles = title1.compareToIgnoreCase(title2);
					if (comparedTitles != 0) {
						return comparedTitles;
					}
					final String desc1 = media1.getDescription() != null ? media1.getDescription() : "";
					final String desc2 = media2.getDescription() != null ? media2.getDescription() : "";
					return desc1.compareToIgnoreCase(desc2);
				})
				.toList());
	}

	/**
	 * A filter named for media items.
	 *
	 * @param name the name
	 * @param predicate the predicate
	 */
	public record Filter(String name, Predicate<TopLevelMedia> predicate) {

		@Override
		public Predicate<TopLevelMedia> predicate() {
			return predicate != null ? predicate : _ -> true;
		}

	}

}
