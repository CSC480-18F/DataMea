package datamea.frontend;

import datamea.backend.*;
import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.SunburstChart;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TreeNodeEvent;
import eu.hansolo.tilesfx.tools.TreeNode;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.scene.layout.AnchorPane.setTopAnchor;

public class DashboardController implements Initializable {

    //------------------Get FXML Variables------------------//
    @FXML
    private StackPane stackPane;

    @FXML
    private JFXDrawer drawer;

    @FXML
    private JFXDrawer filtersDrawer;

    @FXML
    private JFXDrawer settingsDrawer;

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
    private SettingsDrawer settingsDrawerClass;
    private static BooleanProperty loadedFromLoginScreen = new SimpleBooleanProperty(false);
    private static ArrayList<ChartData> topSendersOrRecipientsData = new ArrayList<>();
    private Tile topSendersOrRecipientsRadialChart;
    private GridPane heatMapGridPane;
    private VBox heatMapAndTitle;
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
    private ObservableList<PieChart.Data> languagesData = FXCollections.observableArrayList();
    private static DonutChart languagesDonutChart;
    public static Tile sentimentGauge;
    public static Tile replyRateGauge;
    private ArrayList<Filter> currentFilters = new ArrayList<>();
    private ArrayList<String> currentFiltersNames = new ArrayList<>(); //easiest way of keeping track of whether or not we added a filter already don't yell at me lol it's greasy its 2am cut me some slack gosh
    private DoubleProperty scrollPaneLocation = new SimpleDoubleProperty(this, "scrollPaneLocation");
    private BackgroundSentiment backgroundSentiment;
    public static boolean sentMail;
    private Timeline sentimentTimeline = null;

    public static void setStage(Stage s) {
        myStage = s;
    }

    public static void addTopSendersOrRecipientsData(ChartData d) {
        //topSendersOrRecipientsData.add(d);
        topSendersOrRecipientsData.add(d);
    }

    public User getUser() {
        return currentUser;
    }

    public static void setLoadedFromLoginScreenToTrue() {
        loadedFromLoginScreen.setValue(true);
    }

