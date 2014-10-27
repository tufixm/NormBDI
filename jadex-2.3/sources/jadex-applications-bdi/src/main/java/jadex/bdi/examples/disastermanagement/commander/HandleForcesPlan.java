package jadex.bdi.examples.disastermanagement.commander;

import jadex.bdi.runtime.AgentEvent;
import jadex.bdi.runtime.IBeliefSet;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IGoalListener;
import jadex.bdi.runtime.Plan;
import jadex.bridge.service.IService;
import jadex.extension.envsupport.environment.ISpaceObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *  Handle forces by sending an appropriate number to the disaster site..
 */
public abstract class HandleForcesPlan extends Plan
{
	/**
	 *  The body method is called on the
	 *  instantiated plan instance from the scheduler.
	 */
	public void	allocateForces(String servicename, String typename)
	{		
		while(true)
		{
			final ISpaceObject disaster = (ISpaceObject)getParameter("disaster").getValue();
			Collection forces = (Collection)getServiceContainer().getRequiredServices(servicename).get(this);
			int number = ((Integer)disaster.getProperty(typename)).intValue();
			final IBeliefSet busy = getBeliefbase().getBeliefSet("busy_entities");	
							
//			int as = 0;
			if(forces.size()>0)
			{
				List fs = new ArrayList(forces);
				Iterator it = fs.iterator();
				
//				List goals = new ArrayList();
				while(number>getParameterSet("units").size() && it.hasNext())
				{
					final IService force = (IService)it.next();
					final Object provid = force.getServiceIdentifier().getProviderId();
					if(!busy.containsFact(provid))
					{
//						as++;
						busy.addFact(provid);
						getParameterSet("units").addValue(force);
					
						IGoal sendforce = createGoal("send_rescueforce");
						sendforce.getParameter("disaster").setValue(disaster);
						sendforce.getParameter("rescueforce").setValue(force);
						dispatchSubgoal(sendforce);
						sendforce.addGoalListener(new IGoalListener()
						{
							public void goalFinished(AgentEvent ae)
							{
								// The plans ensure that the force is guaranteed to be not duty when the goal is finished.
								getParameterSet("units").removeValue(force);
								busy.removeFact(provid);
							}
							
							public void goalAdded(AgentEvent ae)
							{
							}
						});
					}
				}
			}
			
//			System.out.println("hf: "+disaster.getId()+" "+number+" "+getParameterSet("units").getValues().length+" "+as+" "+busy.size()+" "+SUtil.arrayToString(busy.getFacts()));
			
//			waitForFactRemoved("busy_entities");
			waitFor(1000);
		}
	}
	
//	public void aborted()
//	{
//		if(getException()!=null)
//		{
//			System.out.println("aborted: "+getException());
//		}
//	}
//	
//	public void failed()
//	{
//		System.out.println("failed");
//	}
	
//	/**
//	 *  The body method is called on the
//	 *  instantiated plan instance from the scheduler.
//	 */
//	public void	body()
//	{		
//		while(true)
//		{
//			final ISpaceObject disaster = (ISpaceObject)getParameter("disaster").getValue();
//			String servicename = (String)getParameter("servicename").getValue();
//			String typename = (String)getParameter("typename").getValue();
//			Collection forces = (Collection)getScope().getRequiredServices(servicename).get(this);
//			int number = ((Integer)disaster.getProperty(typename)).intValue();
//			final IBeliefSet busy = getBeliefbase().getBeliefSet("busy_entities");	
//							
//			int as = 0;
//			if(forces.size()>0)
//			{
//				List fs = new ArrayList(forces);
//				Iterator it = forces.iterator();
//				
////				List goals = new ArrayList();
//				while(number>getParameterSet("units").size() && it.hasNext())
//				{
//					final IService force = (IService)it.next();
//					final Object provid = force.getServiceIdentifier().getProviderId();
//					if(!busy.containsFact(provid))
//					{
//						as++;
//						busy.addFact(provid);
//						getParameterSet("units").addValue(force);
//					
//						IGoal sendforce = createGoal("send_rescueforce");
//						sendforce.getParameter("disaster").setValue(disaster);
//						sendforce.getParameter("rescueforce").setValue(force);
//						dispatchSubgoal(sendforce);
//						sendforce.addGoalListener(new IGoalListener()
//						{
//							public void goalFinished(AgentEvent ae)
//							{
//								// The plans ensure that the force is guaranteed to be not duty when the goal is finished.
//								getParameterSet("units").removeValue(force);
//								busy.removeFact(provid);
//							}
//							
//							public void goalAdded(AgentEvent ae)
//							{
//							}
//						});
//					}
//				}
//			}
//			
//			System.out.println("hf: "+disaster.getId()+" "+number+" "+getParameterSet("units").getValues().length+" "+as+" "+busy.size()+" "+SUtil.arrayToString(busy.getFacts()));
//			
//			waitFor(1000);
//		}
//	}
}
