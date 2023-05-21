package org.fuyi_atlas.map.core.grid;

import org.geotools.util.logging.Logging;
import org.springframework.util.Assert;

import java.util.logging.Logger;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:21
 * @since: 1.0
 **/
public class GridSetFactory {
    private static Logger log = Logging.getLogger(GridSetFactory.class.getName());
    public static final double DEFAULT_PIXEL_SIZE_METER = 2.8E-4D;
    public static int DEFAULT_LEVELS = 22;
    public static final double EPSG4326_TO_METERS = 111319.49079327358D;
    public static final double EPSG3857_TO_METERS = 1.0D;

    public GridSetFactory() {
    }

    private static GridSet baseGridSet(String name, SRS srs, int tileWidth, int tileHeight) {
        GridSet gridSet = new GridSet();
        gridSet.setName(name);
        gridSet.setSrs(srs);
        gridSet.setTileWidth(tileWidth);
        gridSet.setTileHeight(tileHeight);
        return gridSet;
    }

    public static GridSet createGridSet(String name, SRS srs, BoundingBox extent, boolean alignTopLeft, double[] resolutions, double[] scaleDenoms, Double metersPerUnit, double pixelSize, String[] scaleNames, int tileWidth, int tileHeight, boolean yCoordinateFirst) {
        Assert.notNull(name, "name is null");
        Assert.notNull(srs, "srs is null");
        Assert.notNull(extent, "extent is null");
        Assert.isTrue(!extent.isNull() && extent.isSane(), "Extent is invalid: " + extent);
        Assert.isTrue(resolutions != null || scaleDenoms != null, "The gridset definition must have either resolutions or scale denominators");
        Assert.isTrue(resolutions == null || scaleDenoms == null, "Only one of resolutions or scaleDenoms should be provided, not both");

        int i;
        for (i = 1; resolutions != null && i < resolutions.length; ++i) {
            if (resolutions[i] >= resolutions[i - 1]) {
                throw new IllegalArgumentException("Each resolution should be lower than it's prior one. Res[" + i + "] == " + resolutions[i] + ", Res[" + (i - 1) + "] == " + resolutions[i - 1] + ".");
            }
        }

        for (i = 1; scaleDenoms != null && i < scaleDenoms.length; ++i) {
            if (scaleDenoms[i] >= scaleDenoms[i - 1]) {
                throw new IllegalArgumentException("Each scale denominator should be lower than it's prior one. Scale[" + i + "] == " + scaleDenoms[i] + ", Scale[" + (i - 1) + "] == " + scaleDenoms[i - 1] + ".");
            }
        }

        GridSet gridSet = baseGridSet(name, srs, tileWidth, tileHeight);
        gridSet.setResolutionsPreserved(resolutions != null);
        gridSet.setPixelSize(pixelSize);
        gridSet.setOriginalExtent(extent);
        gridSet.yBaseToggle = alignTopLeft;
        gridSet.setyCoordinateFirst(yCoordinateFirst);
        if (metersPerUnit == null) {
            if (srs.equals(SRS.getEPSG4326())) {
                gridSet.setMetersPerUnit(111319.49079327358D);
            } else if (srs.equals(SRS.getEPSG3857())) {
                gridSet.setMetersPerUnit(1.0D);
            } else {
                if (resolutions == null) {
                    log.config("GridSet " + name + " was defined without metersPerUnit, assuming 1m/unit. All scales will be off if this is incorrect.");
                } else {
                    log.config("GridSet " + name + " was defined without metersPerUnit. Assuming 1m per SRS unit for WMTS scale output.");
                    gridSet.setScaleWarning(true);
                }

                gridSet.setMetersPerUnit(1.0D);
            }
        } else {
            gridSet.setMetersPerUnit(metersPerUnit);
        }

        if (resolutions == null) {
            gridSet.setGridLevels(new Grid[scaleDenoms.length]);
        } else {
            gridSet.setGridLevels(new Grid[resolutions.length]);
        }

        for (i = 0; i < gridSet.getNumLevels(); ++i) {
            Grid curGrid = new Grid();
            if (scaleDenoms != null) {
                curGrid.setScaleDenominator(scaleDenoms[i]);
                curGrid.setResolution(pixelSize * (scaleDenoms[i] / gridSet.getMetersPerUnit()));
            } else {
                curGrid.setResolution(resolutions[i]);
                curGrid.setScaleDenominator(resolutions[i] * gridSet.getMetersPerUnit() / 2.8E-4D);
            }

            double mapUnitWidth = (double) tileWidth * curGrid.getResolution();
            double mapUnitHeight = (double) tileHeight * curGrid.getResolution();
            long tilesWide = (long) Math.ceil((extent.getWidth() - mapUnitWidth * 0.01D) / mapUnitWidth);
            long tilesHigh = (long) Math.ceil((extent.getHeight() - mapUnitHeight * 0.01D) / mapUnitHeight);
            curGrid.setNumTilesWide(tilesWide);
            curGrid.setNumTilesHigh(tilesHigh);
            if (scaleNames != null && scaleNames[i] != null) {
                curGrid.setName(scaleNames[i]);
            } else {
                curGrid.setName(gridSet.getName() + ":" + i);
            }

            gridSet.setGrid(i, curGrid);
        }

        return gridSet;
    }

    public static GridSet createGridSet(String name, SRS srs, BoundingBox extent, boolean alignTopLeft, int levels, Double metersPerUnit, double pixelSize, int tileWidth, int tileHeight, boolean yCoordinateFirst) {
        double extentWidth = extent.getWidth();
        double extentHeight = extent.getHeight();
        double resX = extentWidth / (double) tileWidth;
        double resY = extentHeight / (double) tileHeight;
        int tilesWide;
        int tilesHigh;
        if (resX <= resY) {
            tilesWide = 1;
            tilesHigh = (int) Math.round(resY / resX);
            resY /= (double) tilesHigh;
        } else {
            tilesHigh = 1;
            tilesWide = (int) Math.round(resX / resY);
            resX /= (double) tilesWide;
        }

        double res = Math.max(resX, resY);
        double adjustedExtentWidth = (double) (tilesWide * tileWidth) * res;
        double adjustedExtentHeight = (double) (tilesHigh * tileHeight) * res;
        BoundingBox adjExtent = new BoundingBox(extent);
        adjExtent.setMaxX(adjExtent.getMinX() + adjustedExtentWidth);
        if (alignTopLeft) {
            adjExtent.setMinY(adjExtent.getMaxY() - adjustedExtentHeight);
        } else {
            adjExtent.setMaxY(adjExtent.getMinY() + adjustedExtentHeight);
        }

        double[] resolutions = new double[levels];
        resolutions[0] = res;

        for (int i = 1; i < levels; ++i) {
            resolutions[i] = resolutions[i - 1] / 2.0D;
        }

        return createGridSet(name, srs, adjExtent, alignTopLeft, resolutions, (double[]) null, metersPerUnit, pixelSize, (String[]) null, tileWidth, tileHeight, yCoordinateFirst);
    }
}
