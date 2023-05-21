package org.fuyi_atlas.map.core;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:23
 * @since: 1.0
 **/
public class GeoWebCacheException extends Exception {
    private static final long serialVersionUID = 5837933971679774371L;

    public GeoWebCacheException(String msg) {
        super(msg);
    }

    public GeoWebCacheException(Throwable thrw) {
        super(thrw);
    }

    public GeoWebCacheException(String msg, Throwable cause) {
        super(msg, cause);
    }
}