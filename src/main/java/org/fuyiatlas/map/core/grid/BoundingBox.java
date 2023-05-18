package org.fuyiatlas.map.core.grid;

import org.geotools.util.logging.Logging;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:24
 * @since: 1.0
 **/
public class BoundingBox implements Serializable {
    private static final long serialVersionUID = -2555598825074884627L;
    private static Logger log = Logging.getLogger(BoundingBox.class.getName());
    private static String DELIMITER = ",";
    private static double EQUALITYTHRESHOLD = 0.03D;
    public static final BoundingBox WORLD4326 = new BoundingBox(-180.0D, -90.0D, 180.0D, 90.0D);
    public static final BoundingBox WORLD3857 = new BoundingBox(-2.003750834E7D, -2.003750834E7D, 2.003750834E7D, 2.003750834E7D);
    public static final BoundingBox WORLD4549 = new BoundingBox(347872.24670060317D, 2702916.029170417D, 652127.7532993995D, 5912395.200238359D);
    public static final BoundingBox WORLD3857_TMS = new BoundingBox(-2.00375083427892E7D, -2.00375083427892E7D, 2.00375083427892E7D, 2.00375083427892E7D);
    private double[] coords = new double[4];

    private NumberFormat getCoordinateFormatter() {
        NumberFormat COORD_FORMATTER = NumberFormat.getNumberInstance(Locale.ENGLISH);
        COORD_FORMATTER.setMinimumFractionDigits(1);
        COORD_FORMATTER.setGroupingUsed(false);
        COORD_FORMATTER.setMaximumFractionDigits(16);
        return COORD_FORMATTER;
    }

    BoundingBox() {
    }

    public BoundingBox(BoundingBox bbox) {
        this.coords[0] = bbox.coords[0];
        this.coords[1] = bbox.coords[1];
        this.coords[2] = bbox.coords[2];
        this.coords[3] = bbox.coords[3];
    }

    public BoundingBox(String BBOX) {
        this.setFromBBOXString(BBOX, 0);
        if (log.isLoggable(Level.FINER)) {
            log.finer("Created BBOX: " + this.getReadableString());
        }

    }

    public BoundingBox(String[] BBOX) {
        this.setFromStringArray(BBOX);
        if (log.isLoggable(Level.FINER)) {
            log.finer("Created BBOX: " + this.getReadableString());
        }

    }

    public BoundingBox(double minx, double miny, double maxx, double maxy) {
        this.coords[0] = minx;
        this.coords[1] = miny;
        this.coords[2] = maxx;
        this.coords[3] = maxy;
        if (log.isLoggable(Level.FINER)) {
            log.finer("Created BBOX: " + this.getReadableString());
        }

    }

    public double getMinX() {
        return this.coords[0];
    }

    public void setMinX(double minx) {
        this.coords[0] = minx;
    }

    public double getMinY() {
        return this.coords[1];
    }

    public void setMinY(double miny) {
        this.coords[1] = miny;
    }

    public double getMaxX() {
        return this.coords[2];
    }

    public void setMaxX(double maxx) {
        this.coords[2] = maxx;
    }

    public double getMaxY() {
        return this.coords[3];
    }

    public void setMaxY(double maxy) {
        this.coords[3] = maxy;
    }

    public double[] getCoords() {
        return (double[])this.coords.clone();
    }

    public double getWidth() {
        return this.coords[2] - this.coords[0];
    }

    public double getHeight() {
        return this.coords[3] - this.coords[1];
    }

    public void setFromStringArray(String[] BBOX) {
        this.setFromStringArray(BBOX, 0);
    }

    public void setFromStringArray(String[] BBOX, int recWatch) {
        if (BBOX.length == 4) {
            this.coords[0] = Double.parseDouble(BBOX[0]);
            this.coords[1] = Double.parseDouble(BBOX[1]);
            this.coords[2] = Double.parseDouble(BBOX[2]);
            this.coords[3] = Double.parseDouble(BBOX[3]);
        } else if (recWatch < 4) {
            this.setFromBBOXString(BBOX[0], recWatch);
        } else {
            log.severe("Doesnt understand " + Arrays.toString(BBOX));
        }

    }

    public void setFromBBOXString(String BBOX, int recWatch) {
        String[] tokens = BBOX.split(DELIMITER);
        this.setFromStringArray(tokens, recWatch + 1);
    }

