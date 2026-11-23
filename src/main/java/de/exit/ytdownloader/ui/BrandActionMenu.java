package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.util.LucideIcon;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import java.util.HashMap;
import java.util.Map;

/**
 * Modernes Action-Dropdown-Menü für den Brand-Button von ytdownloader (JDMX Studio Dark Style).
 */
public class BrandActionMenu extends Popup {

    private final Map<String, Button> items = new HashMap<>();
    private final Map<String, Runnable> actions = new HashMap<>();

    public BrandActionMenu() {
        setAutoHide(true);
        setHideOnEscape(true);

        VBox root = new VBox(2);
        root.getStyleClass().add("action-menu-popup");
        root.setMinWidth(160);
        root.setPrefWidth(165);

        // Menüeintrag: Updates
        Button btnUpdates = createMenuItem("updates", "Updates", LucideIcon.ARROW_UP_CIRCLE, Color.web("#38BDF8"));

        root.getChildren().add(btnUpdates);
        getContent().add(root);
    }

    private Button createMenuItem(String key, String title, String iconPath, Color iconColor) {
        Button btn = new Button(title);
        btn.getStyleClass().add("action-menu-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setCursor(Cursor.HAND);
        btn.setContentDisplay(ContentDisplay.LEFT);
        btn.setGraphicTextGap(10);

        btn.setGraphic(LucideIcon.createIcon(iconPath, iconColor, 0.85));

        btn.setOnAction(e -> {
            hide();
            Runnable action = actions.get(key);
            if (action != null) {
                action.run();
            }
        });

        items.put(key, btn);
        return btn;
    }

    public void setAction(String key, Runnable action) {
        actions.put(key, action);
    }

    public void showBelow(Node node) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {
            return;
        }
        javafx.geometry.Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        if (bounds != null) {
            show(node.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 4);
        }
    }
}
