package platform.behaviors;

import platform.camera.Camera;
import platform.camera.components.Vector2D;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjLocBounds;
import platform.utilities.LoopTimer;
import platform.behaviors.impl.SearchForCrashStateMachine;

public abstract class ContMotionController extends MotionController {



    protected void calculatePTZVectorToMoveToBottom(MultiCameraGoal multiCameraGoal, Camera camera, Vector2D pt, ObjLocBounds objectLocation, SearchForCrashStateMachine stateMachine, String objectName) {

        float xVariation = 0.1F;
        float yVariation = 0.075F;

        //make a loop speed controller
        LoopTimer timer = stateMachine.getLoopTimer();

        if (objectLocation != null) {

            if (timer.checkPulse()) {

                float moveY = 0.875F - objectLocation.getYMin();
                float moveX = 0.5F - objectLocation.getX_centroid();

                if (Math.abs(moveY) < yVariation && Math.abs(moveX) < xVariation) {

                    multiCameraGoal.getProcessedInfoMap().get(camera).put(objectName, new Boolean(true));
                }
                else {

                    if (Math.abs(moveY) > yVariation) {
                        setPTinY(pt, moveY, camera);
                    }

                    if (Math.abs(moveX) > xVariation) {
                        setPTinX(pt, moveX, camera);
                    }
                }
            }
        }
    }

    protected void calculatePTZVectorToMoveToTop(MultiCameraGoal multiCameraGoal, Camera camera, Vector2D pt, ObjLocBounds objectLocation, SearchForCrashStateMachine stateMachine, String objectName) {

        //make a loop speed controller
        LoopTimer timer = stateMachine.getLoopTimer();

        float xVariation = 0.1F;
        float yVariation = 0.075F;

        if (objectLocation != null) {

            if (timer.checkPulse()) {

                float moveY = 0.125F - objectLocation.getYMax();
                float moveX = 0.5F - objectLocation.getX_centroid();

                if (Math.abs(moveY) < yVariation && Math.abs(moveX) < xVariation) {

                    multiCameraGoal.getProcessedInfoMap().get(camera).put(objectName, new Boolean(true));

                } else {

                    if (Math.abs(moveY) > yVariation) {
                        setPTinY(pt, moveY, camera);
                    }

                    if (Math.abs(moveX) > xVariation) {
                        setPTinX(pt, moveX, camera);
                    }

                }

            }

        }

    }

    protected void calculatePTZVectorToMoveToCenter(MultiCameraGoal multiCameraGoal, Camera camera, Vector2D pt, ObjLocBounds objectLocation, SearchForCrashStateMachine stateMachine, String objectName) {

        //make a loop speed controller
        LoopTimer timer = stateMachine.getLoopTimer();

        if (objectLocation != null) {

            if (timer.checkPulse()) {

                float moveY = 0.5F - objectLocation.getY_centroid();
                float moveX = 0.5F - objectLocation.getX_centroid();

                if (Math.abs(moveY) < 0.1 && Math.abs(moveX) < 0.1) {

                    multiCameraGoal.getProcessedInfoMap().get(camera).put(objectName, new Boolean(true));


                } else {

                    multiCameraGoal.getProcessedInfoMap().get(camera).put("blueObjectLocated", new Boolean(false));

                    if (Math.abs(moveY) > 0.1) {
                        //add y movement to command
                        setPTinY(pt,moveY, camera);
                    }

                    if (Math.abs(moveX) > 0.1) {
                        //add x movement to command
                        setPTinX(pt,moveX, camera);
                    }

                }

            }
        }

    }


    protected void setPTinX(Vector2D pt, float moveX, Camera camera) {

        pt.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);

    }

    protected void setPTinY(Vector2D pt, float moveY, Camera camera) {

        if (moveY > 0){
            //up
            pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());
        }
        else {
            //down
            pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());
        }

    }

}
