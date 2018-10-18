package Controllers;

import Engine.Main;
import Engine.User;
import Engine.Email;
import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.SunburstChart;
import eu.hansolo.tilesfx.events.TileEvent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
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
    private Tile domainDonutChart;
    private Map<String, Long> domains;
    private Map<String, Long> attachments;
    private ArrayList<ChartData> domainsChartData = new ArrayList<>();


    public static void setStage(Stage s) {
        myStage = s;
    }


    public static void addTopSendersData(ChartData d) {
        //topSendersData.add(d);
        topSendersData.add(d);
    }

    public static void setLoadedFromLoginScreenToTrue() {
        loadedFromLoginScreen.setValue(true);
    }

    public static void setHomeOnCloseRequest(Boolean b){
        homeOnCloseRequest.setValue(b);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        String scrollPaneCss = this.getClass().getClassLoader().getResource("scrollpane.css").toExternalForm();
        scrollPane.getStylesheets().add(scrollPaneCss);

        //Resizing crap, this took way to long to figure out thanks javafx
        scrollPane.setFitToWidth(true);
        Platform.runLater(()->scrollPane.requestLayout());
        topBarGridPane.prefWidthProperty().bind(anchorPane.widthProperty());
        masonryPane.prefWidthProperty().bind(anchorPane.widthProperty());
        centerColumn.maxWidthProperty().bind(topBarGridPane.widthProperty());
        gridPaneLeft.maxWidthProperty().bind(topBarGridPane.widthProperty());
        gridPaneRight.maxWidthProperty().bind(topBarGridPane.widthProperty());
        drawer.prefHeightProperty().bind(anchorPane.heightProperty());

        drawer.setVisible(false);
        filtersDrawer.setVisible(false);
        //Allows you to click through the drawer if it's not visible (so we set it invisible when it's not open)
        drawer.setPickOnBounds(false);
        filtersDrawer.setPickOnBounds(false);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Dashboard_Drawer.fxml"));
            VBox box = loader.load();
            dashboardDrawer = loader.getController();
            //dashboardDrawer.dashboardDrawerVBox.maxHeightProperty().bind(anchorPane.heightProperty());
            dashboardDrawer.dashboardDrawerVBox.prefHeightProperty().bind(anchorPane.heightProperty());
            drawer.setSidePane(box);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Filters_Drawer.fxml"));
            HBox box = loader.load();
            filterDrawerClass = loader.getController();
            filterDrawerClass.filterHbox.maxWidthProperty().bind(topBarGridPane.widthProperty());
            filtersDrawer.setSidePane(box);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                    currentUser = Main.getCurrentUser();
                    //Top senders Radial Chart:
                    topSendersRadialChart = TileBuilder.create()
                            .animationDuration(10000)
                            .skinType(Tile.SkinType.RADIAL_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            .title("Top Senders")
                            .textVisible(true)
                            .titleAlignment(TextAlignment.CENTER)
                            .prefSize(400, 400)
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
                            try {
                                loadedFromLoginScreen.setValue(false);
                                AnchorPane newScenePane = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));
                                Scene newScene = new Scene(newScenePane, 1000, 600);
                                myStage.requestFocus();
                                myStage.setScene(newScene);
                                DashboardDrawer.setLoadFolderList(true);
                            } catch (IOException error) {
                                error.printStackTrace();
                            }
                        }
                    });

                    //HeatMap:
                    //rather than using em here, assign the value of em to be whatever the list of emails we want
                    //aka, add filter, and then display those results
                    ArrayList<Email> em = Main.getCurrentUser().getEmails();
                    //ArrayList<Email> em2 = Main.getCurrentUser().getEmailsFromFolder("first year admin stuff", "testFolder");
                    int[][] heatMapData = Main.getCurrentUser().generateDayOfWeekFrequency(em);
                    VBox heatMapAndTitle = new VBox();
                    Pane heatMapPane = new Pane();
                    Label heatMapTitle = new Label("Received Email Frequency");
                    heatMapTitle.setTextFill(Color.LIGHTGRAY);
                    heatMapTitle.setStyle("-fx-font: 22 System;");
                    heatMapPane.setPrefSize(600, 300);
                    heatMapGridPane = new GridPane();
                    heatMapGridPane.setPrefSize(600, 300);

                    for (int i = 0; i < heatMapData.length; i++) {
                        Label day = new Label(Main.getCurrentUser().getDay(i));
                        day.setStyle("-fx-text-fill: #ff931e;");
                        heatMapGridPane.add(day, 0, i + 1);
                        day.setMinWidth(Region.USE_PREF_SIZE);
                        day.setMaxWidth(Region.USE_PREF_SIZE);

                        for (int j = 0; j < heatMapData[1].length; j++) {
                            Label hour = new Label(Integer.toString(j));
                            hour.setStyle("-fx-text-fill: #ff931e;");
                            heatMapGridPane.add(hour, j + 1, 0);
                            hour.setMinWidth(Region.USE_PREF_SIZE);
                            hour.setMaxWidth(Region.USE_PREF_SIZE);


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
                    masonryPane.getChildren().add(heatMapAndTitle);


                    //Folders SunburstChart:
                    foldersSunburstChart = TileBuilder.create()
                            .skinType(Tile.SkinType.SUNBURST)
                            .backgroundColor(Color.TRANSPARENT)
                            .sunburstBackgroundColor(Color.TRANSPARENT)
                            .title("Folder Structure")
                            .textVisible(true)
                            .titleAlignment(TextAlignment.CENTER)
                            .sunburstTextOrientation(SunburstChart.TextOrientation.HORIZONTAL)
                            .showInfoRegion(true)
                            .minSize(400, 400)
                            .prefSize(400, 400)
                            .sunburstTree(currentUser.getFoldersCountForSunburst())
                            .sunburstInteractive(true)
                            .build();
                    masonryPane.getChildren().add(foldersSunburstChart);

                    domains = currentUser.getDomainFreq(currentUser.getEmails());
                    attachments = currentUser.getAttachmentFreq(currentUser.getEmails());
                    int colorCount = 0;
                    for (Map.Entry<String, Long> entry : domains.entrySet()) {
                        ChartData temp = new ChartData();
                        temp.setName(entry.getKey());
                        temp.setValue(entry.getValue());
                        temp.setFillColor(User.colors.get(colorCount));
                        domainsChartData.add(temp);
                        if (colorCount < 19) {
                            colorCount++;
                        }else{
                            colorCount = 0;
                        }
                    }

                    domainDonutChart = TileBuilder.create()
                            .animationDuration(10000)
                            .skinType(Tile.SkinType.DONUT_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            .title("Domain's")
                            .textVisible(true)
                            .titleAlignment(TextAlignment.CENTER)
                            .prefSize(400, 400)
                            .chartData(domainsChartData)
                            .animated(true)
                            .build();
                    //domainDonutChart.setCursor(Cursor.HAND);
                    masonryPane.getChildren().add(domainDonutChart);

                    //Allows the scroll pane to resize the masonry pane after nodes are added
                    Platform.runLater(()->scrollPane.requestLayout());
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
}
