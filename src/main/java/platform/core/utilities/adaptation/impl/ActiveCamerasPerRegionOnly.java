package platform.core.utilities.adaptation.impl;

import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.utilities.adaptation.core.Adaptation;

import java.util.ArrayList;
import java.util.List;

public class ActiveCamerasPerRegionOnly extends Adaptation {

    public ActiveCamerasPerRegionOnly(MultiCameraGoal multiCameraGoal, RegionOfInterest regionOfInterest) {
        super(new ArrayList<Camera>(){} );
        additionalAttribues.put("mcg",multiCameraGoal);
        additionalAttribues.put("roi",regionOfInterest);
    }

    @Override
    protected boolean triggerAdaptation() {
        return false;
    }

    @Override
    protected void adapt() {

        List<Camera> workingCameras = new ArrayList<>();

        for (Camera camera : (List<Camera>) getData()){
            if (camera.isWorking()){
                workingCameras.add(camera);
            }
        }

        setData(workingCameras);

    }

    @Override
    protected Object getData() {
        return null;//((MultiCameraGoal)additionalAttribues.get("mcg")).getActiveCamerasPerRegion().get(additionalAttribues.get("roi"));
    }

    @Override
    protected void setData(Object object) {
        adaptiveData = object;
    }
}
