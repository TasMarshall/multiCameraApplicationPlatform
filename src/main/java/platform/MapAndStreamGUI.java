package platform;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import platform.core.utilities.NanoTimeValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MapAndStreamGUI extends Application {

    MCP_Application mcp_application;
    MCP_Application_Configuration mcp_application_configuration;
    MapView mapView;

    public MapAndStreamGUI(MCP_Application mcp_application){

        this.mcp_application = mcp_application;
        Application.launch(new String[]{});

    }

    public MapAndStreamGUI(MCP_Application_Configuration mcp_application_configuration) {

        this.mcp_application_configuration = mcp_application_configuration;
        Application.launch(new String[]{});

    }

    @Override
    public void start(Stage stage) throws FileNotFoundException, MalformedURLException {

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.close();
                Platform.exit();
                System.exit(0);
            }
        });

        if (mcp_application == null){
            mapView = new MapView(mcp_application_configuration);
        }
        else {
            mapView = new MapView(mcp_application);
        }


        //loop time start
        final NanoTimeValue nanoTimeValue = new NanoTimeValue(System.nanoTime());

        final NanoTimeValue liveMapTimeStart = new NanoTimeValue(nanoTimeValue.value);

        //Live Map update loop
        new AnimationTimer() {
            @Override public void handle(long currentNanoTime) {
                double t = (currentNanoTime - liveMapTimeStart.value) / 1000000000.0;
                if (t > 1) {
                    mapView.updateDynamicMapOverlay();
                    liveMapTimeStart.value = currentNanoTime;
                }
            }
        }.start();

        //Set up GUI from running MCP_Application
        Parent root = null;
        try {
            URL url = new File("src\\main\\java\\platform\\mcpApplication.fxml").toURL();
            root = FXMLLoader.load(url);
            stage.setTitle("Multi Camera Application");
            stage.setScene(new Scene(root, 300, 275));
            stage.show();

            GUI_Controller.init(mapView, mcp_application);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() throws Exception {

        if (mapView.getMapView() != null) {
            mapView.getMapView().dispose();
        }
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public MCP_Application getMcp_application() {
        return mcp_application;
    }

    public void setMcp_application(MCP_Application mcp_application) {
        this.mcp_application = mcp_application;
    }
}