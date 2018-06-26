package platform.core.camera.impl;

import com.sun.javafx.geom.Vec3d;
import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.PTZVector;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraLocation;
import platform.core.camera.core.components.CurrentView;
import platform.core.camera.core.components.PTZControlDomain;
import platform.core.camera.core.components.ViewCapabilities;
import platform.core.cameraMonitor.core.exceptions.AccessCredentialNotSetException;
import platform.core.goals.core.MultiCameraGoal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SimulatedCamera extends Camera {

    private static final Map<String,Object> additionalAttributes = new HashMap<>();
    static
    {
        additionalAttributes.put("streamProt",new String(""));
        additionalAttributes.put("streamExt",new String(""));
        additionalAttributes.put("resolution",new String(""));
        additionalAttributes.put("range",new Double("50"));
    }


    public SimulatedCamera(String modelID, Vec3d globalVector, CameraLocation location, List<MultiCameraGoal> multiCameraGoalList) throws MalformedURLException {
        super(modelID, new URL("http://000.000.0.00:00000/sim"), "admin", "", new ViewCapabilities(62, Arrays.asList(ViewCapabilities.PTZ.P, ViewCapabilities.PTZ.T), ViewCapabilities.PTZControl.CONT,327.5F,32.5F,45,-45,0,0), globalVector, location, multiCameraGoalList,additionalAttributes);
    }

    @Override
    public boolean connectToCamera() {

        System.out.println("Connection to simulated camera successful!");

        return true;

    }

    @Override
    public String getCameraUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    public PTZControlDomain acquireCameraPTZCapabilities() {

        FloatRange floatRange = new FloatRange();
        floatRange.setMin((float)-1.0);
        floatRange.setMax((float)1.0);

        PTZControlDomain ptzCapabilities = new PTZControlDomain(floatRange,floatRange,floatRange);

        return ptzCapabilities;

    }

    @Override
    public CurrentView getCameraCurrentView() {
        return null;
    }

    @Override
    public boolean commandPTZByIMGTest() {
        return false;
    }

    @Override
    public boolean commandPTZMovement(PTZVector ptzVector) {
        return false;
    }

    @Override
    public boolean commandPTZStop() {
        return true;
    }

    @Override
    public boolean simpleUnsecuredFunctionTest()  {

        System.out.println("Date Function Test Result: Simulated Camera " + System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean simpleSecuredFunctionTest()  {

        if (getUsername() == null || getPassword() == null) try {
            throw new AccessCredentialNotSetException(this);
        } catch (AccessCredentialNotSetException e) {
            System.out.println(e.getMessage());
        }

       /* List<Profile> profiles = onvifDevice.getDevices().getProfiles();
        for (Profile p : profiles) {
            System.out.println("URL von Profil \'" + p.getName() + "\': " + onvifDevice.getMedia().getSnapshotUri(p.getToken()));
        }*/

        return true;
    }

    @Override
    public boolean videoSimpleFunctionTest() {
        return true;
    }

    @Override
    public boolean videoFunctionTest()  {
        return true;
    }

    @Override
    public boolean pvtSimpleMotionFunctionTest() {
        return true;
    }

    @Override
    public boolean pvtMotionFunctionTest() {
        return true;
    }

}
