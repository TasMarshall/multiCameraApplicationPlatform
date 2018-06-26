package gui.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class Test extends Application {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    @Override
    public void start(Stage stage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("platform/mcpApplication.fxml"));
        stage.setTitle("Multi Camera Application");
        stage.setScene(new Scene(root, 300, 275));
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
