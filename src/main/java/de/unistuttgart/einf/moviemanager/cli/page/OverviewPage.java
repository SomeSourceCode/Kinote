package de.unistuttgart.einf.moviemanager.cli.page;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.Insets;
import de.unistuttgart.einf.moviemanager.cli.api.TextAlignment;
import de.unistuttgart.einf.moviemanager.cli.api.layout.VerticalBorderPane;
import de.unistuttgart.einf.moviemanager.cli.api.util.MediaFormatter;
import de.unistuttgart.einf.moviemanager.cli.api.widget.TableColumn;
import de.unistuttgart.einf.moviemanager.cli.api.widget.TableView;
import de.unistuttgart.einf.moviemanager.cli.api.widget.Text;
import de.unistuttgart.einf.moviemanager.model.Media;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import de.unistuttgart.einf.moviemanager.service.MediaService;
import de.unistuttgart.einf.moviemanager.service.search.MediaSearchService;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The overview page. Displays a table of all media items that
 * can be filtered.
 */
public class OverviewPage extends Page {

	private final MediaService mediaService;

	private final Text filterText;
	private final TableView<TopLevelMedia> tableView;

	private static final int SEARCH_THRESHOLD = 60;
	private String searchQuery;
	private final HashMap<TopLevelMedia, Integer> mediaToScore = new HashMap<>();

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
		final TableColumn<TopLevelMedia> titleColumn = TableColumn.weighted("Title", 1, media -> {
			if (searchQuery == null) {
				return media.getTitle();
			}
			final String title = media.getTitle() != null ? media.getTitle() : "";
			final Integer score = mediaToScore.get(media);
			if (score != null) {
				return title + " (" + score + "%)";
			}
			return title;
		});
		titleColumn.setPadding(1);
		final TableColumn<TopLevelMedia> ratingColumn = TableColumn.fixed("Rating", 6, MediaFormatter::rating);
		ratingColumn.setPadding(1);
		ratingColumn.setAlignment(TextAlignment.RIGHT);
		final TableColumn<TopLevelMedia> statusColumn = TableColumn.fixed("Status", 11, MediaFormatter::status);
		statusColumn.setPadding(1);
		statusColumn.setAlignment(TextAlignment.RIGHT);

		tableView.setColumns(typeColumn, titleColumn, ratingColumn, statusColumn);
		container.setCenter(tableView);

		refreshItems();
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
	 * Moves the selection to the first occurrence of the given media item.
	 *
	 * @param media the media
	 */
	public void moveTo(TopLevelMedia media) {
		tableView.moveToRow(media);
	}

	/**
	 * Returns the active search query.
	 *
	 * @return the search query
	 */
	public String getSearchQuery() {
		return searchQuery;
	}

	/**
	 * Sets the active search query.
	 *
	 * @param searchQuery the search query
	 */
	public void setSearchQuery(String searchQuery) {
		searchQuery = searchQuery != null && searchQuery.isBlank() ? null : searchQuery;
		if (Objects.equals(this.searchQuery, searchQuery)) {
			return;
		}
		this.searchQuery = searchQuery;
		filterText.setHidden(searchQuery == null);
		if (searchQuery != null) {
			filterText.setText("Query: " + searchQuery);
		}
		refreshItems();
	}

	public void resetSearchQuery() {
		setSearchQuery(null);
	}

	/**
	 * Refreshes the displayed items.
	 */
	public void refreshItems() {
		final Comparator<TopLevelMedia> lexicalComparator = (media1, media2) -> {
			final String title1 = media1.getTitle() != null ? media1.getTitle() : "";
			final String title2 = media2.getTitle() != null ? media2.getTitle() : "";
			final int comparedTitles = title1.compareToIgnoreCase(title2);
			if (comparedTitles != 0) {
				return comparedTitles;
			}
			final String desc1 = media1.getDescription() != null ? media1.getDescription() : "";
			final String desc2 = media2.getDescription() != null ? media2.getDescription() : "";
			return desc1.compareToIgnoreCase(desc2);
		};

		if (searchQuery == null) {
			tableView.setItems(mediaService.getAllMedia().stream()
					.sorted(lexicalComparator)
					.toList());
			mediaToScore.clear();
			return;
		}

		final Set<MediaSearchService.ScoredMedia> scoredMedia = MediaSearchService.search(searchQuery, mediaService.getAllMedia(), SEARCH_THRESHOLD);

		tableView.setItems(scoredMedia.stream()
				.sorted(Comparator.comparingInt(MediaSearchService.ScoredMedia::score).reversed()
						.thenComparing(MediaSearchService.ScoredMedia::media, lexicalComparator))
				.map(MediaSearchService.ScoredMedia::media)
				.toList());

		mediaToScore.clear();
		for (MediaSearchService.ScoredMedia scored : scoredMedia) {
			mediaToScore.put(scored.media(), scored.score());
		}
	}

	@Override
	public Media getActiveMedia() {
		return tableView.getSelectedItem();
	}

}
