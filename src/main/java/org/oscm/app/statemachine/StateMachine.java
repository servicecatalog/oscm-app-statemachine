package org.oscm.app.statemachine;

import java.io.InputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.statemachine.api.StateMachineException;
import org.oscm.app.statemachine.api.StateMachineProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMachine {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(StateMachine.class);

    /**
     * Remember the time that a statemachine action has been started. Used by
     * the statemachine to determine when to throw a timeout exception. The
     * value is stored as an internal instance parameter.
     */
    private static final String ACTION_STARTED_TIMESTAMP = "ACTION_STARTED_TIMESTAMP";

    private States states;
    private String stateId;
    private String machine;
    private String history;

    private transient State previousState;

    public StateMachine(ProvisioningSettings settings)
            throws StateMachineException {
        machine = settings.getParameters()
                .get(StateMachineProperties.SM_STATE_MACHINE).getValue();
        states = loadStateMachine(machine);
        history = settings.getParameters()
                .get(StateMachineProperties.SM_STATE_HISTORY).getValue();
        stateId = settings.getParameters().get(StateMachineProperties.SM_STATE)
                .getValue();
    }

    States loadStateMachine(String filename) throws StateMachineException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader
                .getResourceAsStream("statemachines/" + filename);) {
            JAXBContext jaxbContext = JAXBContext.newInstance(States.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (States) jaxbUnmarshaller.unmarshal(stream);
        } catch (Exception e) {
            throw new StateMachineException(
                    "Failed to load state machine definition file: " + filename,
                    e);
        }
    }

    public String getStateId() {
        return stateId;
    }

    public String getHistory() {
        return history;
    }

    public State getPreviousState() {
        return previousState;
    }

    public static void initializeProvisioningSettings(
            ProvisioningSettings settings, String stateMachine) {

        settings.getParameters().put(StateMachineProperties.SM_STATE_HISTORY,
                new Setting(StateMachineProperties.SM_STATE_HISTORY, ""));
        settings.getParameters().put(StateMachineProperties.SM_STATE_MACHINE,
                new Setting(StateMachineProperties.SM_STATE_MACHINE,
                        stateMachine));
        settings.getParameters().put(StateMachineProperties.SM_STATE,
                new Setting(StateMachineProperties.SM_STATE, "BEGIN"));
    }

    public String executeAction(ProvisioningSettings settings,
            String instanceId, InstanceStatus result)
            throws StateMachineException {

        State currentState = findStateById(stateId);
        String eventId = states.invokeAction(currentState, instanceId, settings,
                result);
        history = appendStateToHistory(stateId, history);
        transition(currentState, eventId);
        State nextState = findStateById(stateId);
        if (hasTimeout(nextState)) {
            if (sameState(currentState, nextState)) {
                if ("suspended".equals(settings.getParameters()
                        .get(ACTION_STARTED_TIMESTAMP).getValue())) {
                    LOGGER.debug(
                            "Reinitialize timeout reference after an occured timeout.");
                    setReferenceForTimeout(settings,
                            String.valueOf(System.currentTimeMillis()));
                } else {
                    String timeoutSec = getReadyTimeout(nextState, settings);
                    if (exceededTimeout(settings, timeoutSec)) {
                        setReferenceForTimeout(settings, "suspended");
                        LOGGER.debug("Aborted execution of state '"
                                + nextState.getId() + "' due to timeout of "
                                + timeoutSec + " sec");
                        throw new StateMachineException(
                                "Statemachine action not finished after "
                                        + timeoutSec + " sec.");
                    }
                }
            } else {
                setReferenceForTimeout(settings,
                        String.valueOf(System.currentTimeMillis()));
            }
        }

        return stateId;
    }

    private void setReferenceForTimeout(ProvisioningSettings settings,
            String value) {
        settings.getParameters().put(ACTION_STARTED_TIMESTAMP,
                new Setting(ACTION_STARTED_TIMESTAMP, value));
    }

    private boolean hasTimeout(State state) {
        return state.getTimeoutSec() != null
                && state.getTimeoutSec().trim().length() > 0;
    }

    private boolean sameState(State oldState, State nextState) {
        return oldState.getId().equals(nextState.getId());
    }

    private String getReadyTimeout(State nextState,
            ProvisioningSettings settings) {
        String timeoutSec = nextState.getTimeoutSec();
        if (timeoutSec.startsWith("$")) {
            String timeoutVar = timeoutSec.substring(2,
                    timeoutSec.length() - 1);
            timeoutSec = getGuestReadyTimeout(settings, timeoutVar);
        }
        return timeoutSec;
    }

    private String getGuestReadyTimeout(ProvisioningSettings settings,
            String key) {
        if (settings.getParameters().containsKey(key)) {
            return getValue(key, settings.getParameters());
        }
        return getValue(key, settings.getConfigSettings());
    }

    private String getValue(String key, Map<String, Setting> source) {
        Setting setting = source.get(key);
        return setting != null ? setting.getValue() : null;
    }

    private boolean exceededTimeout(ProvisioningSettings settings,
            String timeoutSec) {

        if (timeoutSec == null || timeoutSec.trim().length() == 0) {
            LOGGER.warn("Action timeout is not set and therefore ignored.");
            return false;
        }

        try {
            long curMillis = System.currentTimeMillis();
            long startMillis = Long.valueOf(getValue(ACTION_STARTED_TIMESTAMP,
                    settings.getParameters())).longValue();
            long timeout = Long.valueOf(timeoutSec).longValue() * 1000;

            return curMillis - startMillis > timeout;
        } catch (NumberFormatException e) {
            LOGGER.warn("The action timeout '" + timeoutSec
                    + " 'is not a number and therefore ignored.");
            return false;
        }

    }

    ///////////////////////////////////////////////////////////

    State findStateById(String stateId) throws StateMachineException {
        for (State state : states.getStates()) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        throw new StateMachineException("State '" + stateId
                + "' not found for statemachine " + machine);
    }

    private void transition(State currentState, String eventId)
            throws StateMachineException {

        for (Event event : currentState.getEvents()) {
            if (event.getId().equals(eventId)) {
                previousState = currentState;
                stateId = event.getState();
                return;
            }
        }

        throw new StateMachineException("Next state not found ["
                + currentState.getId() + " -> " + eventId
                + "]. Please check the statemachine definition file "
                + machine);
    }

    String appendStateToHistory(String state, String stateHistory) {
        if (stateHistory == null || stateHistory.trim().length() == 0) {
            return state;
        } else if (!stateHistory.endsWith(state)) {
            return stateHistory.concat(",").concat(state);
        }
        return stateHistory;
    }

    public String loadPreviousStateFromHistory(ProvisioningSettings settings)
            throws StateMachineException {

        String currentState = settings.getParameters()
                .get(StateMachineProperties.SM_STATE).getValue();
        String stateHistory = settings.getParameters()
                .get(StateMachineProperties.SM_STATE_HISTORY).getValue();
        String[] states = stateHistory.split(",");
        for (int i = states.length - 1; i >= 0; i--) {
            if (!states[i].equals(currentState)) {
                return states[i];
            }
        }

        throw new StateMachineException(
                "Couldn't find previous state for statemachine " + machine
                        + " and current state " + stateId);
    }
}
