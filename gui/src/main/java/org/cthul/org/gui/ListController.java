package org.cthul.org.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.cthul.org.gui.img.ImageResolver;
import org.cthul.org.model.task.EntryGroup;
import org.cthul.org.model.task.EntryText;
import org.cthul.org.model.io.Token;
import org.cthul.org.model.report.AbstractResultVisitor;
import org.cthul.org.model.report.Report;
import org.cthul.org.model.report.Result;
import org.cthul.org.model.task.Tag;
import org.cthul.org.model.task.Task;

/**
 */
public class ListController  {
    
    private final Runnable onChangeListener = this::onChange;
    private final List<Node> entries = new ArrayList<>();
    private ListWindow listWindow;
    private List<Report> reports;
    
    @FXML
    private GridPane grid;
    
    @FXML
    private Pane contentPane;
    
    @FXML
    private Pane scrollTrack;
    
    @FXML
    private Pane scrollBar;
    
    @FXML
    private Rectangle clipRect;

    private final DoubleProperty scrollValue = new SimpleDoubleProperty(this, "scrollValue", 0);
    private double contentMaxY = 0, contentDeltaH = 0;
    
    @FXML
    public void initialize() {
        grid.setVgap(COLUMN_H_GAP);
        grid.setHgap(COLUMN_H_GAP);
//        scrollPane.setFitToWidth(true);
//        scrollPane.setStyle("");
//        ScrollBar bar = (ScrollBar) scrollPane.lookup(".scroll-bar");
//        bar.setUnitIncrement(ICON_SIZE);
//        scrollPane.setPrefViewportHeight(10000);
        
        scrollBar.maxHeightProperty().bind(
                contentPane.heightProperty()
                        .multiply(contentPane.heightProperty())
                        .divide(grid.heightProperty()));
        scrollBar.heightProperty().addListener((v, o, n) -> refreshScrollRange());
        scrollValue.addListener((v, o, n) -> {
            double value = n.doubleValue();
            if (value < 0) {
                scrollValue.set(0);
                return;
            }
            if (value > 1) {
                scrollValue.set(1);
                return;
            }
            refreshScrollPosition();
        });
        
        class ScrollDragData {
            double v, y;
        }
        ScrollDragData scrollData = new ScrollDragData();
        scrollBar.setOnMousePressed(e -> {
            e.consume();
            if (e.getClickCount() == 2) {
                listWindow.toggleScrollBarPosition();
            }
            scrollData.v = scrollValue.doubleValue();
            scrollData.y = -e.getSceneY();
        });
        scrollBar.setOnMouseDragged(e -> {
            e.consume();
            scrollValue.set(scrollData.v + 
                    (scrollData.y + e.getSceneY()) / 
                            (contentPane.getHeight() - scrollBar.getHeight() ));
        });
    }
    
    private void refreshScrollRange() {
        contentDeltaH = grid.getHeight() - contentPane.getHeight();
        if (contentDeltaH <= 0) {
            scrollTrack.setVisible(false);
            contentMaxY = 0;
        } else {
            scrollTrack.setVisible(true);
            for (Node n: entries) {
                if (n.getBoundsInParent().getMinY() >= contentDeltaH) {
                    // there is more space above `n` than the grid is too large
                    contentMaxY = n.getBoundsInParent().getMinY() + 1;
                    break;
                }
            }
        }
        
        refreshScrollPosition();
    }
    
    private void refreshScrollPosition() {
        if (contentMaxY == 0) {
            grid.setTranslateY(0);
            clipRect.setHeight(contentPane.getHeight());
            return;
        }
        
        double y = scrollValue.doubleValue() * contentMaxY;
        Iterator<Node> entryIt = entries.iterator();
        Node node = entryIt.next();
        
        while (y > node.getBoundsInParent().getMaxY() && entryIt.hasNext()) {
            node = entryIt.next();
        }
        
        double nodeY = node.getBoundsInParent().getMinY();
        grid.setTranslateY(-nodeY);
        
        double scrollBarY = Math.min(
                nodeY * contentPane.getHeight() / grid.getHeight(),
                contentPane.getHeight() - scrollBar.getHeight());
        scrollBar.setTranslateY(scrollBarY);
        
        while (nodeY + contentPane.getHeight() > node.getBoundsInParent().getMaxY() && entryIt.hasNext()) {
            node = entryIt.next();
        }
        
        if (nodeY + contentPane.getHeight() >= node.getBoundsInParent().getMaxY()) {
            clipRect.setHeight(contentPane.getHeight());
        } else {
            clipRect.setHeight(node.getBoundsInParent().getMinY() - nodeY - 1);
        }
    }
    
