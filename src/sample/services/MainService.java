package sample.services;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.io.File;

public class MainService {
    private final  BorderPane contentArea;
    private TabPane tabPane= new TabPane();
    File directory=new File(".");
    OpenService openFileService ;
    SaveService saveService;

    public MainService(BorderPane contentArea) {
        this.contentArea = contentArea;
        this.contentArea.setCenter(tabPane);
        openFileService =new OpenService(tabPane,directory);
        saveService=new SaveService(directory);
    }

    public void openNewFile(){
        Tab newTab=openFileService.openFileInNewArea(true);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    public void openFile(){
        Tab newTab=openFileService.openFileInNewArea(false);
        if (newTab==null){
            return;
        }
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }


    public void openProject(){
       BorderPane projectTreePanel= openFileService.openOrUpdateProject(null);
       if (projectTreePanel==null){
           return;
       }
       contentArea.setLeft(projectTreePanel);
    }


    public void saveFile(){
        saveService.save(tabPane.getSelectionModel().getSelectedItem());
    }
}
