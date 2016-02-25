package org.cthul.org.gui;

import java.io.IOException;
import javafx.application.Application;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.cthul.log.CLogger;
import org.cthul.log.CLoggerFactory;
import org.cthul.org.model.io.CorgConfiguration;
import org.cthul.org.model.report.Report;

/**
 *
 */
public class COrgApplication extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private final CLogger log = CLoggerFactory.getClassLogger();
    
    private Stage stage;
    private GuiSettings settings;
    private CorgConfiguration cfg;
    private WindowManager windowManager;

    @Override
    public void init() throws Exception {
        settings = new GuiSettings("gui.properties");
        String cfgPath = settings.getAutoOpenPath();
        if (cfgPath == null) cfgPath = "myTodo.properties";
        cfg = new CorgConfiguration(cfgPath);
        cfg.initialize();
    }

    @Override
    public void stop() throws Exception {
        try {
            cfg.close();
        } finally {
            settings.close();
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        windowManager = new WindowManager(settings, primaryStage);
        OverviewController ctrl = WindowManager.replaceSceneContent(primaryStage, "Overview.fxml");
        ctrl.setWindowManager(windowManager);
        ListWindow lw = windowManager.newListWindow();
        lw.getReports().add(new Report(cfg.getModel()));
        lw.show();
    }
    
//    private void showOverview() throws IOException {
//        OverviewController ctrl = replaceSceneContent("Overview.fxml");
//        ctrl.setModel(cfg.getModel());
//        stage.show();
//    }
//
//    private void newListStage() throws IOException {
//        OverviewController ctrl = replaceSceneContent("Overview.fxml");
//        ctrl.setModel(cfg.getModel());
//        stage.show();
//    }
//

    
    private static class StageDragger {
        private final Stage stage;
        private boolean dragging = false;
        private double x = 0;
        private double y = 0;

        public StageDragger(Stage stage) {
            this.stage = stage;
            stage.getScene().setOnMousePressed(e -> {
                dragging = e.isPrimaryButtonDown();
                x = stage.getX() - e.getScreenX();
                y = stage.getY() - e.getScreenY();
            });
            stage.getScene().setOnMouseReleased(e -> {
                dragging = e.isPrimaryButtonDown();
            });
            stage.getScene().setOnMouseDragged(e -> {
                if (dragging) {
                    stage.setX(x + e.getScreenX());
                    stage.setY(y + e.getScreenY());
                }
            });
            stage.getScene().setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    stage.close();
                }
            });
        }
        
        
    }
}
