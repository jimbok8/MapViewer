package com.gui.core.mapTree.internal;

import com.gui.core.layers.AbstractLayer;
import javafx.scene.control.TreeItem;

public class LayeredCheckBoxTreeCellEditorConvertor implements TreeCellEditorConvertor<AbstractLayer>{

	@Override
	public AbstractLayer fromString(TreeItem<AbstractLayer> treeItem, String title) {
		AbstractLayer layer = treeItem.getValue();
		layer.setName(title);
		return layer;
	}
}
