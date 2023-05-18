package org.fuyiatlas.map.core.vector;

import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.WebMap;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.Map;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:58
 * @since: 1.0
 **/
public interface VectorTileBuilder {

    void addFeature(String var1, String var2, String var3, Geometry var4, Map<String, Object> var5);

    WebMap build(WMSMapContent var1) throws IOException;
    byte[] build() throws IOException;
}
