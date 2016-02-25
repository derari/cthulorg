package org.cthul.org.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 */
public class WindowManager {
    
    private final GuiSettings guiSettings;
    private final Stage primaryStage;
    private final Stage toolStage;
    private final Map<String, ListWindow> windows = Collections.synchronizedMap(new HashMap<>());
    private final Set<ListWindow> openWindows = Collections.synchronizedSet(new HashSet<>());

    public WindowManager(GuiSettings guiSettings, Stage primaryStage) {
        this(guiSettings, primaryStage, new Stage(StageStyle.UTILITY));
        toolStage.setWidth(1);
        toolStage.setHeight(1);
        toolStage.setX(-100);
        toolStage.setY(-100);
        toolStage.show();
    }
    
    public WindowManager(GuiSettings guiSettings, Stage primaryStage, Stage toolStage) {
        this.guiSettings = guiSettings;
        this.primaryStage = primaryStage;
        this.toolStage = toolStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getToolStage() {
        return toolStage;
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }
    
    public boolean hasOpenLists() {
        return !openWindows.isEmpty();
    }
    
    public void opened(ListWindow listWindow) {
        openWindows.add(listWindow);
    }

    public void closed(ListWindow listWindow) {
        openWindows.remove(listWindow);
        if (!hasOpenLists()) {
            getPrimaryStage().show();
        }
    }
    
    public ListWindow getWindow(String id) {
        return windows.computeIfAbsent(id, k -> new ListWindow(k, this));
    }
    
    public ListWindow newListWindow() {
        final String c = "bdfghjklmnprstwx";
        final String v = "aeiouy";
        final int len = 4;
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len*2);
        String id = "";
        while (!id.isEmpty() && !windows.containsKey(id)) {
            sb.setLength(0);
            for (int i = 0; i < len; i++) {
                sb.append(c.charAt(rnd.nextInt(c.length())));
                sb.append(v.charAt(rnd.nextInt(v.length())));
            }
            id = sb.toString();
        }
        return getWindow(id);
    }
    
    public static <T> T replaceSceneContent(Stage stage, String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = COrgApplication.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(COrgApplication.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        } 
        Scene scene = new Scene(page, page.getPrefWidth(), page.getPrefHeight());
        stage.setScene(scene);
        stage.sizeToScene();
//        new StageDragger(stage);
        
        return loader.getController();
    }

    public void showMainWindow() {
        getPrimaryStage().show();
    }
}
