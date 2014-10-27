package jadex.bdi.planlib.iasteps;

import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bdi.runtime.IBeliefbase;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SetBeliefStep implements IComponentStep<Void>
{
	protected Map beliefs;
	
	/**
	 *  Sets an agent's belief.
	 *  @param belief Name of the belief.
	 *  @param fact New fact of the belief.
	 */
	public SetBeliefStep(final String belief, final Object fact)
	{
		this.beliefs = new HashMap() {{
			put(belief, fact);
		}};
	}
	
	/**
	 *  Sets multiple agent beliefs.
	 *  @param beliefs The beliefs.
	 */
	public SetBeliefStep(Map beliefs)
	{
		this.beliefs = beliefs;
	}
	
	public IFuture<Void> execute(IInternalAccess ia)
	{
		IBeliefbase bb = ((IBDIInternalAccess) ia).getBeliefbase();
		for (Iterator it = beliefs.entrySet().iterator(); it.hasNext(); )
		{
			Map.Entry entry = (Map.Entry) it.next();
			bb.getBelief((String) entry.getKey()).setFact(entry.getValue());
		}
		
		return IFuture.DONE;
	}
}
