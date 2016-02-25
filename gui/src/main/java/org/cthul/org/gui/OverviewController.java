package org.cthul.org.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.cthul.org.model.io.JobManager;

/**
 */
public class OverviewController  {
    
    private WindowManager windowManager;
    
    @FXML
    private VBox vbox;

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
        windowManager.getPrimaryStage().setOnHidden(e -> {
            if (!windowManager.hasOpenLists()) {
                Platform.exit();
                JobManager.shutdown();
            }
        });
    }
}
