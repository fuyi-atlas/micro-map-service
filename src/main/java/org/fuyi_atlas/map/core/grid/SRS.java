package org.fuyi_atlas.map.core.grid;

import org.fuyi_atlas.map.core.GeoWebCacheException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:22
 * @since: 1.0
 **/
public class SRS implements Comparable<SRS>, Serializable {
    private static Map<Integer, SRS> list = new ConcurrentHashMap();
    private static final SRS EPSG4326 = new SRS(4326);
    private static final SRS EPSG3857 = new SRS(3857, new ArrayList(Arrays.asList(900913, 102113, 102100)));
    private static final SRS EPSG900913 = new SRS(900913, new ArrayList(Arrays.asList(3857, 102113, 102100)));
    private int number;
    private transient List<Integer> aliases;

    private SRS() {
    }

    private SRS(int epsgNumber) {
        this(epsgNumber, (List)null);
    }

    private SRS(int epsgNumber, List<Integer> aliases) {
        this.number = epsgNumber;
        this.aliases = aliases;
        this.readResolve();
    }

    private Object readResolve() {
        if (!list.containsKey(this.number)) {
            list.put(this.number, this);
        }

        return this;
    }

    public static SRS getSRS(int epsgCode) {
        Integer code = epsgCode;
        SRS existing = (SRS)list.get(code);
        if (existing != null) {
            return existing;
        } else {
            Iterator var3 = (new ArrayList(list.values())).iterator();

            SRS candidate;
            do {
                if (!var3.hasNext()) {
                    return new SRS(epsgCode);
                }

                candidate = (SRS)var3.next();
            } while(candidate.aliases == null || !candidate.aliases.contains(code));

            list.put(code, candidate);
            return candidate;
        }
    }

    public static SRS getSRS(String epsgStr) throws GeoWebCacheException {
        String crsAuthPrefix = "EPSG:";
        if (epsgStr.substring(0, 5).equalsIgnoreCase("EPSG:")) {
            int epsgNumber = Integer.parseInt(epsgStr.substring(5, epsgStr.length()));
            return getSRS(epsgNumber);
        } else {
            throw new GeoWebCacheException("Can't parse " + epsgStr + " as SRS string.");
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SRS)) {
            return false;
        } else {
            boolean equivalent = false;
            SRS other = (SRS)obj;
            if (other.number == this.number) {
                equivalent = true;
            } else if (this.aliases != null && other.aliases != null) {
                equivalent = this.aliases.contains(other.number) || other.aliases.contains(this.number);
            }

            return equivalent;
        }
    }

    public int getNumber() {
        return this.number;
    }

    public int hashCode() {
        return this.number;
    }

    public String toString() {
        return "EPSG:" + Integer.toString(this.number);
    }

    public static SRS getEPSG4326() {
        return EPSG4326;
    }

    public static SRS getEPSG3857() {
        return EPSG3857;
    }

    public static SRS getEPSG900913() {
        return EPSG900913;
    }

    public int compareTo(SRS other) {
        return this.number - other.number;
    }
}
