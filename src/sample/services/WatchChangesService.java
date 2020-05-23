package sample.services;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class WatchChangesService extends Thread {
    private static final String CREATED = "created";
    private static final String DELETED = "deleted";
    private static final String MODIFIED = "modified";
    private List<Path> watchList= new ArrayList<>();//paths of files includes this watch service
    private Path path;                                // path to directory on watch service
    private BorderPane contentArea;
    private OpenService openService;
    private WatchService watchService;

    public WatchChangesService(Path path, ArrayList<Path>watchList, BorderPane contentArea) {
        this.path = path;
        this.watchList=watchList;
        this.contentArea=contentArea;
        initService();
    }

    private void initService() {
        TabPane tabPane= (TabPane) contentArea.getCenter();
        openService=new OpenService(tabPane,path.toFile());
    }

    public List<Path> getWatchList() {
        return watchList;
    }

    public Path getPath() {
        return path;
    }

    private void eventHandler(Path pathOfChangedFile,String kind) {
        Path fullPath=path.resolve(pathOfChangedFile);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                    if (!watchList.contains(fullPath) &&
                            fullPath.toString().contains(path.toString())){
                        updateProject(pathOfChangedFile,CREATED);
                    }
                } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
                       if (watchList.contains(fullPath)){
                           updateTextArea(pathOfChangedFile,DELETED);
                       }else {
                           updateProject(pathOfChangedFile,DELETED);
                       }
                    } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY.name())) {
                    if (watchList.contains(fullPath)) {
                        updateTextArea(fullPath,MODIFIED);
                    }else {
                        updateProject(pathOfChangedFile,MODIFIED);
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        try {
             watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            // loop forever to watch directory
            while (true) {
                WatchKey watchKey;
                Map<Path,String>events = new HashMap<>();
                watchKey = watchService.take(); // this call is blocking until events are present

                WatchEvent event= watchKey.pollEvents().get(0);
                Path path= (Path) event.context();
                String kind= event.kind().name();
                events.putIfAbsent(path,kind);
                    eventHandler(path,events.get(path));
                    events=null;
                watchKey.cancel();
                sleep(1000);
                // if the watched directed gets deleted, get out of run method
                if (!watchKey.reset()) {
                    watchKey.cancel();
                    watchService.close();
                    break;
                }
            }

        } catch (InterruptedException ex) {
            return;
        } catch (IOException ex) {
            return;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void updateProject(Path changedFile,String change){
        String message="";
        switch (change) {
            case CREATED: message = String.format("New file \" %s \" has been added\n Reload project's content?",changedFile);
            break;
            case DELETED: message = String.format("File \" %s \" has been removed\n Reload project's content?",changedFile);
                break;
            case MODIFIED: message = String.format("File \" %s \" has been changed\n Reload project's content?",changedFile);
                break;
        }
        boolean alertDialogRes=openOptionDialog(message);
        if (alertDialogRes) {
            File projectDirectory=watchList.get(0).toFile();
            BorderPane projectTreePanel= openService.openOrUpdateProject(projectDirectory);
            BorderPane oldProjectTreePanel = (BorderPane) contentArea.getLeft();
            contentArea.getChildren().remove(oldProjectTreePanel);
            contentArea.setLeft(projectTreePanel);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void updateTextArea(Path changedFilePath, String change) {
        String message="";
        switch (change) {
            case DELETED: message = String.format("File \" %s \" has been removed\n Hold your content in editor?",changedFilePath);
                break;
            case MODIFIED: message = String.format("File \" %s \" has been changed\n Update your content in editor?",changedFilePath);
                break;
        }
        boolean alertDialogRes=openOptionDialog(message);
        System.out.println(alertDialogRes);
        if (change.equals(DELETED)) {
            watchList.remove(path.resolve(changedFilePath));
            if (!alertDialogRes){
                openService.closeOrUpdateTab(changedFilePath.toString(),null);
            }
        }else {
            if (alertDialogRes){
                openService.closeOrUpdateTab(changedFilePath.toString(),path.resolve(changedFilePath).toFile());
            }
        }
        if (watchList.isEmpty()){
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean openOptionDialog(String message) {
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alert changes dialog");
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get()==ButtonType.OK;
    }

    public void shutDown(){
        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
