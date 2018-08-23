package platform.utilities;

public class ComponentState {

    public enum State {
        INITIALIZING,
        CALIBRATING,
        OPERATING,
        RECOVERING,
        OFF
    }

    private State state;

    public ComponentState(){
        state = State.INITIALIZING;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
