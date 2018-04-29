//==============================================================================
//   JFXMapPane is a Java library for parsing raw weather data
//   Copyright (C) 2012 Jeffrey L Smith
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//    
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//    
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//    
//  For more information, please email jsmith.carlsbad@gmail.com
//    
//==============================================================================
package com.gui.core.mapViewer;

import java.awt.Point;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gui.core.mapViewerObjects.MapMarkerCircle;
import javafx.scene.image.Image;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gui.core.mapTree.CheckBoxViewTree;
import com.gui.core.mapTreeObjects.Layer;
import com.gui.core.mapTreeObjects.LayerGroup;
import com.gui.core.mapViewerObjects.MapMarkerDot;
import com.gui.is.events.GuiEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javax.annotation.Resource;
import javax.swing.text.html.ImageView;

/**
 *
 * @author taljmars
 */
@ComponentScan("com.gui.core.mapTree")
@Component
public class LayeredViewMap extends ViewMap
{

    private CheckBoxViewTree checkBoxViewTree;

    protected Button btnEditSaveMode;
    protected Button btnEditCancelMode;

    private LayerGroup root;

    private Layer modifiedLayer;

    private VBox editorVbox;

    private AtomicBoolean isMapLayerEditing;

    public LayeredViewMap() {
        super();
        initLayerEditor();

        System.out.println("In LayedViewMap Constructor");
    }

    @Resource(type = CheckBoxViewTree.class)
    public void setCheckBoxViewTree(CheckBoxViewTree checkBoxViewTree) {
            this.checkBoxViewTree = checkBoxViewTree;
        }

    public CheckBoxViewTree getCheckBoxViewTree() {
        return checkBoxViewTree;
    }

    private void initLayerEditor() {
        isMapLayerEditing = new AtomicBoolean(false);
        btnEditSaveMode = new Button("Save");
        btnEditCancelMode = new Button("Cancel");

        btnEditSaveMode.setOnAction( e -> LayerEditorSave());
        btnEditCancelMode.setOnAction( e -> LayerEditorCancel());
        
        editorVbox = new VBox();
        editorVbox.setPadding(new Insets(5));
        editorVbox.getChildren().add(btnEditSaveMode);
        editorVbox.getChildren().add(btnEditCancelMode);
        editorVbox.setLayoutX(50);
        editorVbox.setLayoutY(10);
        
        editorVbox.setVisible(false);
        
        getChildren().add(editorVbox);
    }
    
    public void setRootLayer(LayerGroup rootLayer) {
        setRoot(rootLayer);
    }    

    protected void startModifiedLayerMode(Layer modifiedLayer) {
        this.modifiedLayer = modifiedLayer;
        editorVbox.setVisible(true);
    }
    
    protected Layer finishModifiedLayerMode() {
        editorVbox.setVisible(false);
        Layer modifiedLayer = this.modifiedLayer;
        this.modifiedLayer = null;
        return modifiedLayer;
    }
    
    public void addLayer(Layer layer, LayerGroup parentGroup) {
        layer.setParent(parentGroup);
        parentGroup.addChildren(layer);
    }
    
    public void removeLayer(Layer layer) {
        removeLayersRecursive(layer);
        
        if (layer.getParent() != null) {
            LayerGroup lg = (LayerGroup) layer.getParent();
            lg.removeChildren(layer);
        }
    }
    
    private void removeLayersRecursive(Layer layer) {
        if (layer instanceof LayerGroup) {
            LayerGroup lg = (LayerGroup) layer;
            Iterator<Layer> it = lg.getChildens().iterator();
            while (it.hasNext()) {
                removeLayersRecursive(it.next());
                it.remove();
            }
        }
        else
            layer.removeAllMapObjects();
    }
    
    public void setLayerVisibie(Layer layer, boolean show) {
        if (show) {
            System.err.println("Show show all elements of " + layer);
            layer.regenerateMapObjects();
        }
        else {
            System.err.println("Show hide all elements of " + layer);
            layer.removeAllMapObjects();
        }
    }
    
