package org.fuyiatlas.map.core.grid;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/29 16:21
 * @since: 1.0
 **/
public class Grid  implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    // 指定层级横向的瓦片数量
    private long numTilesWide;
    // 指定层级纵向的瓦片数量
    private long numTilesHigh;
    private double resolution;
    private double scaleDenom;
    private String name;

    public Grid() {
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Grid)) {
            return false;
        } else {
            Grid other = (Grid) obj;
            if (this.numTilesWide != other.numTilesWide) {
                return false;
            } else if (this.numTilesHigh != other.numTilesHigh) {
                return false;
            } else {
                return !(Math.abs(other.resolution - this.resolution) / Math.abs(other.resolution + this.resolution) > 0.005D);
            }
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.numTilesWide, this.numTilesHigh, this.resolution, this.scaleDenom, this.name});
    }

    public String getName() {
        return this.name;
    }

    public double getScaleDenominator() {
        return this.scaleDenom;
    }

    public double getResolution() {
        return this.resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public void setScaleDenominator(double scaleDenom) {
        this.scaleDenom = scaleDenom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNumTilesWide() {
        return this.numTilesWide;
    }

    public void setNumTilesWide(long numTilesWide) {
        this.numTilesWide = numTilesWide;
    }

    public long getNumTilesHigh() {
        return this.numTilesHigh;
    }

    public void setNumTilesHigh(long numTilesHigh) {
        this.numTilesHigh = numTilesHigh;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[name: '" + this.name + "', resolution: " + this.resolution + ", scale denom: " + this.scaleDenom + ", grid extent: " + this.numTilesWide + " x " + this.numTilesHigh + "]";
    }

    public Grid clone() {
        try {
            Grid clon = (Grid) super.clone();
            return clon;
        } catch (CloneNotSupportedException var3) {
            throw new RuntimeException(var3);
        }
    }
}