package jadex.bdi.examples.disastermanagement.commander;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.extension.envsupport.environment.ISpaceObject;

/**
 *  Handle a disaster by assigning units.
 */
public class HandleDisasterPlan extends Plan
{
	
	/**
	 *  The body method is called on the
	 *  instantiated plan instance from the scheduler.
	 */
	public void	body()
	{		
		ISpaceObject disaster = (ISpaceObject)getParameter("disaster").getValue();
//		System.out.println("handle: "+disaster);
	
		IGoal	cc	= createGoal("clear_chemicals"); 
		cc.getParameter("disaster").setValue(disaster);
		dispatchSubgoal(cc);

		IGoal	ef	= createGoal("extinguish_fires"); 
		ef.getParameter("disaster").setValue(disaster);
		dispatchSubgoal(ef);

		IGoal	tv	= createGoal("treat_victims"); 
		tv.getParameter("disaster").setValue(disaster);
		dispatchSubgoal(tv);
		
		waitForGoal(cc);
		waitForGoal(ef);
		waitForGoal(tv);
	}
	
//	public void aborted()
//	{
//		if(getException()!=null)
//		{
//			System.out.println("aborted: "+getException()+" "+this);
//		}
//	}
//	
//	public void failed()
//	{
//		System.err.println("failed: "+this);
//		if(getException()!=null)
//		{
//			getException().printStackTrace();
//		}
//	}
	
//	/** The already assigned fire units. */
//	protected List	fireunits;
//	
//	/** The already assigned chemical units. */
//	protected List	chemicalunits;
//	
//	/** The already assigned ambulance units. */
//	protected List	ambulanceunits;
	
//	/**
//	 *  The body method is called on the
//	 *  instantiated plan instance from the scheduler.
//	 */
//	public void	body()
//	{		
//		// Keep track of assigned units in case plan gets aborted.
//		this.fireunits	= new ArrayList();
//		this.chemicalunits	= new ArrayList();
//		this.ambulanceunits	= new ArrayList();
//		
//		final ISpaceObject disaster = (ISpaceObject)getParameter("disaster").getValue();
////		System.out.println("handle: "+disaster);
//	
//		final IBeliefSet busy = getBeliefbase().getBeliefSet("busy_entities");	
//		
//		// Plan runs in an endless loop until the goal is achieved and the plan is aborted.
//		while(true)
//		{
//			int chemicals = ((Integer)disaster.getProperty("chemicals")).intValue();
//			int fire = ((Integer)disaster.getProperty("fire")).intValue();
//			int victims = ((Integer)disaster.getProperty("victims")).intValue();
//
//			if(chemicals>chemicalunits.size())
//			{
////				Collection clearchemser = (Collection)SServiceProvider.getServices(getScope().getServiceProvider(), IClearChemicalsService.class).get(this);
//				Collection clearchemser = (Collection)getScope().getRequiredServices("clearchemicalsservices").get(this);
//				if(clearchemser.size()>0)
//				{
//					Iterator it=clearchemser.iterator();
//					while(chemicals>chemicalunits.size() && it.hasNext())
//					{
//						final IClearChemicalsService ccs = (IClearChemicalsService)it.next();
//						final Object provid = ccs.getServiceIdentifier().getProviderId();
//						if(!busy.containsFact(provid))
//						{
//							busy.addFact(provid);
//							chemicalunits.add(ccs);
//	//						System.out.println("Unit assigned: "+provid);
//							ccs.clearChemicals(disaster).addResultListener(createResultListener(new IResultListener()
//							{
//								public void resultAvailable(Object result)
//								{
//	//								System.out.println("Unit finished: "+provid);
//									busy.removeFact(provid);
//									chemicalunits.remove(ccs);
//								}
//								
//								public void exceptionOccurred(Exception exception)
//								{
//	//								System.out.println("Unit exception: "+provid);
//									busy.removeFact(provid);
//									chemicalunits.remove(ccs);
//								}
//							}));
//						}
//					}
//				}
//			}
//			
//			if(fire>fireunits.size())
//			{
////				Collection exfireser = (Collection)SServiceProvider.getServices(getScope().getServiceProvider(), IExtinguishFireService.class).get(this);
//				Collection exfireser = (Collection)getScope().getRequiredServices("extinguishfireservices").get(this);
//				if(exfireser.size()>0)
//				{
//					Iterator it=exfireser.iterator();
//					while(fire>fireunits.size() && it.hasNext())
//					{
//						final IExtinguishFireService fes = (IExtinguishFireService)it.next();
//						final Object provid = fes.getServiceIdentifier().getProviderId();
//						if(!busy.containsFact(provid))
//						{
//							busy.addFact(provid);
//							fireunits.add(fes);
//	//						System.out.println("Unit assigned: "+provid);
//							fes.extinguishFire(disaster).addResultListener(createResultListener(new IResultListener()
//							{
//								public void resultAvailable(Object result)
//								{
//	//								System.out.println("Unit finished: "+provid);
//									busy.removeFact(provid);
//									fireunits.remove(fes);
//								}
//								
//								public void exceptionOccurred(Exception exception)
//								{
//	//								System.out.println("Unit exception: "+provid);
//									busy.removeFact(provid);
//									fireunits.remove(fes);
//								}
//							}));
//						}
//					}
//				}
//			}
//			
////			System.out.println("vic: "+victims+" "+ambulanceunits.size()+" "+disaster);
//			if(chemicals==0 && victims>ambulanceunits.size())
//			{
////				Collection treatvicser = (Collection)SServiceProvider.getServices(getScope().getServiceProvider(), ITreatVictimsService.class).get(this);
//				Collection treatvicser = (Collection)getScope().getRequiredServices("treatvictimservices").get(this);
//				if(treatvicser.size()>0)
//				{
//					Iterator it=treatvicser.iterator();
//					while(victims>ambulanceunits.size() && it.hasNext())
//					{
//						final ITreatVictimsService tvs = (ITreatVictimsService)it.next();
//						final Object provid = tvs.getServiceIdentifier().getProviderId();
//						if(!busy.containsFact(provid))
//						{
//							busy.addFact(provid);
//							ambulanceunits.add(tvs);
////							System.out.println("Unit assigned: "+provid);
//							tvs.treatVictims(disaster).addResultListener(createResultListener(new IResultListener()
//							{
//								public void resultAvailable(Object result)
//								{
////									int s = ambulanceunits.size();
//									busy.removeFact(provid);
//									ambulanceunits.remove(tvs);
////									System.out.println("Unit finished: "+provid+", "+disaster+" "+s+" "+ambulanceunits.size());
//								}
//								
//								public void exceptionOccurred(Exception exception)
//								{
////									System.out.println("Unit exception: "+provid);
//									busy.removeFact(provid);
//									ambulanceunits.remove(tvs);
//								}
//							}));
//						}
//					}
//				}
//			}
//			
//			waitFor(1000);	// Wait before looking again for free units.
//		}
//	}
	
//	/**
//	 *  Called when the plan is aborted,
//	 *  e.g. because the goal is achieved or inactivated.
//	 */
//	public void	aborted()
//	{
////		System.out.println("aborted");
//		
//		// Abort all units.
//		// Use arrays, because collection might be altered by abort().
//		
//		IExtinguishFireService[]	fus	= (IExtinguishFireService[])fireunits.toArray(new IExtinguishFireService[fireunits.size()]);
//		for(int i=0; i<fus.length; i++)
//			fus[i].abort();
//
//		IClearChemicalsService[]	ccs	= (IClearChemicalsService[])chemicalunits.toArray(new IClearChemicalsService[chemicalunits.size()]);
//		for(int i=0; i<ccs.length; i++)
//			ccs[i].abort();
//
//		ITreatVictimsService[]	aus	= (ITreatVictimsService[])ambulanceunits.toArray(new ITreatVictimsService[ambulanceunits.size()]);
//		for(int i=0; i<aus.length; i++)
//			aus[i].abort();
//	}
}
