/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) 2016 FUJITSU Limited - ALL RIGHTS RESERVED.                  
 *                                                                                                                                 
 *  Creation Date: 25.02.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.app.statemachine.State;
import org.oscm.app.statemachine.StateMachine;
import org.oscm.app.statemachine.States;

/**
 * @author kulle
 *
 */
@Ignore
public class StateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void before() throws Exception {
        stateMachine = mock(StateMachine.class);
        when(stateMachine.appendStateToHistory(anyString(), anyString()))
                .thenCallRealMethod();
    }

    private State findById(States states, String id) {
        for (State s : states.getStates()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        throw new IllegalStateException(
                "State with id " + id + " not found in state list " + states);
    }

    @Test
    public void appendStateToHistory_nullHistory() throws Exception {
        // given
        String stateHistory = null;
        String state = "state";

        // when
        String history = stateMachine.appendStateToHistory(state, stateHistory);

        // then
        assertEquals("state", history);
    }

    @Test
    public void appendStateToHistory_emptyHistory() throws Exception {
        // given
        String stateHistory = "";
        String state = "state";

        // when
        String history = stateMachine.appendStateToHistory(state, stateHistory);

        // then
        assertEquals("state", history);
    }

    @Test
    public void appendStateToHistory() throws Exception {
        // given
        String stateHistory = "oldState";
        String state = "state";

        // when
        String history = stateMachine.appendStateToHistory(state, stateHistory);

        // then
        assertEquals("oldState,state", history);
    }

    @Test
    public void appendStateToHistory_sameState() throws Exception {
        // given
        String stateHistory = "oldState,state";
        String state = "state";

        // when
        String history = stateMachine.appendStateToHistory(state, stateHistory);

        // then
        assertEquals("oldState,state", history);
    }

    @Test
    public void loadStateMachine() throws Exception {
        // given
        when(stateMachine.loadStateMachine(anyString())).thenCallRealMethod();
        String filename = "modify_vm.xml";

        // when
        States states = stateMachine.loadStateMachine(filename);

        // then
        assertEquals(5, states.getStates().size());
        assertTrue("10".equals(findById(states, "MODIFYING").getTimeoutSec()));
    }

}
