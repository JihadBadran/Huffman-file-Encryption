package App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by jihadbadran on 11/5/17.
 * the Graphical Interface Application Class, used to open the GUI app,
 * @see Main to operate the app from command line
 */
public class GUIApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("Views/main.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        root.getStylesheets().add("App/Views/style.css");
        primaryStage.show();
        primaryStage.setResizable(false);

    }



}
