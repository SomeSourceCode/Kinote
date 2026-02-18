package de.unistuttgart.einf.moviemanager.cli.page;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.Insets;
import de.unistuttgart.einf.moviemanager.cli.api.Interactable;
import de.unistuttgart.einf.moviemanager.cli.api.TextAlignment;
import de.unistuttgart.einf.moviemanager.cli.api.layout.FlowPane;
import de.unistuttgart.einf.moviemanager.cli.api.layout.VBox;
import de.unistuttgart.einf.moviemanager.cli.api.util.MediaFormatter;
import de.unistuttgart.einf.moviemanager.cli.api.widget.*;
import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The details page. Displays editable media information.
 */
public class DetailsPage extends Page {

	private final Media media;
	private final VBox container;

	private TableView<Episode> episodeTable;
	private DropdownMenu<Season> seasonDropdown;

	private final List<Runnable> refreshActions = new ArrayList<>();

	/**
	 * Constructs a new details page for the given media item.
	 *
	 * @param cli the cli
	 * @param media the media
	 */
	public DetailsPage(Cli cli, Media media) {
		if (media == null) {
			throw new IllegalArgumentException("media must be non-null");
		}
		this.media = media;

		super(cli, new VBox());

		this.container = (VBox) getMainComponent();
		container.setAutoSizeHeight(true);
		container.setPadding(new Insets(1, 3));

		buildHeader();
		buildProperties();
		buildDescription();
		if (media instanceof Series asSeries) {
			addSeriesComponents(asSeries);
		}
	}

	private void buildHeader() {
		final TextArea titleArea = new TextArea(media.getTitle());
		titleArea.setInputFilter(text -> !text.contains("\n"));
		titleArea.setOnSubmit(media::setTitle);
		container.addChild(titleArea);

		refreshActions.add(() -> titleArea.setText(media.getTitle()));
	}

	private void buildProperties() {
		final FlowPane propertyContainer = new FlowPane();
		propertyContainer.setAutoAdjustHeight(true);
		propertyContainer.setHGap(3);
		container.addChild(propertyContainer);

		// type
		final String type = MediaFormatter.type(media);
		final Text typeLabel = new Text("[" + type + "]");
		propertyContainer.addChild(typeLabel);

		refreshActions.add(() -> typeLabel.setText("[" + MediaFormatter.type(media) + "]"));

		// age rating
		final RatingSystem ratingSystem = getCli().getSettingsService().getSettings().getRatingSystem();
		final String ageRatingString = MediaFormatter.ageRating(media, ratingSystem);

		if (media instanceof LeafMedia asLeafMedia) {
			final DropdownMenu<AgeRating> fskDropdown = new DropdownMenu<>(AgeRating::getLabel);
			if (media.hasAgeRating(ratingSystem)) {
				fskDropdown.setSelectedOption(media.getAgeRating(ratingSystem));
			}
			fskDropdown.setPlaceholder(ageRatingString);
			fskDropdown.setOptions(Arrays.asList(ratingSystem.getRatings()));
			fskDropdown.setOnSelect(asLeafMedia::setAgeRating);
			fskDropdown.setWidth(10);
			propertyContainer.addChild(fskDropdown);

			refreshActions.add(() -> {
				final RatingSystem currentSystem = getCli().getSettingsService().getSettings().getRatingSystem();
				fskDropdown.setPlaceholder(MediaFormatter.ageRating(media, currentSystem));
				fskDropdown.setOptions(Arrays.asList(currentSystem.getRatings()));

				if (media.hasAgeRating(currentSystem)) {
					fskDropdown.setSelectedOption(media.getAgeRating(currentSystem));
				} else {
					fskDropdown.setSelectedOption(null);
				}
			});
		} else {
			final Text ageRatingText = new Text(ageRatingString);
			propertyContainer.addChild(ageRatingText);

			refreshActions.add(() -> {
				final RatingSystem currentSystem = getCli().getSettingsService().getSettings().getRatingSystem();
				ageRatingText.setText(MediaFormatter.ageRating(media, currentSystem));
			});
		}

		// runtime
		if (media instanceof LeafMedia asLeafMedia) {
			final RuntimeInput runtimeInput = new RuntimeInput(media.getRuntime());
			runtimeInput.setOnRuntimeChange(asLeafMedia::setRuntime);
			propertyContainer.addChild(runtimeInput);

			refreshActions.add(() -> runtimeInput.setRuntime(media.getRuntime()));
		} else {
			final Text runtimeText = new Text(MediaFormatter.runtime(media));
			propertyContainer.addChild(runtimeText);

			refreshActions.add(() -> runtimeText.setText(MediaFormatter.runtime(media)));
		}

		// status
		if (media instanceof LeafMedia asLeafMedia) {
			final Button statusButton = new Button(MediaFormatter.status(media));
			statusButton.setOnAction(() -> {
				asLeafMedia.setWatched(asLeafMedia.getStatus() != Status.WATCHED);
				statusButton.setLabel(MediaFormatter.status(asLeafMedia));
			});
			propertyContainer.addChild(statusButton);

			refreshActions.add(() -> statusButton.setLabel(MediaFormatter.status(media)));
		} else {
			final Text statusText = new Text(MediaFormatter.status(media));
			propertyContainer.addChild(statusText);

			refreshActions.add(() -> statusText.setText(MediaFormatter.status(media)));
		}

		// rating
		if (media instanceof LeafMedia asLeafMedia) {
			final RatingInput ratingInput = new RatingInput(media.getRating());
			ratingInput.setOnRatingChange(asLeafMedia::setRating);
			propertyContainer.addChild(ratingInput);

			refreshActions.add(() -> ratingInput.setRating(media.getRating()));
		} else {
			final Text ratingText = new Text(MediaFormatter.rating(media));
			propertyContainer.addChild(ratingText);

			refreshActions.add(() -> ratingText.setText(MediaFormatter.rating(media)));
		}

		// genres
		if (media instanceof TopLevelMedia asTopLevelMedia) {
			final ChipFlow<Genre> genreChips = new ChipFlow<>(MediaFormatter::genre);
			genreChips.setPadding(new Insets(1, 0));
			genreChips.setOptions(Arrays.asList(Genre.values()));

			for (Genre genre : asTopLevelMedia.getGenres()) {
				genreChips.select(genre);
			}

			genreChips.setOnOptionSelected(asTopLevelMedia::addGenre);
			genreChips.setOnOptionDeselected(asTopLevelMedia::removeGenre);
			container.addChild(genreChips);

			refreshActions.add(() -> {
				Arrays.stream(Genre.values()).forEach(genreChips::deselect);
				for (Genre genre : asTopLevelMedia.getGenres()) {
					genreChips.select(genre);
				}
			});
		} else {
			final Text genreText = new Text(MediaFormatter.genres(media));
			genreText.setPadding(new Insets(1, 0));
			container.addChild(genreText);

			refreshActions.add(() -> genreText.setText(MediaFormatter.genres(media)));
		}
	}

