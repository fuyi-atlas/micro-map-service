package org.fuyi_atlas.map.core.store;

import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 9:54
 * @since: 1.0
 **/
public class DefaultDataStore {

    private static DataStore dataStore;

    public static synchronized DataStore FetchDataStore (Map<String, Object> params) throws IOException {
        if(CollectionUtils.isEmpty(params)){
            params = GetDefaultParams();
        }
        DataStore dataStore = DataStoreFinder.getDataStore(params);
        return dataStore;
    }

    private static Map<String, Object> GetDefaultParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "sqlserver");
        params.put("host", "localhost");
        params.put("port", 1433);
        params.put("schema", "dbo");
        params.put("database", "LM_XS_Geodatabase102");
        params.put("user", "sa");
        params.put("passwd", "GIS@4490");
        params.put("Primary key metadata table", "OBJECTID");
        params.put("preparedStatements", true);
        params.put("encode functions", true);
        return params;
    }

    public static void main(String[] args) throws IOException, ParseException {
        JDBCDataStore dataStore = (JDBCDataStore) FetchDataStore(null);
        String tableName = "SL_FLOWPIPE";
        VirtualTable vt = new VirtualTable(tableName + "_v",
                "select objectid, caliber,material,classcode,id,display,enabled,shape from " + tableName);
        List<String> prime = new ArrayList<>();
        prime.add("objectid");
        vt.setPrimaryKeyColumns(prime);
        vt.addGeometryMetadatata("shape", LineString.class, 4544);
        dataStore.createVirtualTable(vt);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(tableName + "_v");

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        FeatureType schema = featureSource.getSchema();

        // usually "THE_GEOM" for shapefiles
        String geometryPropertyName = schema.getGeometryDescriptor().getLocalName();
//        CoordinateReferenceSystem targetCRS =
//                schema.getGeometryDescriptor().getCoordinateReferenceSystem();

        ReferencedEnvelope bbox = featureSource.getBounds();
        Geometry read = new WKTReader().read("LINESTRING (517951.44299999997 4151878.7929999996, 517951.48900000006 4151878.4289999995)");
        Envelope envelopeInternal = read.getEnvelopeInternal();

        ReferencedEnvelope fBbox = new ReferencedEnvelope(envelopeInternal.getMinX(), envelopeInternal.getMaxX(), envelopeInternal.getMinY(), envelopeInternal.getMaxY(), null);
        Filter filter = ff.bbox(ff.property(geometryPropertyName), fBbox);
        Query query = new Query(tableName + "_v", filter);
//        Query query = new Query(tableName + "_v", filter, new String[]{"id","shape"});

        SimpleFeatureCollection features = featureSource.getFeatures(query);
        SimpleFeatureIterator features1 = features.features();
        while (features1.hasNext()){
            features1.next().getAttributes().stream().forEach(System.out::print);
            System.out.print(", ");
            System.out.println();
        }
        System.out.println();
        System.out.println("============");
        featureSource.getFeatures().features().next().getAttributes().stream().forEach(System.out::print);
    }

}
