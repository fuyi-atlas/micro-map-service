package org.fuyiatlas.map.core.vector;

import no.ecc.vectortile.VectorTileEncoder;
import no.ecc.vectortile.VectorTileEncoderNoClip;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.map.RawMap;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 14:40
 * @since: 1.0
 **/
public class MapBoxTileBuilder extends org.geoserver.wms.mapbox.MapBoxTileBuilder  {
    private static final Logger LOGGER = Logging.getLogger(org.geoserver.wms.mapbox.MapBoxTileBuilder.class);
    private VectorTileEncoder encoder;

    public MapBoxTileBuilder(Rectangle mapSize, ReferencedEnvelope mapArea) {
        super(mapSize, mapArea);
        int extent = Math.max(mapSize.width, mapSize.height);
        int polygonClipBuffer = extent / 32;
        boolean autoScale = false;
        this.encoder = new VectorTileEncoderNoClip(extent, polygonClipBuffer, false);
    }

    public void addFeature(String layerName, String featureId, String geometryName, Geometry geometry, Map<String, Object> properties) {
        int id = -1;
        if (featureId.matches(".*\\.[0-9]+")) {
            try {
                id = Integer.parseInt(featureId.split("\\.")[1]);
            } catch (NumberFormatException var8) {
            }
        }

        if (id < 0) {
            LOGGER.warning("Cannot obtain numeric id from featureId: " + featureId);
        }

        this.encoder.addFeature(layerName, properties, geometry, (long) id);
    }

    public RawMap build(WMSMapContent mapContent) throws IOException {
        byte[] contents = this.encoder.encode();
        return new RawMap(mapContent, contents, "application/vnd.mapbox-vector-tile");
    }

    public byte[] build() {
        long start = System.currentTimeMillis();
        byte[] contents = this.encoder.encode();
        System.out.println("编码耗时：" + (System.currentTimeMillis() - start));
        return contents;
    }
}
