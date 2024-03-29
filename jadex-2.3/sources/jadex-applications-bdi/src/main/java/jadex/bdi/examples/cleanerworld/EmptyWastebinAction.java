package jadex.bdi.examples.cleanerworld;

import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceAction;
import jadex.extension.envsupport.environment.ISpaceObject;

import java.util.Map;

/**
 * 
 */
public class EmptyWastebinAction  extends SimplePropertyObject implements ISpaceAction
{
	/**
	 * Performs the action.
	 * @param parameters parameters for the action
	 * @param space the environment space
	 * @return action return value
	 */
	public Object perform(Map parameters, IEnvironmentSpace space)
	{
		ISpaceObject wastebin = (ISpaceObject)parameters.get(ISpaceAction.OBJECT_ID);
		wastebin.setProperty("wastes", new Integer(0));
		return Boolean.TRUE;
	}
}
