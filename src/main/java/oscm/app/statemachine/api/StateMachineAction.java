/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) 2016 FUJITSU Limited - ALL RIGHTS RESERVED.                  
 *                                                                                                                                 
 *  Creation Date: 22.01.2016                                                      
 *                                                                              
 *******************************************************************************/

package oscm.app.statemachine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kulle
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateMachineAction {

}
