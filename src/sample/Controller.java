package sample;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import sample.services.MainService;

import javax.annotation.PostConstruct;

public class Controller {
    @FXML
    private BorderPane mainFrame;

    @FXML
    private  BorderPane contentArea;

    private MainService service;

    @FXML
    public void onNew(){
        if (service==null){service=new MainService(contentArea);}
        service.openNewFile();
    }
    @FXML
    public void onOpen(){
        if (service==null){service=new MainService(contentArea);}
        service.openFile();
    } @FXML
    public void onOpenProject(){
        if (service==null){service=new MainService(contentArea);}
        service.openProject();
    } @FXML
    public void onSave(){
        if (service==null){service=new MainService(contentArea);}
        service.saveFile();
    }

}
