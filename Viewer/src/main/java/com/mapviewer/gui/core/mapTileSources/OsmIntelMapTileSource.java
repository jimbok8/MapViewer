package com.mapviewer.gui.core.mapTileSources;

import com.mapviewer.gui.is.mapTileSources.AbstractOsmTileSource;

public class OsmIntelMapTileSource extends AbstractOsmTileSource {

	private static final String PATTERN = "https://maps.wikimedia.org/osm-intl";

    public OsmIntelMapTileSource() {
    	super("Intel OSM", PATTERN, "Intel OSM");
    }

    @Override
    public String getBaseUrl() {
    	return this.baseUrl;
    }

    @Override
    public int getMaxZoom() {
        return 18;
    }
}