	private void buildDescription() {
		final Text label = new Text("Description:");
		container.addChild(label);

		final TextArea descriptionArea = new TextArea(media.getDescription());
		descriptionArea.setWrapping(true);
		descriptionArea.setOnSubmit(media::setDescription);
		container.addChild(descriptionArea);

		refreshActions.add(() -> descriptionArea.setText(media.getDescription()));
	}

	private void addSeriesComponents(Series series) {
		final FlowPane seasonBar = new FlowPane();
		seasonBar.setPadding(new Insets(1, 0));
		seasonBar.setAutoAdjustHeight(true);
		seasonBar.setHGap(2);
		container.addChild(seasonBar);

		// episodes
		episodeTable = new TableView<>();
		episodeTable.setShowHeader(false);
		episodeTable.setHeight(7);
		episodeTable.setShowBorders(true);
		episodeTable.setShowVerticalSeparators(true);

		final TableColumn<Episode> episodeColumn = TableColumn.weighted("Episodes", 1, episode -> episode.getNumber() + " " + episode.getTitle());
		episodeColumn.setPadding(1);
		final TableColumn<Episode> ratingColumn = TableColumn.fixed("Rating", 6, MediaFormatter::rating);
		ratingColumn.setPadding(1);
		ratingColumn.setAlignment(TextAlignment.RIGHT);
		final TableColumn<Episode> statusColumn = TableColumn.fixed("Status", 11, MediaFormatter::status);
		statusColumn.setPadding(1);
		statusColumn.setAlignment(TextAlignment.RIGHT);

		episodeTable.setColumns(episodeColumn, ratingColumn, statusColumn);

		episodeTable.setDefaultRowAction((episode, _) -> {
			getCli().navigateTo(new DetailsPage(getCli(), episode));
		});

		// age rating
		final Text ageRatingText = new Text();

		// runtime
		final Text runtimeText = new Text();

		// status
		final Text statusText = new Text();

		// rating
		final Text ratingText = new Text();

		// season dropdown
		seasonDropdown = new DropdownMenu<>(season -> "Season " + season.getNumber());
		seasonDropdown.setPlaceholder("No Seasons");

		final List<Season> seasons = series.getChildren();
		seasonDropdown.setOptions(seasons);

		seasonDropdown.setOnSelect(selectedSeason -> {
			updateEpisodeTable(episodeTable, selectedSeason);
			updateSeasonAgeRatingText(ageRatingText, selectedSeason);
			updateSeasonRuntimeText(runtimeText, selectedSeason);
			updateSeasonStatusText(statusText, selectedSeason);
			updateSeasonRatingText(ratingText, selectedSeason);
		});

		if (!seasons.isEmpty()) {
			seasonDropdown.setSelectedOption(seasons.getFirst());
			updateEpisodeTable(episodeTable, seasons.getFirst());
		}

		seasonBar.addChild(seasonDropdown);
		seasonBar.addChild(ageRatingText);
		seasonBar.addChild(runtimeText);
		seasonBar.addChild(statusText);
		seasonBar.addChild(ratingText);

		container.addChild(episodeTable);

		refreshActions.add(() -> {
			final List<Season> currentSeasons = series.getChildren();
			final Season currentlySelected = seasonDropdown.getSelectedOption();

			seasonDropdown.setOptions(currentSeasons);

			final Season selectionSeason;
			if (currentlySelected != null && currentSeasons.contains(currentlySelected)) {
				selectionSeason = currentlySelected;
			} else if (!currentSeasons.isEmpty()) {
				selectionSeason = currentSeasons.getFirst();
			} else {
				selectionSeason = null;
			}

			seasonDropdown.setSelectedOption(selectionSeason);
			updateEpisodeTable(episodeTable, selectionSeason);
			updateSeasonAgeRatingText(ageRatingText, selectionSeason);
			updateSeasonRuntimeText(runtimeText, selectionSeason);
			updateSeasonStatusText(statusText, selectionSeason);
			updateSeasonRatingText(ratingText, selectionSeason);
		});
	}

