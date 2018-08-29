package platform.camera.components;

public class Vector2D {

    protected float x;
    protected float y;
    protected String space;

    public Vector2D(){

    }

    public Vector2D(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float value) {
        this.x = value;
    }

    public float getY() {
        return y;
    }

    public void setY(float value) {
        this.y = value;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String value) {
        this.space = value;
    }

}




