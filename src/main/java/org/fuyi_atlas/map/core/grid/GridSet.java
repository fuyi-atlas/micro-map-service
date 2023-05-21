package org.fuyi_atlas.map.core.grid;


import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/29 16:27
 * @since: 1.0
 **/
public class GridSet {

    // 名称
    private String name;
    private SRS srs;
    private int tileWidth;
    private int tileHeight;
    protected boolean yBaseToggle = false;
    private boolean yCoordinateFirst = false;
    private boolean scaleWarning = false;
    private double metersPerUnit;

    // Pixel size (to calculate scales). The default is 0.28mm/pixel, corresponding to 90.71428571428572 DPI.
    // 像素大小（用于计算比例, 应用在比例尺的计算中, 如使用比例尺反推分辨率）。 默认为0.28mm/pixel，对应90.71428571428572 DPI。
    private double pixelSize;
    private BoundingBox originalExtent;
    private Grid[] gridLevels;
    private String description;
    private boolean resolutionsPreserved;

    protected GridSet() {
    }

    public GridSet(GridSet g) {
        this.name = g.name;
        this.srs = g.srs;
        this.tileWidth = g.tileWidth;
        this.tileHeight = g.tileHeight;
        this.yBaseToggle = g.yBaseToggle;
        this.yCoordinateFirst = g.yCoordinateFirst;
        this.scaleWarning = g.scaleWarning;
        this.metersPerUnit = g.metersPerUnit;
        this.pixelSize = g.pixelSize;
        this.originalExtent = g.originalExtent;
        this.gridLevels = g.gridLevels;
        this.description = g.description;
        this.resolutionsPreserved = g.resolutionsPreserved;
    }

    public BoundingBox getOriginalExtent() {
        return this.originalExtent;
    }

    void setOriginalExtent(BoundingBox originalExtent) {
        this.originalExtent = originalExtent;
    }

    public boolean isResolutionsPreserved() {
        return this.resolutionsPreserved;
    }

    void setResolutionsPreserved(boolean resolutionsPreserved) {
        this.resolutionsPreserved = resolutionsPreserved;
    }

    /**
     * @param tileIndex [x,y,z]
     * @return
     */
    public BoundingBox boundsFromIndex(long[] tileIndex) {
        int tileZ = (int) tileIndex[2];
        Grid grid = this.getGrid(tileZ);
        long tileX = tileIndex[0];
        long tileY;
        if (this.yBaseToggle) {
            tileY = tileIndex[1] - grid.getNumTilesHigh();
        } else {
            tileY = tileIndex[1];
        }

        double width = grid.getResolution() * (double) this.getTileWidth();
        double height = grid.getResolution() * (double) this.getTileHeight();
        double[] tileOrigin = this.tileOrigin();
        BoundingBox tileBounds = new BoundingBox(tileOrigin[0] + width * (double) tileX, tileOrigin[1] + height * (double) tileY, tileOrigin[0] + width * (double) (tileX + 1L), tileOrigin[1] + height * (double) (tileY + 1L));
        return tileBounds;
    }

    protected BoundingBox boundsFromRectangle(long[] rectangleExtent) {
        Grid grid = this.getGrid((int) rectangleExtent[4]);
        double width = grid.getResolution() * (double) this.getTileWidth();
        double height = grid.getResolution() * (double) this.getTileHeight();
        long bottomY = rectangleExtent[1];
        long topY = rectangleExtent[3];
        if (this.yBaseToggle) {
            bottomY -= grid.getNumTilesHigh();
            topY -= grid.getNumTilesHigh();
        }

        double[] tileOrigin = this.tileOrigin();
        double minx = tileOrigin[0] + width * (double) rectangleExtent[0];
        double miny = tileOrigin[1] + height * (double) bottomY;
        double maxx = tileOrigin[0] + width * (double) (rectangleExtent[2] + 1L);
        double maxy = tileOrigin[1] + height * (double) (topY + 1L);
        BoundingBox rectangleBounds = new BoundingBox(minx, miny, maxx, maxy);
        return rectangleBounds;
    }

//        protected long[] closestIndex(BoundingBox tileBounds) throws GridMismatchException {
//            double wRes = tileBounds.getWidth() / (double)this.getTileWidth();
//            double bestError = 1.7976931348623157E308D;
//            int bestLevel = -1;
//            double bestResolution = -1.0D;
//
//            for(int i = 0; i < this.getNumLevels(); bestLevel = i++) {
//                Grid grid = this.getGrid(i);
//                double error = Math.abs(wRes - grid.getResolution());
//                if (!(error < bestError)) {
//                    break;
//                }
//
//                bestError = error;
//                bestResolution = grid.getResolution();
//            }
//
//            if (Math.abs(wRes - bestResolution) > 0.1D * wRes) {
//                throw new ResolutionMismatchException(wRes, bestResolution);
//            } else {
//                return this.closestIndex(bestLevel, tileBounds);
//            }
//        }
//
//        protected long[] closestIndex(int level, BoundingBox tileBounds) throws GridAlignmentMismatchException {
//            Grid grid = this.getGrid(level);
//            double width = grid.getResolution() * (double)this.getTileWidth();
//            double height = grid.getResolution() * (double)this.getTileHeight();
//            double x = (tileBounds.getMinX() - this.tileOrigin()[0]) / width;
//            double y = (tileBounds.getMinY() - this.tileOrigin()[1]) / height;
//            long posX = Math.round(x);
//            long posY = Math.round(y);
//            if (!(Math.abs(x - (double)posX) > 0.1D) && !(Math.abs(y - (double)posY) > 0.1D)) {
//                if (this.yBaseToggle) {
//                    posY += grid.getNumTilesHigh();
//                }
//
//                long[] ret = new long[]{posX, posY, (long)level};
//                return ret;
//            } else {
//                throw new GridAlignmentMismatchException(x, posX, y, posY);
//            }
//        }

