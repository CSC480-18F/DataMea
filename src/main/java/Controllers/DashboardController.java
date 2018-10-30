package Controllers;

import Engine.Main;
import Engine.User;
import Engine.Email;
import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.SunburstChart;
import eu.hansolo.tilesfx.events.TileEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController implements Initializable {

    @FXML
    private JFXDrawer drawer;

    @FXML
    private JFXDrawer filtersDrawer;

    @FXML
    private JFXHamburger hamburger;

    @FXML
    private JFXMasonryPane masonryPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private GridPane topBarGridPane;

    @FXML
    private ColumnConstraints gridPaneLeft;

    @FXML
    private ColumnConstraints gridPaneRight;

    @FXML
    private ColumnConstraints centerColumn;

    @FXML
    private JFXButton filtersButton;

    @FXML
    public JFXProgressBar progressBar;


    //------------------Declaring Variables------------------//
    private static Stage myStage;
    private DashboardDrawer dashboardDrawer;
    private FilterDrawer filterDrawerClass;
    private static BooleanProperty loadedFromLoginScreen = new SimpleBooleanProperty(false);
    private static ArrayList<ChartData> topSendersData = new ArrayList<>();
    private Tile topSendersRadialChart;
    private GridPane heatMapGridPane;
    private Tile foldersSunburstChart;
    private User currentUser;
    private static BooleanProperty homeOnCloseRequest = new SimpleBooleanProperty(false);
    private DonutChart domainDonutChart;
    private Map<String, Long> domains;
    private ObservableList<PieChart.Data> domainsData = FXCollections.observableArrayList();
    private Tile attachmentsRadialChart;
    private Map<String, Long> attachments;
    private ArrayList<ChartData> attachmentsData = new ArrayList<>();
    private Map<String, Long> languages;
    private ArrayList<ChartData> languagesData = new ArrayList<>();
    public static Tile sentimentGauge;
    private long              lastTimerCall;
    private AnimationTimer    timer;
    private static final Random RND = new Random();

    public static void setStage(Stage s) {
        myStage = s;
    }

    public static void addTopSendersData(ChartData d) {
        //topSendersData.add(d);
        topSendersData.add(d);
    }

    public User getUser(){
        return currentUser;
    }

    public static void setLoadedFromLoginScreenToTrue() {
        loadedFromLoginScreen.setValue(true);
    }

    public static void setHomeOnCloseRequest(Boolean b) {
        homeOnCloseRequest.setValue(b);
    }

    private void addFilter(String name){
        if(!filterDrawerClass.filtersChipView.getChips().contains(name)){
            filterDrawerClass.filtersChipView.getChips().add(name);
            JFXChip chip = (JFXChip) filterDrawerClass.filtersChipView.lookup(".jfx-chip-view .jfx-chip");
            chip.setCursor(Cursor.HAND);
        }else{
            System.out.println("Filter already added");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        String scrollPaneCss = this.getClass().getClassLoader().getResource("scrollpane.css").toExternalForm();
        scrollPane.getStylesheets().add(scrollPaneCss);

        //Resizing crap, this took way to long to figure out thanks javafx
        scrollPane.setFitToWidth(true);
        Platform.runLater(() -> scrollPane.requestLayout());
        topBarGridPane.prefWidthProperty().bind(anchorPane.widthProperty());
        masonryPane.prefWidthProperty().bind(anchorPane.widthProperty());
        centerColumn.maxWidthProperty().bind(topBarGridPane.widthProperty());
        gridPaneLeft.maxWidthProperty().bind(topBarGridPane.widthProperty());
        gridPaneRight.maxWidthProperty().bind(topBarGridPane.widthProperty());
        drawer.prefHeightProperty().bind(anchorPane.heightProperty());
        filtersDrawer.prefWidthProperty().bind(anchorPane.widthProperty());
        progressBar.prefWidthProperty().bind(anchorPane.widthProperty());

        drawer.setVisible(false);
        filtersDrawer.setVisible(false);
        //Allows you to click through the drawer if it's not visible (so we set it invisible when it's not open)
        drawer.setPickOnBounds(false);
        filtersDrawer.setPickOnBounds(false);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Dashboard_Drawer.fxml"));
            VBox box = loader.load();
            dashboardDrawer = loader.getController();
            dashboardDrawer.dashboardDrawerVBox.maxHeightProperty().bind(anchorPane.heightProperty());
            drawer.setSidePane(box);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Filters_Drawer.fxml"));
            AnchorPane filtersPane = loader.load();
            filterDrawerClass = loader.getController();
            filterDrawerClass.filtersAnchorPane.maxWidthProperty().bind(anchorPane.widthProperty());
            filtersDrawer.setSidePane(filtersPane);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*Node scrollbar = scrollPane.lookup(".scroll-bar:vertical .thumb");
        ChangeListener<Object> changeListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                Bounds bounds = scrollPane.getViewportBounds();
                int left = -1 * (int) bounds.getMinX();
                int right = left + (int) bounds.getWidth();
                System.out.println("hval:" + scrollPane.getHvalue() + " left:" + left + " right:" + right);
                Platform.runLater(()->{
                    scrollbar.setOpacity(0.1);
                });
            }
        };
        scrollPane.viewportBoundsProperty().addListener(changeListener);
        scrollPane.hvalueProperty().addListener(changeListener);
        scrollPane.vvalueProperty().addListener(changeListener);*/

        HamburgerBasicCloseTransition basicCloseTransition = new HamburgerBasicCloseTransition(hamburger);
        basicCloseTransition.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            basicCloseTransition.setRate(basicCloseTransition.getRate() * -1);
            basicCloseTransition.play();
            if (drawer.isOpened()) {
                DashboardDrawer.setShrinkListToTrue();
                drawer.close();
                hamburger.setDisable(true);
                //This starts a Thread but immediately schedules it to run after 500 milliseconds, so the drawer closing animation can run before making the drawer invisible
                //Thanks for writing this DL, spent almost 2 hours trying to get the drawer set to invisible after the closing animation played lol
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        drawer.setVisible(false);
                        hamburger.setDisable(false);
                    }
                }, 500, TimeUnit.MILLISECONDS);
            } else {
                drawer.setVisible(true);
                drawer.open();
                DashboardDrawer.setExpandListToTrue();
            }
        });

        filtersButton.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            if (filtersDrawer.isOpened()) {
                filtersDrawer.close();
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        filtersDrawer.setVisible(false);
                    }
                }, 500, TimeUnit.MILLISECONDS);
            } else {
                filtersDrawer.setVisible(true);
                filtersDrawer.open();

            }
        });

        loadedFromLoginScreen.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    //Fix chipview
                    FilterDrawer.setFiltersDrawerLoadedToTrue();

                    //Get Current user
                    currentUser = Main.getCurrentUser();

                    //Top senders Radial Chart:
                    topSendersRadialChart = TileBuilder.create()
                            .animationDuration(10000)
                            .skinType(Tile.SkinType.RADIAL_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            .title("Top Senders")
                            .titleAlignment(TextAlignment.LEFT)
                            .prefSize(400, 400)
                            .maxSize(400, 400)
                            .chartData(topSendersData)
                            .animated(true)
                            .build();
                    topSendersRadialChart.setCursor(Cursor.HAND);
                    masonryPane.getChildren().add(topSendersRadialChart);
                    //Change scenes based on top sender ChartData selected
                    topSendersRadialChart.setOnTileEvent((e) -> {
                        if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                            DashboardDrawer.setLoadFolderList(false);
                            ChartData data = e.getData();
                            System.out.println("Selected " + data.getName());
                            addFilter(data.getName());
                            /*try {
                                loadedFromLoginScreen.setValue(false);
                                AnchorPane newScenePane = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));
                                Scene newScene = new Scene(newScenePane, 1000, 600);
                                myStage.requestFocus();
                                myStage.setScene(newScene);
                                DashboardDrawer.setLoadFolderList(true);
                            } catch (IOException error) {
                                error.printStackTrace();
                            }*/
                        }
                    });

                    //HeatMap:
                    //rather than using em here, assign the value of em to be whatever the list of emails we want
                    //aka, add filter, and then display those results
                    ArrayList<Email> em = Main.getCurrentUser().getEmails();
                    //ArrayList<Email> em2 = Main.getCurrentUser().getEmailsFromFolder("first year admin stuff", "testFolder");
                    int[][] heatMapData = Main.getCurrentUser().generateDayOfWeekFrequency(em, false);
                    VBox heatMapAndTitle = new VBox();
                    Pane heatMapPane = new Pane();
                    Label heatMapTitle = new Label("Received Email Frequency");
                    heatMapTitle.setTextFill(Color.LIGHTGRAY);
                    heatMapTitle.setStyle("-fx-font: 24 System;");
                    heatMapPane.setPrefSize(600, 400);
                    heatMapGridPane = new GridPane();
                    heatMapGridPane.setPrefSize(600, 400);

                    for (int i = 0; i < heatMapData.length; i++) {
                        Label day = new Label(Main.getCurrentUser().getDay(i));
                        day.setStyle("-fx-text-fill: #ff931e;");
                        heatMapGridPane.add(day, 0, i + 1);
                        day.setMinWidth(Region.USE_PREF_SIZE);
                        day.setMaxWidth(Region.USE_PREF_SIZE);

                        for (int j = 0; j < heatMapData[1].length; j++) {
                            Label hour = new Label(Integer.toString(j));
                            StackPane hourPane = new StackPane();
                            hourPane.setMinSize(20, 20);
                            hour.setStyle("-fx-text-fill: #ff931e;");
                            hourPane.getChildren().add(hour);
                            heatMapGridPane.add(hourPane, j + 1, 0);


                            StackPane pane = new StackPane();
                            pane.setCursor(Cursor.HAND);
                            pane.setMinSize(20, 20);
                            pane.setStyle(Main.getCurrentUser().getColorForHeatMap(heatMapData[i][j]));

                            Label freq = new Label(Integer.toString(heatMapData[i][j]));
                            freq.setTextFill(Color.LIGHTGRAY);

                            pane.setOnMouseEntered(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    pane.getChildren().add(freq);
                                    StackPane.setAlignment(freq, Pos.CENTER);
                                }
                            });

                            pane.setOnMouseExited(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    pane.getChildren().remove(freq);
                                }
                            });

                            heatMapGridPane.add(pane, j + 1, i + 1);
                        }
                    }
                    heatMapGridPane.setGridLinesVisible(true);
                    heatMapPane.getChildren().add(heatMapGridPane);
                    heatMapAndTitle.getChildren().addAll(heatMapTitle, heatMapPane);
                    heatMapAndTitle.setSpacing(5);
                    heatMapAndTitle.setPadding(new Insets(20));
                    heatMapAndTitle.setPrefSize(600,400);
                    heatMapAndTitle.setMaxSize(600,400);
                    masonryPane.getChildren().add(heatMapAndTitle);

                    //Folders SunburstChart:
                    foldersSunburstChart = TileBuilder.create()
                            .skinType(Tile.SkinType.SUNBURST)
                            .backgroundColor(Color.TRANSPARENT)
                            .sunburstBackgroundColor(Color.TRANSPARENT)
                            .title("Folder Structure")
                            .textVisible(true)
                            .titleAlignment(TextAlignment.LEFT)
                            .sunburstTextOrientation(SunburstChart.TextOrientation.HORIZONTAL)
                            .showInfoRegion(true)
                            .minSize(400, 400)
                            .prefSize(400, 400)
                            .sunburstTree(currentUser.getFoldersCountForSunburst())
                            .sunburstInteractive(true)
                            .build();
                    //Doesn't work big bummer!!!!
                    foldersSunburstChart.setOnTileEvent((e) -> {
                        if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                            System.out.println("Clicked on folder " + e.getData().getName());
                            updateTopSenders(e.getData().getName(),e.getData().getName(),null,null,null,null,null);
                        }
                    });
                    masonryPane.getChildren().add(foldersSunburstChart);


                    //Domains donut chart
                    domains = currentUser.getDomainFreq(currentUser.getEmails(), false);
                    int colorCount = 0;
                    PieChart.Data domainOther = new PieChart.Data("Other", 0);
                    int domainCount = 0;
                    for (Map.Entry<String, Long> entry : domains.entrySet()) {
                        if (domainCount < 5) {
                            PieChart.Data temp = new PieChart.Data(entry.getKey(), entry.getValue());
                            domainsData.add(temp);
                            domainCount++;
                        } else {
                            double otherValue = domainOther.getPieValue();
                            domainOther = new PieChart.Data("Other", otherValue);
                        }
                    }
                    domainsData.add(domainOther);
                    domainDonutChart = new DonutChart(domainsData);
                    domainDonutChart.setPrefSize(500, 400);
                    domainDonutChart.setMaxSize(500, 400);
                    domainDonutChart.setTitle("Domains");
                    domainDonutChart.setLegendVisible(true);
                    domainDonutChart.setLegendSide(Side.BOTTOM);
                    domainDonutChart.setLabelsVisible(true);
                    domainDonutChart.getData().stream().forEach(data -> {
                        Tooltip tooltip = new Tooltip();
                        tooltip.setText((int) data.getPieValue() + " emails");
                        Tooltip.install(data.getNode(), tooltip);
                        data.pieValueProperty().addListener((observableTwo, oldValueTwo, newValueTwo) ->
                                tooltip.setText((int) newValueTwo + " emails"));
                    });
                    for (PieChart.Data d : domainsData) {
                        d.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent e) {
                                d.getNode().setCursor(Cursor.HAND);
                            }
                        });
                    }
                    for (PieChart.Data d : domainsData) {
                        d.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent e) {
                                addFilter(d.getName());
                            }
                        });
                    }
                    domainDonutChart.getStylesheets().add(this.getClass().getClassLoader().getResource("donutchart.css").toExternalForm());
                    masonryPane.getChildren().add(domainDonutChart);

                    //Attachments radial chart
                    attachments = currentUser.getAttachmentFreq(currentUser.getEmails(), false);
                    int attachmentsCount = 0;
                    int attachmentsTotal = 0;
                    for (Map.Entry<String, Long> entry : attachments.entrySet()) {
                        if (attachmentsCount < 5) {
                            ChartData temp = new ChartData();
                            temp.setName(entry.getKey());
                            temp.setValue(entry.getValue());
                            temp.setFillColor(User.colors.get(attachmentsCount));
                            attachmentsTotal += entry.getValue();
                            attachmentsData.add(temp);
                            attachmentsCount++;
                        }else{
                            attachmentsTotal += entry.getValue();
                        }
                    }
                    attachmentsRadialChart = TileBuilder.create()
                            .animationDuration(10000)
                            .skinType(Tile.SkinType.RADIAL_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            .title("Attachments")
                            .titleAlignment(TextAlignment.LEFT)
                            .textVisible(true)
                            .text("Total attachments: " + attachmentsTotal)
                            .prefSize(400, 400)
                            .maxSize(400, 400)
                            .chartData(attachmentsData)
                            .animated(true)
                            .build();
                    attachmentsRadialChart.setOnTileEvent((e) -> {
                        if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                            DashboardDrawer.setLoadFolderList(false);
                            ChartData data = e.getData();
                            System.out.println("Selected " + data.getName());
                            addFilter(data.getName());
                        }
                    });
                    masonryPane.getChildren().add(attachmentsRadialChart);

                    //Sentiment Gauge:
                    sentimentGauge = TileBuilder.create()
                            .skinType(Tile.SkinType.BAR_GAUGE)
                            .backgroundColor(Color.TRANSPARENT)
                            .title("Sentiment")
                            .unit("%")
                            .value(0)
                            .gradientStops(new Stop(0, Color.valueOf("#fc5c65")),
                                    new Stop(0.25, Color.valueOf("#fd9644")),
                                    new Stop(0.5, Color.valueOf("#fed330")),
                                    new Stop(0.75, Color.valueOf("#26de81")),
                                    new Stop(1.0, Color.valueOf("#45aaf2")))
                            .strokeWithGradient(true)
                            .highlightSections(true)
                            .averagingPeriod(25)
                            .autoReferenceValue(true)
                            .titleAlignment(TextAlignment.LEFT)
                            .prefSize(350, 350)
                            .maxSize(350, 350)
                            .animated(true)
                            .build();
                    masonryPane.getChildren().add(sentimentGauge);
                    //Emulate Data
                    lastTimerCall = System.nanoTime();
                    /*timer = new AnimationTimer() {
                        @Override
                        public void handle(final long now) {
                            if (now > lastTimerCall + 2_000_000_000) {
                                sentimentGauge.setValue(RND.nextDouble() * sentimentGauge.getRange() + sentimentGauge.getMinValue());

                                lastTimerCall = now;
                            }
                        }
                    };
                    timer.start();*/


                    //Update List of folders in drawer
                    dashboardDrawer.listView.setOnMouseClicked(new ListViewHandler(){
                        @Override
                        public void handle(javafx.scene.input.MouseEvent event) {
                            String folderSelected = dashboardDrawer.list.get(dashboardDrawer.listView.getSelectionModel().getSelectedIndex());
                            System.out.print("Selected" + folderSelected);
                            updateTopSenders(folderSelected,folderSelected,null,null,null,null,null);
                        }
                    });


                    //Allows the scroll pane to resize the masonry pane after nodes are added, keep at bottom!
                    Platform.runLater(() -> scrollPane.requestLayout());
                }
            }
        });

        homeOnCloseRequest.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            Platform.exit();
                            System.exit(0);
                        }
                    });
                }
            }
        });
    }

    public void updateTopSenders(String folderName, String subFolderName, Date startDate, Date endDate, String sender, String domain, String attachment){
        masonryPane.getChildren().removeAll(topSendersRadialChart);
        //If the it's not updating from a new folder keep the main folder
        if (folderName == null){
            folderName = currentUser.recoverFolders().get(0).folderName;
        }
        if (subFolderName == null){
            subFolderName = currentUser.recoverFolders().get(0).folderName;
        }
        //Update array list of top senders with new folder info
        topSendersData = new ArrayList<>();
        Map<String,Long> topSenders = currentUser.getSendersFreq(currentUser.filter(folderName, subFolderName,startDate,endDate,sender,domain,attachment));
        int numSendersInFolder = topSenders.size();
        //only display top 7 senders for the selected folder
        if (numSendersInFolder > 7) {
            numSendersInFolder = 7;
        }

        //Maps are the worst thing ever, thanks a lot Cedric...
        List<Map.Entry<String,Long>> entries = new ArrayList<>(topSenders.entrySet());
        for (int i = 0; i < 7; i++) {
            //Created ChartData for top senders radial chart
            ChartData temp = new ChartData();
                if (i < entries.size()) {
                    temp.setValue((double) entries.get(i).getValue());
                    temp.setName(entries.get(i).getKey());
                    temp.setFillColor(User.colors.get(i));
                } else {
                    temp.setValue(0);
                    temp.setName("");
                }

            addTopSendersData(temp);
        }
        //Build new radial chart for top senders
        topSendersRadialChart = TileBuilder.create()
                .animationDuration(10000)
                .skinType(Tile.SkinType.RADIAL_CHART)
                .backgroundColor(Color.TRANSPARENT)
                .title("Top Senders")
                .titleAlignment(TextAlignment.LEFT)
                .prefSize(400, 400)
                .maxSize(400, 400)
                .chartData(topSendersData)
                .animated(true)
                .build();
        masonryPane.getChildren().add(0,topSendersRadialChart);
    }
}