    public String getReadableString() {
        return "Min X: " + this.coords[0] + " Min Y: " + this.coords[1] + " Max X: " + this.coords[2] + " Max Y: " + this.coords[3];
    }

    public String toString() {
        NumberFormat formatter = this.getCoordinateFormatter();
        StringBuilder buff = new StringBuilder(40);
        buff.append(formatter.format(this.coords[0]));
        buff.append(',');
        buff.append(formatter.format(this.coords[1]));
        buff.append(',');
        buff.append(formatter.format(this.coords[2]));
        buff.append(',');
        buff.append(formatter.format(this.coords[3]));
        return buff.toString();
    }

    public String toKMLLatLonBox() {
        return "<LatLonBox><north>" + Double.toString(this.coords[3]) + "</north><south>" + Double.toString(this.coords[1]) + "</south><east>" + Double.toString(this.coords[2]) + "</east><west>" + Double.toString(this.coords[0]) + "</west></LatLonBox>";
    }

    public String toKMLLatLonAltBox() {
        return "<LatLonAltBox><north>" + Double.toString(this.coords[3]) + "</north><south>" + Double.toString(this.coords[1]) + "</south><east>" + Double.toString(this.coords[2]) + "</east><west>" + Double.toString(this.coords[0]) + "</west></LatLonAltBox>";
    }

    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            BoundingBox other = (BoundingBox)obj;
            return this.equals(other, EQUALITYTHRESHOLD);
        } else {
            return false;
        }
    }

    public boolean equals(BoundingBox other, double threshold) {
        return Math.abs(this.getMinX() - other.getMinX()) < threshold && Math.abs(this.getMinY() - other.getMinY()) < threshold && Math.abs(this.getWidth() - other.getWidth()) < threshold && Math.abs(this.getHeight() - other.getHeight()) < threshold;
    }

    public boolean contains(BoundingBox other) {
        return this.coords[0] - EQUALITYTHRESHOLD <= other.coords[0] && this.coords[1] - EQUALITYTHRESHOLD <= other.coords[1] && this.coords[2] + EQUALITYTHRESHOLD >= other.coords[2] && this.coords[3] + EQUALITYTHRESHOLD >= other.coords[3];
    }

    public boolean isSane() {
        return this.coords[0] < this.coords[2] && this.coords[1] < this.coords[3];
    }

    public boolean isNull() {
        return this.coords[0] > this.coords[2] || this.coords[1] > this.coords[3];
    }

    public int hashCode() {
        return Float.floatToIntBits((float)this.coords[0]) ^ Float.floatToIntBits((float)this.coords[1]);
    }

    public boolean intersects(BoundingBox other) {
        if (!this.isNull() && !other.isNull()) {
            return !(other.getMinX() > this.getMaxX()) && !(other.getMaxX() < this.getMinX()) && !(other.getMinY() > this.getMaxY()) && !(other.getMaxY() < this.getMinY());
        } else {
            return false;
        }
    }

    public BoundingBox intersection(BoundingBox bboxB) {
        return intersection(this, bboxB);
    }

    public static BoundingBox intersection(BoundingBox bboxA, BoundingBox bboxB) {
        BoundingBox retBbox = new BoundingBox(0.0D, 0.0D, -1.0D, -1.0D);
        if (bboxA.intersects(bboxB)) {
            int i;
            for(i = 0; i < 2; ++i) {
                if (bboxA.coords[i] > bboxB.coords[i]) {
                    retBbox.coords[i] = bboxA.coords[i];
                } else {
                    retBbox.coords[i] = bboxB.coords[i];
                }
            }

            for(i = 2; i < 4; ++i) {
                if (bboxA.coords[i] < bboxB.coords[i]) {
                    retBbox.coords[i] = bboxA.coords[i];
                } else {
                    retBbox.coords[i] = bboxB.coords[i];
                }
            }
        }

        return retBbox;
    }

    public void scale(double xFactor, double yFactor) {
        double x = this.coords[2] - this.coords[0];
        double xdiff = (x * xFactor - x) / 2.0D;
        double y = this.coords[3] - this.coords[1];
        double ydiff = (y * yFactor - y) / 2.0D;
        this.coords[0] -= xdiff;
        this.coords[1] -= ydiff;
        this.coords[2] += xdiff;
        this.coords[3] += ydiff;
    }

    public void scale(double factor) {
        this.scale(factor, factor);
    }
}