package me.nefelion.spotifynotifier.gui.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.nefelion.spotifynotifier.*;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.LoadingDialog;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerFollowed {
    private static final String DEFAULT_CONTROL_INNER_BACKGROUND = "derive(-fx-base,80%)";
    private static final String HIGHLIGHTED_CONTROL_INNER_BACKGROUND = "derive(palegreen, 50%)";
    private final Label placeholderLabelGListFollowed, placeholderLabelGListSpotify;
    private ControllerOutline controllerOutline;
    private String lastSearch = "", newCoverUrl = "", todayCoverUrl = "", tomorrowCoverUrl = "", currentArtistName = "";
    private double maxGListSpotifyPopularity = Double.MAX_VALUE;
    private Task<Void> task;
    private Timer elapsed;
    private boolean processing = false;
    private int pageNumber;

    @FXML
    private VBox GMainVBOX, GVboxInfo;
    @FXML
    private ListView<FollowedArtist> GListFollowed;
    @FXML
    private ListView<Artist> GListSpotify;
    @FXML
    private TextField GTextFieldSearchFollowed, GTextFieldSearchSpotify;
    @FXML
    private Label GLabelCurrentArtist, GLabelProcessedArtists, GLabelLoadedReleases, GLabelNewReleases, GLabelPercentage,
            GLabelLastChecked, GLabelTimeElapsed, GLabelNumberOfArtists, GLabelNewReleasesHour, GLabelToday, GLabelTomorrow,
            GLabelLoadedReleasesP, GLabelNewReleasesP, GLabelTodayP, GLabelTomorrowP;
    @FXML
    private ProgressBar GProgressBar;
    @FXML
    private Button GButtonCheckReleases, GButtonAbort;
    @FXML
    private ImageView GImageViewCover;


    public ControllerFollowed() {
        placeholderLabelGListFollowed = initializePlaceholderLabelGListFollowedArtists();
        placeholderLabelGListSpotify = initializePlaceholderLabelGListSearchSpotifyArtists();
    }

    public VBox getGMainVBOX() {
        return GMainVBOX;
    }

    public void setControllerOutline(ControllerOutline controllerOutline) {
        this.controllerOutline = controllerOutline;
    }


    @FXML
    private void initialize() {
        initializeGListFollowedArtists();
        initializeGListSearchSpotifyArtists();
        initializeGVboxInfo();
        initializeGLabelLastChecked();
        initializeGTextFieldSearchFollowed();
        initializeGTextFieldSearchSpotify();
        initializeGButtonCheckReleases();
        initializeGLabelNewReleasesHour();

        startRefreshingTimer();
    }

    private void startRefreshingTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    refreshGLabelLastChecked();
                    refreshGListFollowedArtists();
                    refreshCoverImages(GLabelNewReleasesP, newCoverUrl);
                    refreshCoverImages(GLabelTodayP, todayCoverUrl);
                    refreshCoverImages(GLabelTomorrowP, tomorrowCoverUrl);
                });
            }
        }, 0, 1000);
    }

    private void explore(int iterations) {
        LoadingDialog dialog = new LoadingDialog();
        dialog.setTitle("Explore, " + (iterations == 1 ? "1 iteration" : iterations + " iterations"));
        dialog.setHeaderText("Loading similar artists to explore...");
        dialog.setProgressText("Loaded artists: 0");

        ExploreProcessor processor = new ExploreProcessor()
                .setIterations(iterations)
                .setArtistCountConsumer(count -> dialog.setProgressText("Loaded artists: " + count))
                .setProgressConsumer(dialog::setProgress);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                processor.process();
                return null;
            }
        };
        task.setOnSucceeded((e) -> {
            dialog.close();
            List<FollowedArtist> artists = processor.getOutputArtists().stream()
                    .map(p -> new FollowedArtist(p.getName(), p.getId())).collect(Collectors.toList());

            explore(artists);
        });
        dialog.setCancelListener(task::cancel);

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        dialog.showAndWait();
    }

    private void doTheDialogStuff() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Explore");
        dialog.setHeaderText("Explore depth");
        ((Stage) (dialog.getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));

        // set the button types
        ButtonType exploreButtonType = new ButtonType("Explore", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(exploreButtonType, ButtonType.CANCEL);

        // create the explore button and disable it by default
        Node exploreButton = dialog.getDialogPane().lookupButton(exploreButtonType);
        exploreButton.setDisable(true);

        // create the radio buttons
        RadioButton rb1 = new RadioButton("1");
        RadioButton rb2 = new RadioButton("2");
        ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(rb1, rb2);
        rb1.setOnAction(e -> exploreButton.setDisable(false));
        rb2.setOnAction(e -> exploreButton.setDisable(false));
        dialog.getDialogPane().setContent(new VBox(rb1, rb2));

        dialog.getDialogPane().lookupButton(exploreButtonType).addEventFilter(ActionEvent.ACTION, event -> {
            dialog.close();
            Platform.runLater(() -> explore(rb1.isSelected() ? 1 : 2));
        });
        dialog.showAndWait();
    }

    @FXML
    private void onKeyTypedSearchFollowed(KeyEvent keyEvent) {
        refreshGListFollowedArtists();
    }

    @FXML
    private void onActionTextFieldSearchSpotify(ActionEvent actionEvent) {
        searchSpotify();
    }

    @FXML
    private void onActionCheckReleases(ActionEvent actionEvent) {
        List<FollowedArtist> artists = new ArrayList<>(TempData.getInstance().getFileData().getFollowedArtists().stream()
                .sorted(Comparator.comparing(FollowedArtist::getName)).toList());

        checkReleases(artists);
    }

    private void checkReleases(List<FollowedArtist> list) {
        processing = true;
        resetInfoBoard();
        GButtonCheckReleases.setDisable(true);
        GVboxInfo.setVisible(true);

        ReleasesProcessor processor = getDefaultReleasesProcessor(list);
        startCountingProcessorTime();


        task = new Task<>() {
            @Override
            public Void call() {
                processor.process();
                processing = false;
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            GButtonCheckReleases.setDisable(false);
            FileData fileData = TempData.getInstance().getFileData();
            fileData.setLastChecked(Utilities.now());
            FileManager.saveFileData(fileData);

            showReleases("Releases", processor);

            resetInfoBoard();
            GVboxInfo.setVisible(false);
            refreshGLabelLastChecked();
        });
        task.setOnCancelled(e -> {
            GVboxInfo.setVisible(false);
            GButtonCheckReleases.setDisable(false);
            resetInfoBoard();
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void explore(List<FollowedArtist> list) {
        processing = true;
        resetInfoBoard();
        GButtonCheckReleases.setDisable(true);
        GVboxInfo.setVisible(true);
        GLabelNewReleases.setText("N/A");

        ReleasesProcessor processor = getDefaultReleasesProcessor(list);
        processor.setMaximumPages(5);
        processor.dischardFollowedArtists(true);

        startCountingProcessorTime();


        task = new Task<>() {
            @Override
            public Void call() {
                processor.process();
                processing = false;
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            GButtonCheckReleases.setDisable(false);
            showReleases("Explore releases", processor);

            resetInfoBoard();
            GVboxInfo.setVisible(false);
            refreshGLabelLastChecked();
        });
        task.setOnCancelled(e -> {
            GVboxInfo.setVisible(false);
            GButtonCheckReleases.setDisable(false);
            resetInfoBoard();
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private ReleasesProcessor getDefaultReleasesProcessor(List<FollowedArtist> list) {
        ReleasesProcessor processor = new ReleasesProcessor(list);
        processor.progressConsumer((var) -> {
                    GLabelPercentage.setText((int) (var * 100) + "%");
                    GProgressBar.setProgress(var);
                })
                .currentArtistConsumer(this::setCurrentArtistName)
                .processedArtistsNumberConsumer(art -> GLabelProcessedArtists.setText(art + " / " + list.size()))
                .loadedReleasesNumberConsumer(n -> setLoadedReleasesText(n, GLabelLoadedReleasesP, GLabelLoadedReleases))
                .newReleasesNumberConsumer(n -> {
                    if (GLabelNewReleases.getText().equals("0") && n > 0) {
                        GVboxInfo.setStyle("-fx-background-color: " + HIGHLIGHTED_CONTROL_INNER_BACKGROUND + ";");
                        Toolkit.getDefaultToolkit().beep();
                    }

                    setLoadedReleasesText(n, GLabelNewReleasesP, GLabelNewReleases);
                })
                .todayReleasesNumberConsumer(n -> setLoadedReleasesText(n, GLabelTodayP, GLabelToday))
                .tomorrowReleasesNumberConsumer(n -> setLoadedReleasesText(n, GLabelTomorrowP, GLabelTomorrow))
                .newReleaseConsumer(r -> {
                    addReleaseToLabel(r, GLabelNewReleasesP);
                    try {
                        newCoverUrl = r.getAlbum().getImages()[1].getUrl();
                    } catch (Exception ignored) {
                    }
                })
                .todayReleaseConsumer(r -> {
                    addReleaseToLabel(r, GLabelTodayP);
                    try {
                        todayCoverUrl = r.getAlbum().getImages()[1].getUrl();
                    } catch (Exception ignored) {
                    }
                })
                .tomorrowReleaseConsumer(r -> {
                    addReleaseToLabel(r, GLabelTomorrowP);
                    try {
                        tomorrowCoverUrl = r.getAlbum().getImages()[1].getUrl();
                    } catch (Exception ignored) {
                    }
                })
                .pageNumberConsumer(n -> pageNumber = n)
                .numberOfPagesConsumer(this::setNumberOfPages);
        return processor;
    }

    private void startCountingProcessorTime() {
        final long startMs = System.currentTimeMillis();
        elapsed = new Timer(true);
        elapsed.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() ->
                        GLabelTimeElapsed.setText(Utilities.convertMsToDuration((int) (System.currentTimeMillis() - startMs)))
                );
            }
        }, 0, 1000);
    }


    private void setNumberOfPages(Integer integer) {
        if (pageNumber == 0) return;
        if (integer < 4) return;
        int percentage = (pageNumber + 1) * 100 / integer;

        Platform.runLater(() -> GLabelCurrentArtist.setText(percentage + "% " + currentArtistName));
    }

    private void setCurrentArtistName(String s) {
        currentArtistName = s;
        GLabelCurrentArtist.setText(currentArtistName);
    }


    private void refreshCoverImages(Label gLabel, String coverUrl) {
        Tooltip tooltip = gLabel.getTooltip();
        if (coverUrl != null && !coverUrl.isEmpty() && tooltip != null) {
            ImageView node = new ImageView(new Image(coverUrl));
            tooltip.setGraphic(node);
            coverUrl = null;
        }
    }


    private void addReleaseToLabel(ReleasedAlbum r, Label gLabel) {
        Platform.runLater(() -> {
            if (gLabel.getTooltip() == null) {
                Tooltip tooltip = new Tooltip();
                tooltip.setText(r.toString());
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.INDEFINITE);
                gLabel.setTooltip(tooltip);
                gLabel.setUnderline(true);
                return;
            }

            String newText = gLabel.getTooltip().getText() + "\n" + r;
            String[] lines = newText.split("\n");
            final int maxLines = 20;
            if (lines.length > maxLines) {
                newText = String.join("\n", Arrays.copyOfRange(lines, lines.length - maxLines, lines.length));
            }

            gLabel.getTooltip().setText(newText);
        });
    }

    private void setLoadedReleasesText(Integer integer, Label glabelP, Label glabel) {
        if ((int) glabel.getOpacity() != 1 && integer > 0) {
            glabelP.setOpacity(1);
            glabel.setOpacity(1);
        }
        glabel.setText(integer + "");
    }

    @FXML
    private void onActionGButtonAbort(ActionEvent actionEvent) {
        task.cancel();
    }


    private void initializeGLabelNewReleasesHour() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Pacific/Tongatapu"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timestamp = calendar.getTimeInMillis();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(timestamp);

        String time = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        String time12 = String.format("%02d:%02d %s",
                calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
        GLabelNewReleasesHour.setText("Tomorrow's albums appear at " + time + " (" + time12 + ")");
        GLabelNewReleasesHour.setStyle("-fx-font-style: italic; -fx-opacity: 0.4;");
    }

    private void initializeGButtonCheckReleases() {
        GButtonCheckReleases.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                CustomMenuItem explore1 = new CustomMenuItem(new Label("Explore"));
                explore1.setOnAction(event1 -> doTheDialogStuff());
                Tooltip tooltip = new Tooltip("The Explore functionality shows all the albums of all the artists who are similar to the artists you follow.");
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setStyle("-fx-font-size: 14px;");
                Tooltip.install(explore1.getContent(), tooltip);
                GButtonCheckReleases.setContextMenu(new ContextMenu(explore1));
            }
        });

        Tooltip tooltip = new Tooltip("Right-click for more options");
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-font-size: 14px;");
        GButtonCheckReleases.setTooltip(tooltip);


        String defaultStyle = """
                -fx-background-color:
                        linear-gradient(#f0ff35,#a9ff00),
                        radial-gradient(center 50% -40%, radius 200%, #b8ee36 45%, #80c800 50%);
                -fx-background-radius: 6,5;
                -fx-background-insets: 0,1;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0.0, 0, 1);
                -fx-text-fill: #395306;
                """;

        String highlightedStyle = """
                -fx-background-color:
                        linear-gradient(#a9ff00,#f0ff35),
                        radial-gradient(center 50% -40%, radius 200%, #80c800 45%, #b8ee36 50%);
                -fx-background-radius: 6,5;
                -fx-background-insets: 0,1;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0.0, 0, 1);
                -fx-text-fill: #395306;
                """;


        GButtonCheckReleases.setStyle(defaultStyle);
        GButtonCheckReleases.setOnMouseEntered(event -> GButtonCheckReleases.setStyle(highlightedStyle));
        GButtonCheckReleases.setOnMouseExited(event -> GButtonCheckReleases.setStyle(defaultStyle));
    }

    private Label initializePlaceholderLabelGListFollowedArtists() {
        final Label label;
        label = new Label();
        label.setTextFill(Color.GRAY);
        return label;
    }

    private Label initializePlaceholderLabelGListSearchSpotifyArtists() {
        final Label label;
        label = new Label("Search for the artists you want to follow");
        label.setTextFill(Color.GRAY);
        return label;
    }

    private void initializeGListFollowedArtists() {
        GListFollowed.setPlaceholder(placeholderLabelGListFollowed);

        GListFollowed.setCellFactory(listView -> {
            ListCell<FollowedArtist> cell = new ListCell<>() {
                @Override
                protected void updateItem(FollowedArtist item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) setText(null);
                    else setText(item.getName());
                }
            };

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem showReleasesMenuItem = new MenuItem("Show releases");
            final MenuItem showOnSpotifyMenuItem = new MenuItem("Show on Spotify");
            final MenuItem unfollowMenuItem = new MenuItem("Unfollow");

            showReleasesMenuItem.setOnAction(event -> {
                FollowedArtist artist = cell.getItem();
                showFollowedArtistReleases(artist);
            });
            showOnSpotifyMenuItem.setOnAction(event -> {
                FollowedArtist artist = cell.getItem();
                boolean browser = TempData.getInstance().getFileData().isUseBrowserInsteadOfApp();
                String url = (browser ? "https://open.spotify.com/artist/" : "spotify://artist/") + artist.getID();
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            unfollowMenuItem.setOnAction(event -> {
                FollowedArtist artist = cell.getItem();
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        TheEngine.getInstance().unfollowArtistID(artist.getID());
                        return null;
                    }
                };
                task.setOnSucceeded(e -> refreshGListFollowedArtists());
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            });

            contextMenu.getItems().add(showReleasesMenuItem);
            contextMenu.getItems().add(showOnSpotifyMenuItem);
            contextMenu.getItems().add(unfollowMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            cell.contextMenuProperty().bind(
                    Bindings.when(cell.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            return cell;
        });

        GListFollowed.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                FollowedArtist artist = GListFollowed.getSelectionModel().getSelectedItem();
                if (artist == null) return;
                showFollowedArtistReleases(artist);
            }
        });


        refreshGListFollowedArtists();
    }

    private void initializeGListSearchSpotifyArtists() {
        GListSpotify.setPlaceholder(placeholderLabelGListSpotify);

        GListSpotify.setCellFactory(listView -> {
            ListCell<Artist> cell = new ListCell<>() {
                @Override
                protected void updateItem(Artist item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("-fx-control-inner-background: " + DEFAULT_CONTROL_INNER_BACKGROUND + ";");
                        setTooltip(null);
                    } else {
                        setText(item.getName());
                        StringBuilder style = new StringBuilder();

                        if (TheEngine.isFollowed(item.getId())) {
                            style.append("-fx-control-inner-background: " + HIGHLIGHTED_CONTROL_INNER_BACKGROUND + ";");
                        } else {
                            style.append("-fx-control-inner-background: " + DEFAULT_CONTROL_INNER_BACKGROUND + ";");
                        }

                        double vis = 0.5 + (item.getPopularity() / maxGListSpotifyPopularity) / 2;
                        style.append("-fx-text-fill: " + "rgba(0,0,0,").append(vis).append(");");

                        setStyle(style.toString());

                        Tooltip tooltip = new Tooltip("...");
                        tooltip.setShowDelay(Duration.ZERO);
                        Task<Void> task = new Task<>() {
                            @Override
                            protected Void call() {

                                try {
                                    ImageView node = new ImageView(new Image(item.getImages()[1].getUrl()));
                                    node.setFitHeight(160);
                                    node.setPreserveRatio(true);
                                    Platform.runLater(() -> {
                                        tooltip.setGraphic(node);
                                        tooltip.setText("");
                                    });
                                } catch (Exception e) {
                                    Platform.runLater(() -> tooltip.setText("No image available"));
                                }

                                return null;
                            }
                        };
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();

                        setTooltip(tooltip);

                    }

                }
            };

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem followMenuItem = new MenuItem("Follow");
            final MenuItem showReleasesMenuItem = new MenuItem("Show releases");


            showReleasesMenuItem.setOnAction(event -> {
                Artist artist = cell.getItem();
                showArtistsReleases(artist);
            });
            followMenuItem.setOnAction(event -> {
                Artist artist = cell.getItem();
                followArtist(artist);
            });

            contextMenu.getItems().add(followMenuItem);
            contextMenu.getItems().add(showReleasesMenuItem);


            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            cell.contextMenuProperty().bind(
                    Bindings.when(cell.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            return cell;
        });

        GListSpotify.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Artist artist = GListSpotify.getSelectionModel().getSelectedItem();
                if (artist == null) return;
                followArtist(artist);
            }
        });
    }

    private void initializeGTextFieldSearchSpotify() {
        GTextFieldSearchSpotify.focusedProperty().addListener(getFocusChangeListener(GTextFieldSearchSpotify));
        GTextFieldSearchSpotify.setOnKeyTyped(e -> {
            if (GTextFieldSearchSpotify.getText().trim().isEmpty()) {
                placeholderLabelGListSpotify.setText("");
                GListSpotify.setItems(null);
            }
        });
    }

    private void initializeGTextFieldSearchFollowed() {
        GTextFieldSearchFollowed.setDisable(TempData.getInstance().getFileData().getFollowedArtists().isEmpty());

        GTextFieldSearchFollowed.focusedProperty().addListener(getFocusChangeListener(GTextFieldSearchFollowed));

        GTextFieldSearchFollowed.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                if (GListFollowed.getItems().size() == 1) {
                    FollowedArtist artist = GListFollowed.getItems().get(0);
                    showFollowedArtistReleases(artist);
                }
            }
        });

    }

    private void initializeGVboxInfo() {
        GVboxInfo.setVisible(false);
        resetInfoBoard();
    }

    private void initializeGLabelLastChecked() {
        refreshGLabelLastChecked();
    }


    private void followArtist(Artist artist) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                GMainVBOX.setCursor(Cursor.WAIT);
                TheEngine.getInstance().followArtistID(artist.getId());
                return true;
            }
        };
        task.setOnSucceeded(t -> {
            refreshGListFollowedArtists();
            GMainVBOX.setCursor(Cursor.DEFAULT);
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void refreshGLabelLastChecked() {
        String timeAgo = Utilities.getTimeAgo(TempData.getInstance().getFileData().getLastChecked());
        GLabelLastChecked.setText(timeAgo.equals("-") ? "" : "Last checked: " + timeAgo);
    }

    private void refreshGLabelNumberOfArtists(int size) {
        GTextFieldSearchFollowed.setDisable(size == 0);
        GLabelNumberOfArtists.setText("" + (size > 0 ? size : ""));
    }

    private void searchSpotify() {
        String search = GTextFieldSearchSpotify.getText().trim();
        if (search.isEmpty() || search.equals(lastSearch)) return;
        lastSearch = search;

        List<Artist> artistList = new ArrayList<>();
        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                artistList.addAll(TheEngine.getInstance().searchArtist(search));
                return true;
            }
        };

        task.setOnSucceeded(e -> updateGListSearchSpotifyArtistsWith(artistList));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void refreshGListFollowedArtists() {
        String search = GTextFieldSearchFollowed.getText().trim();
        List<FollowedArtist> followedArtists = TempData.getInstance().getFileData().getFollowedArtists()
                .stream().sorted(Comparator.comparing(FollowedArtist::getName)).toList();

        String s = search.toLowerCase();
        Comparator<FollowedArtist> startWithComparator =
                (p, q) -> Boolean.compare(q.getName().toLowerCase().startsWith(s), p.getName().toLowerCase().startsWith(s));

        GListFollowed.setItems(FXCollections.observableList(followedArtists
                .stream()
                .filter(p -> p.getName().toLowerCase().contains(s) || areFirstLetters(p.getName(), s))
                .sorted(startWithComparator.thenComparing(FollowedArtist::getName))
                .collect(Collectors.toList())));

        placeholderLabelGListFollowed.setText(followedArtists.isEmpty()
                ? "You do not follow any artists yet"
                : "Nothing matches a search for '" + search + "'");

        refreshGLabelNumberOfArtists(followedArtists.size());
        if (!processing) GButtonCheckReleases.setDisable(followedArtists.isEmpty());
    }

    private boolean areFirstLetters(String p, String search) {
        String firstLetters = Arrays.stream(p.split(" ")).map(s -> s.substring(0, 1)).collect(Collectors.joining());

        return firstLetters.toLowerCase().startsWith(search.toLowerCase());
    }

    private void updateGListSearchSpotifyArtistsWith(List<Artist> list) {
        String search = GTextFieldSearchSpotify.getText().trim();

        Optional<Integer> optional = list.stream().max(Comparator.comparingInt(Artist::getPopularity)).map(Artist::getPopularity);
        optional.ifPresent(integer -> maxGListSpotifyPopularity = (double) integer);
        placeholderLabelGListSpotify.setText("Can't find '" + search + "' on Spotify");
        GListSpotify.setItems(FXCollections.observableArrayList(list));
    }

    private void resetInfoBoard() {
        GLabelPercentage.setText("");
        GProgressBar.setProgress(0.0);
        GLabelCurrentArtist.setText("...");
        GLabelProcessedArtists.setText("0");
        GLabelLoadedReleases.setText("0");
        GLabelNewReleases.setText("0");
        GLabelToday.setText("0");
        GLabelTomorrow.setText("0");
        if (elapsed != null) elapsed.cancel();
        GLabelTimeElapsed.setText("0:00");
        GVboxInfo.setStyle("");


        setOpacity(0.5, GLabelToday, GLabelTodayP,
                GLabelTomorrow, GLabelTomorrowP,
                GLabelNewReleases, GLabelNewReleasesP,
                GLabelLoadedReleases, GLabelLoadedReleasesP);

        removeTooltipsAndUnderline(GLabelTodayP, GLabelTomorrowP, GLabelNewReleasesP);
    }

    private void removeTooltipsAndUnderline(Label... gLabel) {
        for (Label label : gLabel) {
            label.setTooltip(null);
            label.setUnderline(false);
        }
    }

    private void setOpacity(Double opacity, Node... node) {
        for (Node n : node) n.setOpacity(opacity);
    }

    private void showReleases(String title, ReleasesProcessor processor) {
        controllerOutline.showAlbums(title, processor);
        controllerOutline.selectTab(ControllerOutline.TAB.ALBUMS);
    }

    private void showArtistsReleases(Artist artist) {
        showFollowedArtistReleases(new FollowedArtist(artist.getName(), artist.getId()));
    }

    private void showFollowedArtistReleases(FollowedArtist artist) {
        GMainVBOX.setCursor(Cursor.WAIT);
        ReleasesProcessor processor = new ReleasesProcessor(artist);
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                processor.process();
                return true;
            }
        };
        task.setOnSucceeded(b -> {
            showReleases(artist.getName(), processor);
            GMainVBOX.setCursor(Cursor.DEFAULT);
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private ChangeListener<Boolean> getFocusChangeListener(TextField GTextField) {
        return (arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                Platform.runLater(GTextField::selectAll);
            }
        };
    }

}
