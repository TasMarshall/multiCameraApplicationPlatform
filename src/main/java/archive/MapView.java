package archive;

import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import platform.MultiCameraCore;
import platform.MultiCameraCore_Configuration;
import platform.camera.Camera;
import platform.camera.components.ViewCapabilities;
import platform.camera.impl.SimulatedCamera;
import platform.goals.MultiCameraGoal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapView {

    MultiCameraCore mcp_application;

    private GraphicsOverlay staticGraphicsOverlay;
    private GraphicsOverlay dynamicGraphicsOverlay;
    private Map<String, Graphic> dynamicGraphicMapGUI;
    private SpatialReference spatialReference;

    private com.esri.arcgisruntime.mapping.view.MapView mapView;

    // colors for symbols
    private static final int PURPLE = 0xFF800080;
    private static final int BLUE = 0xFF0000FF;
    private static final int RED = 0xFFFF0000;
    private static final int GREEN = 0xFF00FF00;
    private static final int DARKGREEN = 0xFF008000;
    private static final int CYAN = 0xFF008000;

    public MapView(MultiCameraCore mcp_application){
        initMap(mcp_application);
        drawStaticOverlayOnMap();
        initDynamicOverlayOnMap(false);
    }

    public MapView(MultiCameraCore_Configuration mcp_application_configuration) {
    }

    private void initMap(MultiCameraCore mcp_application) {

        this.mcp_application = mcp_application;

        double viewRangeLong = mcp_application.getGlobalMap().getLongMax() - mcp_application.getGlobalMap().getLongMin();
        double viewRangeLat = mcp_application.getGlobalMap().getLatMax() - mcp_application.getGlobalMap().getLatMin();

        double largerViewRange;

        if (viewRangeLat > viewRangeLong){ largerViewRange = viewRangeLat; }
        else { largerViewRange =  viewRangeLong; }

        int levelOfDetail = calculateIdealLevelOfDetail(largerViewRange);

        double initLong = (viewRangeLong) / 2+ mcp_application.getGlobalMap().getLongMin();
        double initLat = (viewRangeLat) / 2 + mcp_application.getGlobalMap().getLatMin();

        // create a ArcGISMap with the a Basemap instance with an Imagery base layer
        ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS,initLat,initLong, levelOfDetail);
        map.setMaxScale(800);
        map.setMinScale(200000);

        // set the map to be displayed in this view
        mapView = new com.esri.arcgisruntime.mapping.view.MapView();
        mapView.setMap(map);

        this.dynamicGraphicsOverlay = new GraphicsOverlay();

    }

    private void drawStaticOverlayOnMap() {

        this.staticGraphicsOverlay = new GraphicsOverlay();

        // create a graphics overlay
        SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();

        drawROIOnMap(mcp_application.getGlobalMap(),SPATIAL_REFERENCE);

        for(MultiCameraGoal multiCameraGoal: mcp_application.getMultiCameraGoals()) {

            drawROIOnMap(multiCameraGoal.getMap(), SPATIAL_REFERENCE);

        }

        // add graphics overlay to the map view
        mapView.getGraphicsOverlays().add(staticGraphicsOverlay);

    }

    public void initDynamicOverlayOnMap(boolean updateNotInit) {

        if (!updateNotInit) {
            if (mapView.getGraphicsOverlays().contains(dynamicGraphicsOverlay))
                mapView.getGraphicsOverlays().remove(dynamicGraphicsOverlay);

            dynamicGraphicMapGUI = new HashMap<String,Graphic>();
        }


        // create a graphics overlay
        spatialReference = SpatialReferences.getWgs84();

        for (Camera camera: mcp_application.getAllCameras()){
            if (camera.isWorking()) {
                if ( camera instanceof SimulatedCamera){
                    drawCameraOnMap(camera, CYAN, updateNotInit);
                }
                else {
                    drawCameraOnMap(camera, DARKGREEN, updateNotInit);
                }
            }
            else {drawCameraOnMap(camera, RED, updateNotInit);}
        }

        // add graphics overlay to the map view
        if (!updateNotInit) {
            mapView.getGraphicsOverlays().add(dynamicGraphicsOverlay);
        }

    }

    private void drawCameraOnMap(Camera camera, int color, boolean updateNotInit) {

        drawCameraMarker(camera,color,updateNotInit);

        drawCameraRange(camera,updateNotInit);

        if (camera.getTargetView() != null) {
            drawTargetGraphic(camera, updateNotInit);

            //todo: get target points for ellipse
            createViewGeometry(camera,spatialReference, updateNotInit);
        }


        //add camera to the map of what is on the dynamic graphic overlay
        //dynamicGraphicMapGUI.put(camera.getIdAsString(),cameraGraphic);

    }

    private void drawCameraRange(Camera camera, boolean updateNotInit) {
        //if PTZ or PT draw range circle todo: add zoom effect based on zoom level
        if (camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.P) || camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.T) || camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.Z)) {

            if (updateNotInit){
                //do nothing...

                /*Polygon circlePolygon = createCircleGeometry(camera.getLocation().getLongitude(), camera.getLocation().getLatitude(), distanceInLatLong(camera.getEffectiveRange(),camera.getLocation().getLongitude(), camera.getLocation().getLatitude(),0)[0], spatialReference);
                Graphic rangeGraphic = dynamicGraphicMapGUI.get(camera.getIdAsString() + "range");
                rangeGraphic.setGeometry(circlePolygon);*/
            }
            else {
                //create the graphic
                SimpleLineSymbol polygonOutline = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
                        0xFF005000, 2);
                SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL,
                        0xFF005000, polygonOutline);
                Polygon circlePolygon = createCircleGeometry(camera.getLocation().getLongitude(), camera.getLocation().getLatitude(), distanceInLatLong((double)camera.getAdditionalAttributes().get("range"),camera.getLocation().getLongitude(), camera.getLocation().getLatitude(),0)[0], spatialReference);

                Graphic polygonGraphic = new Graphic(circlePolygon, fillSymbol);

                polygonGraphic.getAttributes().put("NAME", "camera-" + camera.getIdAsString() + "-range");
                //add the graphic to the graphics layer
                dynamicGraphicMapGUI.put(camera.getIdAsString() + "range", polygonGraphic);
                dynamicGraphicsOverlay.getGraphics().add(polygonGraphic);
            }
        }
    }

    private void drawTargetGraphic(Camera camera, boolean updateNotInit) {

        SimpleMarkerSymbol targetCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 	BLUE, 5);


            Point point = new Point(camera.getTargetView().getTargetLatLon()[1], camera.getTargetView().getTargetLatLon()[0], spatialReference);


        if (updateNotInit){
            Graphic targetGraphic = dynamicGraphicMapGUI.get(camera.getIdAsString()+"-target");
            targetGraphic.setGeometry(point);
        }
        else {
            Graphic targetGraphic = new Graphic(point, targetCircleSymbol);
            targetGraphic.getAttributes().put("NAME","camera-" + camera.getIdAsString() + "-target");
            dynamicGraphicsOverlay.getGraphics().add(targetGraphic);
            dynamicGraphicMapGUI.put(camera.getIdAsString() + "-target",targetGraphic);
        }
    }


    private void drawCameraMarker(Camera camera, int color, boolean updateNotInit) {

        SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 	color, 10);
        Point point = new Point(camera.getLocation().getLongitude(), camera.getLocation().getLatitude(), spatialReference);

        if (updateNotInit){
            Graphic cameraGraphic = dynamicGraphicMapGUI.get(camera.getIdAsString()+"marker");
            cameraGraphic.setGeometry(point);
            cameraGraphic.setSymbol(circleSymbol);
        }
        else {
            Graphic cameraGraphic = new Graphic(point, circleSymbol);
            dynamicGraphicsOverlay.getGraphics().add(cameraGraphic);
            cameraGraphic.getAttributes().put("NAME","camera-" + camera.getIdAsString() + "-marker");
            dynamicGraphicMapGUI.put(camera.getIdAsString() + "marker",cameraGraphic);
        }
    }

    private void drawROIOnMap(platform.map.Map map, SpatialReference spatial_reference) {

        PointCollection points = new PointCollection(spatial_reference);
        for (int i = 0; i <= map.getY().length; i++){
            if (i<map.getY().length) points.add(new Point(map.getX()[i],map.getY()[i]));
            else { points.add(new Point(map.getX()[0],map.getY()[0])); }
        }

        // create the polyline from the point collection


        Graphic graphic;

        if (map.getCoordinateSys() == platform.map.Map.CoordinateSys.INDOOR){
            Polygon polygon = new Polygon(points);
            // create a green (0xFF005000) simple line symbol
            SimpleLineSymbol outlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF005000, 1);
            SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, 0xFF005000,
                    outlineSymbol);
            graphic = new Graphic(polygon, fillSymbol);
        }
        else {
            Polyline polyline = new Polyline(points);
            // create a purple (0xFF800080) simple line symbol
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF800080, 1);
            // create the graphic with polyline and symbol
            graphic = new Graphic(polyline, lineSymbol);
        }

        // add graphic to the graphics overlay
        staticGraphicsOverlay.getGraphics().add(graphic);

    }

    private void drawParabolaOnMap(boolean updateNotInit, Camera camera, double a, double rotAngle0, double lat, double lon, double camLat, double camLon) {
        double rotAngle = rotAngle0*-1;

        double xLat = distanceInMetres(lat,lat+1,lon,lon,0,0);
        double xLong = distanceInMetres(lat,lat,lon,lon+1,0,0);
        double latLongDistRatio = xLat/xLong;

        PointCollection points1 = new PointCollection(spatialReference);
        PointCollection points2 = new PointCollection(spatialReference);

        for(int y = 0; y < 100 ; y += 5){

            double x = Math.sqrt (y/a);
            double x2 = x * -1;

            double[] xScaled = distanceInLatLong(x,lat,lon,90);
            double[] yScaled = distanceInLatLong(y,lat,lon,0);

            x = xScaled[1];
            x2 = xScaled[1]*-1;
            double yn = yScaled[0];

            double xRot = Math.cos(rotAngle) * x - Math.sin(rotAngle) * yn ;
            double x2Rot = Math.cos(rotAngle) * x2 - Math.sin(rotAngle) * yn ;
            double yRot = Math.sin(rotAngle) * x  + Math.cos(rotAngle) * yn ;
            double y2Rot = Math.sin(rotAngle) * x2  + Math.cos(rotAngle) * yn ;

            points1.add(new Point(xRot * latLongDistRatio + lon, yRot+lat));
            points2.add(new Point(x2Rot * latLongDistRatio  + lon, y2Rot+lat));

        }

        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF800080, 1);
        Polyline polyline = new Polyline(points1);

        SimpleLineSymbol lineSymbol2 = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF800080, 1);
        Polyline polyline2 = new Polyline(points2);

        PointCollection pc = new PointCollection(spatialReference);
        pc.add(camLon,camLat);
        pc.add(points1.get(0));
        Polyline polyline3 = new Polyline(pc);

        if (updateNotInit && (dynamicGraphicMapGUI.get(camera.getIdAsString() + "-view-1") != null)){

            Graphic graphic = dynamicGraphicMapGUI.get(camera.getIdAsString()+"-view-1");
            Graphic graphic2 = dynamicGraphicMapGUI.get(camera.getIdAsString()+"-view-2");
            Graphic graphic3 = dynamicGraphicMapGUI.get(camera.getIdAsString()+"-view-3");

            graphic.setGeometry(polyline);
            graphic2.setGeometry(polyline2);
            graphic3.setGeometry(polyline3);

        }
        else {

            if (dynamicGraphicMapGUI.get(camera.getIdAsString() + "-view") != null){
                dynamicGraphicsOverlay.getGraphics().remove(dynamicGraphicMapGUI.get(camera.getIdAsString()+"view"));
                dynamicGraphicMapGUI.remove(camera.getIdAsString() + "-view");
            }

            Graphic graphic = new Graphic(polyline, lineSymbol);
            Graphic graphic2 = new Graphic(polyline2, lineSymbol);
            Graphic graphic3 = new Graphic(polyline3, lineSymbol);

            graphic.getAttributes().put("NAME","camera-" + camera.getIdAsString() + "-view-1");
            graphic2.getAttributes().put("NAME","camera-" + camera.getIdAsString() + "-view-2");
            graphic3.getAttributes().put("NAME","camera-" + camera.getIdAsString() + "-view-3");

            dynamicGraphicMapGUI.put(camera.getIdAsString() + "-view-1",graphic);
            dynamicGraphicMapGUI.put(camera.getIdAsString() + "-view-2",graphic2);
            dynamicGraphicMapGUI.put(camera.getIdAsString() + "-view-3",graphic3);

            Collection<Graphic> graphics = new ArrayList<>();
            graphics.add(graphic);
            graphics.add(graphic2);
            graphics.add(graphic3);

            dynamicGraphicsOverlay.getGraphics().addAll(graphics);
        }

    }


    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distanceInMetres(double lat1, double lat2, double lon1,
                                          double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    //http://www.geomidpoint.com/destination/calculation.html
    public static double[] distanceInLatLong(double distanceInMetres, double lat,double lon, double bearing){

        //Convert the starting point latitude 'lat1' (in the range -90 to 90) to radians.
        double lat1 = lat * Math.PI/180;
        //Convert the starting point longitude 'lon1' (in the range -180 to 180) to radians.
        double lon1 = lon * Math.PI/180;

        final double R = 6371.0; // Radius of the earth

        //Given the distance 'dist' in miles or kilometers. Convert distance to the distance in radians.
        double distInRadians = (distanceInMetres/1000.0)/R;

        //Calculate the destination coordinates.
        double lat2 = Math.asin(Math.sin(lat1)*Math.cos(distInRadians) + Math.cos(lat1)*Math.sin(distInRadians)*Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing)*Math.sin(distInRadians)*Math.cos(lat1), Math.cos(distInRadians)-Math.sin(lat1)*Math.sin(lat2));

        double dLat = (lat2 - lat1)*180/Math.PI;
        double dLon = (lon2 -lon1)*180/Math.PI;

        return new double[]{(dLat),dLon};
    }

    public Polygon createCircleGeometry( double longi, double lat, double radius, SpatialReference spatial_reference) {

        PointCollection points = new PointCollection(spatial_reference);

        double xLat = distanceInMetres(lat,lat+1,longi,longi,0,0);
        double xLong = distanceInMetres(lat,lat,longi,longi+1,0,0);
        double latLongDistRatio = xLat/xLong;

        for ( int i = 0; i <= 360; i += 10) {
            double radian = i * (Math.PI / 180.0);
            double x = longi + latLongDistRatio*radius * Math.cos(radian);
            double y = lat + radius * Math.sin(radian);

            points.add(new Point(x,y));
        }

        Part part = new Part(points);

        Polygon polygon = new Polygon(part);

        return polygon;
    }

    public Polygon createViewGeometry(Camera camera, SpatialReference spatial_reference, boolean updateNotInit) {

        double camLat, camLon, camHei, tarLat, tarLon, tarHei;

        camLat = camera.getLocation().getLatitude();
        camLon = camera.getLocation().getLongitude();
        camHei = camera.getLocation().getHeight2Ground();

        tarLat = camera.getTargetView().getTargetLatLon()[0];
        tarLon = camera.getTargetView().getTargetLatLon()[1];
        tarHei = 0;

        double tarCamDiffLon = tarLon - camLon;
        double tarCamDiffLat = tarLat - camLat;

        double tarCamLen = Math.sqrt((tarLat-camLat)*(tarLat-camLat) + (tarLon-camLon)*(tarLon-camLon));
        double unitLen = 1;

        double dot1 = (tarLat-camLat)*1.0 + (tarLon-camLon)*0;

        double angleBetweenVectors = Math.acos((dot1/tarCamLen));

        double axisDirection;

        if ((tarLon-camLon)>0) {
            axisDirection = angleBetweenVectors;
        }
        else {
            axisDirection = 2*Math.PI - angleBetweenVectors;
        }

        double theta = Math.toRadians(camera.getViewCapabilities().getViewAngle()/2);
        double d1, d2, d3;

        d1 = distanceInMetres(camLat,tarLat,camLon,tarLon,camHei,tarHei);

        double theta2 = Math.acos((camHei-tarHei)/d1);

        d2 = camHei * Math.tan(theta2 - theta);

        if ((theta2 + theta)>Math.PI/2) {
 /*           d3 = d2*2;  //todo fix this
            majorAxis = d3 - d2;
            minorAxis =  d2 + majorAxis/2 * Math.tan(theta); //approx*/

            double[] paraOrigin = distanceInLatLong(d2, camLat,camLon,axisDirection );

            double opposite =  d1 * Math.sin(theta)/2;
            //find a by substituting valuues into y = a*x^2
            double a = ((d1-d2) / (opposite*opposite));

            drawParabolaOnMap(updateNotInit,camera,a,axisDirection,paraOrigin[0]+camLat,paraOrigin[1]+camLon, camLat,camLon);

        }
        else{


            double majorAxis;
            double minorAxis;

            d3 = camHei * Math.tan(theta2 + theta);
            majorAxis = d3 - d2;
            minorAxis =  d2 + majorAxis/2 * Math.tan(theta); //approx


            BigDecimal bd = new BigDecimal(majorAxis);
            bd = bd.setScale(0, RoundingMode.HALF_UP);

            int majorAxisLength = bd.intValue();

            BigDecimal bd2 = new BigDecimal(minorAxis);
            bd2 = bd2.setScale(0, RoundingMode.HALF_UP);

            int minorAxisLength = bd2.intValue();

            double[] distMod = distanceInLatLong((d2+majorAxisLength/2 - d1),camLat,camLon,axisDirection);

           /* double xLat = distanceInMetres(tarLat,tarLat+1,tarLon,tarLon,0,0);
            double xLong = distanceInMetres(tarLat,tarLat,tarLon,tarLon+1,0,0);
            double latLongDistRatio = xLat/xLong;*/

            Point point = new Point(tarLon+Math.signum(tarLon-camLon)*distMod[1], tarLat+Math.signum(tarLat-camLat)*distMod[0],spatialReference);

            LinearUnit unitOfMeasurement = new LinearUnit(LinearUnitId.METERS);
            AngularUnit angularUnit = new AngularUnit(AngularUnitId.RADIANS);

            Geometry ellipseGeometry = GeometryEngine.ellipseGeodesic(new GeodesicEllipseParameters(axisDirection, angularUnit,point, unitOfMeasurement, 1000, 100, GeometryType.POLYGON, majorAxisLength, minorAxis));
            SimpleLineSymbol polygonOutline = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
                    0xFF005000, 2);

            if (updateNotInit && (dynamicGraphicMapGUI.get(camera.getIdAsString() + "-view") != null)){
                Graphic graphic = dynamicGraphicMapGUI.get(camera.getIdAsString()+"-view");
                graphic.setGeometry(ellipseGeometry);
                graphic.setSymbol(polygonOutline);
            }
            else {

                if (dynamicGraphicMapGUI.get(camera.getIdAsString() + "-view-1") != null) {
                    dynamicGraphicsOverlay.getGraphics().remove(camera.getIdAsString() + "-view-1");
                    dynamicGraphicsOverlay.getGraphics().remove(camera.getIdAsString() + "-view-2");
                    dynamicGraphicsOverlay.getGraphics().remove(camera.getIdAsString() + "-view-3");
                    dynamicGraphicMapGUI.remove(camera.getIdAsString() + "-view-1");
                    dynamicGraphicMapGUI.remove(camera.getIdAsString() + "-view-2");
                    dynamicGraphicMapGUI.remove(camera.getIdAsString() + "-view-3");
                }

                Graphic graphic = new Graphic(ellipseGeometry,polygonOutline);
                graphic.getAttributes().put("NAME","camera-" + camera.getIdAsString() + "-view");
                dynamicGraphicsOverlay.getGraphics().add(graphic);
                dynamicGraphicMapGUI.put(camera.getIdAsString() + "-view",graphic);
            }
        }

        return null;

    }

    private int calculateIdealLevelOfDetail(double largerViewRange) {

        double distance = distanceInMetres(0,0,largerViewRange,0,0,0);
        double idealScale = distance / 0.2;

        int levelOfDetail = 15;

        if (idealScale < 4513){
            levelOfDetail = 17;
        }
        if (idealScale < 2256){
            levelOfDetail = 18;
        }
        if (idealScale < 1129){
            levelOfDetail = 19;
        }
        return levelOfDetail;

    }

    private void updateDynamicOverlayOnMap() {

        /*SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();

        if (mcp_application.localONVIFCameraMonitor != null && mcp_application.localONVIFCameraMonitor.getCameras().size() > 0) {
            for (Camera camera : mcp_application.localONVIFCameraMonitor.getCameras()) {
                //if the camera has changed then remove from maps and overlay, then add again.
                if (dynamicGraphicMapGUI.get(camera.getIdAsString()).getGeometry().getExtent().getCenter().getX() == camera.getLocation().getLongitude() &&
                        dynamicGraphicMapGUI.get(camera.getIdAsString()).getGeometry().getExtent().getCenter().getY() == camera.getLocation().getLatitude()) {
                    dynamicGraphicsOverlay.getGraphics().remove(dynamicGraphicMapGUI.get(camera.getIdAsString()));
                    dynamicGraphicMapGUI.remove(camera.getIdAsString());
                    //if online print normal color else print red
                    if (camera.isWorking()) { drawCameraOnMap(camera, SPATIAL_REFERENCE,0xFF008000);}
                    else {drawCameraOnMap(camera, SPATIAL_REFERENCE,0xFFFF0000);}
                }
            }
        }

        if (mcp_application.simulatedCameraMonitor != null && mcp_application.simulatedCameraMonitor.getCameras().size() > 0) {
            for (Camera camera : mcp_application.simulatedCameraMonitor.getCameras()) {


                //if the camera has changed then remove from maps and overlay, then add again.
                if (dynamicGraphicMapGUI.get(camera.getIdAsString()).getGeometry().getExtent().getCenter().getX() == camera.getLocation().getLongitude() &&
                        dynamicGraphicMapGUI.get(camera.getIdAsString()).getGeometry().getExtent().getCenter().getY() == camera.getLocation().getLatitude()) {
                    dynamicGraphicsOverlay.getGraphics().remove(dynamicGraphicMapGUI.get(camera.getIdAsString()));
                    dynamicGraphicMapGUI.remove(camera.getIdAsString());
                    //if online print normal color else print red
                    if (camera.isWorking()) {
                        drawCameraOnMap(camera, SPATIAL_REFERENCE, 0xFF008080);
                    } else {
                        drawCameraOnMap(camera, SPATIAL_REFERENCE, 0xFFFF0000);
                    }
                }
            }
        }*/

        //////////////////////////////////////////////////////
        /////////////////   ADD POLYGON    ///////////////////
        //////////////////////////////////////////////////////

/*        // create a new point collection for polygon
        PointCollection points1 = new PointCollection(SPATIAL_REFERENCE);

        // create and add points to the point collection
        points1.add(new Point(-2.6425, 56.0784));
        points1.add(new Point(-2.6430, 56.0763));
        points1.add(new Point(-2.6410, 56.0759));
        points1.add(new Point(-2.6380, 56.0765));
        points1.add(new Point(-2.6380, 56.0784));
        points1.add(new Point(-2.6410, 56.0786));


        // create the polyline from the point collection
        Polygon polygon = new Polygon(points1);

        // create a green (0xFF005000) simple line symbol
        SimpleLineSymbol outlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF005000, 1);
        // create a green (0xFF005000) mesh simple fill symbol
        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, 0xFF005000,
                outlineSymbol);

        // create the graphic with polyline and symbol
        Graphic graphic1 = new Graphic(polygon, fillSymbol);

        // add graphic to the graphics overlay
        dynamicGraphicsOverlay.getGraphics().add(graphic1);*/

    }

    public com.esri.arcgisruntime.mapping.view.MapView getMapView() {
        return mapView;
    }

    public void updateDynamicMapOverlay() {

        initDynamicOverlayOnMap(true);

    }
}


