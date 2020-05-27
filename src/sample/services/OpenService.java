package sample.services;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import sample.Area;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class OpenService {

    private BorderPane contentArea;
    private TabPane tabPane;
    private File directory;

    private Map<File,WatchChangesService> watchChangesServices=new HashMap<>();

    public OpenService(TabPane tabPane, File directory) {
        this.tabPane=tabPane;
        this.directory = directory;
        this.contentArea= (BorderPane) tabPane.getParent();
    }

    public Tab  openFileInNewArea(boolean isNewEmptyArea){
        Tab newTab;
        if (isNewEmptyArea){                                               //if user try to open new document
            newTab= getNewTab(null);
            newTab.setContent(new Area(""));
        }else {                                                            //if user try to open file from his file system
            FileChooser chooser=getFileChooser("NAVIGATE TO YOUR FILE");
            File selectedFile= chooser.showOpenDialog(null);
            if (selectedFile==null){                                      //if user canceled dialog
                return null;
            }
            newTab=getNewTab(selectedFile.getName());
            try {
                String text=new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
                newTab.setContent(new Area(text));
                watchServiceOperator(true,selectedFile);
                directory=new File(selectedFile.getParent());//in next time, default navigate to directory used before
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newTab;
    }

    private Tab getNewTab(String title) {
        String tabTitle= (title!=null?title:"new"+(tabPane.getTabs().size()+1));
        Tab tab=new Tab(tabTitle);
        tab.setClosable(true);
        tab.setDisable(false);
        tab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                Area textArea= (Area) tab.getContent();
                File filePath=Paths.get(directory.getAbsolutePath(),tab.getText()).toFile();
                if (textArea.getText().isEmpty() || textArea.getText().equals(textArea.getStartText())){
                    watchServiceOperator(false,filePath);
                    return;                     //if user try to close empty document will do it
                }                               //or document without changes
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                String fileName=tabTitle;
                String message=String.format("Save changes in document %s?",fileName);
                alert.setContentText(message);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    SaveService saveService = new SaveService(directory);
                    boolean saveRes=saveService.save(tabPane.getTabs().stream()
                            .filter(t->t.equals(tab)).
                                    collect(Collectors.toList()).get(0));
                    if (saveRes) {
                        watchServiceOperator(false,filePath);
                        tabPane.getTabs().remove(tab);
                    }
                } else if (result.get() == ButtonType.NO){
                    watchServiceOperator(false,filePath);
                    tabPane.getTabs().remove(tab);

                }
            }
        });
        return tab;
    }

    private FileChooser getFileChooser(String chooserTitle) {
        FileChooser.ExtensionFilter filter=new FileChooser.ExtensionFilter("standard text format TXT","*.txt");
        FileChooser chooser=new FileChooser();
        chooser.setTitle(chooserTitle);
        chooser.getExtensionFilters().add(filter);
        chooser.setInitialDirectory(directory);
        return chooser;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public BorderPane openOrUpdateProject(File file){
        File selectedDirectory;
        if (file==null) {            //if file was null open new project else update current project tree
            DirectoryChooser chooser=new DirectoryChooser();
            chooser.setInitialDirectory(directory);
            chooser.setTitle("NAVIGATE TO YOUR PROJECT");
            selectedDirectory=chooser.showDialog(null);
        }else {
            selectedDirectory=file;
        }
        if (selectedDirectory==null){        //if dialog was canceled
            return null;
        }
        BorderPane projectPanel=new BorderPane();
        String titlePanelButtonName=String.format("project %s  close x",selectedDirectory.getName());
        Button closeButton=getCloseButton(titlePanelButtonName,projectPanel,selectedDirectory);
        TreeView<File>projectTree=getProjectTree(selectedDirectory);
        projectPanel.setTop(closeButton);
        projectPanel.setCenter(projectTree);
        return projectPanel;
    }

    private TreeView<File> getProjectTree(File selectedDirectory) {
        TreeItem<File>rootItem=new TreeItem<>(selectedDirectory);
        fillTree(rootItem,selectedDirectory);
        TreeView<File>projectTree=new TreeView<>(rootItem);
        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
            handleMouseClicked(event,projectTree);
        };
        projectTree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);

        watchServiceOperator(true,selectedDirectory);                      //power on watch service
        return projectTree;
    }

    private void handleMouseClicked(MouseEvent event, TreeView<File> projectTree) {
        Node node = event.getPickResult().getIntersectedNode();
        // Accept clicks only on node cells, and not on empty spaces of the TreeView
        if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
            File file =(File)projectTree.getSelectionModel().getSelectedItem().getValue();
            Tab newTab=getNewTab(file.getName());
            try {
                String text=new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                newTab.setContent(new Area(text));
                tabPane.getTabs().add(newTab);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
  
  //used recursive method: in the loop if current file is directory method call himself
    private void fillTree(TreeItem<File> rootItem, File directory) {
        try( DirectoryStream<Path>stream=Files.newDirectoryStream(Paths.get(directory.getAbsolutePath()))) {
            for (Path entry:stream){
                File file=entry.toFile();
                TreeItem<File>item=new TreeItem<>(file);
                rootItem.getChildren().add(item);
                if (file.isDirectory()){
                    fillTree(item,file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Button getCloseButton(String titlePanelButtonName, BorderPane projectPanel,File rootFile) {
        Button closeButton=new Button(titlePanelButtonName);
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                watchServiceOperator(false,rootFile); //turn off watch service
                BorderPane parent= (BorderPane) projectPanel.getParent();
                parent.getChildren().remove(projectPanel);
            }
        });
        return closeButton;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void closeOrUpdateTab(String title, File newFile) {
      List <Tab> tabs= tabPane.getTabs().stream().filter(t->title.contains(t.getText())).collect(Collectors.toList());
      tabs.forEach(tab -> tabPane.getTabs().remove(tab));
      if (newFile!=null){
         Tab newTab=getNewTab(newFile.getName());
          try {
              String text=new String(Files.readAllBytes(Paths.get(newFile.getAbsolutePath())));
              newTab.setContent(new Area(text));
          } catch (IOException e) {
              e.printStackTrace();
          }
          tabPane.getTabs().add(newTab);
      }

    }

//this operator allows turn on/off watchChanges on opened file/directory service
// if variable boolean on true-> turn on change listener
//if variable boolean on false-> turn off change listener
    private void watchServiceOperator(boolean on,File file){
        if (file.isDirectory()){                                           //if file directory we working with project
            if (on){                                                       // here is turn on mechanism for project
                WatchChangesService watchChangesService=
                        new WatchChangesService(
                                file.toPath(),
                                new ArrayList<>(Arrays.asList(file.toPath())),contentArea);
                watchChangesService.start();
                watchChangesServices.putIfAbsent(file,watchChangesService);
            }else {                                                         // here is turn off mechanism for project
                WatchChangesService watchChangesService=watchChangesServices.get(file);
                if (watchChangesService.getWatchList().size()==1) {
                    watchChangesService.shutDown();
                    watchChangesServices.remove(file);
                }
            }
        }else {                                                        //if we working with tabbed pane and single files
            File dir=file.getParentFile();
            WatchChangesService watchChangesService=watchChangesServices.getOrDefault(dir,null);
            if (on){                                                             // here is turn on mechanism for files
                if (watchChangesService!=null && watchChangesService.getWatchList().contains(file.toPath())){
                    return;                                                  // when file already added to watch service
                }
                if (watchChangesService!=null){                           //if directory added to service by anther file
                    watchChangesService.getWatchList().add(file.toPath());
                }else {
                   WatchChangesService watchService= new WatchChangesService(dir.toPath(),
                            new ArrayList<>(Arrays.asList(file.toPath())),contentArea);
                    watchService.start();
                    watchChangesServices.put(dir,watchService);
                }
            }else {                                                           // here is turn off mechanism for files
                if (watchChangesService!=null && watchChangesService.getWatchList().size()==1){
                    watchChangesService.shutDown();                     // when watch service work for only this file
                    watchChangesServices.remove(dir);
                }
                if (watchChangesService!=null && watchChangesService.getWatchList().size()<1){
                    watchChangesService.getWatchList().remove(file.toPath());//when another file from this directory
                }                                                            // still open
            }
        }
    }
}
