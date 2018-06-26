package platform.core.cameraMonitor.core.exceptions;


import platform.core.camera.core.Camera;

public class AccessCredentialNotSetException extends Exception {

    Camera camera;

    public AccessCredentialNotSetException(Camera camera){
        super("Error - Camera " + camera.getIdAsString() + " has an undefined username or password, secure function test skipped.");

        //TODO Add notifier
        //TODO Add logging
    }

}
