package platform.core.map;


import static platform.MapView.distanceInLatLong;

public class MapManager {

    public enum MapUnit {
        METRES,
    }

    double latAnchor;
    double lonAnchor;

    LocalMap localMap;
    GlobalMap globalMap;

    public MapManager(double lonAnchor, double latAnchor, LocalMap localMap, MapUnit mapUnit){

        this.lonAnchor = lonAnchor;
        this.latAnchor = latAnchor;

        this.localMap = localMap;

        if (mapUnit == MapUnit.METRES){
            globalMap = convertMetresToGlobal(localMap);
        }

    }

    private GlobalMap convertMetresToGlobal(LocalMap localMap) {

        double[] lats = new double[localMap.getX().length];
        double[] lons = new double[localMap.getX().length];

        for (int i = 0; i < localMap.getX().length; i ++){
            lons[i] = distanceInLatLong(localMap.getX()[i],latAnchor,lonAnchor,90)[1] + lonAnchor;
            lats[i] = distanceInLatLong(localMap.getY()[i],latAnchor,lonAnchor,0)[0] + latAnchor;
        }

        return new GlobalMap(lons,lats);
    }
}
