package jadex.bdi.testcases.misc;

import jadex.base.test.TestReport;
import jadex.bdi.runtime.Plan;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;

/**
 *  Test start/termination flags work. 
 */
public class FlagPlan extends Plan
{
	 /**
	  *  The plan body.
	  */
	public void body()
	{
		IComponentManagementService cms = (IComponentManagementService)SServiceProvider.getServiceUpwards(
			getServiceContainer(), IComponentManagementService.class).get(this);
		
		TestReport tr = new TestReport("#1", "Start agent as suspended.");
		CreationInfo ci =  new CreationInfo("donothing", null, getComponentIdentifier());
		ci.setSuspend(Boolean.TRUE);
		IComponentIdentifier cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		IComponentDescription desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not suspended: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#2", "Start agent as master.");
		ci =  new CreationInfo("donothing", null, getComponentIdentifier());
		ci.setMaster(Boolean.TRUE);
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(desc.getMaster()!=null && desc.getMaster().booleanValue())
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not master: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#3", "Start agent as daemon.");
		ci =  new CreationInfo("donothing", null, getComponentIdentifier());
		ci.setDaemon(Boolean.TRUE);
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(desc.getDaemon()!=null && desc.getDaemon().booleanValue())
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not daemon: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#4", "Start agent as autoshutdown.");
		ci =  new CreationInfo("donothing", null, getComponentIdentifier());
		ci.setAutoShutdown(Boolean.TRUE);
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(desc.getAutoShutdown()!=null && desc.getAutoShutdown().booleanValue())
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not autoshutdown: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#5", "Start agent as suspended.");
		ci =  new CreationInfo("suspend", null, getComponentIdentifier());
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not suspended: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#6", "Start agent as master.");
		ci =  new CreationInfo("master", null, getComponentIdentifier());
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(desc.getMaster()!=null && desc.getMaster().booleanValue())
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not master: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#7", "Start agent as daemon.");
		ci =  new CreationInfo("daemon", null, getComponentIdentifier());
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(desc.getDaemon()!=null && desc.getDaemon().booleanValue())
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not daemon: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		tr = new TestReport("#8", "Start agent as autoshutdown.");
		ci =  new CreationInfo("autoshutdown", null, getComponentIdentifier());
		cid = (IComponentIdentifier)cms.createComponent(null, "jadex/bdi/testcases/misc/Flag.agent.xml", ci , null).get(this);
		desc = (IComponentDescription)cms.getComponentDescription(cid).get(this);
		
		if(desc.getAutoShutdown()!=null && desc.getAutoShutdown().booleanValue())
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Component not autoshutdown: "+desc);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
	}
}