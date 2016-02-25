package org.cthul.org.gui;

import com.sun.javafx.collections.ObservableListWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.cthul.org.model.report.Report;

/**
 *
 */
public class ListWindow {
    
    private final String id;
    private final WindowManager windowManager;
    private final ObservableList<Report> reports = new ObservableListWrapper<>(new ArrayList<>());
    
    private boolean windowed = false;
    private boolean pinned = false;
    private boolean visible = false;
    private boolean scrollBarLeft = false;
    private ListController ctrl = null;
    private Scene scene = null;
    private Stage stage = null;
    
    private Rectangle2D standaloneBounds;

    public ListWindow(String id, WindowManager windowManager) {
        this.id = id;
        this.windowManager = windowManager;
        reports.addListener((Change<? extends Report> c) -> {
            if (ctrl != null) {
                ctrl.setContent(this, reports);
            }
        });
    }

    public String getId() {
        return id;
    }

    public List<Report> getReports() {
        return reports;
    }

    protected Scene getScene() {
        if (scene != null) {
            return scene;
        }
        
        String fxml = "List.fxml";
        FXMLLoader loader = new FXMLLoader();
        try (InputStream in = COrgApplication.class.getResourceAsStream(fxml)) {
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            loader.setLocation(COrgApplication.class.getResource(fxml));
            Pane page = loader.load(in);
            scene = new Scene(page, page.getPrefWidth(), page.getPrefHeight());
            initController(loader.getController());
            initScene(scene);
            if (visible) {
                show();
            }
            return scene;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ListController getController() {
        if (ctrl == null) getScene();
        return ctrl;
    }

    protected void initScene(Scene scene) {
        scene.setFill(null);
        class DragData {
            double x,y;
        }
        DragData stageDrag = new DragData();
        scene.setOnMousePressed(e -> {
            stageDrag.x = stage.getX() - e.getScreenX();
            stageDrag.y = stage.getY() - e.getScreenY();
        });
        scene.setOnMouseDragged(e -> {
            if (pinned) return;
            stage.setX(stageDrag.x + e.getScreenX());
            stage.setY(stageDrag.y + e.getScreenY());
        });
        
//        ScrollPane sb = (ScrollPane) scene.lookup(".scroll-pane");
//        sb.setOnMousePressed(e -> {
//            System.out.println("---------------------");
//            stageDrag.x = stage.getX() - e.getScreenX();
//            stageDrag.y = stage.getY() - e.getScreenY();
//        });
//        sb.setPannable(true);
//        sb.setOnMouseMoved(e -> {
////            if (pinned) return;
//            System.out.println("------ " + e.isPrimaryButtonDown());
//            if (!e.isPrimaryButtonDown()) return;
//            stage.setX(stageDrag.x + e.getScreenX());
//            stage.setY(stageDrag.y + e.getScreenY());
//        });
        
        DragData dragLeft = new DragData(), dragRight = new DragData();
        Pane p = (Pane) scene.lookup("#btnResizeLeft");
        p.setOnMousePressed(e -> {
            dragLeft.x = stage.getWidth() + e.getScreenX();
            dragLeft.y = stage.getHeight() - e.getScreenY();
        });
        p.setOnMouseDragged(e -> {
            if (pinned) return;
            e.consume();
            double oldWidth = stage.getWidth();
            stage.setWidth(dragLeft.x - e.getScreenX());
            stage.setHeight(dragLeft.y + e.getScreenY());
            stage.setX(stage.getX() + oldWidth - stage.getWidth());
        });
        p = (Pane) scene.lookup("#btnResizeRight");
        p.setOnMousePressed(e -> {
            dragRight.x = stage.getWidth() - e.getScreenX();
            dragRight.y = stage.getHeight() - e.getScreenY();
        });
        p.setOnMouseDragged(e -> {
            if (pinned) return;
            e.consume();
            stage.setWidth(dragRight.x + e.getScreenX());
            stage.setHeight(dragRight.y + e.getScreenY());
        });
    }
    
    protected void initController(ListController ctrl) {
        this.ctrl = ctrl;
        ctrl.setContent(this, reports);
        
        String s = windowManager.getGuiSettings().getWindowSettings(id);
        if (s != null) {
            loadSettings(s);
        }
        setPinned(pinned);
        setScrollBarLeft(scrollBarLeft);
    }
    
    public void show(boolean windowed) {
        if (stage != null && this.windowed == windowed) {
            showCurrentStage();
            return;
        }
        this.windowed = windowed;
        showNewStage();
    }
    
    public void show() {
        show(windowed);
    }
    
    public void setWindowed(boolean windowed) {
        if (visible) {
            show(windowed);
        } else {
            this.windowed = windowed;
        }
    }
    
    public void toggleFrame() {
        setWindowed(!windowed);
    }

    public void togglePinned() {
        setPinned(!pinned);
    }
    
    public void toggleScrollBarPosition() {
        setScrollBarLeft(!scrollBarLeft);
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        if (pinned) {
            getScene().getRoot().getStyleClass().remove("unpinned");
            getScene().getRoot().getStyleClass().add("pinned");
        } else {
            getScene().getRoot().getStyleClass().remove("pinned");
            getScene().getRoot().getStyleClass().add("unpinned");
        }
    }
    
    public void setScrollBarLeft(boolean scrollBarLeft) {
        this.scrollBarLeft = scrollBarLeft;
        getController().setScrollBarLeft(scrollBarLeft);
    }
    
    protected void showNewStage() {
        if (stage != null) {
            stage.setOnHiding(null);
            stage.close();
        }
        
        stage = new Stage();
        if (windowed) {
//            stage.initStyle(StageStyle.UTILITY);
            stage.getIcons().add(ListController.IMAGE_RESOLVER.getImage("00/task/done"));
            getScene().getRoot().getStyleClass().remove("standalone");
            getScene().getRoot().getStyleClass().add("windowed");
            if (standaloneBounds != null) {
                stage.centerOnScreen();
                stage.setWidth(standaloneBounds.getWidth());
                stage.setHeight(standaloneBounds.getHeight());
            }
        } else {
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initOwner(windowManager.getToolStage());
            getScene().getRoot().getStyleClass().remove("windowed");
            getScene().getRoot().getStyleClass().add("standalone");
            if (standaloneBounds != null) {
                stage.setWidth(standaloneBounds.getWidth());
                stage.setHeight(standaloneBounds.getHeight());
                if (Screen.getScreensForRectangle(standaloneBounds).isEmpty()) {
                    stage.centerOnScreen();
                } else {
                    stage.setX(standaloneBounds.getMinX());
                    stage.setY(standaloneBounds.getMinY());
                }
            }
        }
        stage.setScene(getScene());
        if (standaloneBounds == null) {
            stage.sizeToScene();
            stage.centerOnScreen();
        }
        
        stage.setOnHiding(e -> close());
        showCurrentStage();
    }
    
    protected void showCurrentStage() {
        visible = true;
        windowManager.opened(this);
        stage.show();
    }
    
    public void close() {
        visible = false;
        exit();
    }
    
    public void exit() {
        if (!windowed) {
            standaloneBounds = new Rectangle2D(
                    stage.getX(), stage.getY(), 
                    stage.getWidth(), stage.getHeight());
        }
        windowManager.closed(this);
        if (stage != null) {
            stage.hide();
        }
        windowManager.getGuiSettings().setWindowSettings(id, settingsString());
    }
    
    public void showMainWindow() {
        windowManager.showMainWindow();
    }
    
    protected void loadSettings(String settings) {
        for (String s: settings.split(";")) {
            if (s.isEmpty()) continue;
            String[] v = s.split(":");
            switch (v[0]) {
                case "windowed":
                    windowed = Boolean.parseBoolean(v[1]);
                    break;
                case "pinned":
                    pinned = Boolean.parseBoolean(v[1]);
                    break;
                case "scrollBarLeft":
                    scrollBarLeft = Boolean.parseBoolean(v[1]);
                    break;
                case "visible":
                    visible = Boolean.parseBoolean(v[1]);
                    break;
                case "bounds":
                    standaloneBounds = parseRect(v[1]);
                    break;
                default:
            }
        }
    }
    
    protected String settingsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("windowed:").append(windowed);
        sb.append(";pinned:").append(pinned);
        sb.append(";scrollBarLeft:").append(scrollBarLeft);
        sb.append(";visible:").append(visible);
        if (standaloneBounds != null) {
            sb.append(";bounds:")
                    .append(storeRect(standaloneBounds));
        }
        return sb.toString();
    }
    
    private static String storeRect(Rectangle2D r) {
        return String.format(Locale.ENGLISH, "%.1f,%.1f,%.1f,%.1f", 
                r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }
    
    private static Rectangle2D parseRect(String s) {
        String[] d = s.split(",");
        return new Rectangle2D(
                Double.parseDouble(d[0]), Double.parseDouble(d[1]),
                Double.parseDouble(d[2]), Double.parseDouble(d[3]));
    }
}
