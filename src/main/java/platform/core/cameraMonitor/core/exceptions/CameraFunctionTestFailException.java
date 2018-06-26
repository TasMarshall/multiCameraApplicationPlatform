package platform.core.cameraMonitor.core.exceptions;


import platform.core.camera.core.Camera;

public class CameraFunctionTestFailException extends Exception{

    public CameraFunctionTestFailException(Camera camera, String message){
        super("Camera: " + camera.getIdAsString() + " failed a " +message +" function test.");

        //TODO Add notifier
        //TODO Add logging

    }

}
