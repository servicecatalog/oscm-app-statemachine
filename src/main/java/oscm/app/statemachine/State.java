package oscm.app.statemachine;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class State {

    private String id;
    private String action;
    private String timeoutSec;
    private List<Event> events;

    @XmlElement(name = "event")
    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getAction() {
        return action;
    }

    @XmlAttribute
    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(String id) {
        this.id = id;
    }

    public String getTimeoutSec() {
        return timeoutSec;
    }

    @XmlAttribute(required = false)
    public void setTimeoutSec(String timeout) {
        this.timeoutSec = timeout;
    }

}