package platform.core.goals.impl;

import com.sun.javafx.geom.Vec3d;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.BehaviourOfInterest;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.utilities.adaptation.impl.ActiveCamerasPerRegionOnly;
import platform.core.utilities.mapeLoop;

import java.util.ArrayList;
import java.util.List;

public class EvenCameraCoverageSim extends MultiCameraGoal implements mapeLoop {

    //Add this goal to an mcp_application and it will look set a single region of interest, find what cameras are within a bound of it
    //and add itself to those cameras. Those cameras can then access this goals targets if the given camera is in a position to do so.
    //If they are, they add them selves to the active camera list of the MultiCameraGoal class.

    public EvenCameraCoverageSim(int priority, List<RegionOfInterest> regionsOfInterest, List<ObjectOfInterest> objectsOfInterest, List<BehaviourOfInterest> behavioursOfInterest) {
        super(priority, regionsOfInterest, objectsOfInterest, behavioursOfInterest, 1);

        for (RegionOfInterest regionOfInterest: regionsOfInterest){
            ActiveCamerasPerRegionOnly activeCamerasPerRegionOnly = new ActiveCamerasPerRegionOnly(this,regionOfInterest);
            adaptationMap.put(regionOfInterest + "activeAdaptor",activeCamerasPerRegionOnly);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       MAPE LOOP                                 /////
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void monitor() {
        super.monitor();
    }

    @Override
    public void analyse() {
        super.analyse();

    }

    @Override
    public void plan() {

        super.plan();

        //re-optimize
        for (RegionOfInterest regionOfInterest: getRegionsOfInterest()) {

            adaptationMap.get(regionOfInterest + "activeAdaptor").directAdaptation();
            List<Camera> adaptedCameraList = (List<Camera>) adaptationMap.get(regionOfInterest + "activeAdaptor").getAdaptiveData();

            optimizeCoverage(adaptedCameraList,regionOfInterest);

        }

    }

    private void optimizeCoverage(List<Camera> cameras, RegionOfInterest regionOfInterest) {
        calculateEvenCameraTargets(cameras.size(),regionOfInterest,cameras);
    }

    @Override
    public void execute() {
        super.execute();
        //optimize

    }

    protected void calculateEvenCameraTargets(int numberOfCameras, RegionOfInterest regionOfInterest, List<Camera> cameras) {

        List<Vec3d> targets = new ArrayList<>();

        double dLong = regionOfInterest.getArea().getLongDiff();
        double dLat = regionOfInterest.getArea().getLatDiff();

        double spaceVal = dLong / numberOfCameras;

        Double smallestLong = Double.POSITIVE_INFINITY;
        Camera sCamera = null;// = getActiveCameras().get(0);
        List<Camera> assignedCameras = new ArrayList<>();

        for (int i = 0; i< numberOfCameras; i++){
            Vec3d vec3d = new Vec3d(regionOfInterest.getArea().getLongMin() + spaceVal/2 + i*spaceVal, regionOfInterest.getArea().getLatMin() + dLat/2, 0);
            targets.add(vec3d);

            for (Camera camera : cameras) {
                if (!assignedCameras.contains(camera)) {
                    if (camera.getLocation().getLongitude() < smallestLong) {
                        smallestLong = camera.getLocation().getLongitude();
                        sCamera = camera;
                    }
                }
            }

            sCamera.getTargetView().setTargetLatLon(vec3d.y,vec3d.x);
            assignedCameras.add(sCamera);
            smallestLong = Double.POSITIVE_INFINITY;

        }
    }

}
