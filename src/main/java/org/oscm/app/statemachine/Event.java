/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.statemachine;

import javax.xml.bind.annotation.XmlAttribute;

public class Event {

    private String id;
    private String state;

    public String getState() {
        return state;
    }

    @XmlAttribute
    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return state + ", " + id;
    }

}