    public static void setHomeOnCloseRequest(Boolean b) {
        homeOnCloseRequest.setValue(b);
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        sentMail = false;

        String scrollPaneCss = this.getClass().getClassLoader().getResource("scrollpane.css").toExternalForm();
        scrollPane.getStylesheets().add(scrollPaneCss);

        //Resizing crap, this took way to long to figure out thanks javafx
        scrollPane.setFitToWidth(true);
        Platform.runLater(() -> scrollPane.requestLayout());
        anchorPane.prefWidthProperty().bind(stackPane.widthProperty());
        anchorPane.prefHeightProperty().bind(stackPane.heightProperty());
        topBarGridPane.prefWidthProperty().bind(anchorPane.widthProperty());
        masonryPane.prefWidthProperty().bind(anchorPane.widthProperty());
        centerColumn.maxWidthProperty().bind(topBarGridPane.widthProperty());
        gridPaneLeft.maxWidthProperty().bind(topBarGridPane.widthProperty());
        gridPaneRight.maxWidthProperty().bind(topBarGridPane.widthProperty());
        drawer.prefHeightProperty().bind(anchorPane.heightProperty());
        settingsDrawer.prefHeightProperty().bind(anchorPane.heightProperty());
        filtersDrawer.prefWidthProperty().bind(anchorPane.widthProperty());
        progressBar.prefWidthProperty().bind(anchorPane.widthProperty());

        //Resize masonry pane when filters bar is opened
        scrollPaneLocation.addListener(it -> updateScrollPaneAnchors());

        drawer.setVisible(false);
        filtersDrawer.setVisible(false);
        settingsDrawer.setVisible(false);
        //Allows you to click through the drawer if it's not visible (so we set it invisible when it's not open)
        drawer.setPickOnBounds(false);
        filtersDrawer.setPickOnBounds(false);
        settingsDrawer.setPickOnBounds(false);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Dashboard_Drawer.fxml"));
            AnchorPane pane = loader.load();
            dashboardDrawer = loader.getController();
            dashboardDrawer.drawerAnchorpane.maxHeightProperty().bind(anchorPane.heightProperty());
            dashboardDrawer.dashboardDrawerVBox.maxHeightProperty().bind(anchorPane.heightProperty());
            String darkerScrollPaneCss = this.getClass().getClassLoader().getResource("darker_scrollpane.css").toExternalForm();
            dashboardDrawer.drawerScrollpane.getStylesheets().add(darkerScrollPaneCss);
            drawer.setSidePane(pane);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Filters_Drawer.fxml"));
            AnchorPane filtersPane = loader.load();
            filterDrawerClass = loader.getController();
            filterDrawerClass.filtersAnchorPane.maxWidthProperty().bind(topBarGridPane.widthProperty());
            filtersDrawer.setSidePane(filtersPane);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Settings_Drawer.fxml"));
            VBox settingsVBox = loader.load();
            settingsDrawerClass = loader.getController();
            settingsDrawerClass.settingsDrawerVBox.maxHeightProperty().bind(anchorPane.heightProperty());
            settingsDrawer.setSidePane(settingsVBox);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        HamburgerBasicCloseTransition basicCloseTransition = new HamburgerBasicCloseTransition(hamburger);
        basicCloseTransition.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            basicCloseTransition.setRate(basicCloseTransition.getRate() * -1);
            basicCloseTransition.play();
            if (drawer.isOpened()) {
                if (settingsDrawer.isOpened()) {
                    settingsDrawer.close();
                    dashboardDrawer.settingsButton.setText("Open Settings");
                    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                    executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            Platform.runLater(()->settingsDrawer.setVisible(false));
                        }
                    }, 500, TimeUnit.MILLISECONDS);
                    executor.shutdown();
                }
                DashboardDrawer.setShrinkListToTrue();
                drawer.close();
                hamburger.setDisable(true);
                //This starts a Thread but immediately schedules it to run after 500 milliseconds, so the drawer closing animation can run before making the drawer invisible
                //Thanks for writing this DL, spent almost 2 hours trying to get the drawer set to invisible after the closing animation played lol
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Platform.runLater(()->{
                            drawer.setVisible(false);
                            hamburger.setDisable(false);
                        });
                    }
                }, 500, TimeUnit.MILLISECONDS);
                executor.shutdown();
            } else {
                drawer.setVisible(true);
                drawer.open();
                DashboardDrawer.setExpandListToTrue();
            }
        });

        filtersButton.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            if (filtersDrawer.isOpened()) {
                filtersDrawer.close();
                filtersButton.setText("Open Filters");
                changeScrollPaneHeight(0);
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Platform.runLater(()-> filtersDrawer.setVisible(false));
                    }
                }, 500, TimeUnit.MILLISECONDS);
                executor.shutdown();
            } else {
                filtersDrawer.setVisible(true);
                filtersDrawer.open();
                filtersButton.setText("Close Filters");
                changeScrollPaneHeight(75);
            }
        });

        filterDrawerClass.applyFilters.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            updateAllCharts(currentFilters);
        });

        dashboardDrawer.settingsButton.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            if (settingsDrawer.isOpened()) {
                settingsDrawer.close();
                dashboardDrawer.settingsButton.setText("Open Settings");
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Platform.runLater(()-> settingsDrawer.setVisible(false));
                    }
                }, 500, TimeUnit.MILLISECONDS);
                executor.shutdown();
            } else {
                settingsDrawer.setVisible(true);
                settingsDrawer.open();
                dashboardDrawer.settingsButton.setText("Close Settings");
            }
        });

        settingsDrawerClass.resetButton.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("All progress will be lost!"));
            content.setBody(new Text("Are you sure you want to reset your Data Mea, the program will close and you will have to re-login and load again."));
            JFXDialog wrongInfo = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
            JFXButton okay = new JFXButton("Okay");
            JFXButton cancel = new JFXButton("Cancel");
            okay.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        currentUser.resetUser();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                    wrongInfo.close();
                    Platform.exit();
                    System.exit(0);
                    //Delete TextFiles here
                }
            });
            cancel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    wrongInfo.close();
                }
            });
            content.setActions(okay, cancel);
            wrongInfo.show();
        });

        loadedFromLoginScreen.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    //Get Current user
                    currentUser = Main.getCurrentUser();

                    //Top senders Radial Chart:
                    VBox topSendersVBOX = new VBox();
                    HBox topSendersTitleWithTooltip = new HBox();
                    topSendersTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label topSendersTitle = new Label("Top Senders");
                    topSendersTitle.setTextFill(Color.LIGHTGRAY);
                    topSendersTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView topSendersQuestionMark = new FontAwesomeIconView();
                    topSendersQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    topSendersQuestionMark.setSize("24");
                    topSendersQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip topSendersTooltip = new Tooltip("This graph displays the user's top email senders.");
                    topSendersTooltip.setFont(Font.font(12));
                    Tooltip.install(topSendersQuestionMark, topSendersTooltip);
                    topSendersTitleWithTooltip.setSpacing(10);
                    topSendersTitleWithTooltip.getChildren().addAll(topSendersTitle,topSendersQuestionMark);
                    topSendersOrRecipientsRadialChart = TileBuilder.create()
                            .skinType(Tile.SkinType.RADIAL_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            //.title("Top Senders")
                            .titleAlignment(TextAlignment.LEFT)
                            .minSize(480, 480)
                            .prefSize(480, 480)
                            .maxSize(480, 480)
                            .chartData(topSendersOrRecipientsData)
                            .build();
                    topSendersOrRecipientsRadialChart.setCursor(Cursor.HAND);
                    topSendersVBOX.getChildren().addAll(topSendersTitleWithTooltip,topSendersOrRecipientsRadialChart);
                    topSendersVBOX.setSpacing(5);
                    topSendersVBOX.setPadding(new Insets(20));
                    topSendersVBOX.setPrefSize(400, 480);
                    topSendersVBOX.setMaxSize(480, 480);
                    masonryPane.getChildren().add(topSendersVBOX);
                    //Change scenes based on top sender ChartData selected
                    topSendersOrRecipientsRadialChart.setOnTileEvent((e) -> {
                        if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                            ChartData data = e.getData();
                            System.out.println("Selected " + data.getName());
                            openFilterDrawer();
                            addFilter(data.getName(), true, false, false, false, false);
                        }
                    });

                    //Folders SunburstChart:
                    VBox foldersVBOX = new VBox();
                    HBox foldersTitleWithTooltip = new HBox();
                    foldersTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label foldersTitle = new Label("Folder Structure");
                    foldersTitle.setTextFill(Color.LIGHTGRAY);
                    foldersTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView foldersQuestionMark = new FontAwesomeIconView();
                    foldersQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    foldersQuestionMark.setSize("24");
                    foldersQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip foldersTooltip = new Tooltip("This graph will provide the user with a detailed look at the folder structure within their Gmail account.");
                    foldersTooltip.setFont(Font.font(12));
                    Tooltip.install(foldersQuestionMark, foldersTooltip);
                    foldersTitleWithTooltip.setSpacing(10);
                    foldersTitleWithTooltip.getChildren().addAll(foldersTitle,foldersQuestionMark);
                    TreeNode<ChartData> folderTree = currentUser.getFoldersCountForSunburst(currentUser.getEmails());
                    folderTree.setOnTreeNodeEvent(e -> {
                        System.out.println("TreeNodeEvent");
                        TreeNodeEvent.EventType type = e.getType();
                        if (TreeNodeEvent.EventType.NODE_SELECTED == type) {
                            TreeNode<ChartData> segment = e.getSource();
                            foldersSunburstChart.fireTileEvent(new TileEvent(TileEvent.EventType.SELECTED_CHART_DATA, segment.getItem()));
                        }
                    });
                    foldersSunburstChart = TileBuilder.create()
                            .skinType(Tile.SkinType.SUNBURST)
                            .backgroundColor(Color.TRANSPARENT)
                            .sunburstBackgroundColor(Color.TRANSPARENT)
                            //.title("Folder Structure")
                            .textVisible(true)
                            .titleAlignment(TextAlignment.LEFT)
                            .sunburstTextOrientation(SunburstChart.TextOrientation.HORIZONTAL)
                            .minSize(400, 480)
                            .prefSize(480, 480)
                            .sunburstTree(currentUser.getFoldersCountForSunburst(currentUser.getEmails()))
                            .sunburstInteractive(true)
                            .build();
                    //Broken because of sentiment?????
                    foldersSunburstChart.setOnTileEvent((e) -> {
                        if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                            System.out.println("Clicked on folder " + e.getData().getName());
                            openFilterDrawer();
                            addFilter(e.getData().getName(), false, true, false, false, false);
                        }
                    });
                    foldersVBOX.getChildren().addAll(foldersTitleWithTooltip,foldersSunburstChart);
                    foldersVBOX.setSpacing(5);
                    foldersVBOX.setPadding(new Insets(20));
                    foldersVBOX.setPrefSize(400, 480);
                    foldersVBOX.setMaxSize(480, 480);
                    masonryPane.getChildren().add(foldersVBOX);

                    //Sent VS received chart
                    VBox sentVSReceivedVBOX = new VBox();
                    HBox sentVSReceivedTitleWithTooltip = new HBox();
                    sentVSReceivedTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label sentVSReceivedTitle = new Label("Sent vs Received");
                    sentVSReceivedTitle.setTextFill(Color.LIGHTGRAY);
                    sentVSReceivedTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView sentVSReceivedQuestionMark = new FontAwesomeIconView();
                    sentVSReceivedQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    sentVSReceivedQuestionMark.setSize("24");
                    sentVSReceivedQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip sentVSReceivedTooltip = new Tooltip("This graph displays the number of emails that they have sent and received by with the user's Gmail account.");
                    sentVSReceivedTooltip.setFont(Font.font(12));
                    Tooltip.install(sentVSReceivedQuestionMark, sentVSReceivedTooltip);
                    sentVSReceivedTitleWithTooltip.setSpacing(10);
                    sentVSReceivedTitleWithTooltip.getChildren().addAll(sentVSReceivedTitle,sentVSReceivedQuestionMark);
                    PieChart.Data sent = new PieChart.Data("Sent", currentUser.numberOfSentMail);
                    PieChart.Data recevied = new PieChart.Data("Received", User.getTotalNumberOfEmails() - currentUser.numberOfSentMail);
                    ObservableList<PieChart.Data> sentReceivedData = FXCollections.observableArrayList();
                    sentReceivedData.add(recevied);
                    sentReceivedData.add(sent);
                    DonutChart sentReceivedDonutChart = new DonutChart(sentReceivedData);
                    sentReceivedDonutChart.setPrefSize(480, 480);
                    sentReceivedDonutChart.setMaxSize(480, 480);
                    //sentReceivedDonutChart.setTitle("Sent vs Received");
                    sentReceivedDonutChart.setLabelsVisible(true);
                    sentReceivedDonutChart.setLegendVisible(false);
                    sentReceivedDonutChart.getData().stream().forEach(data -> {
                        Tooltip tooltip = new Tooltip();
                        tooltip.setText((int) data.getPieValue() + " emails");
                        Tooltip.install(data.getNode(), tooltip);
                        data.pieValueProperty().addListener((observableTwo, oldValueTwo, newValueTwo) ->
                                tooltip.setText((int) newValueTwo + " emails"));
                    });
                    for (PieChart.Data d : sentReceivedData) {
                        d.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent e) {
                                d.getNode().setCursor(Cursor.DEFAULT);
                            }
                        });
                    }
                    sentReceivedDonutChart.getStylesheets().add(this.getClass().getClassLoader().getResource("donutchart.css").toExternalForm());
                    sentVSReceivedVBOX.getChildren().addAll(sentVSReceivedTitleWithTooltip,sentReceivedDonutChart);
                    sentVSReceivedVBOX.setSpacing(5);
                    sentVSReceivedVBOX.setPadding(new Insets(20));
                    sentVSReceivedVBOX.setPrefSize(480, 480);
                    sentVSReceivedVBOX.setMaxSize(480, 480);
                    masonryPane.getChildren().add(sentVSReceivedVBOX);

                    //Domains donut chart
                    VBox domainsVBOX = new VBox();
                    HBox domainsTitleWithTooltip = new HBox();
                    domainsTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label domainsTitle = new Label("Domains");
                    domainsTitle.setTextFill(Color.LIGHTGRAY);
                    domainsTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView domainsQuestionMark = new FontAwesomeIconView();
                    domainsQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    domainsQuestionMark.setSize("24");
                    domainsQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip domainsTooltip = new Tooltip("This graph displays the most frequent email domains that a user is in contact with (i.e. @yahoo.com, @oswego.edu, @gmail.com, etc.).");
                    domainsTooltip.setFont(Font.font(12));
                    Tooltip.install(domainsQuestionMark, domainsTooltip);
                    domainsTitleWithTooltip.setSpacing(10);
                    domainsTitleWithTooltip.getChildren().addAll(domainsTitle,domainsQuestionMark);
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
                    domainDonutChart.setPrefSize(480, 480);
                    domainDonutChart.setMaxSize(480, 480);
                    //domainDonutChart.setTitle("Domains");
                    domainDonutChart.setLegendVisible(true);
                    domainDonutChart.setLegendSide(Side.BOTTOM);
                    domainDonutChart.setLabelsVisible(false);
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
                                //addFilter(d.getName());
                                openFilterDrawer();
                                addFilter(d.getName(), false, false, true, false, false);
                            }
                        });
                    }
                    domainDonutChart.getStylesheets().add(this.getClass().getClassLoader().getResource("donutchart.css").toExternalForm());
                    domainsVBOX.getChildren().addAll(domainsTitleWithTooltip,domainDonutChart);
                    domainsVBOX.setSpacing(5);
                    domainsVBOX.setPadding(new Insets(20));
                    domainsVBOX.setPrefSize(480, 480);
                    domainsVBOX.setMaxSize(480, 480);
                    masonryPane.getChildren().add(domainsVBOX);

                    //Attachments radial chart
                    attachments = currentUser.getAttachmentFreq(currentUser.getEmails(), false);
                    int attachmentsCount = 0;
                    int attachmentsTotal = 0;
                    for (Map.Entry<String, Long> entry : attachments.entrySet()) {
                        if (attachmentsCount < 7) {
                            ChartData temp = new ChartData();
                            temp.setName(entry.getKey());
                            temp.setValue(entry.getValue());
                            temp.setFillColor(User.colors.get(attachmentsCount));
                            attachmentsTotal += entry.getValue();
                            attachmentsData.add(temp);
                            attachmentsCount++;
                        } else {
                            attachmentsTotal += entry.getValue();
                        }
                    }
                    for (int i = attachmentsCount; i < 7; i++) {
                        ChartData temp = new ChartData();
                        temp.setName("");
                        temp.setValue(0);
                        attachmentsData.add(temp);
                    }

                    //Attachemnts Chart
                    VBox attachmentsVBOX = new VBox();
                    HBox attachmentsTitleWithTooltip = new HBox();
                    attachmentsTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label attachmentsTitle = new Label("Attachments");
                    attachmentsTitle.setTextFill(Color.LIGHTGRAY);
                    attachmentsTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView attachmentsQuestionMark = new FontAwesomeIconView();
                    attachmentsQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    attachmentsQuestionMark.setSize("24");
                    attachmentsQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip attachmentsTooltip = new Tooltip("This graph displays the most frequent attachments found in a user's email folders.");
                    attachmentsTooltip.setFont(Font.font(12));
                    Tooltip.install(attachmentsQuestionMark, attachmentsTooltip);
                    attachmentsTitleWithTooltip.setSpacing(10);
                    attachmentsTitleWithTooltip.getChildren().addAll(attachmentsTitle,attachmentsQuestionMark);
                    attachmentsRadialChart = TileBuilder.create()
                            .animationDuration(10000)
                            .skinType(Tile.SkinType.RADIAL_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            //.title("Attachments")
                            .titleAlignment(TextAlignment.LEFT)
                            .textVisible(true)
                            .text("Total attachments: " + attachmentsTotal)
                            .prefSize(480, 480)
                            .maxSize(480, 480)
                            .chartData(attachmentsData)
                            .animated(true)
                            .build();
                    attachmentsRadialChart.setOnTileEvent((e) -> {
                        if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                            DashboardDrawer.setLoadFolderList(false);
                            ChartData data = e.getData();
                            System.out.println("Selected " + data.getName());
                            openFilterDrawer();
                            addFilter(data.getName(), false, false, false, true, false);
                            //addFilter(data.getName());
                        }
                    });
                    attachmentsVBOX.getChildren().addAll(attachmentsTitleWithTooltip,attachmentsRadialChart);
                    attachmentsVBOX.setSpacing(5);
                    attachmentsVBOX.setPadding(new Insets(20));
                    attachmentsVBOX.setPrefSize(400, 480);
                    attachmentsVBOX.setMaxSize(480, 480);
                    masonryPane.getChildren().add(attachmentsVBOX);

                    //Reply Rate Gauge
                    VBox replyRateVBOX = new VBox();
                    HBox replyRateTitleWithTooltip = new HBox();
                    replyRateTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label replyRateTitle = new Label("Received Email Frequency");
                    replyRateTitle.setTextFill(Color.LIGHTGRAY);
                    replyRateTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView replyRateQuestionMark = new FontAwesomeIconView();
                    replyRateQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    replyRateQuestionMark.setSize("24");
                    replyRateQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip replyRateTooltip = new Tooltip("This graph shows the overall interaction between the user and the user's send/receive recipients.");
                    replyRateTooltip.setFont(Font.font(12));
                    Tooltip.install(replyRateQuestionMark, replyRateTooltip);
                    replyRateTitleWithTooltip.setSpacing(10);
                    replyRateTitleWithTooltip.getChildren().addAll(replyRateTitle,replyRateQuestionMark);
                    replyRateGauge = TileBuilder.create()
                            .skinType(Tile.SkinType.BAR_GAUGE)
                            .backgroundColor(Color.TRANSPARENT)
                            //.title("Reply Rate")
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
                    double replyRate = currentUser.getReplyFrequency(currentUser.getEmails());
                    replyRateGauge.setValue(replyRate);
                    replyRateVBOX.getChildren().addAll(replyRateTitleWithTooltip, replyRateGauge);
                    replyRateVBOX.setSpacing(5);
                    replyRateVBOX.setPadding(new Insets(20));
                    replyRateVBOX.setPrefSize(350, 350);
                    replyRateVBOX.setMaxSize(350, 350);
                    masonryPane.getChildren().add(replyRateVBOX);


                    //HeatMap:
                    //rather than using em here, assign the value of em to be whatever the list of emails we want
                    //aka, add filter, and then display those results



                    ArrayList<Email> em = Main.getCurrentUser().getEmails();
                    //ArrayList<Email> em2 = Main.getCurrentUser().getEmailsFromFolder("first year admin stuff", "testFolder");
                    int[][] heatMapData = Main.getCurrentUser().generateDayOfWeekFrequency(em, false);
                    heatMapAndTitle = new VBox();
                    Pane heatMapPane = new Pane();
                    Label heatMapTitle = new Label("Received Email Frequency");
                    heatMapTitle.setTextFill(Color.LIGHTGRAY);
                    heatMapTitle.setStyle("-fx-font: 24 System;");
                    HBox HeatMapTitleWithTooltip = new HBox();
                    HeatMapTitleWithTooltip.setAlignment(Pos.CENTER);
                    FontAwesomeIconView HeatMapQuestionMark = new FontAwesomeIconView();
                    HeatMapQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    HeatMapQuestionMark.setSize("24");
                    HeatMapQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip HeatMapTooltip = new Tooltip("This graph displays the frequency of emails given by day and hour. The more vibrant the color, the more emails in the respective time slot.");
                    HeatMapTooltip.setFont(Font.font(12));
                    Tooltip.install(HeatMapQuestionMark, HeatMapTooltip);
                    HeatMapTitleWithTooltip.setSpacing(10);
                    HeatMapTitleWithTooltip.getChildren().addAll(heatMapTitle,HeatMapQuestionMark);
                    heatMapPane.setPrefSize(600, 480);
                    heatMapGridPane = new GridPane();
                    heatMapGridPane.setPrefSize(600, 480);

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
                            pane.setStyle(currentUser.getColorForHeatMap(heatMapData[i][j], heatMapData));

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
                    heatMapAndTitle.getChildren().addAll(HeatMapTitleWithTooltip, heatMapPane);
                    heatMapAndTitle.setSpacing(10);
                    heatMapAndTitle.setPadding(new Insets(20));
                    heatMapAndTitle.setPrefSize(600, 480);
                    heatMapAndTitle.setMaxSize(600, 480);
                    masonryPane.getChildren().add(heatMapAndTitle);


                    //Sentiment Gauge:
                    VBox sentimentVBOX = new VBox();
                    HBox sentimentTitleWithTooltip = new HBox();
                    sentimentTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label sentimentTitle = new Label("Sentiment");
                    sentimentTitle.setTextFill(Color.LIGHTGRAY);
                    sentimentTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView sentimentQuestionMark = new FontAwesomeIconView();
                    sentimentQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    sentimentQuestionMark.setSize("24");
                    sentimentQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip sentimentTooltip = new Tooltip("This graph displays the general attitude sentiment of emails whether mostly negative, neutral, or positive.");
                    sentimentTooltip.setFont(Font.font(12));
                    Tooltip.install(sentimentQuestionMark, sentimentTooltip);
                    sentimentTitleWithTooltip.setSpacing(10);
                    sentimentTitleWithTooltip.getChildren().addAll(sentimentTitle,sentimentQuestionMark);
                    sentimentGauge = TileBuilder.create()
                            .skinType(Tile.SkinType.BAR_GAUGE)
                            .backgroundColor(Color.TRANSPARENT)
                            //.title("Sentiment")
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
                    sentimentVBOX.getChildren().addAll(sentimentTitleWithTooltip, sentimentGauge);
                    sentimentVBOX.setSpacing(5);
                    sentimentVBOX.setPadding(new Insets(20));
                    sentimentVBOX.setPrefSize(350, 350);
                    sentimentVBOX.setMaxSize(350, 350);
                    masonryPane.getChildren().add(sentimentVBOX);


                    backgroundSentiment = new BackgroundSentiment();
                    new Thread(backgroundSentiment).start();
                    progressBar.progressProperty().unbind();
                    progressBar.setProgress(0);
                    progressBar.progressProperty().bind(backgroundSentiment.progressProperty());
                    progressBar.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING,
                            new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent event) {
                                    progressBar.setProgress(backgroundSentiment.getProgress());
                                    if (backgroundSentiment.getProgress() == 1.0){
                                        progressBar.setVisible(false);
                                        JFXSnackbar notification = new JFXSnackbar(anchorPane);
                                        notification.getStylesheets().add(this.getClass().getClassLoader().getResource("Dashboard_css.css").toExternalForm());
                                        notification.show("Sentiment score is done",5000);
                                    }
                                }
                            });

                    if (backgroundSentiment.getProgress() != 1.0){
                        JFXSnackbar notification = new JFXSnackbar(anchorPane);
                        notification.getStylesheets().add(this.getClass().getClassLoader().getResource("Dashboard_css.css").toExternalForm());
                        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                        executor.schedule(new Runnable() {
                            @Override
                            public void run() {
                                Platform.runLater(()->notification.show("Sentiment score is calculating",5000));
                            }
                        }, 2000, TimeUnit.MILLISECONDS);
                        executor.shutdown();
                    }


                    sentimentTimeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            ArrayList<Email> em = currentUser.recoverSerializedEmails();
                            int[] sentimentScores = currentUser.getSentimentForFilteredEmails(em);
                            double score = Email.getOverallSentimentDbl(sentimentScores);
                            sentimentGauge.setValue(score);
                        }
                    }));
                    sentimentTimeline.setCycleCount(Animation.INDEFINITE);
                    sentimentTimeline.play();

                    //languages donut chart
                    VBox languagesVBOX = new VBox();
                    HBox languagesTitleWithTooltip = new HBox();
                    languagesTitleWithTooltip.setAlignment(Pos.CENTER);
                    Label languagesTitle = new Label("Languages");
                    languagesTitle.setTextFill(Color.LIGHTGRAY);
                    languagesTitle.setStyle("-fx-font: 24 System;");
                    FontAwesomeIconView languagesQuestionMark = new FontAwesomeIconView();
                    languagesQuestionMark.setGlyphName("QUESTION_CIRCLE");
                    languagesQuestionMark.setSize("24");
                    languagesQuestionMark.setFill(Color.LIGHTGRAY);
                    Tooltip languagesTooltip = new Tooltip("This graph displays the most frequent languages that a user receives emails in.");
                    languagesTooltip.setFont(Font.font(12));
                    Tooltip.install(languagesQuestionMark, languagesTooltip);
                    languagesTitleWithTooltip.setSpacing(10);
                    languagesTitleWithTooltip.getChildren().addAll(languagesTitle,languagesQuestionMark);
                    languages = currentUser.getLanguageFreq(currentUser.getEmails(), false);
                    int languagesCount = 0;
                    for (Map.Entry<String, Long> entry : languages.entrySet()) {
                        PieChart.Data temp = new PieChart.Data(entry.getKey(), entry.getValue());
                        if (!entry.getKey().equals("")) {
                            languagesData.add(temp);
                            languagesCount++;
                        }
                    }
                    languagesDonutChart = new DonutChart(languagesData);
                    languagesDonutChart.setPrefSize(480, 480);
                    languagesDonutChart.setMaxSize(480, 480);
                    //languagesDonutChart.setTitle("Languages");
                    languagesDonutChart.setLegendVisible(true);
                    languagesDonutChart.setLabelsVisible(true);
                    languagesDonutChart.getData().stream().forEach(data -> {
                        Tooltip tooltip = new Tooltip();
                        tooltip.setText((int) data.getPieValue() + " emails");
                        Tooltip.install(data.getNode(), tooltip);
                        data.pieValueProperty().addListener((observableTwo, oldValueTwo, newValueTwo) ->
                                tooltip.setText((int) newValueTwo + " emails"));
                    });
                    for (PieChart.Data d : languagesData) {
                        d.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent e) {
                                d.getNode().setCursor(Cursor.HAND);
                            }
                        });
                    }
                    for (PieChart.Data d : languagesData) {
                        d.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent e) {
                                //addFilter(d.getName());
                                System.out.println("<" + d.getName() + ">");
                                addFilter(d.getName(), false, false, false, false, true);
                            }
                        });
                    }
                    languagesDonutChart.getStylesheets().add(this.getClass().getClassLoader().getResource("donutchart.css").toExternalForm());
                    languagesVBOX.getChildren().addAll(languagesTitleWithTooltip,languagesDonutChart);
                    languagesVBOX.setSpacing(5);
                    languagesVBOX.setPadding(new Insets(20));
                    languagesVBOX.setPrefSize(480, 480);
                    languagesVBOX.setMaxSize(480, 480);
                    masonryPane.getChildren().add(languagesVBOX);

                    //Update List of folders in drawer
                    dashboardDrawer.listView.setOnMouseClicked(new ListViewHandler() {
                        @Override
                        public void handle(javafx.scene.input.MouseEvent event) {
                            String folderSelected = dashboardDrawer.list.get(dashboardDrawer.listView.getSelectionModel().getSelectedIndex());
                            System.out.print("Selected" + folderSelected);
                            basicCloseTransition.setRate(basicCloseTransition.getRate() * -1);
                            basicCloseTransition.play();
                            drawer.close();
                            final ScheduledThreadPoolExecutor drawerExecutor = new ScheduledThreadPoolExecutor(2);
                            drawerExecutor.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    Platform.runLater(()->drawer.setVisible(false));
                                }
                            }, 500, TimeUnit.MILLISECONDS);
                            drawerExecutor.shutdown();
                            if (settingsDrawer.isOpened()) {
                                settingsDrawer.close();
                                dashboardDrawer.settingsButton.setText("Open Settings");
                                final ScheduledThreadPoolExecutor settingsExecutor = new ScheduledThreadPoolExecutor(2);
                                settingsExecutor.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        Platform.runLater(()->settingsDrawer.setVisible(false));
                                    }
                                }, 500, TimeUnit.MILLISECONDS);
                                settingsExecutor.shutdown();
                            }
                            openFilterDrawer();
                            addFilter(folderSelected, false, true, false, false, false);
                        }
                    });

                    //Get sentiment from previous launch if any
                    DashboardController.sentimentGauge.setValue(Email.getOverallSentimentDbl(currentUser.getOverallSentiment()));

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


    public void updateAllCharts(ArrayList<Filter> filters) {
        String folderName = null, subFolderName = null, sender = null,
                domain = null, attachment = null, startDate = null, endDate = null, language = null;
        Date sDate = null, eDate = null;

        sentMail = currentFiltersNames.contains("Sent Mail");

        //TODO figure out how to add folder/subfolder stuff along with doing dates


        for (Filter f : filters) {
            if (f.isTopSender()) {
                sender = f.getName();
            } else if (f.isAttachment()) {
                attachment = f.getName();
            } else if (f.isDomain()) {
                domain = f.getName();
            } else if (f.isLanguage()) {
                language = f.getName();
            } else if (f.isFolder()) {
                folderName = f.getName();
                if (folderName.equalsIgnoreCase("sent mail"))
                    sentMail = true;
            } else if (f.isStartDate()) {
                startDate = f.getName();
            } else if (f.isEndDate()) {
                endDate = f.getName();
            }
        }


        if (folderName != null) {

            //this indicates that it is a subfolder
            if (folderName.startsWith("          ")) {
                subFolderName = folderName.trim();

                for (UserFolder f : currentUser.getFolders()) {
                    for (String s : f.subFolders) {
                        if (s.equals(subFolderName)) {
                            folderName = f.folderName;
                            break;
                        }
                    }
                }

            } else {
                subFolderName = folderName;
            }

        }

        System.out.println("Folder:" + folderName + " Subfolder: " + subFolderName);


        //TODO Modify the string that is being passed in, to be a valid date
        //TODO modify the filters so that they take a language -- shouldnt take very long

        // TODO ADD OTHER CHARTS BELOW

        ArrayList<Email> em = currentUser.filter(folderName, subFolderName, sDate, eDate, sender, domain, attachment, language);

        updateTopSendersOrRecipients(em);
        updateSunBurstChart(em);
        updateDomains(em);
        updateAttachments(em);
        updateReplyRate(em);
        updateHeatMap(em);
        updateSentimentGauge(folderName, subFolderName, sDate, eDate, sender, domain, attachment, language);
        updateLanguages(folderName, subFolderName, sDate, eDate, sender, domain, attachment, language);

    }

    private void updateSunBurstChart(ArrayList<Email> em) {

        masonryPane.getChildren().remove(foldersSunburstChart);


        TreeNode<ChartData> folderTree = currentUser.getFoldersCountForSunburst(em);
        folderTree.setOnTreeNodeEvent(e -> {
            System.out.println("TreeNodeEvent");
            TreeNodeEvent.EventType type = e.getType();
            if (TreeNodeEvent.EventType.NODE_SELECTED == type) {
                TreeNode<ChartData> segment = e.getSource();
                foldersSunburstChart.fireTileEvent(new TileEvent(TileEvent.EventType.SELECTED_CHART_DATA, segment.getItem()));
            }
        });
        foldersSunburstChart = TileBuilder.create()
                .skinType(Tile.SkinType.SUNBURST)
                .backgroundColor(Color.TRANSPARENT)
                .sunburstBackgroundColor(Color.TRANSPARENT)
                .title("Folder Structure")
                .textVisible(true)
                .titleAlignment(TextAlignment.LEFT)
                .sunburstTextOrientation(SunburstChart.TextOrientation.HORIZONTAL)
                .minSize(400, 480)
                .prefSize(400, 480)
                .sunburstTree(currentUser.getFoldersCountForSunburst(em))
                .sunburstInteractive(true)
                .build();
        //Broken because of sentiment?????
        foldersSunburstChart.setOnTileEvent((e) -> {
            if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                System.out.println("Clicked on folder " + e.getData().getName());
                openFilterDrawer();
                addFilter(e.getData().getName(), false, true, false, false, false);
            }
        });
        masonryPane.getChildren().add(foldersSunburstChart);


    }


    private void updateReplyRate(ArrayList<Email> em) {

        masonryPane.getChildren().remove(replyRateGauge);

        replyRateGauge = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_GAUGE)
                .backgroundColor(Color.TRANSPARENT)
                .title("Reply Rate")
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
        double replyRate = currentUser.getReplyFrequency(em);
        replyRateGauge.setValue(replyRate);
        masonryPane.getChildren().add(replyRateGauge);
    }

    private void updateSentimentGauge(String folderName, String subFolderName, Date startDate, Date endDate, String sender, String domain, String attachment, String language) {

        masonryPane.getChildren().removeAll(sentimentGauge);
        sentimentTimeline.stop();
        sentimentTimeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ArrayList<Email> em = currentUser.filter(folderName, subFolderName, startDate, endDate, sender, domain, attachment, language);
                int[] sentimentScores = currentUser.getSentimentForFilteredEmails(em);
                double score = Email.getOverallSentimentDbl(sentimentScores);
                sentimentGauge.setValue(score);
            }
        }));
        sentimentTimeline.setCycleCount(Animation.INDEFINITE);
        sentimentTimeline.play();

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

    }

    public void updateHeatMap(ArrayList<Email> em) {

        masonryPane.getChildren().removeAll(heatMapAndTitle);

        String title = sentMail ? "Sent Email Frequency" : "Received Email Frequency";

        int[][] heatMapData;
        heatMapData = currentUser.generateDayOfWeekFrequency(em, sentMail);
        heatMapAndTitle = new VBox();
        Pane heatMapPane = new Pane();
        Label heatMapTitle = new Label(title);
        heatMapTitle.setTextFill(Color.LIGHTGRAY);
        heatMapTitle.setStyle("-fx-font: 24 System;");
        heatMapPane.setPrefSize(600, 480);
        heatMapGridPane = new GridPane();
        heatMapGridPane.setPrefSize(600, 480);

        for (int i = 0; i < heatMapData.length; i++) {
            Label day = new Label(currentUser.getDay(i));
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
                pane.setStyle(currentUser.getColorForHeatMap(heatMapData[i][j], heatMapData));

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
        heatMapAndTitle.setPrefSize(600, 480);
        heatMapAndTitle.setMaxSize(600, 480);
        masonryPane.getChildren().add(heatMapAndTitle);

    }

    public void updateTopSendersOrRecipients(ArrayList<Email> em) {
        masonryPane.getChildren().removeAll(topSendersOrRecipientsRadialChart);

        //Update array list of top senders with new folder info
        topSendersOrRecipientsData = new ArrayList<>();
        String title;
        Map<String, Long> topSendersOrRecipients;

        if (sentMail) {
            topSendersOrRecipients = currentUser.getSendersOrRecipientsFreq(em, sentMail);
            title = "Top Recipients";

            List<Map.Entry<String, Long>> entries = new ArrayList<>(topSendersOrRecipients.entrySet());
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
                addTopSendersOrRecipientsData(temp);

            }
        } else {
            topSendersOrRecipients = currentUser.getSendersOrRecipientsFreq(em, sentMail);
            title = "Top Senders";

            List<Map.Entry<String, Long>> entries = new ArrayList<>(topSendersOrRecipients.entrySet());
            for (int i = 0; i < 7; i++) {
                //Created ChartData for top senders radial chart
                ChartData temp = new ChartData();
                if (i < entries.size()) {
                    temp.setValue((double) entries.get(i).getValue());
                    temp.setName(Sender.filterEmailAddress(entries.get(i).getKey()));
                    temp.setFillColor(User.colors.get(i));
                } else {
                    temp.setValue(0);
                    temp.setName("");
                }
                addTopSendersOrRecipientsData(temp);
            }

            //Maps are the worst thing ever, thanks a lot Cedric...


        }
        //Build new radial chart for top senders
        topSendersOrRecipientsRadialChart = TileBuilder.create()
                .animationDuration(10000)
                .skinType(Tile.SkinType.RADIAL_CHART)
                .backgroundColor(Color.TRANSPARENT)
                .title(title)
                .titleAlignment(TextAlignment.LEFT)
                .prefSize(480, 480)
                .maxSize(480, 480)
                .chartData(topSendersOrRecipientsData)
                .animated(true)
                .build();
        topSendersOrRecipientsRadialChart.setOnTileEvent((e) -> {
            if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                ChartData data = e.getData();
                System.out.println("Selected " + data.getName());
                openFilterDrawer();
                addFilter(data.getName(), true, false, false, false, false);
            }
        });

        masonryPane.getChildren().add(0, topSendersOrRecipientsRadialChart);

    }

    public void updateDomains(ArrayList<Email> em) {
        masonryPane.getChildren().removeAll(domainDonutChart);

        domains = null;
        domainsData = FXCollections.observableArrayList();
        domains = currentUser.getDomainFreq(em, sentMail);

        PieChart.Data domainOther = new PieChart.Data("Other", 0);
        int domainCount = 0;
        for (Map.Entry<String, Long> entry : domains.entrySet()) {
            if (domainCount < 7) {
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
        domainDonutChart.setPrefSize(500, 480);
        domainDonutChart.setMaxSize(500, 480);
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
                    openFilterDrawer();
                    addFilter(d.getName(), false, false, true, false, false);
                    //addFilter(d.getName());
                }
            });
        }
        domainDonutChart.getStylesheets().add(this.getClass().getClassLoader().getResource("donutchart.css").toExternalForm());
        masonryPane.getChildren().add(domainDonutChart);
    }

    public void updateLanguages(String folderName, String subFolderName, Date startDate, Date endDate, String sender, String domain, String attachment, String language) {
        masonryPane.getChildren().removeAll(languagesDonutChart);

        languages = null;
        languagesData = FXCollections.observableArrayList();
        languages = currentUser.getLanguageFreq(currentUser.filter(folderName, subFolderName, startDate, endDate, sender, domain, attachment, language), sentMail);

        int languagesCount = 0;
        for (Map.Entry<String, Long> entry : languages.entrySet()) {
            PieChart.Data temp = new PieChart.Data(entry.getKey(), entry.getValue());
            if (!entry.getKey().equals("")) {
                languagesData.add(temp);
                languagesCount++;
            }
        }
        languagesDonutChart = new DonutChart(languagesData);
        languagesDonutChart.setPrefSize(300, 300);
        languagesDonutChart.setMaxSize(300, 300);
        languagesDonutChart.setTitle("Languages");
        languagesDonutChart.setLegendVisible(false);
        languagesDonutChart.setLabelsVisible(true);
        languagesDonutChart.getData().stream().forEach(data -> {
            Tooltip tooltip = new Tooltip();
            tooltip.setText((int) data.getPieValue() + " emails");
            Tooltip.install(data.getNode(), tooltip);
            data.pieValueProperty().addListener((observableTwo, oldValueTwo, newValueTwo) ->
                    tooltip.setText((int) newValueTwo + " emails"));
        });
        for (PieChart.Data d : languagesData) {
            d.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    d.getNode().setCursor(Cursor.HAND);
                }
            });
        }
        for (PieChart.Data d : languagesData) {
            d.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    openFilterDrawer();
                    addFilter(d.getName(), false, false, false, false, true);
                    //addFilter(d.getName());
                }
            });
        }
        languagesDonutChart.getStylesheets().add(this.getClass().getClassLoader().getResource("donutchart.css").toExternalForm());
        masonryPane.getChildren().add(languagesDonutChart);
    }


    public void updateAttachments(ArrayList<Email> em) {
        masonryPane.getChildren().removeAll(attachmentsRadialChart);
        //If the it's not updating from a new folder keep the main folder

        attachments = null;

        attachments = currentUser.getAttachmentFreq(em, sentMail);

        attachmentsData = new ArrayList<>();
        int attachmentsCount = 0;
        int attachmentsTotal = 0;
        for (Map.Entry<String, Long> entry : attachments.entrySet()) {
            if (attachmentsCount < 7) {
                ChartData temp = new ChartData();
                temp.setName(entry.getKey());
                temp.setValue(entry.getValue());
                temp.setFillColor(User.colors.get(attachmentsCount));
                attachmentsTotal += entry.getValue();
                attachmentsData.add(temp);
                attachmentsCount++;
            } else {
                attachmentsTotal += entry.getValue();
            }
        }
        for (int i = attachmentsCount; i < 7; i++) {
            ChartData temp = new ChartData();
            temp.setName("");
            temp.setValue(0);
            attachmentsData.add(temp);
        }
        attachmentsRadialChart = TileBuilder.create()
                .animationDuration(10000)
                .skinType(Tile.SkinType.RADIAL_CHART)
                .backgroundColor(Color.TRANSPARENT)
                .title("Attachments")
                .titleAlignment(TextAlignment.LEFT)
                .textVisible(true)
                .text("Total attachments: " + attachmentsTotal)
                .prefSize(480, 480)
                .maxSize(480, 480)
                .chartData(attachmentsData)
                .animated(true)
                .build();
        attachmentsRadialChart.setOnTileEvent((e) -> {
            if (e.getEventType() == TileEvent.EventType.SELECTED_CHART_DATA) {
                DashboardDrawer.setLoadFolderList(false);
                ChartData data = e.getData();
                System.out.println("Selected " + data.getName());
                openFilterDrawer();
                addFilter(data.getName(), false, false, false, true, false);
                //addFilter(data.getName());
            }
        });
        masonryPane.getChildren().add(attachmentsRadialChart);
    }


    private void addFilter(String name, boolean isTopSender, boolean isFolder, boolean isDomain, boolean isAttachment, boolean isLanguage) {


        if (sentMail && isFolder) {
            if (!name.equalsIgnoreCase("sent mail"))
                sentMail = false;
        }


        if (!currentFiltersNames.contains(name)) {
            currentFiltersNames.add(name);
            Filter newFilter = new Filter(name);
            Pane filterChip = new Pane();
            filterChip.getStyleClass().add("pane");
            filterChip.setPrefHeight(35);
            filterChip.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            HBox filterChipHBox = new HBox();
            filterChipHBox.getStyleClass().add("hbox-filter");
            filterChipHBox.setSpacing(10);
            filterChipHBox.setAlignment(Pos.CENTER);
            filterChipHBox.setPrefHeight(35);
            filterChipHBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            JFXButton exitButton = new JFXButton();
            if (isTopSender) {
                for (Filter f : currentFilters) {
                    int count = 0;
                    if (f.isTopSender()) {
                        currentFilters.remove(f);
                        currentFiltersNames.remove(f.getName());
                        //remove appropriate chip
                        ObservableList<Node> chips = filterDrawerClass.filterHbox.getChildren();

                        for (Node n : chips) {
                            Label chipLabel = (Label) n.lookup(".hbox-filter .label");
                            String chipText = chipLabel.getText();
                            if (chipText.equals(f.getName())) {

                                break;
                            }
                            count++;
                        }
                        filterDrawerClass.filterHbox.getChildren().remove(count);
                        break;
                    }
                }
                newFilter.setTopSender(true);
                currentFilters.add(newFilter);
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("USER");
                icon.setFill(Paint.valueOf("#34495e"));
                filterChipHBox.getChildren().add(icon);
                Label nameLabel = new Label(name);
                nameLabel.setAlignment(Pos.CENTER_LEFT);
                nameLabel.maxWidth(Double.MAX_VALUE);
                filterChipHBox.getChildren().add(nameLabel);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                exitButton.setMinSize(20, 20);
                exitButton.setPrefSize(20, 20);
                exitButton.setText("");
                FontAwesomeIconView exitIcon = new FontAwesomeIconView();
                exitIcon.setGlyphName("TIMES");
                exitIcon.setSize("12.5");
                exitIcon.setFill(Paint.valueOf("#ecf0f1"));
                exitButton.setGraphic(exitIcon);
                exitButton.setCursor(Cursor.HAND);
                filterChipHBox.getChildren().add(exitButton);
                filterChip.getChildren().add(filterChipHBox);
                String filterCss = this.getClass().getClassLoader().getResource("filterchip.css").toExternalForm();
                filterChip.getStylesheets().add(filterCss);
                filterDrawerClass.filterHbox.getChildren().add(filterChip);
            } else if (isFolder) {


                for (Filter f : currentFilters) {
                    int count = 0;
                    if (f.isFolder()) {
                        currentFilters.remove(f);
                        currentFiltersNames.remove(f.getName());
                        //remove appropriate chip
                        ObservableList<Node> chips = filterDrawerClass.filterHbox.getChildren();

                        for (Node n : chips) {
                            Label chipLabel = (Label) n.lookup(".hbox-filter .label");
                            String chipText = chipLabel.getText();
                            if (chipText.equals(f.getName())) {

                                break;
                            }
                            count++;
                        }
                        filterDrawerClass.filterHbox.getChildren().remove(count);
                        break;
                    }
                }

                newFilter.setFolder(true);
                currentFilters.add(newFilter);
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("FOLDER");
                icon.setFill(Paint.valueOf("#34495e"));
                filterChipHBox.getChildren().add(icon);
                Label nameLabel = new Label(name);
                nameLabel.setAlignment(Pos.CENTER_LEFT);
                nameLabel.maxWidth(Double.MAX_VALUE);
                filterChipHBox.getChildren().add(nameLabel);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                exitButton.setMinSize(20, 20);
                exitButton.setPrefSize(20, 20);
                exitButton.setText("");
                FontAwesomeIconView exitIcon = new FontAwesomeIconView();
                exitIcon.setGlyphName("TIMES");
                exitIcon.setSize("12.5");
                exitIcon.setFill(Paint.valueOf("#ecf0f1"));
                exitButton.setGraphic(exitIcon);
                exitButton.setCursor(Cursor.HAND);
                filterChipHBox.getChildren().add(exitButton);
                filterChip.getChildren().add(filterChipHBox);
                String filterCss = this.getClass().getClassLoader().getResource("filterchip.css").toExternalForm();
                filterChip.getStylesheets().add(filterCss);
                filterDrawerClass.filterHbox.getChildren().add(filterChip);
            } else if (isDomain) {

                for (Filter f : currentFilters) {
                    int count = 0;
                    if (f.isDomain()) {
                        currentFilters.remove(f);
                        currentFiltersNames.remove(f.getName());
                        //remove appropriate chip
                        ObservableList<Node> chips = filterDrawerClass.filterHbox.getChildren();

                        for (Node n : chips) {
                            Label chipLabel = (Label) n.lookup(".hbox-filter .label");
                            String chipText = chipLabel.getText();
                            if (chipText.equals(f.getName())) {

                                break;
                            }
                            count++;
                        }
                        filterDrawerClass.filterHbox.getChildren().remove(count);
                        break;
                    }
                }
                newFilter.setDomain(true);
                currentFilters.add(newFilter);
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("AT");
                icon.setFill(Paint.valueOf("#34495e"));
                filterChipHBox.getChildren().add(icon);
                Label nameLabel = new Label(name);
                nameLabel.setAlignment(Pos.CENTER_LEFT);
                nameLabel.maxWidth(Double.MAX_VALUE);
                filterChipHBox.getChildren().add(nameLabel);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                exitButton.setMinSize(20, 20);
                exitButton.setPrefSize(20, 20);
                exitButton.setText("");
                FontAwesomeIconView exitIcon = new FontAwesomeIconView();
                exitIcon.setGlyphName("TIMES");
                exitIcon.setSize("12.5");
                exitIcon.setFill(Paint.valueOf("#ecf0f1"));
                exitButton.setGraphic(exitIcon);
                exitButton.setCursor(Cursor.HAND);
                filterChipHBox.getChildren().add(exitButton);
                filterChip.getChildren().add(filterChipHBox);
                String filterCss = this.getClass().getClassLoader().getResource("filterchip.css").toExternalForm();
                filterChip.getStylesheets().add(filterCss);
                filterDrawerClass.filterHbox.getChildren().add(filterChip);
            } else if (isAttachment) {

                for (Filter f : currentFilters) {
                    int count = 0;
                    if (f.isAttachment()) {
                        currentFilters.remove(f);
                        currentFiltersNames.remove(f.getName());

                        //remove appropriate chip
                        ObservableList<Node> chips = filterDrawerClass.filterHbox.getChildren();

                        for (Node n : chips) {
                            Label chipLabel = (Label) n.lookup(".hbox-filter .label");
                            String chipText = chipLabel.getText();
                            if (chipText.equals(f.getName())) {

                                break;
                            }
                            count++;
                        }
                        filterDrawerClass.filterHbox.getChildren().remove(count);
                        break;
                    }
                }
                newFilter.setAttachment(true);
                currentFilters.add(newFilter);
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("PAPERCLIP");
                icon.setFill(Paint.valueOf("#34495e"));
                filterChipHBox.getChildren().add(icon);
                Label nameLabel = new Label(name);
                nameLabel.setAlignment(Pos.CENTER_LEFT);
                nameLabel.maxWidth(Double.MAX_VALUE);
                filterChipHBox.getChildren().add(nameLabel);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                exitButton.setMinSize(20, 20);
                exitButton.setPrefSize(20, 20);
                exitButton.setText("");
                FontAwesomeIconView exitIcon = new FontAwesomeIconView();
                exitIcon.setGlyphName("TIMES");
                exitIcon.setSize("12.5");
                exitIcon.setFill(Paint.valueOf("#ecf0f1"));
                exitButton.setGraphic(exitIcon);
                exitButton.setCursor(Cursor.HAND);
                filterChipHBox.getChildren().add(exitButton);
                filterChip.getChildren().add(filterChipHBox);
                String filterCss = this.getClass().getClassLoader().getResource("filterchip.css").toExternalForm();
                filterChip.getStylesheets().add(filterCss);
                filterDrawerClass.filterHbox.getChildren().add(filterChip);
            } else if (isLanguage) {
                for (Filter f : currentFilters) {
                    int count = 0;
                    if (f.isLanguage()) {
                        currentFilters.remove(f);
                        currentFiltersNames.remove(f.getName());
                        //remove appropriate chip
                        ObservableList<Node> chips = filterDrawerClass.filterHbox.getChildren();

                        for (Node n : chips) {
                            Label chipLabel = (Label) n.lookup(".hbox-filter .label");
                            String chipText = chipLabel.getText();
                            if (chipText.equals(f.getName())) {

                                break;
                            }
                            count++;
                        }
                        filterDrawerClass.filterHbox.getChildren().remove(count);
                        break;
                    }
                }
                newFilter.setLanguage(true);
                currentFilters.add(newFilter);
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("GLOBE_AMERICAS");
                icon.setFill(Paint.valueOf("#34495e"));
                filterChipHBox.getChildren().add(icon);
                Label nameLabel = new Label(name);
                nameLabel.setAlignment(Pos.CENTER_LEFT);
                nameLabel.maxWidth(Double.MAX_VALUE);
                filterChipHBox.getChildren().add(nameLabel);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                exitButton.setMinSize(20, 20);
                exitButton.setPrefSize(20, 20);
                exitButton.setText("");
                FontAwesomeIconView exitIcon = new FontAwesomeIconView();
                exitIcon.setGlyphName("TIMES");
                exitIcon.setSize("12.5");
                exitIcon.setFill(Paint.valueOf("#ecf0f1"));
                exitButton.setGraphic(exitIcon);
                exitButton.setCursor(Cursor.HAND);
                filterChipHBox.getChildren().add(exitButton);
                filterChip.getChildren().add(filterChipHBox);
                String filterCss = this.getClass().getClassLoader().getResource("filterchip.css").toExternalForm();
                filterChip.getStylesheets().add(filterCss);
                filterDrawerClass.filterHbox.getChildren().add(filterChip);
            }
            exitButton.setOnAction((e) -> {
                int position = currentFilters.indexOf(newFilter);
                currentFiltersNames.remove(newFilter.getName());
                currentFilters.remove(position);
                filterDrawerClass.filterHbox.getChildren().remove(position);
            });
        }
    }

    //Resizing ScrollPane stuff
    public void changeScrollPaneHeight(double height) {
        KeyValue keyValue = new KeyValue(scrollPaneLocation, height);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(500), keyValue);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    private double getMasonryPaneLocation() {
        return scrollPaneLocation.get();
    }

    private void updateScrollPaneAnchors() {
        setTopAnchor(scrollPane, 50 + getMasonryPaneLocation());
    }

    //Open Filters drawer when a filter is clicked
    private void openFilterDrawer() {
        if (filtersDrawer.isClosed()) {
            filtersDrawer.setVisible(true);
            filtersDrawer.open();
            changeScrollPaneHeight(75);
            filtersButton.setText("Close Filters");
        }
    }
}