    public void setContent(ListWindow listWindow, ObservableList<Report> reports) {
        this.listWindow = listWindow;
        this.reports = reports;
        this.reports.forEach(r -> r.addListener(onChangeListener));
        reports.addListener((Change<? extends Report> c) -> {
            c.getRemoved().forEach(r -> r.removeListener(onChangeListener));
            c.getAddedSubList().forEach(r -> r.addListener(onChangeListener));
            onChange();
        });
        onChange();
    }
    
    public void setScrollBarLeft(boolean scrollBarLeft) {
        if (scrollBarLeft) {
            StackPane.setAlignment(scrollTrack, Pos.TOP_LEFT);
            StackPane.setAlignment(contentPane, Pos.TOP_RIGHT);
        } else {
            StackPane.setAlignment(scrollTrack, Pos.TOP_RIGHT);
            StackPane.setAlignment(contentPane, Pos.TOP_LEFT);
        }
    }
    
    private void onChange() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        entries.clear();
        
        ColumnConstraints c = new ColumnConstraints(1);
        grid.getColumnConstraints().add(c);
        
        ResultRenderer renderer = new ResultRenderer();
        reports.forEach(r -> r.accept(renderer));
        
        c.setPrefWidth(renderer.extraWidth);
        c = grid.getColumnConstraints().get(grid.getColumnConstraints().size()-1);
        c.setFillWidth(true);
        c.setHgrow(Priority.ALWAYS);
        c.setMaxWidth(10000);
        
//        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scrollPane.setStyle("-fx-background-color:blue; -fx-control-inner-background: transparent;");

    }
    
    @FXML
    public void closeClicked() {
        listWindow.close();
    }
    
    @FXML
    public void frameClicked() {
        listWindow.toggleFrame();
    }
    
    @FXML
    public void pinClicked() {
        listWindow.togglePinned();
    }
    
    @FXML
    public void editClicked() {
    }
    
    @FXML
    public void optionsClicked() {
        listWindow.showMainWindow();
    }

    class ResultRenderer extends AbstractResultVisitor {
        
        int line = 0;
        int level = 0;
        double extraWidth = 0;

        @Override
        protected void visit(Result result, EntryGroup group) {
            level++;
            super.visit(result, group);
            level--;
        }

        @Override
        protected void visit(Result result, EntryText entry) {
            while (level >= grid.getColumnConstraints().size()) {
                ColumnConstraints c = new ColumnConstraints(COLUMN_WIDTH);
                grid.getColumnConstraints().add(c);
            }
            TextFlow textFlow = new TextFlow();
            textFlow.setLineSpacing(COLUMN_H_GAP-1);
            textFlow.setTranslateY((ICON_SIZE-TEXT_SIZE)/4.0-1);
            entry.getNode().accept(new TextFlowRenderer(textFlow, entry));
            GridPane.setColumnSpan(textFlow, GridPane.REMAINING);
            grid.add(textFlow, level, line);
            entries.add(textFlow);
            
            HBox iconBox = new HBox(TAG_BOX_H_GAP);
            iconBox.setAlignment(Pos.TOP_RIGHT);
            grid.add(iconBox, 0, line);
            GridPane.setColumnSpan(iconBox, level);
            
            renderTaskIcons(entry.getTasks().get(0), iconBox);
            line++;
        }

        private void renderTaskIcons(Task task, HBox iconBox) {
            task.getPrefixTags().stream()
                    .filter(t -> !t.isHidden())
                    .forEach(tag -> {
                Image img = IMAGE_RESOLVER.getImage(tag.getIconName());
                ImageView iv = new ImageView(img);
                iv.setFitWidth(ICON_SIZE);
                iv.setFitHeight(ICON_SIZE);
                iv.getStyleClass().add("tag");
                iv.getStyleClass().add(task.getStatus().getName());
                iconBox.getChildren().add(iv);
            });
            Image img = IMAGE_RESOLVER.getImage(task.getIconName());
            ImageView iv = new ImageView(img);
            iv.setFitWidth(ICON_SIZE);
            iv.setFitHeight(ICON_SIZE);
            iv.getStyleClass().add("tag");
            iv.getStyleClass().add(task.getStatus().getName());
            iconBox.getChildren().add(iv);
            int tagCount = iconBox.getChildren().size();
            
            extraWidth = Math.max(extraWidth, 
                    (tagCount) * (ICON_SIZE+TAG_BOX_H_GAP) 
//                    - COLUMN_H_GAP
                    - (level-1)*(COLUMN_WIDTH + COLUMN_H_GAP));
        }
    }

    class TextFlowRenderer extends AbstractVisitor {
        
        final Pattern P_LEADING_SPACE = Pattern.compile("^\\s+");
     
        private final TextFlow textFlow;
        private final EntryText et;
        private final Iterator<Task> tasks;
        private final LinkedHashSet<String> styles = new LinkedHashSet<>();
        private int skipTags;
        private int skipTasks;
        private Iterator<Tag> tags;
        private boolean beforeText = true;
        private boolean afterTag = false;
        private boolean lastTagWasHidden = false;
        

        public TextFlowRenderer(TextFlow textFlow, EntryText et) {
            this.textFlow = textFlow;
            this.et = et;
            tasks = et.getTasks().iterator();
            Task t = tasks.next();
            tags = t.getTags().iterator();
            skipTags = t.getPrefixTags().size();
            skipTasks = t.hasToken() ? 1 : 0;
            styles.add(t.getType().getName());
            styles.add(t.getStatus().getName());
//            textFlow.setTranslateY(-5);
        }

        @Override
        public void visit(Emphasis emphasis) {
            if (styles.add("emph")) {
                super.visit(emphasis);
                styles.remove("emph");
            } else {
                super.visit(emphasis);
            }
        }

        @Override
        public void visit(StrongEmphasis emphasis) {
            if (styles.add("strong")) {
                super.visit(emphasis);
                styles.remove("strong");
            } else {
                super.visit(emphasis);
            }
        }

        @Override
        public void visit(Text text) {
            String str = text.getLiteral();
            if (beforeText || (lastTagWasHidden && !afterTag)) {
                String trimmed = str.trim();
                if (trimmed.isEmpty()) return;
                beforeText = afterTag = false;
                if (trimmed.equals(".")) return;
                str = P_LEADING_SPACE.matcher(str).replaceAll("");
            }
            if (afterTag) {
                if (str.trim().isEmpty()) return;
                afterTag = false;
                Matcher m = P_LEADING_SPACE.matcher(str);
                if (m.find()) {
                    str = m.replaceAll("\u00a0"); //non-breaking space
                } else {
                    str = "\ufeff" + str; //non-breaking zero-width space
                }
            }
            javafx.scene.text.Text t = new javafx.scene.text.Text(str);
            t.getStyleClass().add("text");
            t.getStyleClass().addAll(styles);
            textFlow.getChildren().add(t);
            super.visit(text);
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            visit(new Text(" "));
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            visit(new Text("\n"));
        }
        
        private void insertTagImage(String name) {
            Image img = IMAGE_RESOLVER.getImage(name);
            ImageView iv = new ImageView(img);
            iv.getStyleClass().add("tag");
            iv.getStyleClass().addAll(styles);
            iv.setFitWidth(ICON_SIZE);
            iv.setFitHeight(TEXT_SIZE);
            iv.setScaleY(ICON_SIZE/TEXT_SIZE);
            iv.setTranslateY((ICON_SIZE-TEXT_SIZE)/4.0);
            textFlow.getChildren().add(iv);
            afterTag = true;
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof Token.Task) {
                if (skipTasks > 0) {
                    skipTasks--;
                    return;
                }
                Task t = tasks.next();
                tags = t.getTags().iterator();
                styles.clear();
                styles.add(t.getType().getName());
                styles.add(t.getStatus().getName());
                insertTagImage(t.getIconName());
            } else if (customNode instanceof Token.Tag) {
                if (skipTags > 0) {
                    tags.next();
                    skipTags--;
                    return;
                }
                Tag t = tags.next();
                lastTagWasHidden = t.isHidden();
                if (!t.isHidden()) {
                    insertTagImage(t.getIconName());
                }
            } else if (customNode instanceof Token.Temporal) {
                Token.Temporal tmp = (Token.Temporal) customNode;
                javafx.scene.text.Text t = new javafx.scene.text.Text();
                t.setText(tmp.getValue());
                t.getStyleClass().add("text");
//                t.getStyleClass().add("temporal");
                t.getStyleClass().addAll(styles);
                TextFlow tf = new TextFlow(t);
                tf.getStyleClass().add("temporal");
                textFlow.getChildren().add(tf);
            }
            super.visit(customNode);
        }
    }
    
    private static final double TAG_BOX_H_GAP = 3;
    private static final double ICON_SIZE = 18;
    private static final double TEXT_SIZE = 12;
    private static final double COLUMN_H_GAP = 3;
    private static final double COLUMN_WIDTH = 18;
    static final ImageResolver IMAGE_RESOLVER = new ImageResolver(32);
}