    public void hideLayer(Layer layer) {
        if (layer instanceof LayerGroup) {
            LayerGroup lg = (LayerGroup) layer;
            Iterator<Layer> it = lg.getChildens().iterator();
            while (it.hasNext()) {
                hideLayer(it.next());
            }
        }
        else
            layer.removeAllMapObjects();
    }
    
    public void showLayer(Layer layer) {
        if (layer instanceof LayerGroup) {
            LayerGroup lg = (LayerGroup) layer;
            Iterator<Layer> it = lg.getChildens().iterator();
            while (it.hasNext()) {
                showLayer(it.next());
            }
        }
        else
            layer.regenerateMapObjects();
    }
    
    private ContextMenu popup;
    
    private ContextMenu buildPopup(Point point) {
        ContextMenu popup = new ContextMenu();        
        
        MenuItem menuItemAddMarker = new MenuItem("Add Marker");
        popup.getItems().add(menuItemAddMarker);

        MenuItem menuItemAddMarkerImage = new MenuItem("Add Marker with Image");
        popup.getItems().add(menuItemAddMarkerImage);

        MenuItem menuItemAddCircle = new MenuItem("Add Circle");
        popup.getItems().add(menuItemAddCircle);

        Image img = new Image(this.getClass().getResource("/com/mapImages/droneConnected.png").toString());
        javafx.scene.image.ImageView iview = new javafx.scene.image.ImageView(img);
        iview.setFitHeight(40);
        iview.setFitWidth(40);

        menuItemAddMarker.setOnAction( arg -> this.addMapMarker(new MapMarkerDot("12", getPosition(point))));
        menuItemAddMarkerImage.setOnAction( arg -> this.addMapMarker(new MapMarkerDot(iview, 45.0, getPosition(point))));
        menuItemAddCircle.setOnAction( arg -> this.addMapMarker(new MapMarkerCircle("12", getPosition(point), 50000)));

        return popup;
    }
    
    @Override
    protected void HandleMouseClick(MouseEvent me) {
        if (popup != null)
            popup.hide();
        
        if (!me.isPopupTrigger())
            return;
        
        Point point = new Point((int) me.getX(), (int) me.getY());
        popup = buildPopup(point);
        popup.show(this, me.getScreenX(), me.getScreenY());
    }
    
    @SuppressWarnings("unused")
    protected void EditModeOff() {
        isMapLayerEditing.set(false);
        System.out.println("Edit mode is off");
    }

    protected void EditModeOn() {
        isMapLayerEditing.set(true);
        System.out.println("Edit mode is on");
    }

    public boolean isEditingLayer() {
        return isMapLayerEditing.get();
    }
    
    @EventListener
    public void onApplicationEvent(GuiEvent command) {
        switch (command.getCommand()) {
        case EDITMODE_EXISTING_LAYER_START:
            EditModeOn();
            Layer layer = (Layer) command.getSource();
            HandlEditModeForExistingLayer(layer);
            break;
        }
    }
    
    public void HandlEditModeForExistingLayer(Layer layer) {
        EditModeOn();
    }

    public void LayerEditorCancel() {
        checkBoxViewTree.refresh();
        this.finishModifiedLayerMode();
    }

    public void LayerEditorSave() {
        checkBoxViewTree.refresh();
        this.finishModifiedLayerMode();
    }


    public Layer findLayerByName(String name) {
        return findLayerByName(root, name);
    }

    private Layer findLayerByName(Layer root, String name) {
        if (root instanceof LayerGroup) {
            for (Layer layer : ((LayerGroup) root).getChildens()) {
                Layer res = findLayerByName(layer, name);
                if (res != null)
                    return res;
            }
            return null;
        }
        if (root.getName().equals(name))
            return root;

        return null;
    }

    /**
     * @return the root
     */
    public LayerGroup getRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(LayerGroup root) {
        this.root = root;
    }

}
