package org.fuyiatlas.map.core.vector;

import com.google.common.collect.ImmutableSet;
import org.geoserver.wms.vector.VectorTileBuilderFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;

import java.awt.*;
import java.util.Set;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:59
 * @since: 1.0
 **/
public class MapBoxTileBuilderFactory  implements VectorTileBuilderFactory {
    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";
    public static final String LEGACY_MIME_TYPE = "application/x-protobuf;type=mapbox-vector";
    public static final Set<String> OUTPUT_FORMATS = ImmutableSet.of("application/vnd.mapbox-vector-tile", "application/x-protobuf;type=mapbox-vector", "pbf");

    public MapBoxTileBuilderFactory() {
    }

    public Set<String> getOutputFormats() {
        return OUTPUT_FORMATS;
    }

    public String getMimeType() {
        return "application/vnd.mapbox-vector-tile";
    }

    public MapBoxTileBuilder newBuilder(Rectangle screenSize, ReferencedEnvelope mapArea) {
        return new MapBoxTileBuilder(screenSize, mapArea);
    }

    public boolean shouldOversampleScale() {
        return true;
    }

    public int getOversampleX() {
        return 16;
    }

    public int getOversampleY() {
        return 16;
    }
}
