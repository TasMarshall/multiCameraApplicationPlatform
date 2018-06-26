package platform.core.goals.impl;

import platform.core.camera.core.Camera;
import platform.core.goals.core.components.BehaviourOfInterest;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;

import java.util.ArrayList;
import java.util.List;

public class EvenCameraCoverageSimAdapted extends EvenCameraCoverageSim {

    public EvenCameraCoverageSimAdapted(int priority, List<RegionOfInterest> regionsOfInterest, List<ObjectOfInterest> objectsOfInterest, List<BehaviourOfInterest> behavioursOfInterest) {
        super(priority, regionsOfInterest, objectsOfInterest, behavioursOfInterest);
    }

    @Override
    public void plan() {

        //re-optimize
        for (RegionOfInterest regionOfInterest: getRegionsOfInterest()) {
            optimizeCoverage(getActiveCamerasPerRegion().get(regionOfInterest),getActiveCamerasPerRegion().get(regionOfInterest).size(),regionOfInterest);
        }


    }

    public void optimizeCoverage(List<Camera> cameras, int size, RegionOfInterest regionOfInterest) {

        List<Camera> workingCameras = new ArrayList<>();

        for (Camera camera : cameras){
            if (camera.isWorking()){
                workingCameras.add(camera);
            }
        }

        calculateEvenCameraTargets(workingCameras.size(),regionOfInterest, workingCameras);
    }

}
