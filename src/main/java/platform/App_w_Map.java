package platform;

import com.sun.javafx.geom.Vec3d;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import platform.core.camera.core.Camera;

import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.camera.core.components.CameraLocation;
import platform.core.camera.core.components.TargetView;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.goals.components.Area;
import platform.core.goals.components.RectangleArea;
import platform.core.goals.impl.EvenCameraCoverageSim;
import platform.core.goals.impl.component.Road;
import platform.core.goals.impl.component.Traffic;
import platform.core.goals.impl.component.TrafficFlow;
import platform.core.utilities.FrameCount;
import platform.core.utilities.NanoTimeValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class App_w_Map extends Application {

    MCP_Application mcp_application;
    MapView mapView;

    /**
     * Opens and runs application.
     *
     * @param args arguments passed to this application
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.close();
                Platform.exit();
                System.exit(0);
            }
        });

        List<Camera> onvifCameras = new ArrayList<>();

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();
        try {
            Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_onvif1.xml");
            onvifCameras.add(camera);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*HSIP2Time2Camera hsip2Time2Camera2 = new HSIP2Time2Camera(new URL("http://193.159.244.134/onvif/device_service"), "","","service", "Xbks8tr8vT",new CameraLocation(53.947986, -1.041108,10));
        hsip2Time2Camera2.setTargetView(new TargetView(new Vec3d(1,1,1)));
        hsip2Time2Camera2.getTargetView().setTargetLatLon(53.947729, -1.042398); //set target52
        onvifCameras.add(hsip2Time2Camera2);

        HSIP2Time2Camera hsip2Time2Camera3 = new HSIP2Time2Camera(new URL("http://193.159.244.132/onvif/device_service"), "","","service", "Xbks8tr8vT",new CameraLocation(53.947503, -1.042712,10));
        hsip2Time2Camera3.setTargetView(new TargetView(new Vec3d(1,1,1)));
        hsip2Time2Camera3.getTargetView().setTargetLatLon(53.947029, -1.042398); //set target52
        onvifCameras.add(hsip2Time2Camera3);*/

        List<Camera> simulatedCameras = new ArrayList<>();
        SimulatedCamera simulatedCamera = null;
        try {
            simulatedCamera = (SimulatedCamera) cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_sim1.xml");
            simulatedCameras.add(simulatedCamera);
            simulatedCamera.setTargetView(new TargetView());
            simulatedCamera.getTargetView().setTargetLatLon(53.947529, -1.042098); //set target52


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SimulatedCamera simulatedCamera2 = null;

        try {
            simulatedCamera2 = (SimulatedCamera) cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_sim2.xml");
            simulatedCamera2.setTargetView(new TargetView());
            simulatedCamera2.getTargetView().setTargetLatLon(53.947529, -1.042098); //set target52
            simulatedCameras.add(simulatedCamera2);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*BallFinder ballFinder = new BallFinder();*/
        Traffic traffic = new Traffic();
        TrafficFlow trafficFlow = new TrafficFlow();
        Road area = new Road(new RectangleArea(-1.044000,53.947100,-1.039891, 53.947718), Area.CoordinateSys.OUTDOOR);

        EvenCameraCoverageSim evenCameraCoverageSim = new EvenCameraCoverageSim(1,Arrays.asList(area), Arrays.asList(traffic),Arrays.asList(trafficFlow));

        mcp_application = new MCP_Application(Arrays.asList(evenCameraCoverageSim), onvifCameras, simulatedCameras);

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
            GUI_Controller.init(this);
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