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
    @FXML private TextField txField,tyField,tzField,rxField,ryField,rzField,sxField,syField,szField;
    @FXML private Button translateXMinusBtn,translateXPlusBtn,translateYMinusBtn,translateYPlusBtn,translateZMinusBtn,translateZPlusBtn;
    @FXML private Button rotateXMinusBtn,rotateXPlusBtn,rotateYMinusBtn,rotateYPlusBtn,rotateZMinusBtn,rotateZPlusBtn;
    @FXML private Button scaleXMinusBtn,scaleXPlusBtn,scaleYMinusBtn,scaleYPlusBtn,scaleZMinusBtn,scaleZPlusBtn;
    @FXML private Button resetTransformButton,resetAllTransformsButton,saveModelButton;
    @FXML private TextField vertexIndicesField,polygonIndicesField;
    @FXML private Button deleteVertexBtn,deletePolygonBtn;
    @FXML private CheckBox deleteFreeVerticesCheckBox;
    @FXML private VBox modelListVBox;

    private final ArrayList<Model> models = new ArrayList<>();
    private final ArrayList<CheckBox> modelVisibilityCheckboxes = new ArrayList<>();
    private final ArrayList<RadioButton> modelActiveRadios = new ArrayList<>();
    private ToggleGroup activeModelGroup = new ToggleGroup();
    private int activeModelIndex = -1;
    private final Camera camera = new Camera(new Vector3f(0,0,100),new Vector3f(0,0,0),1,1,0.01f,1000);
    private final float STEP=0.5f,CAMERA_STEP=2.5f;

    public void setScene(Scene scene){
        scene.addEventFilter(KeyEvent.KEY_PRESSED,e->{
            switch(e.getCode()){
                case UP -> camera.movePosition(new Vector3f(0,0,-CAMERA_STEP));
                case DOWN -> camera.movePosition(new Vector3f(0,0,CAMERA_STEP));
                case LEFT -> camera.movePosition(new Vector3f(CAMERA_STEP,0,0));
                case RIGHT -> camera.movePosition(new Vector3f(-CAMERA_STEP,0,0));
                case W -> camera.movePosition(new Vector3f(0,CAMERA_STEP,0));
                case S -> camera.movePosition(new Vector3f(0,-CAMERA_STEP,0));
                default -> {return;}
            }
            canvas.requestFocus();
            e.consume();
        });
    }

    @FXML
    private void initialize(){
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        anchorPane.widthProperty().addListener((a,b,c)->canvas.setWidth(c.doubleValue()-260));
        anchorPane.heightProperty().addListener((a,b,c)->canvas.setHeight(c.doubleValue()-30));

        Arrays.asList(txField,tyField,tzField,rxField,ryField,rzField,sxField,syField,szField)
                .forEach(f->{f.setFocusTraversable(false);f.textProperty().addListener((o,ov,nv)->applyFromFields());});

        setTransformControls(false);

        Timeline t = new Timeline(new KeyFrame(Duration.millis(16),e->{
            var g = canvas.getGraphicsContext2D();
            g.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
            camera.setAspectRatio((float)(canvas.getWidth()/canvas.getHeight()));
            for(int i=0;i<models.size();i++){
                if(modelVisibilityCheckboxes.get(i).isSelected())
                    RenderEngine.render(g,camera,models.get(i),(int)canvas.getWidth(),(int)canvas.getHeight());
            }
        }));
        t.setCycleCount(Animation.INDEFINITE);
        t.play();
    }

    private void applyFromFields(){
        Model active = getActiveModel();
        if(active==null) return;
        try{
            active.getTransform().setTranslation(new Vector3f(Float.parseFloat(txField.getText()),Float.parseFloat(tyField.getText()),Float.parseFloat(tzField.getText())));
            active.getTransform().setRotation(new Vector3f(Float.parseFloat(rxField.getText()),Float.parseFloat(ryField.getText()),Float.parseFloat(rzField.getText())));
            active.getTransform().setScale(new Vector3f(Float.parseFloat(sxField.getText()),Float.parseFloat(syField.getText()),Float.parseFloat(szField.getText())));
        }catch(NumberFormatException ignored){}
    }

    private void loadActiveModelTransform(){
        Model active = getActiveModel();
        if(active==null) return;
        Vector3f t=active.getTransform().getTranslation();
        Vector3f r=active.getTransform().getRotation();
        Vector3f s=active.getTransform().getScale();
        txField.setText(String.valueOf(t.getX()));
        tyField.setText(String.valueOf(t.getY()));
        tzField.setText(String.valueOf(t.getZ()));
        rxField.setText(String.valueOf(r.getX()));
        ryField.setText(String.valueOf(r.getY()));
        rzField.setText(String.valueOf(r.getZ()));
        sxField.setText(String.valueOf(s.getX()));
        syField.setText(String.valueOf(s.getY()));
        szField.setText(String.valueOf(s.getZ()));
    }

    private void setTransformControls(boolean v){
        Arrays.asList(translateXMinusBtn,translateXPlusBtn,translateYMinusBtn,translateYPlusBtn,translateZMinusBtn,translateZPlusBtn,
                        rotateXMinusBtn,rotateXPlusBtn,rotateYMinusBtn,rotateYPlusBtn,rotateZMinusBtn,rotateZPlusBtn,
                        scaleXMinusBtn,scaleXPlusBtn,scaleYMinusBtn,scaleYPlusBtn,scaleZMinusBtn,scaleZPlusBtn,
                        resetTransformButton,resetAllTransformsButton,deleteVertexBtn,deletePolygonBtn,saveModelButton)
                .forEach(b->b.setDisable(!v));
    }

    private Model getActiveModel(){
        if(activeModelIndex>=0 && activeModelIndex<models.size()) return models.get(activeModelIndex);
        return null;
    }

    @FXML private void onOpenModelMenuItemClick() throws Exception {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ","*.obj"));
        File f = fc.showOpenDialog((Stage) canvas.getScene().getWindow());
        if(f==null) return;
        Model model = ObjReader.read(Files.readString(f.toPath()));
        models.add(model);
        int index = models.size()-1;

        CheckBox cb = new CheckBox("Model "+models.size());
        cb.setSelected(true);
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(activeModelGroup);
        rb.setSelected(true);
        activeModelIndex = index;
        rb.setOnAction(e->{activeModelIndex=index;loadActiveModelTransform();});

        modelVisibilityCheckboxes.add(cb);
        modelActiveRadios.add(rb);
        HBox hbox = new HBox(5); hbox.getChildren().addAll(rb,cb);
        modelListVBox.getChildren().add(hbox);

        loadActiveModelTransform();
        setTransformControls(true);
        canvas.requestFocus();
    }

    @FXML private void onResetTransformButtonClick(){
        Model active = getActiveModel();
        if(active==null) return;
        active.getTransform().setTranslation(new Vector3f(0,0,0));
        active.getTransform().setRotation(new Vector3f(0,0,0));
        active.getTransform().setScale(new Vector3f(1,1,1));
        loadActiveModelTransform();
    }

    @FXML private void onResetAllTransformsButtonClick(){
        for(Model m:models){
            m.getTransform().setTranslation(new Vector3f(0,0,0));
            m.getTransform().setRotation(new Vector3f(0,0,0));
            m.getTransform().setScale(new Vector3f(1,1,1));
        }
        loadActiveModelTransform();
    }

    @FXML private void onTranslateXMinus(){modifyActiveTransform("tx",-STEP);}
    @FXML private void onTranslateXPlus(){modifyActiveTransform("tx",STEP);}
    @FXML private void onTranslateYMinus(){modifyActiveTransform("ty",-STEP);}
    @FXML private void onTranslateYPlus(){modifyActiveTransform("ty",STEP);}
    @FXML private void onTranslateZMinus(){modifyActiveTransform("tz",-STEP);}
    @FXML private void onTranslateZPlus(){modifyActiveTransform("tz",STEP);}
    @FXML private void onRotateXMinus(){modifyActiveTransform("rx",-STEP);}
    @FXML private void onRotateXPlus(){modifyActiveTransform("rx",STEP);}
    @FXML private void onRotateYMinus(){modifyActiveTransform("ry",-STEP);}
    @FXML private void onRotateYPlus(){modifyActiveTransform("ry",STEP);}
    @FXML private void onRotateZMinus(){modifyActiveTransform("rz",-STEP);}
    @FXML private void onRotateZPlus(){modifyActiveTransform("rz",STEP);}
    @FXML private void onScaleXMinus(){modifyActiveTransform("sx",-STEP);}
    @FXML private void onScaleXPlus(){modifyActiveTransform("sx",STEP);}
    @FXML private void onScaleYMinus(){modifyActiveTransform("sy",-STEP);}
    @FXML private void onScaleYPlus(){modifyActiveTransform("sy",STEP);}
    @FXML private void onScaleZMinus(){modifyActiveTransform("sz",-STEP);}
    @FXML private void onScaleZPlus(){modifyActiveTransform("sz",STEP);}

    private void modifyActiveTransform(String type,float delta){
        Model active=getActiveModel();
        if(active==null) return;
        Vector3f t=active.getTransform().getTranslation();
        Vector3f r=active.getTransform().getRotation();
        Vector3f s=active.getTransform().getScale();
        switch(type){
            case "tx"->t.setX(t.getX()+delta);
            case "ty"->t.setY(t.getY()+delta);
            case "tz"->t.setZ(t.getZ()+delta);
            case "rx"->r.setX(r.getX()+delta);
            case "ry"->r.setY(r.getY()+delta);
            case "rz"->r.setZ(r.getZ()+delta);
            case "sx"->s.setX(Math.max(0.1f,s.getX()+delta));
            case "sy"->s.setY(Math.max(0.1f,s.getY()+delta));
            case "sz"->s.setZ(Math.max(0.1f,s.getZ()+delta));
        }
        loadActiveModelTransform();
    }

    @FXML private void onDeleteVertices(){
        Model active=getActiveModel();
        if(active==null) return;
        VertexRemover.deleteVertices(active,parse(vertexIndicesField.getText()),true);
        vertexIndicesField.clear();
    }

    @FXML private void onDeletePolygons(){
        Model active=getActiveModel();
        if(active==null) return;
        PolygonRemover.deletePolygons(active,parse(polygonIndicesField.getText()),deleteFreeVerticesCheckBox.isSelected());
        polygonIndicesField.clear();
    }

    private Set<Integer> parse(String s){
        Set<Integer> r = new HashSet<>();
        if(s==null||s.isBlank()) return r;
        for(String p:s.split(",")) r.add(Integer.parseInt(p.trim()));
        return r;
    }

    @FXML private void onSaveModel() throws Exception {
        Model active=getActiveModel();
        if(active==null) return;
        FileChooser fc=new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ","*.obj"));
        File f = fc.showSaveDialog((Stage) canvas.getScene().getWindow());
        if(f==null) return;
        ObjWriter.saveModel(active,f.getAbsolutePath());
    }
}
