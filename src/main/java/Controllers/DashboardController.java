package Controllers;

import Engine.Main;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.TileEvent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    private static Stage                myStage;
    private        DashboardDrawer      dashboardDrawer;
    private        FilterDrawer         filterDrawerClass;
    private static BooleanProperty      loadedFromLoginScreen = new SimpleBooleanProperty(false);
    private static ArrayList<ChartData> topSendersData        = new ArrayList<>();
    private        Tile                 topSendersRadialChart;
    private        GridPane             heatMapGridPane;


    public static void setStage(Stage s){
        myStage = s;
    }


    public static void addTopSendersData(ChartData d){
        //topSendersData.add(d);
        topSendersData.add(d);
    }

    public static void setLoadedFromLoginScreenToTrue(){
        loadedFromLoginScreen.setValue(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //topSendersDoughnutChart.setTree(treeNode);
        //Resizing crap, this took way to long to figure out thanks javafx
        topBarGridPane.prefWidthProperty().bind(anchorPane.widthProperty());
        masonryPane.prefWidthProperty().bind(anchorPane.widthProperty());
        masonryPane.maxHeightProperty().bind(anchorPane.heightProperty());
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
            dashboardDrawer.dashboardDrawerVBox.maxHeightProperty().bind(anchorPane.heightProperty());
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
                dashboardDrawer.shrinkListView();
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
                dashboardDrawer.expandListView();
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
                    //Create Radial Chart
                    topSendersRadialChart = TileBuilder.create()
                            .animationDuration(10000)
                            .skinType(Tile.SkinType.RADIAL_CHART)
                            .backgroundColor(Color.TRANSPARENT)
                            .title("Top Senders")
                            .textVisible(true)
                            .titleAlignment(TextAlignment.CENTER)
                            .prefSize(400,400)
                            .chartData(topSendersData)
                            .animated(true)
                            .build();
                    masonryPane.getChildren().add(topSendersRadialChart);
                    //Change scenes based on top sender ChartData selected
                    topSendersRadialChart.setOnTileEvent( (e) -> {
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
                            }catch(IOException error){
                                error.printStackTrace();
                            }
                        }
                    });

                    int[][] heatMapData = Main.getCurrentUser().generateDayOfWeekFrequency();
                    Pane heatMapPane = new Pane();
                    heatMapPane.setMinSize(400,400);
                    heatMapPane.setPrefSize(400,400);
                    heatMapGridPane = new GridPane();
                    heatMapGridPane.setPrefSize(400, 400);
                    //heatMapGridPane.add(new Label(""), 0, 0);
                    for (int i = 0 ; i < heatMapData.length; i++) {
                        for (int j = 0; j < heatMapData[1].length; j++) {
                            Pane pane = new Pane();
                            pane.setMinSize(20,20);
                            pane.setStyle(Main.getCurrentUser().getColorForHeatMap(heatMapData[i][j]));
                            heatMapGridPane.add(pane, i, j);
                        }
                    }
                    heatMapPane.getChildren().add(heatMapGridPane);
                    masonryPane.getChildren().add(heatMapPane);
                }
            }
        });
    }
}