	private void updateEpisodeTable(TableView<Episode> table, Season season) {
		if (season == null) {
			table.setItems(null);
			return;
		}
		table.setItems(season.getChildren());
	}

	private void updateSeasonAgeRatingText(Text text, Season season) {
		if (season == null) {
			text.setHidden(true);
			return;
		}
		text.setHidden(false);
		text.setText(MediaFormatter.ageRating(season, getCli().getSettingsService().getSettings().getRatingSystem()));
	}

	private void updateSeasonRuntimeText(Text text, Season season) {
		if (season == null || !season.hasRuntime()) {
			text.setHidden(true);
			return;
		}
		text.setHidden(false);
		text.setText(MediaFormatter.runtime(season));
	}

	private void updateSeasonStatusText(Text text, Season season) {
		if (season == null) {
			text.setHidden(true);
			return;
		}
		text.setHidden(false);
		text.setText(MediaFormatter.status(season));
	}

	private void updateSeasonRatingText(Text text, Season season) {
		if (season == null || !season.hasRating()) {
			text.setHidden(true);
			return;
		}
		text.setHidden(false);
		text.setText(MediaFormatter.rating(season));
	}

	@Override
	public Media getActiveMedia() {
		final Interactable effectiveFocus = getEffectiveFocus();
		if (effectiveFocus == null) {
			return media;
		}

		if (Objects.equals(effectiveFocus, episodeTable)) {
			final Episode selectedEpisode = episodeTable.getSelectedItem();
			if (selectedEpisode != null) {
				return selectedEpisode;
			}
		} else if (seasonDropdown != null && seasonDropdown.isChild(effectiveFocus)) {
			final Season selectedSeason = seasonDropdown.getSelectedOption();
			if (selectedSeason != null) {
				return selectedSeason;
			}
		}

		return media;
	}

	@Override
	public void refresh() {
		if (media instanceof Episode episode) {
			final Season episodeParent = episode.getParent();
			if (episodeParent == null) {
				getCli().navigateBack();
			} else if (episodeParent.getParent() == null) {
				getCli().navigateBack();
			}
			return;
		}
		if (media instanceof TopLevelMedia topLevelMedia && !getCli().getMediaService().getAllMedia().contains(topLevelMedia)) {
			getCli().navigateToOverview();
			return;
		}
		for (Runnable action : refreshActions) {
			action.run();
		}
	}

}
