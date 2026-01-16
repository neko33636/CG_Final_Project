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
    @FXML private TextField txField, tyField, tzField, rxField, ryField, rzField, sxField, syField, szField;
    @FXML private Button translateXMinusBtn, translateXPlusBtn, translateYMinusBtn, translateYPlusBtn, translateZMinusBtn, translateZPlusBtn;
    @FXML private Button rotateXMinusBtn, rotateXPlusBtn, rotateYMinusBtn, rotateYPlusBtn, rotateZMinusBtn, rotateZPlusBtn;
    @FXML private Button scaleXMinusBtn, scaleXPlusBtn, scaleYMinusBtn, scaleYPlusBtn, scaleZMinusBtn, scaleZPlusBtn;
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
    private final float STEP = 0.5f;
    private final float CAMERA_STEP = 2.5f;

    public void setScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case UP -> camera.movePosition(new Vector3f(0, 0, -CAMERA_STEP));
                case DOWN -> camera.movePosition(new Vector3f(0, 0, CAMERA_STEP));
                case LEFT -> camera.movePosition(new Vector3f(CAMERA_STEP, 0, 0));
                case RIGHT -> camera.movePosition(new Vector3f(-CAMERA_STEP, 0, 0));
                case W -> camera.movePosition(new Vector3f(0, CAMERA_STEP, 0));
                case S -> camera.movePosition(new Vector3f(0, -CAMERA_STEP, 0));
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

        Arrays.asList(txField, tyField, tzField, rxField, ryField, rzField, sxField, syField, szField)
                .forEach(f -> f.textProperty().addListener((o, ov, nv) -> applyFromFields()));

        setTransformControls(false);

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
                loadActiveModelTransform();
            });

            modelListVBox.getChildren().add(new HBox(5, rb, cb));
            activeModelIndex = index;
            loadActiveModelTransform();
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

    private void applyFromFields() {
        Model m = getActiveModel();
        if (m == null) return;
        try {
            m.getTransform().setTranslation(new Vector3f(Float.parseFloat(txField.getText()),
                    Float.parseFloat(tyField.getText()),
                    Float.parseFloat(tzField.getText())));
            m.getTransform().setRotation(new Vector3f(Float.parseFloat(rxField.getText()),
                    Float.parseFloat(ryField.getText()),
                    Float.parseFloat(rzField.getText())));
            m.getTransform().setScale(new Vector3f(Float.parseFloat(sxField.getText()),
                    Float.parseFloat(syField.getText()),
                    Float.parseFloat(szField.getText())));
        } catch(NumberFormatException ignored){}
    }

    private Model getActiveModel() {
        return (activeModelIndex>=0 && activeModelIndex<models.size()) ? models.get(activeModelIndex) : null;
    }

    private void loadActiveModelTransform() {
        Model m = getActiveModel();
        if(m==null) return;
        txField.setText(String.valueOf(m.getTransform().getTranslation().getX()));
        tyField.setText(String.valueOf(m.getTransform().getTranslation().getY()));
        tzField.setText(String.valueOf(m.getTransform().getTranslation().getZ()));
        rxField.setText(String.valueOf(m.getTransform().getRotation().getX()));
        ryField.setText(String.valueOf(m.getTransform().getRotation().getY()));
        rzField.setText(String.valueOf(m.getTransform().getRotation().getZ()));
        sxField.setText(String.valueOf(m.getTransform().getScale().getX()));
        syField.setText(String.valueOf(m.getTransform().getScale().getY()));
        szField.setText(String.valueOf(m.getTransform().getScale().getZ()));
    }

    private void setTransformControls(boolean v) {
        Arrays.asList(
                translateXMinusBtn, translateXPlusBtn, translateYMinusBtn, translateYPlusBtn,
                translateZMinusBtn, translateZPlusBtn,
                rotateXMinusBtn, rotateXPlusBtn, rotateYMinusBtn, rotateYPlusBtn,
                rotateZMinusBtn, rotateZPlusBtn,
                scaleXMinusBtn, scaleXPlusBtn, scaleYMinusBtn, scaleYPlusBtn,
                scaleZMinusBtn, scaleZPlusBtn,
                resetTransformButton, resetAllTransformsButton,
                deleteVertexBtn, deletePolygonBtn, saveModelButton
        ).forEach(b -> b.setDisable(!v));
    }

    @FXML private void onTranslateXMinus() { modifyActive("tx",-STEP); }
    @FXML private void onTranslateXPlus() { modifyActive("tx",STEP); }
    @FXML private void onTranslateYMinus() { modifyActive("ty",-STEP); }
    @FXML private void onTranslateYPlus() { modifyActive("ty",STEP); }
    @FXML private void onTranslateZMinus() { modifyActive("tz",-STEP); }
    @FXML private void onTranslateZPlus() { modifyActive("tz",STEP); }
    @FXML private void onRotateXMinus() { modifyActive("rx",-STEP); }
    @FXML private void onRotateXPlus() { modifyActive("rx",STEP); }
    @FXML private void onRotateYMinus() { modifyActive("ry",-STEP); }
    @FXML private void onRotateYPlus() { modifyActive("ry",STEP); }
    @FXML private void onRotateZMinus() { modifyActive("rz",-STEP); }
    @FXML private void onRotateZPlus() { modifyActive("rz",STEP); }
    @FXML private void onScaleXMinus() { modifyActive("sx",-STEP); }
    @FXML private void onScaleXPlus() { modifyActive("sx",STEP); }
    @FXML private void onScaleYMinus() { modifyActive("sy",-STEP); }
    @FXML private void onScaleYPlus() { modifyActive("sy",STEP); }
    @FXML private void onScaleZMinus() { modifyActive("sz",-STEP); }
    @FXML private void onScaleZPlus() { modifyActive("sz",STEP); }

    private void modifyActive(String type, float delta) {
        Model m = getActiveModel();
        if(m==null) return;
        Vector3f t = m.getTransform().getTranslation();
        Vector3f r = m.getTransform().getRotation();
        Vector3f s = m.getTransform().getScale();
        switch(type) {
            case "tx" -> t.setX(t.getX()+delta);
            case "ty" -> t.setY(t.getY()+delta);
            case "tz" -> t.setZ(t.getZ()+delta);
            case "rx" -> r.setX(r.getX()+delta);
            case "ry" -> r.setY(r.getY()+delta);
            case "rz" -> r.setZ(r.getZ()+delta);
            case "sx" -> s.setX(Math.max(0.1f,s.getX()+delta));
            case "sy" -> s.setY(Math.max(0.1f,s.getY()+delta));
            case "sz" -> s.setZ(Math.max(0.1f,s.getZ()+delta));
        }
        loadActiveModelTransform();
    }

    @FXML private void onResetTransformButtonClick() {
        Model m = getActiveModel();
        if(m==null) return;
        m.getTransform().setTranslation(new Vector3f(0,0,0));
        m.getTransform().setRotation(new Vector3f(0,0,0));
        m.getTransform().setScale(new Vector3f(1,1,1));
        loadActiveModelTransform();
    }

    @FXML private void onResetAllTransformsButtonClick() {
        for(Model m: models) {
            m.getTransform().setTranslation(new Vector3f(0,0,0));
            m.getTransform().setRotation(new Vector3f(0,0,0));
            m.getTransform().setScale(new Vector3f(1,1,1));
        }
        loadActiveModelTransform();
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
