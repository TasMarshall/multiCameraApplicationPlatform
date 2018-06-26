package platform.core.utilities;

import java.util.Date;

public class GoalChangeEvent extends Event {
    public GoalChangeEvent(String eventDescription, Date date) {
        super(eventDescription, date);
    }
}
