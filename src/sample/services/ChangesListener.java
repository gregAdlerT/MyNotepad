package sample.services;

import java.io.File;

public class ChangesListener {
    File file;
    private volatile Thread processingThread;

    public ChangesListener(File file) {
        this.file = file;
    }
    public void startListening() {
        processingThread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                    processFileNotifications();
                } catch (InterruptedException e) {
                    processingThread=null;
                }
            }
        };
        processingThread.start();
    }

    public void shutDownListener() {
        Thread thr = processingThread;
        if (thr != null) {
            thr.interrupt();
        }
    }


    private void processFileNotifications() {
        if (!file.isDirectory()){
            checkChanges(file);
        }
    }

    private void checkChanges(File file) {

    }


}
