package jadex.bdi.planlib.cms;

import jadex.bdi.runtime.Plan;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;

import java.util.Map;

/**
 *  Plan for creating a Jadex component on the platform.
 */

// todo: refactor parameters according to the new cms interface with CreationInfo

public class CMSLocalCreateComponentPlan extends Plan
{

	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		String	type	= (String)getParameter("type").getValue();
		String	name	= (String)getParameter("name").getValue();
		String	config	= (String)getParameter("configuration").getValue();
		Map	args	= (Map)getParameter("arguments").getValue();
		Boolean	suspend	= (Boolean)getParameter("suspend").getValue();
		Boolean	master	= (Boolean)getParameter("master").getValue();
		IComponentIdentifier	parent	= (IComponentIdentifier)getParameter("parent").getValue();
		IResourceIdentifier	rid	= (IResourceIdentifier)getParameter("rid").getValue();

		try
		{
			// todo: support parent/master etc.
			IFuture ret = ((IComponentManagementService)getServiceContainer().getRequiredService("cms").get(this))
				.createComponent(name, type, new CreationInfo(config, args, parent, suspend, master, null, null, null, null, rid), null);
			IComponentIdentifier aid = (IComponentIdentifier)ret.get(this);
			getParameter("componentidentifier").setValue(aid);
		}
		catch(Exception e)
		{
//			e.printStackTrace();
			fail(e); // Do not show exception on console. 
		}
	}
}