    public long[] closestRectangle(BoundingBox rectangleBounds) {
        double rectWidth = rectangleBounds.getWidth();
        double rectHeight = rectangleBounds.getHeight();
        double bestError = 1.7976931348623157E308D;
        int bestLevel = -1;

        for (int i = 0; i < this.getNumLevels(); ++i) {
            Grid grid = this.getGrid(i);
            double countX = rectWidth / (grid.getResolution() * (double) this.getTileWidth());
            double countY = rectHeight / (grid.getResolution() * (double) this.getTileHeight());
            double error = Math.abs(countX - (double) Math.round(countX)) + Math.abs(countY - (double) Math.round(countY));
            if (error < bestError) {
                bestError = error;
                bestLevel = i;
            } else if (error >= bestError) {
                break;
            }
        }

        return this.closestRectangle(bestLevel, rectangleBounds);
    }

    protected long[] closestRectangle(int level, BoundingBox rectangeBounds) {
        Grid grid = this.getGrid(level);
        double width = grid.getResolution() * (double) this.getTileWidth();
        double height = grid.getResolution() * (double) this.getTileHeight();
        long minX = (long) Math.floor((rectangeBounds.getMinX() - this.tileOrigin()[0]) / width);
        long minY = (long) Math.floor((rectangeBounds.getMinY() - this.tileOrigin()[1]) / height);
        long maxX = (long) Math.ceil((rectangeBounds.getMaxX() - this.tileOrigin()[0]) / width);
        long maxY = (long) Math.ceil((rectangeBounds.getMaxY() - this.tileOrigin()[1]) / height);
        if (this.yBaseToggle) {
            minY += grid.getNumTilesHigh();
            maxY += grid.getNumTilesHigh();
        }

        long[] ret = new long[]{minX, minY, maxX - 1L, maxY - 1L, (long) level};
        return ret;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GridSet)) {
            return false;
        } else {
            GridSet other = (GridSet) obj;
            if (this == other) {
                return true;
            } else {
                boolean equals = Objects.equals(this.getSrs(), other.getSrs()) && Objects.equals(this.getName(), other.getName()) && Objects.equals(this.getDescription(), other.getDescription()) && Objects.equals(this.getTileWidth(), other.getTileWidth()) && Objects.equals(this.getTileHeight(), other.getTileHeight()) && Objects.equals(this.isTopLeftAligned(), other.isTopLeftAligned()) && Objects.equals(this.isyCoordinateFirst(), other.isyCoordinateFirst()) && Objects.equals(this.getOriginalExtent(), other.getOriginalExtent()) && Arrays.equals(this.gridLevels, other.gridLevels);
                return equals;
            }
        }
    }

    public int hashCode() {
        int hashCode = HashCodeBuilder.reflectionHashCode(this, new String[0]);
        return hashCode;
    }

    public BoundingBox getBounds() {
        int i;
        long tilesWide;
        long tilesHigh;
        for (i = this.getNumLevels() - 1; i > 0; --i) {
            tilesWide = this.getGrid(i).getNumTilesWide();
            tilesHigh = this.getGrid(i).getNumTilesHigh();
            if (tilesWide == 1L && tilesHigh == 0L) {
                break;
            }
        }

        tilesWide = this.getGrid(i).getNumTilesWide();
        tilesHigh = this.getGrid(i).getNumTilesHigh();
        long[] ret = new long[]{0L, 0L, tilesWide - 1L, tilesHigh - 1L, (long) i};
        return this.boundsFromRectangle(ret);
    }

    public double[] getOrderedTopLeftCorner(int gridIndex) {
        double[] leftTop = new double[2];
        if (this.yBaseToggle) {
            leftTop[0] = this.tileOrigin()[0];
            leftTop[1] = this.tileOrigin()[1];
        } else {
            Grid grid = this.getGrid(gridIndex);
            double dTileHeight = (double) this.getTileHeight();
            double dGridExtent = (double) grid.getNumTilesHigh();
            double top = this.tileOrigin()[1] + dTileHeight * grid.getResolution() * dGridExtent;
            if (Math.abs(top - (double) Math.round(top)) < top / 200.0D) {
                top = (double) Math.round(top);
            }

            leftTop[0] = this.tileOrigin()[0];
            leftTop[1] = top;
        }

        if (this.isyCoordinateFirst()) {
            double[] ret = new double[]{leftTop[1], leftTop[0]};
            return ret;
        } else {
            return leftTop;
        }
    }

    public String guessMapUnits() {
        if (113000.0D > this.getMetersPerUnit() && this.getMetersPerUnit() > 110000.0D) {
            return "degrees";
        } else if (1100.0D > this.getMetersPerUnit() && this.getMetersPerUnit() > 900.0D) {
            return "kilometers";
        } else if (1.1D > this.getMetersPerUnit() && this.getMetersPerUnit() > 0.9D) {
            return "meters";
        } else if (0.4D > this.getMetersPerUnit() && this.getMetersPerUnit() > 0.28D) {
            return "feet";
        } else if (0.03D > this.getMetersPerUnit() && this.getMetersPerUnit() > 0.02D) {
            return "inches";
        } else if (0.02D > this.getMetersPerUnit() && this.getMetersPerUnit() > 0.005D) {
            return "centimeters";
        } else {
            return 0.002D > this.getMetersPerUnit() && this.getMetersPerUnit() > 5.0E-4D ? "millimeters" : "unknown";
        }
    }

    public boolean isTopLeftAligned() {
        return this.yBaseToggle;
    }

    void setTopLeftAligned(boolean yBaseToggle) {
        this.yBaseToggle = yBaseToggle;
    }

    public int getNumLevels() {
        return this.gridLevels.length;
    }

    public Grid getGrid(int zLevel) {
        return this.gridLevels[zLevel];
    }

    public void setGrid(int zLevel, Grid grid) {
        this.gridLevels[zLevel] = grid;
    }

    void setGridLevels(Grid[] gridLevels) {
        this.gridLevels = gridLevels;
    }

    public double[] tileOrigin() {
        BoundingBox extent = this.getOriginalExtent();
        double[] tileOrigin = new double[]{extent.getMinX(), this.yBaseToggle ? extent.getMaxY() : extent.getMinY()};
        return tileOrigin;
    }

    public boolean isyCoordinateFirst() {
        return this.yCoordinateFirst;
    }

    void setyCoordinateFirst(boolean yCoordinateFirst) {
        this.yCoordinateFirst = yCoordinateFirst;
    }

    public boolean isScaleWarning() {
        return this.scaleWarning;
    }

    void setScaleWarning(boolean scaleWarning) {
        this.scaleWarning = scaleWarning;
    }

    public double getMetersPerUnit() {
        return this.metersPerUnit;
    }

    void setMetersPerUnit(double metersPerUnit) {
        this.metersPerUnit = metersPerUnit;
    }

    public double getPixelSize() {
        return this.pixelSize;
    }

    void setPixelSize(double pixelSize) {
        this.pixelSize = pixelSize;
    }

    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SRS getSrs() {
        return this.srs;
    }

    void setSrs(SRS srs) {
        this.srs = srs;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public boolean shouldTruncateIfChanged(GridSet another) {
        boolean needsTruncate = !this.getBounds().equals(another.getBounds());
        needsTruncate |= this.isTopLeftAligned() != another.isTopLeftAligned();
        needsTruncate |= this.getTileWidth() != another.getTileWidth();
        needsTruncate |= this.getTileHeight() != another.getTileHeight();
        needsTruncate |= !this.getSrs().equals(another.getSrs());
        if (needsTruncate) {
            return true;
        } else if (this.getNumLevels() > another.getNumLevels()) {
            return true;
        } else {
            for (int i = 0; i < this.getNumLevels(); ++i) {
                if (!this.getGrid(i).equals(another.getGrid(i))) {
                    return true;
                }
            }

            return false;
        }
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
