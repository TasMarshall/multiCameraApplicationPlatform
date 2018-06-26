package platform.core.utilities;

import java.util.Date;

public class Event {

    private String eventDescription;
    private Date date;

    public Event(String eventDescription, Date date) {
        this.eventDescription = eventDescription;
        this.date = date;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public Date getDate() {
        return date;
    }
}
