package platform.core.camera.core;

import org.onvif.ver10.schema.PTZVector;
import platform.core.camera.core.components.CurrentView;
import platform.core.camera.core.components.PTZControlDomain;

public interface CameraStandardSpecificFunctions {

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