/**//*
//////////////////////////////////////////////////////
    /////////////////   ADD GRAPHIC  /////////////////////
    //////////////////////////////////////////////////////

    // create a graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();

    // create a red (0xFFFF0000) circle simple marker symbol
    SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 10);
    SimpleMarkerSymbol redCircleSymbol2 = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 2);

    List<Graphic> graphics = new ArrayList<Graphic>();

  */
/*      for (Camera camera: mcp_application.simulatedCameraMonitor.getCameras()){

            Point point = new Point(camera.getLocation().getLongitude(), camera.getLocation().getLatitude(), SPATIAL_REFERENCE);
            // create graphics and add to graphics overlay
            Graphic cameraGraphic = new Graphic(point, redCircleSymbol);

            graphics.add(cameraGraphic);

        }*//*


        graphicsOverlay.getGraphics().addAll(graphics);

                // add graphics overlay to the map view
                mapView.getGraphicsOverlays().add(graphicsOverlay);


                //////////////////////////////////////////////////////
                /////////////////   ADD LINES    /////////////////////
                //////////////////////////////////////////////////////

                // create a new point collection for polyline
                PointCollection points = new PointCollection(SPATIAL_REFERENCE);


// create and add points to the point collection
                points.add(new Point(-2.715, 56.061));
                points.add(new Point(-2.6438, 56.079));
                points.add(new Point(-2.638, 56.079));
                points.add(new Point(-2.636, 56.078));
                points.add(new Point(-2.636, 56.077));
                points.add(new Point(-2.637, 56.076));
                points.add(new Point(-2.715, 56.061));


// create the polyline from the point collection
                Polyline polyline = new Polyline(points);

                // create a purple (0xFF800080) simple line symbol
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF800080, 4);

                // create the graphic with polyline and symbol
                Graphic graphic = new Graphic(polyline, lineSymbol);

                // add graphic to the graphics overlay
                graphicsOverlay.getGraphics().add(graphic);

                //////////////////////////////////////////////////////
                /////////////////   ADD LINES    /////////////////////
                //////////////////////////////////////////////////////

                // create a new point collection for polygon
                PointCollection points1 = new PointCollection(SPATIAL_REFERENCE);

// create and add points to the point collection
                points1.add(new Point(-2.6425, 56.0784));
                points1.add(new Point(-2.6430, 56.0763));
                points1.add(new Point(-2.6410, 56.0759));
                points1.add(new Point(-2.6380, 56.0765));
                points1.add(new Point(-2.6380, 56.0784));
                points1.add(new Point(-2.6410, 56.0786));


// create the polyline from the point collection
                Polygon polygon = new Polygon(points1);

                // create a green (0xFF005000) simple line symbol
                SimpleLineSymbol outlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF005000, 1);
// create a green (0xFF005000) mesh simple fill symbol
                SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, 0xFF005000,
                outlineSymbol);

                // create the graphic with polyline and symbol
                Graphic graphic1 = new Graphic(polygon, fillSymbol);

                // add graphic to the graphics overlay
                graphicsOverlay.getGraphics().add(graphic1);

                /////////////////////////////////////////////////////////
                /////////////// CHANGE GRAPHIC ////////////////////////
                //////////////////////////////////////////////////////
                graphicsOverlay.getGraphics().get(0).setSymbol(redCircleSymbol2);

// add the map view to stack pane
                stackPane.getChildren().addAll(mapView);*/
