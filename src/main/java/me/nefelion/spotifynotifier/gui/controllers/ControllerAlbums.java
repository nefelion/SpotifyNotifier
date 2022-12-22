package me.nefelion.spotifynotifier.gui.controllers;

import com.neovisionaries.i18n.CountryCode;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.nefelion.spotifynotifier.*;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.AlbumInfoDialog;
import me.nefelion.spotifynotifier.gui.apps.util.UtilShowAlbums;
import me.nefelion.spotifynotifier.records.TempAlbumInfo;
import me.nefelion.spotifynotifier.records.VBoxStack;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class ControllerAlbums {
    public static final Stack<VBoxStack> VBOXSTACK = new Stack<>();
    private static MediaPlayer player;
    private final HashMap<String, TempAlbumInfo> infoHashMap = new HashMap<>();
    private ControllerOutline controllerOutline;
    private List<ReleasedAlbum> newAlbums, allAlbums, filteredNewAlbums, filteredAllAlbums;
    private ReleasedAlbum currentSelectedAlbum;
    private TrackSimplified currentSelectedTrack;
    private boolean oneArtist, autoPlayback, hideCovers = false;
    private final Tooltip GTooltipTracklist = new Tooltip();
    private final ContextMenu GContextMenuTracklist = new ContextMenu();
    private int hoveredIndexTracklist = -1, todayReleases, tomorrowReleases;

    @FXML
    private VBox GMainVBOX, GVboxInfo;
    @FXML
    private Accordion GAccordionAlbums, GAccordionTracklist;
    @FXML
    private TitledPane GTitledPaneNewReleases, GTitledPaneAllReleases, GTitledPaneTracklist, GTitledPaneInfo,
            GTitledPaneInfoAlbum, GTitledPaneInfoDate, GTitledPaneInfoArtists, GTitledPaneInfoFeaturing;
    @FXML
    private TableView<ReleasedAlbum> GTableNewReleases, GTableAllReleases;
    @FXML
    private TableColumn<ReleasedAlbum, String> GAllReleases_Type, GAllReleases_Date, GAllReleases_Artist, GAllReleases_Release,
            GNewReleases_Type, GNewReleases_Date, GNewReleases_Artist, GNewReleases_Release;
    @FXML
    private ListView<TrackSimplified> GListTracklist;
    @FXML
    private ImageView GCoverImageView;
    @FXML
    private Text GTextVolume, GTextLoadingArtist;
    @FXML
    private Button GButtonStopPlaying, GButtonBack, GButtonRandom;
    @FXML
    private Slider GSliderVolume;
    @FXML
    private CheckBox GCheckboxAlbums, GCheckboxSingles, GCheckboxFeaturing, GCheckboxCompilations;
    @FXML
    private ProgressBar GProgressBar;
    @FXML
    private Label GLabelCurrentPlaying, GLabelInfoAlbum, GLabelInfoDate, GLabelInfoLength, GLabelInfoArtists, GLabelInfoFeaturing;


    public void setControllerOutline(ControllerOutline controllerOutline) {
        this.controllerOutline = controllerOutline;
    }

    @FXML
    private void initialize() {
        initializeGTitledPaneNewReleases();
        initializeGTitledPaneAllReleases();

        initializeGNewReleasesColumns();
        initializeGAllReleasesColumns();

        initializeGListTracklist();

        initializeGTableNewReleases();
        initializeGTableAllReleases();

        initializeGTextVolume();
        initializeGSliderVolume();

        initializeGTextCurrentPlaying();
        initializeGButtonStopPlaying();

        initializeGProgressBar();
        initializeGButtonBack();
        initializeGButtonRandom();

        initializeInfoTitledPanes();
        initializeGTitledPaneTracklist();
    }

    @FXML
    private void onActionGButtonStopPlaying(ActionEvent actionEvent) {
        stopCurrent();
    }

    @FXML
    private void onActionGButtonBack(ActionEvent actionEvent) {
        stopCurrent();
        VBoxStack vBox = VBOXSTACK.pop();
        controllerOutline.setAlbumsVBOX(vBox.vbox());
        controllerOutline.setAlbumsTitle(vBox.title());
    }

    @FXML
    private void recalculateFilteredAlbums() {
        if (newAlbums != null) {
            recalculateFilteredAlbums(filteredNewAlbums, newAlbums);
            refreshReleases("New releases", filteredNewAlbums, GTitledPaneNewReleases, GTableNewReleases);
        }
        if (allAlbums != null) {
            recalculateFilteredAlbums(filteredAllAlbums, allAlbums);
            refreshReleases("All releases", filteredAllAlbums, GTitledPaneAllReleases, GTableAllReleases);
        }

    }

    private void recalculateFilteredAlbums(List<ReleasedAlbum> filteredAlbums, List<ReleasedAlbum> albums) {
        filteredAlbums.clear();
        for (ReleasedAlbum album : albums) {
            if (album.isFeaturing()) {
                if (!GCheckboxFeaturing.isSelected()) continue;
            } else {
                if (!GCheckboxAlbums.isSelected() && album.isAlbum()) continue;
                if (!GCheckboxCompilations.isSelected() && album.isCompilation()) continue;
                if (!GCheckboxSingles.isSelected() && album.isSingle()) continue;
            }
            filteredAlbums.add(album);
        }
    }

    private void refreshReleases(String albumType, List<ReleasedAlbum> filteredAlbums, TitledPane gTitledPaneReleases, TableView<ReleasedAlbum> gTableReleases) {
        gTableReleases.setItems(FXCollections.observableArrayList(filteredAlbums));
        gTitledPaneReleases.setText(albumType + " (" + filteredAlbums.size() + ")");
    }

    private void initializeGButtonRandom() {
        Tooltip tooltip = new Tooltip("Random release" +
                "\nRight click for auto playback");
        tooltip.setShowDelay(Duration.seconds(0));
        GButtonRandom.setTooltip(tooltip);

        GButtonRandom.setOnMousePressed(event -> {
            MouseButton button = event.getButton();
            if (button == MouseButton.PRIMARY) pickRandomAlbum(false);
            else if (button == MouseButton.SECONDARY) pickRandomAlbum(true);
        });
    }

    private void pickRandomAlbum(boolean playback) {
        if (filteredAllAlbums.size() < 2) return;
        TableView<ReleasedAlbum> GTable;

        if (GTitledPaneNewReleases.isExpanded()) GTable = GTableNewReleases;
        else if (GTitledPaneAllReleases.isExpanded()) GTable = GTableAllReleases;
        else return;
        autoPlayback = playback;

        int size = GTable.getItems().size();
        int randIndex = new Random().nextInt(size);
        while (GTable.getSelectionModel().getSelectedIndex() == randIndex)
            randIndex = new Random().nextInt(size);

        GTable.scrollTo(randIndex);
        GTable.getSelectionModel().select(randIndex);
    }

    private void initializeGButtonBack() {
        if (VBOXSTACK.isEmpty()) GButtonBack.setVisible(false);
    }

    private void initializeGProgressBar() {
        showProgressBar(false);
        GProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    private void initializeGTextVolume() {
        GTextVolume.setVisible(false);
    }

    private void initializeGButtonStopPlaying() {
        GButtonStopPlaying.setVisible(false);
    }

    private void initializeGTextCurrentPlaying() {
        GLabelCurrentPlaying.setVisible(false);
    }

    private void initializeGSliderVolume() {
        GSliderVolume.setValue(TempData.getInstance().getVolumeSliderValue());
        GSliderVolume.valueProperty().addListener((observable, oldValue, newValue) -> setVolume());
        GSliderVolume.setVisible(false);
    }

    private void initializeGTableNewReleases() {
        addAlbumSelectionListenerFor(GTableNewReleases);
        setTableViewRowFactory(GTableNewReleases);
        initializeAlbumCopyingFor(GTableNewReleases);
    }

    private void initializeGTableAllReleases() {
        addAlbumSelectionListenerFor(GTableAllReleases);
        setTableViewRowFactory(GTableAllReleases);
        initializeAlbumCopyingFor(GTableAllReleases);
    }

    private void initializeAlbumCopyingFor(TableView<ReleasedAlbum> GTableAllReleases) {
        GTableAllReleases.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.C) {
                    ReleasedAlbum album = GTableAllReleases.getSelectionModel().getSelectedItem();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(album.getAlbum().getArtists()[0].getName() + "  " + album.getAlbumName());
                    Clipboard.getSystemClipboard().setContent(content);
                } else if (event.getCode() == KeyCode.D) {
                    ReleasedAlbum album = GTableAllReleases.getSelectionModel().getSelectedItem();
                    DiscordMessage.copy(album);
                }
            }
        });
    }

    private void initializeGListTracklist() {
        GListTracklist.setPlaceholder(new Label("Select an album"));

        GListTracklist.setCellFactory(list -> {
            final ListCell<TrackSimplified> cell = new ListCell<>() {
                @Override
                protected void updateItem(TrackSimplified item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        return;
                    }
                    setText(item.getTrackNumber() + ". " + item.getName());

                    if (Arrays.stream(item.getArtists()).anyMatch(p -> p.getId().equals(currentSelectedAlbum.getFollowedArtist().getID()))) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }

                    setOnMouseClicked(mouseClickedEvent -> {
                        if (mouseClickedEvent.getButton().equals(MouseButton.PRIMARY) && mouseClickedEvent.getClickCount() == 2) {
                            playSelected();
                        }
                    });
                }
            };
            cell.setOnMouseEntered(e -> {
                hoveredIndexTracklist = cell.getIndex();
                ObservableList<TrackSimplified> items = GListTracklist.getItems();
                if (hoveredIndexTracklist >= items.size()) return;
                TrackSimplified track = getHoveredTrack();
                if (track != null) {
                    GTooltipTracklist.setText(getTooltipTracklistText(track));
                    GTooltipTracklist.show(GListTracklist, e.getScreenX() + 10, e.getScreenY());
                }
            });
            return cell;
        });


        GListTracklist.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            GContextMenuTracklist.hide();
            if (!e.isSecondaryButtonDown()) return;
            ObservableList<TrackSimplified> items = GListTracklist.getItems();
            if (items == null) return;
            if (hoveredIndexTracklist >= items.size()) return;


            // create and show a context menu over selected track
            MenuItem menuItemPlay = new MenuItem("Play preview");
            menuItemPlay.setOnAction(event -> {
                int preSelected = GListTracklist.getSelectionModel().getSelectedIndex();
                GListTracklist.getSelectionModel().select(hoveredIndexTracklist);
                if (preSelected == hoveredIndexTracklist) playSelected();
            });
            MenuItem menuItemCopyLink = new MenuItem("Copy Spotify link");
            menuItemCopyLink.setOnAction(event -> {
                ClipboardContent content = new ClipboardContent();
                content.putString("https://open.spotify.com/track/" + getHoveredTrack().getId());
                Clipboard.getSystemClipboard().setContent(content);
            });
            MenuItem menuItemCopyPreviewLink = new MenuItem("Copy direct preview link");
            menuItemCopyPreviewLink.setOnAction(event -> {
                String previewUrl = getHoveredTrack().getPreviewUrl();
                if (previewUrl != null) {
                    previewUrl = previewUrl.substring(0, previewUrl.indexOf("?"));
                    ClipboardContent content = new ClipboardContent();
                    content.putString(previewUrl);
                    Clipboard.getSystemClipboard().setContent(content);
                } else Utilities.okMSGBOX("No preview available for this track");
            });
            MenuItem menuItemOpenLink = new MenuItem("Show in Spotify");
            menuItemOpenLink.setOnAction(event -> {
                boolean browser = TempData.getInstance().getFileData().isUseBrowserInsteadOfApp();
                String url = (browser ? "https://open.spotify.com/track/" : "spotify://track/") + getHoveredTrack().getId();
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            });
            GContextMenuTracklist.getItems().clear();
            GContextMenuTracklist.getItems().addAll(menuItemPlay, menuItemCopyLink, menuItemCopyPreviewLink, menuItemOpenLink);
            GContextMenuTracklist.show(GListTracklist, e.getScreenX(), e.getScreenY());

            e.consume();
        });

        GListTracklist.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            GTooltipTracklist.hide();
            //GContextMenuTracklist.hide();
        });

        GListTracklist.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) return;
            currentSelectedTrack = newSelection;
            playSelected();
        });

        GListTracklist.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.C) {
                    TrackSimplified track = GListTracklist.getSelectionModel().getSelectedItem();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(track.getArtists()[0].getName() + "  " + currentSelectedAlbum.getAlbumName() + "  " + track.getName());
                    Clipboard.getSystemClipboard().setContent(content);
                }
            }
        });
    }

    private void initializeGTitledPaneTracklist() {
        ContextMenu contextMenu = new ContextMenu();
        GTitledPaneTracklist.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!e.isSecondaryButtonDown()) return;
            if (!infoHashMap.containsKey(currentSelectedAlbum.getId())) return;

            MenuItem menuCopyTracklist = new MenuItem("Copy tracklist");
            menuCopyTracklist.setOnAction(event1 -> copyTracklist(infoHashMap.get(currentSelectedAlbum.getId())));
            MenuItem menuCopyPreviews = new MenuItem("Copy links to previews");
            menuCopyPreviews.setOnAction(event1 -> copyPreviews(infoHashMap.get(currentSelectedAlbum.getId())));
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(menuCopyTracklist, menuCopyPreviews);
            contextMenu.show(GTitledPaneTracklist, e.getScreenX(), e.getScreenY());
        });
    }

    private TrackSimplified getHoveredTrack() {
        return GListTracklist.getItems().get(hoveredIndexTracklist);
    }

    private String getTooltipTracklistText(TrackSimplified track) {
        return track.getName() + "\n" +
                Utilities.convertMsToDuration(track.getDurationMs()) + "\n" +
                Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", "));
    }

    private void initializeGTitledPaneNewReleases() {
        GTitledPaneNewReleases.setDisable(true);
    }

    private void initializeRightClickForNewReleases() {
        ContextMenu GContextMenuNewReleases = new ContextMenu();
        int size = newAlbums == null ? 0 : newAlbums.size();
        MenuItem menuCreateDiscordMessage = new MenuItem("Create discord message for all new releases (" + size + ")");
        MenuItem menuCreateDiscordTodayMessage = new MenuItem("Create discord message for today's releases only (" + todayReleases + ")");
        MenuItem menuCreateDiscordTomorrowMessage = new MenuItem("Create discord message for tomorrow's releases only (" + tomorrowReleases + ")");

        menuCreateDiscordMessage.setOnAction(event -> DiscordMessage.copy(newAlbums));
        menuCreateDiscordTodayMessage.setOnAction(event -> DiscordMessage.copyTodays(allAlbums));
        menuCreateDiscordTomorrowMessage.setOnAction(event -> DiscordMessage.copyTomorrows(allAlbums));

        GContextMenuNewReleases.getItems().clear();
        GContextMenuNewReleases.getItems().addAll(menuCreateDiscordMessage);
        if (todayReleases > 0) GContextMenuNewReleases.getItems().add(menuCreateDiscordTodayMessage);
        if (tomorrowReleases > 0) GContextMenuNewReleases.getItems().add(menuCreateDiscordTomorrowMessage);

        GTitledPaneNewReleases.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!e.isSecondaryButtonDown()) return;
            GContextMenuNewReleases.show(GTitledPaneNewReleases, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private void initializeRightClickForAllReleases() {
        if (todayReleases == 0 && tomorrowReleases == 0) return;

        ContextMenu GContextMenuAllReleases = new ContextMenu();
        MenuItem menuCreateDiscordTodayMessage = new MenuItem("Create discord message for today's releases only (" + todayReleases + ")");
        MenuItem menuCreateDiscordTomorrowMessage = new MenuItem("Create discord message for tomorrow's releases only (" + tomorrowReleases + ")");

        menuCreateDiscordTodayMessage.setOnAction(event -> DiscordMessage.copyTodays(allAlbums));
        menuCreateDiscordTomorrowMessage.setOnAction(event -> DiscordMessage.copyTomorrows(allAlbums));

        GContextMenuAllReleases.getItems().clear();
        if (todayReleases > 0) GContextMenuAllReleases.getItems().add(menuCreateDiscordTodayMessage);
        if (tomorrowReleases > 0) GContextMenuAllReleases.getItems().add(menuCreateDiscordTomorrowMessage);

        GTitledPaneAllReleases.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!e.isSecondaryButtonDown()) return;
            GContextMenuAllReleases.show(GTitledPaneAllReleases, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private void initializeGTitledPaneAllReleases() {
        GTitledPaneAllReleases.setDisable(true);
    }

    private void initializeGNewReleasesColumns() {
        GNewReleases_Type.setCellValueFactory(new PropertyValueFactory<>("albumType"));
        GNewReleases_Date.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        GNewReleases_Date.setCellFactory(column -> getDateCell());
        GNewReleases_Artist.setCellValueFactory(new PropertyValueFactory<>("artistString"));
        GNewReleases_Artist.setCellFactory(column -> getArtistCell());
        GNewReleases_Release.setCellValueFactory(new PropertyValueFactory<>("albumName"));
        GNewReleases_Release.setCellFactory(column -> getAlbumCell(column));
    }

    private void initializeGAllReleasesColumns() {
        GAllReleases_Type.setCellValueFactory(new PropertyValueFactory<>("albumType"));
        GAllReleases_Date.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        GAllReleases_Date.setCellFactory(column -> getDateCell());
        GAllReleases_Artist.setCellValueFactory(new PropertyValueFactory<>("artistString"));
        GAllReleases_Artist.setCellFactory(column -> getArtistCell());
        GAllReleases_Release.setCellValueFactory(new PropertyValueFactory<>("albumName"));
        GAllReleases_Release.setCellFactory(this::getAlbumCell);
    }

    private TableCell<ReleasedAlbum, String> getDateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setTooltip(null);
                    setText(null);
                    return;
                }

                Tooltip tooltip;
                String ago = Utilities.convertDateToAgo(item);
                if (Utilities.convertDateToDaysAgo(item) > 30) {
                    tooltip = new Tooltip(ago);
                    setText(item);
                } else {
                    tooltip = new Tooltip(item);
                    setText(ago);
                }

                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setStyle("-fx-text-fill: white");
                setTooltip(tooltip);

                // if release date of item above is the same, remove the date
                if (getIndex() > 0) {
                    ReleasedAlbum previousItem = getTableView().getItems().get(getIndex() - 1);
                    if (previousItem.getReleaseDate().equals(item)) setText(null);
                }
            }
        };
    }

    private TableCell<ReleasedAlbum, String> getAlbumCell(TableColumn<ReleasedAlbum, String> column) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    return;
                }

                if (column.getWidth() < new Text(item + "  ").getLayoutBounds().getWidth()) {
                    tooltipProperty().bind(Bindings.when(Bindings.or(emptyProperty(), itemProperty().isNull()))
                            .then((Tooltip) null)
                            .otherwise(new Tooltip(item) {
                                {
                                    setStyle("-fx-text-fill: white");
                                    setShowDelay(Duration.ZERO);
                                }
                            }));
                } else {
                    tooltipProperty().bind(Bindings.when(Bindings.or(emptyProperty(), itemProperty().isNull()))
                            .then((Tooltip) null).otherwise((Tooltip) null));
                }

                setOnMouseClicked((MouseEvent event) -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        if (event.getClickCount() == 2) {
                            if (GListTracklist.getItems() == null) autoPlayback = true;
                            else {
                                autoPlayback = false;
                                GListTracklist.getSelectionModel().select(0);
                            }
                        }
                    }
                });


                setText(item);
            }
        };
    }

    private TableCell<ReleasedAlbum, String> getArtistCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setTooltip(null);
                    setText(null);
                    return;
                }

                ReleasedAlbum releasedAlbum = getTableView().getItems().get(getIndex());

                setOnMouseClicked((MouseEvent event) -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        if (event.getClickCount() == 2) {
                            if (oneArtist) return;
                            showReleases(releasedAlbum.getFollowedArtist().getName(), releasedAlbum.getFollowedArtist());
                        }
                    }
                });


                ArtistSimplified[] artists = releasedAlbum.getAlbum().getArtists();
                if (artists.length > 1) {
                    Comparator<ArtistSimplified> followedComparator =
                            (p, q) -> Boolean.compare(TheEngine.isFollowed(q.getId()), TheEngine.isFollowed(p.getId()));
                    Tooltip tooltip = new Tooltip(Arrays.stream(artists)
                            .sorted(followedComparator.thenComparing(ArtistSimplified::getName))
                            .map(ArtistSimplified::getName)
                            .collect(Collectors.joining("\n")));
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setStyle("-fx-text-fill: white");
                    setTooltip(tooltip);
                } else setTooltip(null);


                setText(item);
            }
        };
    }

    private void initializeInfoTitledPanes() {
        GTitledPaneInfo.setDisable(true);
        GTitledPaneInfoArtists.managedProperty().bind(GTitledPaneInfoArtists.visibleProperty());
        GTitledPaneInfoFeaturing.managedProperty().bind(GTitledPaneInfoFeaturing.visibleProperty());
        addRightClickCopyForFeaturing();
    }

    private void addRightClickCopyForFeaturing() {
        ContextMenu GContextMenuInfoFeaturing = new ContextMenu();
        MenuItem copyFeaturingMenuItem = new MenuItem("Copy");
        copyFeaturingMenuItem.setOnAction(event -> {
            String artists = GLabelInfoFeaturing.getText();
            if (artists == null || artists.isEmpty()) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(artists);
            clipboard.setContent(content);
        });
        GContextMenuInfoFeaturing.getItems().clear();
        GContextMenuInfoFeaturing.getItems().add(copyFeaturingMenuItem);
        GTitledPaneInfoFeaturing.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!e.isSecondaryButtonDown()) return;
            GContextMenuInfoFeaturing.show(GTitledPaneInfoFeaturing, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }


    public void setNewAlbums(List<ReleasedAlbum> newAlbums) {
        if (newAlbums == null) return;
        if (newAlbums.isEmpty()) {
            GTitledPaneNewReleases.getStylesheets().clear();
            return;
        }

        sortReleases(newAlbums);
        this.newAlbums = newAlbums;
        this.filteredNewAlbums = new ArrayList<>(newAlbums);

        refreshReleases("New releases", filteredNewAlbums, GTitledPaneNewReleases, GTableNewReleases);

        GTitledPaneNewReleases.setDisable(false);

        GAccordionAlbums.setExpandedPane(GTitledPaneNewReleases);
        GAccordionTracklist.setExpandedPane(GTitledPaneTracklist);
        Platform.runLater(() -> GTableNewReleases.getSelectionModel().select(0));

    }

    public void setAllAlbums(List<ReleasedAlbum> allAlbums) {
        initializeCheckboxesFor(allAlbums);

        todayReleases = allAlbums.stream().filter(releasedAlbum -> releasedAlbum.getReleaseDate().equals(Utilities.getTodayDate())).toList().size();
        tomorrowReleases = allAlbums.stream().filter(releasedAlbum -> releasedAlbum.getReleaseDate().equals(Utilities.getTomorrowDate())).toList().size();
        initializeRightClickForAllReleases();
        initializeRightClickForNewReleases();

        if (allAlbums.isEmpty()) {
            GTitledPaneTracklist.setDisable(true);
            GTitledPaneInfo.setDisable(true);
            return;
        }

        sortReleases(allAlbums);
        this.allAlbums = allAlbums;
        this.filteredAllAlbums = new ArrayList<>(allAlbums);
        this.oneArtist = allAlbums.stream().filter(p -> !p.getFollowedArtist().getID().equals(allAlbums.get(0).getFollowedArtist().getID())).findAny().isEmpty();

        refreshReleases("All releases", filteredAllAlbums, GTitledPaneAllReleases, GTableAllReleases);


        GTitledPaneAllReleases.setDisable(false);
        if (newAlbums == null || newAlbums.isEmpty()) {
            GAccordionAlbums.setExpandedPane(GTitledPaneAllReleases);
            GAccordionTracklist.setExpandedPane(GTitledPaneTracklist);
            Platform.runLater(() -> GTableAllReleases.getSelectionModel().select(0));
        }
    }

    private static void sortReleases(List<ReleasedAlbum> albums) {
        Comparator<ReleasedAlbum> reminded = Comparator.comparing(ReleasedAlbum::isReminded, Comparator.reverseOrder());
        Comparator<ReleasedAlbum> date = Comparator.comparing(ReleasedAlbum::getLocalDate, Comparator.reverseOrder());
        Comparator<ReleasedAlbum> featuring = Comparator.comparing(ReleasedAlbum::isFeaturing);
        Comparator<ReleasedAlbum> album = Comparator.comparing(ReleasedAlbum::isAlbum, Comparator.reverseOrder());
        Comparator<ReleasedAlbum> compilation = Comparator.comparing(ReleasedAlbum::isCompilation, Comparator.reverseOrder());
        Comparator<ReleasedAlbum> albumName = Comparator.comparing(ReleasedAlbum::getAlbumName);

        albums.sort(reminded
                .thenComparing(date)
                .thenComparing(featuring)
                .thenComparing(album)
                .thenComparing(compilation)
                .thenComparing(albumName)
        );
    }

    private void initializeCheckboxesFor(List<ReleasedAlbum> albums) {
        long numberOfAlbums = albums.stream().filter(p -> p.isAlbum() && !p.isFeaturing()).count();
        long numberOfCompilations = albums.stream().filter(ReleasedAlbum::isCompilation).count();
        long numberOfSingles = albums.stream().filter(p -> p.isSingle() && !p.isFeaturing()).count();
        long numberOfFeaturing = albums.stream().filter(ReleasedAlbum::isFeaturing).count();

        GCheckboxAlbums.setText("Albums (" + numberOfAlbums + ")");
        GCheckboxSingles.setText("Singles (" + numberOfSingles + ")");
        GCheckboxFeaturing.setText("Featuring (" + numberOfFeaturing + ")");
        GCheckboxCompilations.setText("Compilations (" + numberOfCompilations + ")");

        configCheckboxBasedOnNumbers(GCheckboxAlbums, numberOfAlbums);
        configCheckboxBasedOnNumbers(GCheckboxSingles, numberOfSingles);
        configCheckboxBasedOnNumbers(GCheckboxFeaturing, numberOfFeaturing);
        configCheckboxBasedOnNumbers(GCheckboxCompilations, numberOfCompilations);
    }

    private void configCheckboxBasedOnNumbers(CheckBox checkbox, long number) {
        checkbox.setSelected(number != 0);
        checkbox.setDisable(number == 0);
    }

    private void addAlbumSelectionListenerFor(TableView<ReleasedAlbum> gTable) {
        gTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (getSelectedAlbum() == null) return;
            String id = getSelectedAlbum().getId();

            if (newSelection != null) {
                if (gTable.equals(GTableNewReleases))
                    GTableAllReleases.getSelectionModel().clearSelection();
                else if (gTable.equals(GTableAllReleases))
                    GTableNewReleases.getSelectionModel().clearSelection();
            }

            if (getSelectedAlbum().equals(currentSelectedAlbum)) return;
            currentSelectedAlbum = getSelectedAlbum();

            if (!infoHashMap.containsKey(id)) {
                GCoverImageView.setImage(null);
                GListTracklist.setPlaceholder(new Label("Loading..."));

                ReleasedAlbum selectedAlbum = getSelectedAlbum();
                GListTracklist.setItems(null);
                Thread taskThread = new Thread(() -> downloadInfoForAlbum(selectedAlbum));
                taskThread.setDaemon(true);
                taskThread.start();
            } else loadInfo();


            if (!GTitledPaneTracklist.isExpanded() && !GTitledPaneInfo.isExpanded()) {
                GTitledPaneTracklist.setExpanded(true);
            }
        });
    }

    private void loadInfo() {
        TempAlbumInfo info = infoHashMap.get(getSelectedAlbum().getId());
        if (info == null) return;
        updateGCoverImageView(info);

        GListTracklist.setItems(FXCollections.observableArrayList(info.trackList()));
        updateInfoTab(info);
        GTitledPaneInfo.setDisable(false);
        if (autoPlayback) GListTracklist.getSelectionModel().select(0);
        autoPlayback = false;
    }

    private void updateGCoverImageView(TempAlbumInfo info) {
        GCoverImageView.setImage(info.cover());
        ContextMenu contextMenu = getCopyCoverToClipboardContextMenu(info);
        GCoverImageView.setOnContextMenuRequested(e -> contextMenu.show(GCoverImageView, e.getScreenX(), e.getScreenY()));
    }

    private ContextMenu getCopyCoverToClipboardContextMenu(TempAlbumInfo info) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyMI = new MenuItem("Copy Cover to clipboard (300x300)");
        MenuItem hideMI = new MenuItem("Hide Covers");
        copyMI.setOnAction(event1 -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putImage(info.cover());
            clipboard.setContent(content);
        });
        hideMI.setOnAction(event1 -> {
            hideCovers = !hideCovers;
            GCoverImageView.setOpacity(hideCovers ? 0 : 1);
        });
        contextMenu.getItems().addAll(copyMI, hideMI);
        contextMenu.setAutoHide(true);
        return contextMenu;
    }

    private void updateInfoTab(TempAlbumInfo info) {
        Comparator<ArtistSimplified> comparator = (o1, o2) -> {
            return (TheEngine.isFollowed(o2.getId()) ? 1 : 0) - (TheEngine.isFollowed(o1.getId()) ? 1 : 0);
        };
        comparator.thenComparing(ArtistSimplified::getName);

        Album album = info.album();
        List<ArtistSimplified> artists = new ArrayList<>(List.of(album.getArtists()));
        List<ArtistSimplified> performers = getPerformers(album, info.trackList());

        artists.sort(comparator);
        performers.sort(comparator);

        TextFlow flowArtists = getInfoTextFlow(artists);
        TextFlow flowPerformers = getInfoTextFlow(performers);


        String length = Utilities.convertMsToDuration(info.trackList().stream().mapToInt(TrackSimplified::getDurationMs).sum());
        int tracks = info.trackList().size();

        GTitledPaneInfoArtists.setText(artists.size() > 1 ? "Artists" : "Artist");
        GTitledPaneInfoArtists.setContent(flowArtists);
        GTitledPaneInfoFeaturing.setVisible(!performers.isEmpty());
        GTitledPaneInfoFeaturing.setContent(flowPerformers);

        GLabelInfoAlbum.setText(album.getName());
        GLabelInfoDate.setText(album.getReleaseDate() + " (" + Utilities.convertDateToAgo(album.getReleaseDate()) + ")");
        GLabelInfoLength.setText(length + " (" + tracks + " " + (tracks > 1 ? "tracks" : "track") + ")");
        GLabelInfoArtists.setText(artists.stream().map(ArtistSimplified::getName).collect(Collectors.joining(", ")));
        GLabelInfoFeaturing.setText(performers.stream().map(ArtistSimplified::getName).collect(Collectors.joining(", ")));

    }

    private void downloadInfoForAlbum(ReleasedAlbum releasedAlbum) {
        Platform.runLater(() -> GMainVBOX.setCursor(Cursor.WAIT));
        String id = releasedAlbum.getId();
        Album album = TheEngine.getInstance().getAlbum(id);
        List<TrackSimplified> trackList = TheEngine.getInstance().getTracks(album.getId());
        try {
            URL url = new URL(album.getImages()[1].getUrl());
            InputStream input = url.openStream();
            Platform.runLater(() -> {
                infoHashMap.put(id, new TempAlbumInfo(album, new Image(input), trackList));
                if (Objects.equals(getSelectedAlbum().getId(), id)) loadInfo();
            });
        } catch (IOException e) {
            System.err.println("CAN'T DOWNLOAD COVER");
        }
        Platform.runLater(() -> GMainVBOX.setCursor(Cursor.DEFAULT));
    }

    private ReleasedAlbum getSelectedAlbum() {
        return GTableNewReleases.getSelectionModel().getSelectedItem() == null
                ? GTableAllReleases.getSelectionModel().getSelectedItem()
                : GTableNewReleases.getSelectionModel().getSelectedItem();
    }

    private void playSelected() {
        GMainVBOX.setCursor(Cursor.WAIT);
        stopCurrent();
        Thread taskThread = new Thread(() -> {
            TrackSimplified selected = GListTracklist.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            if (selected.getPreviewUrl() == null) {
                Platform.runLater(() -> {
                    GLabelCurrentPlaying.setText("Spotify blocks this track from being played here.");
                    GLabelCurrentPlaying.setStyle("-fx-text-fill: red");
                    GLabelCurrentPlaying.setVisible(true);
                    Tooltip tooltip = new Tooltip("Spotify blocks this track from being played here.");
                    tooltip.setShowDelay(Duration.ZERO);
                    GLabelCurrentPlaying.setTooltip(tooltip);
                    GMainVBOX.setCursor(Cursor.DEFAULT);
                });
                return;
            }

            MediaPlayer newPlayer = new MediaPlayer(new Media(selected.getPreviewUrl()));
            Platform.runLater(() -> {
                stopCurrent();
                player = newPlayer;
                player.setOnEndOfMedia(() -> setPlayerVisible(false));
                if (selected.getId().equals(currentSelectedTrack.getId())) play();
            });
        });
        taskThread.setDaemon(true);
        taskThread.start();
    }

    private synchronized void play() {
        setVolume();
        player.play();
        GMainVBOX.setCursor(Cursor.DEFAULT);
        TrackSimplified t = currentSelectedTrack;
        AlbumSimplified a = currentSelectedAlbum.getAlbum();
        String artists = Arrays.stream(t.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", "));

        GLabelCurrentPlaying.setText(t.getName());
        GLabelCurrentPlaying.setStyle("");
        Tooltip tooltip = new Tooltip(artists + " \n" + a.getName() + " (" + a.getReleaseDate() + ")" + " \n" + t.getName());
        tooltip.setShowDelay(Duration.seconds(0));
        GLabelCurrentPlaying.setTooltip(tooltip);
        setPlayerVisible(true);
    }

    private synchronized void stopCurrent() {
        if (player != null) {
            player.stop();
            setPlayerVisible(false);
        }
    }

    private void setPlayerVisible(boolean b) {
        GLabelCurrentPlaying.setVisible(b);
        GButtonStopPlaying.setVisible(b);
        GTextVolume.setVisible(b);
        GSliderVolume.setVisible(b);
    }

    private void setVolume() {
        TempData.getInstance().setVolumeSliderValue(GSliderVolume.getValue());

        double sliderPercentage = GSliderVolume.getValue() / GSliderVolume.getMax();

        sliderPercentage += 0.18 * (1 - sliderPercentage);
        double volume = Math.pow(sliderPercentage, 2.71828205201156);
        player.setVolume(volume);
    }

    private void setTableViewRowFactory(TableView<ReleasedAlbum> GTable) {

        GTable.setRowFactory(tableView -> {
            TableRow<ReleasedAlbum> row = new TableRow<>() {
                @Override
                protected void updateItem(ReleasedAlbum item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) return;

                    if (!item.isFeaturing()) getStyleClass().remove("featuring");
                    else if (!getStyleClass().contains("featuring")) getStyleClass().add("featuring");

                    if (!item.isReminded()) getStyleClass().remove("reminded");
                    else if (!getStyleClass().contains("reminded")) getStyleClass().add("reminded");

                    // if next item has other date, add a separator
                    int nextIndex = getTableView().getItems().indexOf(item) + 1;
                    int size = getTableView().getItems().size();
                    if (nextIndex < size) {
                        ReleasedAlbum nextItem = getTableView().getItems().get(nextIndex);
                        if (!item.getReleaseDate().equals(nextItem.getReleaseDate())) {
                            if (!getStyleClass().contains("line")) getStyleClass().add("line");
                        } else getStyleClass().remove("line");
                    } else getStyleClass().remove("line");

                }
            };

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem showOnSpotifyMenuItem = new MenuItem("Show in Spotify");
            final MenuItem copySpotifyLinkMenuItem = new MenuItem("Copy Spotify link");
            final MenuItem showReleasesMenuItem = new MenuItem("Show releases by...");
            final MenuItem showSimilarMenuItem = new MenuItem("Show similar artists");
            final MenuItem discordMessageMenuItem = new MenuItem("Copy Discord message (CTRL+D)");
            final MenuItem followUnfollowMenuItem = new MenuItem();
            final MenuItem remindMenuItem = new MenuItem("Remind");
            final MenuItem moreInfoMenuItem = new MenuItem("More info");
            showOnSpotifyMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                Runtime rt = Runtime.getRuntime();
                boolean browser = TempData.getInstance().getFileData().isUseBrowserInsteadOfApp();
                String url = (browser ? "https://open.spotify.com/album/" : "spotify://album/") + album.getId();
                try {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            copySpotifyLinkMenuItem.setOnAction(event -> {
                String copiedStuff = getSelectedAlbum().getLink();
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(copiedStuff),
                                null
                        );
            });
            showReleasesMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                showReleases(album.getFollowedArtist().getName(), new FollowedArtist(album.getFollowedArtist().getName(), album.getFollowedArtist().getID()));
            });
            showSimilarMenuItem.setOnAction(event -> requestSimilarArtistsView(row.getItem().getFollowedArtist().getID(), row.getItem().getFollowedArtist().getName()));
            discordMessageMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                DiscordMessage.copy(album);
            });
            remindMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                if (Arrays.stream(album.getAlbum().getArtists()).noneMatch(p -> TheEngine.isFollowed(p.getId()))) {
                    Utilities.okMSGBOX("You can't set a reminder for an album if you don't follow the artist!", Alert.AlertType.ERROR);
                    return;
                }

                FileManager.addToRemind(album.getId());
                String countryName = TempData.getInstance().getFileData().getCountryCode().getName();
                if (Utilities.okUndoMSGBOX("Reminder added. The album will appear in the 'New Releases' tab when it is released in " + countryName + "."
                        + "\n\nTo change the country, go to the 'Settings' (3 dots in the top right corner)."))
                    if (FileManager.removeFromRemind(album.getId())) Utilities.okMSGBOX("Reminder removed.");
            });
            moreInfoMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                AlbumInfoDialog albumInfoDialog = new AlbumInfoDialog(album.getAlbum());
                albumInfoDialog.showAndWait();
            });

            showReleasesMenuItem.setDisable(true);
            remindMenuItem.setDisable(true);
            contextMenu.getItems().add(showOnSpotifyMenuItem);
            contextMenu.getItems().add(copySpotifyLinkMenuItem);
            contextMenu.getItems().add(showReleasesMenuItem);
            contextMenu.getItems().add(showSimilarMenuItem);
            contextMenu.getItems().add(discordMessageMenuItem);
            contextMenu.getItems().add(followUnfollowMenuItem);
            contextMenu.getItems().add(remindMenuItem); // oznacz nizej ktory to index!!
            contextMenu.getItems().add(moreInfoMenuItem);
            contextMenu.getItems().forEach(p -> p.setVisible(false));

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            tableView.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) (event) -> {
                getContextMenuLambdaBody(row, showReleasesMenuItem, followUnfollowMenuItem, contextMenu);
            });
            contextMenu.setOnHiding((event) -> {
                getContextMenuLambdaBody(row, showReleasesMenuItem, followUnfollowMenuItem, contextMenu);
            });


            return row;
        });

    }

    private void getContextMenuLambdaBody(TableRow<ReleasedAlbum> row, MenuItem showReleasesMenuItem, MenuItem
            followUnfollowMenuItem, ContextMenu contextMenu) {
        ReleasedAlbum album = row.getItem();
        if (album == null) {
            showReleasesMenuItem.setDisable(true);
            followUnfollowMenuItem.setDisable(true);
            return;
        }


        showReleasesMenuItem.setDisable(false);
        contextMenu.getItems().forEach(p -> p.setVisible(true));
        showReleasesMenuItem.setOnAction((ev) -> requestPerformersView(album));

        MenuItem remindMenuItem = contextMenu.getItems().get(6);
        if (FileManager.getHashSet(FileManager.REMIND_DATA).contains(album.getId())) {
            remindMenuItem.setText("Reminder added");
            remindMenuItem.setDisable(true);
        } else {
            CountryCode countryCode = TempData.getInstance().getFileData().getCountryCode();
            boolean isAvailableInCountry = Arrays.asList(album.getAlbum().getAvailableMarkets()).contains(countryCode);
            remindMenuItem.setText(isAvailableInCountry ? "Available in " + countryCode.getAlpha2() : "Remind");
            remindMenuItem.setDisable(isAvailableInCountry);
        }

        boolean isArtistFollowed = TheEngine.isFollowed(album.getFollowedArtist().getID());
        followUnfollowMenuItem.setDisable(false);
        followUnfollowMenuItem.setText((isArtistFollowed ? "Unfollow " : "Follow ") + album.getFollowedArtist().getName());
        followUnfollowMenuItem.setOnAction((ev) -> {
            if (isArtistFollowed) TheEngine.getInstance().unfollowArtistID(album.getFollowedArtist().getID());
            else TheEngine.getInstance().followArtistID(album.getFollowedArtist().getID());
        });
    }

    private void copyTracklist(TempAlbumInfo info) {
        ClipboardContent content = new ClipboardContent();
        StringBuilder sb = new StringBuilder();

        for (TrackSimplified track : info.trackList()) {
            List<ArtistSimplified> performers = getPerformers(info.album(), track);

            sb.append(track.getTrackNumber()).append(". ")
                    .append(track.getName())
                    .append(" (").append(Utilities.convertMsToDuration(track.getDurationMs())).append(")")
                    .append(performers.isEmpty() ? "" : performers.stream().map(ArtistSimplified::getName)
                            .collect(Collectors.joining(", ", " [", "]")))
                    .append("\n");
        }

        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void copyPreviews(TempAlbumInfo info) {
        ClipboardContent content = new ClipboardContent();
        StringBuilder sb = new StringBuilder();

        for (TrackSimplified track : info.trackList()) {
            List<ArtistSimplified> performers = getPerformers(info.album(), track);

            String previewUrl = track.getPreviewUrl();
            if (previewUrl != null && previewUrl.contains("?"))
                previewUrl = previewUrl.substring(0, previewUrl.indexOf("?"));

            sb.append(track.getTrackNumber()).append(". ")
                    .append(track.getName())
                    .append(" (").append(Utilities.convertMsToDuration(track.getDurationMs())).append(")")
                    .append(performers.isEmpty() ? "" : performers.stream().map(ArtistSimplified::getName)
                            .collect(Collectors.joining(", ", " [", "]")))
                    .append("\n").append(previewUrl).append("\n\n");
        }

        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void requestSimilarArtistsView(String id, String name) {
        List<FollowedArtist> similarArtists = new ArrayList<>();
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                List<Artist> artists = List.of(TheEngine.getInstance().getRelatedArtists(id));
                similarArtists.addAll(artists.stream()
                        .map(p -> new FollowedArtist(p.getName(), p.getId()))
                        .sorted(Comparator.comparing(FollowedArtist::getName))
                        .toList());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> showSimilarArtists(name, similarArtists)));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showSimilarArtists(String name, List<FollowedArtist> similarArtists) {
        similarArtists.removeIf(p -> TheEngine.isFollowed(p.getID()));
        String title = "Artists similar to " + name;
        showModalWindowWithArtistsToPick(similarArtists, title);
    }

    private void requestPerformersView(ReleasedAlbum album) {
        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                if (!infoHashMap.containsKey(album.getId())) downloadInfoForAlbum(album);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            List<FollowedArtist> artistsAndPerformers = getArtistsAndPerformers(infoHashMap.get(album.getId()).trackList())
                    .stream().map(p -> new FollowedArtist(p.getName(), p.getId())).collect(Collectors.toList());
            artistsAndPerformers.removeIf(p -> oneArtist && p.getID().equals(allAlbums.get(0).getFollowedArtist().getID()));
            showModalWindowWithArtistsToPick(artistsAndPerformers, "Pick an artist");
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();


    }

    private void showModalWindowWithArtistsToPick(List<FollowedArtist> artists, String title) {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);


        ListView<FollowedArtist> list = new ListView<>();
        list.setPlaceholder(new Label("Nothing to be found here"));
        list.setCellFactory((param) -> new ListCell<>() {
            @Override
            protected void updateItem(FollowedArtist item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    return;
                }
                setText(item.getName());
            }
        });
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) return;
            showReleases(newSelection.getName(), newSelection);
            stage.close();
        });
        list.setMaxHeight(500);

        list.setItems(FXCollections.observableArrayList(artists));
        list.setPrefHeight(artists.size() * 24 + 2);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(list);

        if (artists.size() > 1) {
            Button button = new Button("Select all");
            button.setOnAction(e -> {
                showReleases("Multiple artists", artists.toArray(new FollowedArtist[0]));
                stage.close();
            });
            Platform.runLater(button::requestFocus);
            vBox.getChildren().add(button);
        }

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void showReleases(String title, FollowedArtist... artists) {
        stopCurrent();
        ReleasesProcessor processor = new ReleasesProcessor(artists);
        disableAllElements(true);
        showProgressBar(true);

        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                processor.currentArtistConsumer(GTextLoadingArtist::setText);
                processor.process();
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            showReleases(title, processor);
            disableAllElements(false);
            showProgressBar(false);
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void showReleases(String title, ReleasesProcessor processor) {
        stopCurrent();
        VBox oldVbox = controllerOutline.getAlbumsVBOX();
        if (oldVbox != null) VBOXSTACK.push(new VBoxStack(oldVbox, controllerOutline.getAlbumsTitle()));
        controllerOutline.setAlbumsVBOX(UtilShowAlbums.getAlbumsVBOX(processor.getNewAlbums(), processor.getAllAlbums()));
        controllerOutline.setAlbumsTitle(title);
    }

    private void showProgressBar(boolean b) {
        GProgressBar.setVisible(b);
        GTextLoadingArtist.setText("");
        GTextLoadingArtist.setVisible(b);
    }

    private void disableAllElements(boolean b) {
        GSliderVolume.setDisable(b);
        GTableAllReleases.setDisable(b);
        GTableNewReleases.setDisable(b);
        GListTracklist.setDisable(b);
    }

    public VBox getGMainVBOX() {
        return GMainVBOX;
    }

    private List<ArtistSimplified> getPerformers(Album album, List<TrackSimplified> tracklist) {
        HashSet<String> artistSet = Arrays.stream(album.getArtists()).map(ArtistSimplified::getId).collect(Collectors.toCollection(HashSet::new));

        List<ArtistSimplified> performers = getArtistsAndPerformers(tracklist);
        performers.removeIf(p -> artistSet.contains(p.getId()));

        return performers;
    }

    private List<ArtistSimplified> getPerformers(Album album, TrackSimplified track) {
        HashSet<String> artistSet = Arrays.stream(album.getArtists()).map(ArtistSimplified::getId).collect(Collectors.toCollection(HashSet::new));

        List<ArtistSimplified> performers = new ArrayList<>(List.of(track.getArtists()));
        performers.removeIf(p -> artistSet.contains(p.getId()));

        return performers;
    }


    private List<ArtistSimplified> getArtistsAndPerformers(List<TrackSimplified> tracklist) {
        List<ArtistSimplified> artistsAndPerformers = new LinkedList<>();
        HashSet<String> nameSet = new HashSet<>();
        for (TrackSimplified track : tracklist)
            for (ArtistSimplified artist : track.getArtists())
                if (nameSet.add(artist.getId()))
                    artistsAndPerformers.add(artist);

        return artistsAndPerformers.stream().sorted(Comparator.comparing(ArtistSimplified::getName)).collect(Collectors.toList());
    }

    private TextFlow getInfoTextFlow(List<ArtistSimplified> artists) {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(270);
        if (artists.isEmpty()) return flow;

        for (ArtistSimplified a : artists) {
            Text text = new Text(a.getName());
            if (TheEngine.isFollowed(a.getId())) text.setStyle("-fx-font-weight: bold");
            flow.getChildren().addAll(text, new Text(", "));
        }
        flow.getChildren().remove(flow.getChildren().size() - 1);

        return flow;
    }

}