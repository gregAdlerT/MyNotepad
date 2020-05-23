package sample;

import javafx.scene.control.TextArea;

public class Area extends TextArea {
    String startText;
    public Area(String startText){
        this.startText=startText;
        initTextArea();
    }

    private void initTextArea() {
        this.appendText(startText);

    }

    public String getStartText() {
        return startText;
    }
}
