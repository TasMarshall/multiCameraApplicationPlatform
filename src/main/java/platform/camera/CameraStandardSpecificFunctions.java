package platform.camera;

import platform.camera.components.CurrentView;
import platform.camera.components.PTZControlDomain;
import platform.camera.components.PTZVector;

public interface CameraStandardSpecificFunctions {

    public abstract boolean reconnectToCamera();

    public abstract void canConnectAndSimpleInit();

    public abstract boolean connectToCamera() ;

    public abstract String getCameraUniqueIdentifier();

    public abstract PTZControlDomain acquireCameraPTZCapabilities();

    public abstract CurrentView getCameraCurrentView();

    public abstract boolean commandPTZByIMGTest();

    public abstract boolean simpleUnsecuredFunctionTest();

    public abstract boolean simpleSecuredFunctionTest();

    public abstract boolean videoSimpleFunctionTest();

    public abstract boolean videoFunctionTest();

    public abstract boolean pvtSimpleMotionFunctionTest() ;

    public abstract boolean pvtMotionFunctionTest();

    public abstract boolean commandPTZMovement(PTZVector ptzVector);

    public boolean commandPTZStop();

}
