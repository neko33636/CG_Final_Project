package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.removers.PolygonRemover;
import com.cgvsu.removers.VertexRemover;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class GuiController {

    @FXML private AnchorPane anchorPane;
    @FXML private Canvas canvas;
    @FXML private TextField txField, tyField, tzField;
    @FXML private TextField rxField, ryField, rzField;
    @FXML private TextField sxField, syField, szField;
    @FXML private Button resetTransformButton, resetAllTransformsButton, saveModelButton;
    @FXML private TextField vertexIndicesField, polygonIndicesField;
    @FXML private Button deleteVertexBtn, deletePolygonBtn;
    @FXML private CheckBox deleteFreeVerticesCheckBox;
    @FXML private VBox modelListVBox;

    private final ArrayList<Model> models = new ArrayList<>();
    private final ArrayList<CheckBox> modelVisibilityCheckboxes = new ArrayList<>();
    private final ToggleGroup activeModelGroup = new ToggleGroup();
    private int activeModelIndex = -1;
    private final Camera camera = new Camera(new Vector3f(0,0,100), new Vector3f(0,0,0), 1,1,0.01f,1000);
    private boolean isUpdatingFields = false;

    public void setScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case UP -> camera.movePosition(new Vector3f(0, 0, -2.5f));
                case DOWN -> camera.movePosition(new Vector3f(0, 0, 2.5f));
                case LEFT -> camera.movePosition(new Vector3f(2.5f, 0, 0));
                case RIGHT -> camera.movePosition(new Vector3f(-2.5f, 0, 0));
                case W -> camera.movePosition(new Vector3f(0, 2.5f, 0));
                case S -> camera.movePosition(new Vector3f(0, -2.5f, 0));
                default -> { return; }
            }
            canvas.requestFocus();
            e.consume();
        });
    }

    @FXML
    private void initialize() {
        canvas.setFocusTraversable(true);
        anchorPane.widthProperty().addListener((a,b,c)->canvas.setWidth(c.doubleValue()-260));
        anchorPane.heightProperty().addListener((a,b,c)->canvas.setHeight(c.doubleValue()-30));

        setTransformControls(false);
        txField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float value = Float.parseFloat(newValue);
                    getActiveModel().getTransform().getTranslation().setX(value);
                } catch (NumberFormatException ignored) {}
            }
        });
        tyField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float value = Float.parseFloat(newValue);
                    getActiveModel().getTransform().getTranslation().setY(value);
                } catch (NumberFormatException ignored) {}
            }
        });
        tzField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float value = Float.parseFloat(newValue);
                    getActiveModel().getTransform().getTranslation().setZ(value);
                } catch (NumberFormatException ignored) {}
            }
        });

        rxField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float degrees = Float.parseFloat(newValue);
                    float radians = (float) Math.toRadians(degrees);
                    getActiveModel().getTransform().getRotation().setX(radians);
                } catch (NumberFormatException ignored) {}
            }
        });
        ryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float degrees = Float.parseFloat(newValue);
                    float radians = (float) Math.toRadians(degrees);
                    getActiveModel().getTransform().getRotation().setY(radians);
                } catch (NumberFormatException ignored) {}
            }
        });
        rzField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float degrees = Float.parseFloat(newValue);
                    float radians = (float) Math.toRadians(degrees);
                    getActiveModel().getTransform().getRotation().setZ(radians);
                } catch (NumberFormatException ignored) {}
            }
        });

        sxField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float value = Float.parseFloat(newValue);
                    getActiveModel().getTransform().getScale().setX(value);
                } catch (NumberFormatException ignored) {}
            }
        });
        syField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float value = Float.parseFloat(newValue);
                    getActiveModel().getTransform().getScale().setY(value);
                } catch (NumberFormatException ignored) {}
            }
        });
        szField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFields && getActiveModel() != null && !newValue.isEmpty()) {
                try {
                    float value = Float.parseFloat(newValue);
                    getActiveModel().getTransform().getScale().setZ(value);
                } catch (NumberFormatException ignored) {}
            }
        });
        Timeline t = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            var g = canvas.getGraphicsContext2D();
            g.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
            camera.setAspectRatio((float)(canvas.getWidth()/canvas.getHeight()));
            for (int i = 0; i < models.size(); i++) {
                if(modelVisibilityCheckboxes.get(i).isSelected())
                    RenderEngine.render(g,camera,models.get(i),(int)canvas.getWidth(),(int)canvas.getHeight());
            }
        }));
        t.setCycleCount(Animation.INDEFINITE);
        t.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ", "*.obj"));
        File f = fc.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (f == null) return;

        try {
            Model model = ObjReader.read(Files.readString(f.toPath()));
            models.add(model);
            int index = models.size()-1;

            CheckBox cb = new CheckBox("Model "+models.size());
            cb.setSelected(true);
            modelVisibilityCheckboxes.add(cb);

            RadioButton rb = new RadioButton();
            rb.setToggleGroup(activeModelGroup);
            rb.setSelected(true);
            rb.setOnAction(e -> {
                activeModelIndex = index;
                loadActiveModelToFields();
            });

            modelListVBox.getChildren().add(new HBox(5, rb, cb));
            activeModelIndex = index;
            loadActiveModelToFields();
            setTransformControls(true);
            canvas.requestFocus();

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Model load error");
            alert.setHeaderText("Failed to load model");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void loadActiveModelToFields() {
        Model m = getActiveModel();
        if (m == null) return;

        isUpdatingFields = true;
        try {
            txField.setText(String.valueOf(m.getTransform().getTranslation().getX()));
            tyField.setText(String.valueOf(m.getTransform().getTranslation().getY()));
            tzField.setText(String.valueOf(m.getTransform().getTranslation().getZ()));

            rxField.setText(String.valueOf(Math.toDegrees(m.getTransform().getRotation().getX())));
            ryField.setText(String.valueOf(Math.toDegrees(m.getTransform().getRotation().getY())));
            rzField.setText(String.valueOf(Math.toDegrees(m.getTransform().getRotation().getZ())));

            sxField.setText(String.valueOf(m.getTransform().getScale().getX()));
            syField.setText(String.valueOf(m.getTransform().getScale().getY()));
            szField.setText(String.valueOf(m.getTransform().getScale().getZ()));
        } finally {
            isUpdatingFields = false;
        }
    }

    private Model getActiveModel() {
        return (activeModelIndex>=0 && activeModelIndex<models.size()) ? models.get(activeModelIndex) : null;
    }

    private void setTransformControls(boolean v) {
        Arrays.asList(
                resetTransformButton, resetAllTransformsButton,
                deleteVertexBtn, deletePolygonBtn, saveModelButton
        ).forEach(b -> b.setDisable(!v));
    }

    @FXML private void onResetTransformButtonClick() {
        Model m = getActiveModel();
        if(m==null) return;
        m.getTransform().getTranslation().setX(0);
        m.getTransform().getTranslation().setY(0);
        m.getTransform().getTranslation().setZ(0);
        m.getTransform().getRotation().setX(0);
        m.getTransform().getRotation().setY(0);
        m.getTransform().getRotation().setZ(0);
        m.getTransform().getScale().setX(1);
        m.getTransform().getScale().setY(1);
        m.getTransform().getScale().setZ(1);
        loadActiveModelToFields();
    }

    @FXML private void onResetAllTransformsButtonClick() {
        for(Model m: models) {
            m.getTransform().getTranslation().setX(0);
            m.getTransform().getTranslation().setY(0);
            m.getTransform().getTranslation().setZ(0);
            m.getTransform().getRotation().setX(0);
            m.getTransform().getRotation().setY(0);
            m.getTransform().getRotation().setZ(0);
            m.getTransform().getScale().setX(1);
            m.getTransform().getScale().setY(1);
            m.getTransform().getScale().setZ(1);
        }
        loadActiveModelToFields();
    }

    @FXML private void onDeleteVertices() {
        Model m = getActiveModel();
        if(m==null) return;
        VertexRemover.deleteVertices(m, parse(vertexIndicesField.getText()), true);
        vertexIndicesField.clear();
    }

    @FXML private void onDeletePolygons() {
        Model m = getActiveModel();
        if(m==null) return;
        PolygonRemover.deletePolygons(m, parse(polygonIndicesField.getText()), deleteFreeVerticesCheckBox.isSelected());
        polygonIndicesField.clear();
    }

    @FXML private void onSaveModel() throws Exception {
        Model m = getActiveModel();
        if(m==null) return;
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ","*.obj"));
        File f = fc.showSaveDialog((Stage) canvas.getScene().getWindow());
        if(f!=null) ObjWriter.saveModel(m, f.getAbsolutePath());
    }

    private Set<Integer> parse(String s) {
        Set<Integer> r = new HashSet<>();
        if(s==null || s.isBlank()) return r;
        for(String p: s.split(",")) r.add(Integer.parseInt(p.trim()));
        return r;
    }
}