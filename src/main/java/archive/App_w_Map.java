package archive;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import platform.MultiCameraCore;
import platform.MultiCameraCore_Configuration;
import platform.utilities.FrameCount;
import platform.utilities.NanoTimeValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
public class App_w_Map extends Application {

    public static String mcp_application_config;
    MultiCameraCore mcp_application;
    MapView mapView;

    /**
     * Opens and runs application.
     *
     * @param args arguments passed to this application
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            if (args[0].equals("-mcp_config_file")){
                mcp_application_config = args[1];
                Application.launch(args);
            }
        }

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

        MultiCameraCore_Configuration mcp_application_configuration = new MultiCameraCore_Configuration();
        mcp_application = mcp_application_configuration.createMCAppFromMCPConfigurationFile(mcp_application_config +".xml");



        mapView = new MapView(mcp_application);

        //loop time start
        final NanoTimeValue nanoTimeValue = new NanoTimeValue(System.nanoTime());

        final NanoTimeValue mcAppTimeStart = new NanoTimeValue(nanoTimeValue.value);
        final FrameCount mcAppFrameCount = new FrameCount(nanoTimeValue.value);

        final NanoTimeValue liveMapTimeStart = new NanoTimeValue(nanoTimeValue.value);

        //Multi Camera Application update loop
        new AnimationTimer() {
            @Override public void handle(long currentNanoTime) {

                //Simple FPS calculator
                mcAppFrameCount.tick(currentNanoTime);

                double t = (currentNanoTime - mcAppTimeStart.value) / 1000000000.0;
                if (t > 0.1) {
                    mcp_application.executeMAPELoop();
                    mcAppTimeStart.value = currentNanoTime;
                }
            }
        }.start();

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
            GUI_Controller.init(mapView,mcp_application);
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

    public MultiCameraCore getMcp_application() {
        return mcp_application;
    }

    public void setMcp_application(MultiCameraCore mcp_application) {
        this.mcp_application = mcp_application;
    }
}