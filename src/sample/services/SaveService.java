package sample.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import sample.Area;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public class SaveService {
    File directory;

    public SaveService(File directory) {
        this.directory = directory;
    }

    public boolean save(Tab tab){
        Area textArea= (Area) tab.getContent();
        String text=textArea.getText();
        directory.mkdirs();
        FileChooser chooser=new FileChooser();
        FileChooser.ExtensionFilter filter=new FileChooser.ExtensionFilter("standard text format TXT","*.txt");
        chooser.setSelectedExtensionFilter(filter);
        chooser.setTitle("save");
        chooser.setInitialFileName(tab.getText());/////////////
        chooser.setInitialDirectory(directory);
        File selectedFile=chooser.showSaveDialog(null);
        if (selectedFile!=null){
            if (!selectedFile.getAbsolutePath().contains(".txt")){
                selectedFile=new File(selectedFile.getAbsolutePath()+".txt");
            }
            if (isFileExists(selectedFile)) {        //if not exist or file exist and changes confirmed
                try {
                    OutputStream output = new FileOutputStream(selectedFile);
                    output.write(text.getBytes());
                    output.flush();
                    output.close();
                    directory = new File(selectedFile.getParent());//in next saving, default navigate to directory used before
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean isFileExists(File selectedFile) {
        if ( selectedFile.exists()) {
            String message = String.format("The file %s already exist, do you wish override him?",selectedFile.getName());
            Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setContentText(message);
            Optional<ButtonType> result = alert.showAndWait();
            return result.get()==ButtonType.OK;
        }
        return true;
    }
}
