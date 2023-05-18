package no.ecc.vectortile;

import org.locationtech.jts.geom.Geometry;

/**
 * provides VectorTileEncoder that doesn't do any clipping.
 * Our clipping system is "better" (more robust, faster, and maintainable here).
 *
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 16:09
 * @since: 1.0
 **/
public class VectorTileEncoderNoClip extends VectorTileEncoder {

    public VectorTileEncoderNoClip(int extent, int polygonClipBuffer, boolean autoScale) {
        super(extent, polygonClipBuffer, autoScale);
    }

    /*
     * returns original geometry - no clipping. Assume upstream has already clipped!
     */
    @Override
    protected Geometry clipGeometry(Geometry geometry) {
        return geometry;
    }
}
