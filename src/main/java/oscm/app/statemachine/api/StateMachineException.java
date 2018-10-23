/*******************************************************************************
 *
 *  COPYRIGHT (C) 2016 FUJITSU Limited - ALL RIGHTS RESERVED.
 *
 *  Creation Date: 22.01.2016
 *
 *******************************************************************************/

package oscm.app.statemachine.api;

/**
 * @author kulle
 *
 */
public class StateMachineException extends Exception {

    private static final long serialVersionUID = -5619220680463876008L;

    private String instanceId;

    private String clazz;

    private String method;

    public StateMachineException(String msg) {
        super(msg);
    }

    public StateMachineException(String msg, Throwable e) {
        super(msg, e);
    }

    public StateMachineException(String msg, Throwable e, String instanceId,
            String clazz, String method) {

        super(msg, e);
        this.instanceId = instanceId;
        this.clazz = clazz;
        this.method = method;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getClazz() {
        return clazz;
    }

    public String getMethod() {
        return method;
    }

}
