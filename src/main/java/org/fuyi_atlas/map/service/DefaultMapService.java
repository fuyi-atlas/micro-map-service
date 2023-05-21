package org.fuyi_atlas.map.service;

import lombok.extern.slf4j.Slf4j;
import org.fuyi_atlas.map.core.grid.*;
import org.fuyi_atlas.map.core.store.DefaultDataStore;
import org.fuyi_atlas.map.core.vector.MapBoxTileBuilder;
import org.fuyi_atlas.map.core.vector.MapBoxTileBuilderFactory;
import org.fuyi_atlas.map.core.vector.VectorTileMapOutputFormat;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.vector.Pipeline;
import org.geoserver.wms.vector.PipelineBuilder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:04
 * @since: 1.0
 **/
@Slf4j
@Service
public class DefaultMapService {

    private JDBCDataStore dataStore;
    private VectorTileMapOutputFormat outputFormat;
    private String tableName = "SL_FLOWPIPE";
    private String vtName = tableName;
    private VirtualTable virtualTable;
    private GridSet gridSet;
    private static int DEFAULT_MAP_WIDTH = 256;
    private static int DEFAULT_MAP_HEIGHT = 256;
    private static int DEFAULT_BUFFER_FACTOR = 6;
    //    private final CoordinateReferenceSystem source = CRS.decode("EPSG:4544", true);
    private final CoordinateReferenceSystem source = CRS.decode("EPSG:4549", true);
    private final CoordinateReferenceSystem target = CRS.decode("EPSG:900913", true);
    private final MapBoxTileBuilderFactory tileBuilderFactory = new MapBoxTileBuilderFactory();
    private final FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();
    private GeometryFactory factory = new GeometryFactory();

    public DefaultMapService() throws FactoryException {
        try {
            this.dataStore = (JDBCDataStore) DefaultDataStore.FetchDataStore(null);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
//        this.vtName = tableName + "_view";
        this.vtName = tableName.toLowerCase(Locale.ROOT);
        this.virtualTable = new VirtualTable(vtName,
                "select objectid, caliber,material,classcode,id,display,enabled,shape from " + tableName);
        List<String> prime = new ArrayList<>();
        prime.add("objectid");
        virtualTable.setPrimaryKeyColumns(prime);
        virtualTable.addGeometryMetadatata("shape", LineString.class, 4549);
        try {
            this.dataStore.createVirtualTable(virtualTable);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try {
            this.outputFormat = new VectorTileMapOutputFormat();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        // 需要将其转换为米
        double pixelSize = 0.00028D;
        this.gridSet = buildGriDSet(SRS.getEPSG900913(), pixelSize);
    }

    public byte[] generate(long x, long y, long z) throws IOException, FactoryException, TransformException {
        long tilesHigh = this.gridSet.getGrid((int) z).getNumTilesHigh();
        y = tilesHigh - y - 1L;
        long[] index = new long[]{x, y, z};
        BoundingBox boundingBox = this.gridSet.boundsFromIndex(index);
        Envelope envelope = new Envelope(boundingBox.getMinX(), boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxY());
        MathTransform mathTransform = null;
        try {
            mathTransform = CRS.findMathTransform(this.source, this.target, true).inverse();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        Envelope finalBbox = JTS.transform(envelope, mathTransform);
        log.info("bbox: {}", factory.toGeometry(finalBbox));

        int mapWidth = DEFAULT_MAP_WIDTH;
        int mapHeight = DEFAULT_MAP_HEIGHT;
        Rectangle paintArea = new Rectangle(mapWidth, mapHeight);
        if (this.tileBuilderFactory.shouldOversampleScale()) {
            paintArea = new Rectangle(this.tileBuilderFactory.getOversampleX() * mapWidth, this.tileBuilderFactory.getOversampleY() * mapHeight);
        }

        ReferencedEnvelope renderingArea = new ReferencedEnvelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY(), target);
        MapBoxTileBuilder vectorTileBuilder = this.tileBuilderFactory.newBuilder(paintArea, renderingArea);

        ContentFeatureSource featureSource = this.dataStore.getFeatureSource(this.vtName);
        Query query = new Query(vtName);
        GeometryDescriptor geometryDescriptor = featureSource.getSchema().getGeometryDescriptor();
        Pipeline pipeline = null;
        if (null != geometryDescriptor) {
            CoordinateReferenceSystem sourceCrs = geometryDescriptor.getType().getCoordinateReferenceSystem();
            int buffer = DEFAULT_BUFFER_FACTOR * 1;
            if (this.tileBuilderFactory.shouldOversampleScale()) {
                buffer *= Math.max(Math.max(this.tileBuilderFactory.getOversampleX(), this.tileBuilderFactory.getOversampleY()), 1);
            }
            String geometryPropertyName = geometryDescriptor.getLocalName();
            ReferencedEnvelope fBbox = new ReferencedEnvelope(finalBbox.getMinX(), finalBbox.getMaxX(), finalBbox.getMinY(), finalBbox.getMaxY(), null);
            Filter filter = filterFactory.bbox(filterFactory.property(geometryPropertyName), fBbox);
            query = new Query(vtName, filter);
            Hints hints = query.getHints();
            pipeline = this.getPipeline(renderingArea, paintArea, sourceCrs, featureSource.getSupportedHints(), hints, buffer);
            hints.remove(Hints.SCREENMAP);

        }
        long start = System.currentTimeMillis();
        SimpleFeatureCollection features = featureSource.getFeatures(query);
        System.out.println("查询耗时：" + (System.currentTimeMillis() - start));

        return outputFormat.build(features.features(), pipeline, geometryDescriptor, vectorTileBuilder);
    }

    /**
     * @param srs            空间参考
     * @param baseResolution 分辨率基数
     * @return
     */
    public static GridSet buildGriDSet(SRS srs, double baseResolution) {
        return GridSetFactory.createGridSet(String.valueOf(srs.getNumber()), srs, BoundingBox.WORLD3857, false, 30, null,
                baseResolution, 256, 256, false);
    }

    private double overSamplingFactor = 2.0D;
    private boolean transformToScreenCoordinates = true;
    private boolean clipToMapBounds = true;

    /**
     * @param renderingArea
     * @param paintArea
     * @param sourceCrs     数据源坐标系
     * @param fsHints
     * @param qHints
     * @param buffer
     * @return
     */
    protected Pipeline getPipeline(ReferencedEnvelope renderingArea, Rectangle paintArea, CoordinateReferenceSystem sourceCrs, Set<RenderingHints.Key> fsHints, Hints qHints, int buffer) {
        try {
            PipelineBuilder builder = PipelineBuilder.newBuilder(renderingArea, paintArea, sourceCrs, this.overSamplingFactor, buffer);
            Pipeline pipeline = builder.preprocess().transform(this.transformToScreenCoordinates).clip(this.clipToMapBounds, this.transformToScreenCoordinates).simplify(this.transformToScreenCoordinates, fsHints, qHints).collapseCollections().build();
            return pipeline;
        } catch (FactoryException var10) {
            throw new ServiceException(var10);
        }
    }

    public void setTransformToScreenCoordinates(boolean useScreenCoords) {
        this.transformToScreenCoordinates = useScreenCoords;
    }

    public void setClipToMapBounds(boolean clip) {
        this.clipToMapBounds = clip;
    }
}
