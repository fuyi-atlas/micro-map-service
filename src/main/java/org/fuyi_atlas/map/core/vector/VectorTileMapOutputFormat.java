package org.fuyi_atlas.map.core.vector;

import com.google.common.base.Stopwatch;
import org.geoserver.wms.vector.Pipeline;
import org.geoserver.wms.vector.VectorTileBuilder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 14:26
 * @since: 1.0
 **/
public class VectorTileMapOutputFormat {

    private final Logger LOGGER = LoggerFactory.getLogger(VectorTileMapOutputFormat.class);

    public VectorTileMapOutputFormat() throws FactoryException {
    }

    public byte[] build(SimpleFeatureIterator featureIterator, Pipeline pipeline, GeometryDescriptor geometryDescriptor, VectorTileBuilder vectorTileBuilder) throws TransformException, FactoryException {
        Stopwatch sw = Stopwatch.createStarted();
        int count = 0;
        int total = 0;
        if (Objects.isNull(pipeline)) {
            return null;
        }
        Throwable var11 = null;
        try {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                ++total;
                Geometry originalGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();

                Geometry finalGeom;
                try {
                    finalGeom = pipeline.execute(originalGeom);
                } catch (Exception var26) {
                    LOGGER.warn(var26.getLocalizedMessage(), var26);
                    continue;
                }

                if (!finalGeom.isEmpty()) {
                    String layerName = feature.getType().getName().getLocalPart();
                    String featureId = feature.getIdentifier().toString();
                    String geometryName = geometryDescriptor.getName().getLocalPart();
                    Map<String, Object> properties = this.getProperties(feature);
                    vectorTileBuilder.addFeature(layerName, featureId, geometryName, finalGeom, properties);
                    ++count;
                }
            }
        } catch (Throwable var27) {
            var11 = var27;
            throw var27;
        } finally {
            if (featureIterator != null) {
                if (var11 != null) {
                    try {
                        featureIterator.close();
                    } catch (Throwable var25) {
                        var11.addSuppressed(var25);
                    }
                } else {
                    featureIterator.close();
                }
            }
        }
        sw.stop();
//        if (LOGGER.isDebugEnabled()) {
            String msg = String.format("Added %,d out of %,d features of '%s' in %s", count, total, "sl_flowpipe", sw);
            LOGGER.info(msg);
//        }
        return ((MapBoxTileBuilder) vectorTileBuilder).build();
    }

    private Map<String, Object> getProperties(ComplexAttribute feature) {
        Map<String, Object> props = new TreeMap();
        Iterator var3 = feature.getProperties().iterator();

        while (var3.hasNext()) {
            Property p = (Property) var3.next();
            if (p instanceof Attribute && !(p instanceof GeometryAttribute)) {
                String name = p.getName().getLocalPart();
                Object value;
                if (p instanceof ComplexAttribute) {
                    value = this.getProperties((ComplexAttribute) p);
                } else {
                    value = p.getValue();
                }

                if (value != null) {
                    props.put(name, value);
                }
            }
        }

        return props;
    }
}
