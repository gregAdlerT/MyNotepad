package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("My Notepad++");
        InputStream iconStream=getClass().getResourceAsStream("/resources/NotepadIcon.png");
        Image image= new Image(iconStream);
        iconStream.close();////////////////////////////////////////?????????????????????TODO check
        primaryStage.getIcons().add(image);
        primaryStage.setScene(new Scene(root, 800, 600));
        createMainFrame(primaryStage);
        primaryStage.show();
    }

    private void createMainFrame(Stage primaryStage) {
        Box mainContents=new Box();
    }


    public static void main(String[] args) {
        launch(args);

    }
}
