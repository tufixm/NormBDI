package jadex.bdi.planlib;

import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.IValueFetcher;
import jadex.commons.SUtil;
import jadex.commons.SimplePropertyObject;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.transformation.annotations.Classname;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.IPerceptProcessor;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector1;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector1Double;
import jadex.javaparser.IParsedExpression;
import jadex.javaparser.SimpleValueFetcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  Default bdi agent vision processor.
 *  Updates the agent's beliefsets according to the percepts of new/disappeared waste.
 */
public class DefaultBDIVisionProcessor extends SimplePropertyObject implements IPerceptProcessor
{
	//-------- constants --------

	/** The percept types property. */
	public static String PROPERTY_PERCEPTTYPES = "percepttypes";
	
	
	/** The add action. */
	public static String ADD = "add";
	
	/** The remove action. */
	public static String REMOVE = "remove";
	
	/** The remove_outdated action (checks all entries in the belief set, if they should be seen, but are no longer there). */
	public static String REMOVE_OUTDATED = "remove_outdated";
	
	/** The set action. */
	public static String SET = "set";
	
	/** The unset action (sets a belief fact to null). */
	public static String UNSET = "unset";
	
	/** The maxrange property. */
	public static String PROPERTY_MAXRANGE = "range";

	/** The maxrange property. */
	public static String PROPERTY_RANGE = "range_property";

	//-------- attributes --------
	
	/** The percepttypes infos. */
	protected Map percepttypes;
	
	//-------- methods --------
	
	/**
	 *  Process a new percept.
	 *  @param space The space.
	 *  @param type The type.
	 *  @param percept The percept.
	 *  @param agent The agent identifier.
	 *  @param agent The avatar of the agent (if any).
	 */
	public void processPercept(final IEnvironmentSpace space, final String type, final Object percept, final IComponentDescription agent, final ISpaceObject avatar)
	{
		boolean	invoke	= false;
		final String[][] metainfos = getMetaInfos(type);
		for(int i=0; !invoke && metainfos!=null && i<metainfos.length; i++)
		{
			invoke	= ADD.equals(metainfos[i][0])
				|| REMOVE.equals(metainfos[i][0])
				|| SET.equals(metainfos[i][0])
				|| UNSET.equals(metainfos[i][0])
				|| REMOVE_OUTDATED.equals(metainfos[i][0]) && percept.equals(avatar);
		}
		
		if(invoke)
		{
			// HACK!!! todo
			SServiceProvider.getService(space.getExternalAccess().getServiceProvider(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
				.addResultListener(new IResultListener()
			{
				public void resultAvailable(Object result)
				{
					IFuture fut = ((IComponentManagementService)result).getExternalAccess(agent.getName());
					fut.addResultListener(new IResultListener()
					{
						public void exceptionOccurred(Exception exception)
						{
							// Happens when component already removed
//							exception.printStackTrace();
						}
						public void resultAvailable(Object result)
						{
							final IBDIExternalAccess exta = (IBDIExternalAccess)result;
							
							for(int i=0; i<metainfos.length; i++)
							{
								final IParsedExpression	cond	= metainfos[i].length==2 ? null
									: (IParsedExpression)getProperty(metainfos[i][2]);
								final SimpleValueFetcher fetcher = new SimpleValueFetcher();
								if(cond!=null)
								{
//									fetcher	= new SimpleValueFetcher();
									fetcher.setValue("$space", space);
									fetcher.setValue("$percept", percept);
									fetcher.setValue("$avatar", avatar);
									fetcher.setValue("$type", type);
									fetcher.setValue("$aid", agent);
									fetcher.setValue("$scope", exta);
								}
								final String name = metainfos[i][1];

								if(ADD.equals(metainfos[i][0]))
								{
									exta.scheduleStep(new IComponentStep<Void>()
									{
										@Classname("add")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											IBDIInternalAccess	scope	= (IBDIInternalAccess)ia;
											Object[]	facts	= scope.getBeliefbase().getBeliefSet(name).getFacts();
											if(cond!=null)
												fetcher.setValue("$facts", facts);
											
											if(!SUtil.arrayContains(facts, percept) && (cond==null || evaluate(cond, fetcher)))
											{
												scope.getBeliefbase().getBeliefSet(name).addFact(percept);
//												System.out.println("added: "+percept+" to: "+belset);
											}
											return IFuture.DONE;
										}
									});
								}
								else if(REMOVE.equals(metainfos[i][0]))
								{
									exta.scheduleStep(new IComponentStep<Void>()
									{
										@Classname("remove")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											IBDIInternalAccess	scope	= (IBDIInternalAccess)ia;
											Object[]	facts	= scope.getBeliefbase().getBeliefSet(name).getFacts();
											if(cond!=null)
												fetcher.setValue("$facts", facts);
											
											if(SUtil.arrayContains(facts, percept) && (cond==null || evaluate(cond, fetcher)))
											{
												scope.getBeliefbase().getBeliefSet(name).removeFact(percept);
//												System.out.println("removed: "+percept+" from: "+belset);
											}
											return IFuture.DONE;
										}
									});
								}
								else if(SET.equals(metainfos[i][0]))
								{
									exta.scheduleStep(new IComponentStep<Void>()
									{
										@Classname("set")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											IBDIInternalAccess	scope	= (IBDIInternalAccess)ia;
											Object	fact	= scope.getBeliefbase().getBelief(name).getFact();
											if(cond!=null)
												fetcher.setValue("$fact", fact);
											
											if(cond==null || evaluate(cond, fetcher))
											{
												scope.getBeliefbase().getBelief(name).setFact(percept);
//												System.out.println("set: "+percept+" on: "+belset);
											}
											return IFuture.DONE;
										}
									});
								}
								else if(UNSET.equals(metainfos[i][0]))
								{
									exta.scheduleStep(new IComponentStep<Void>()
									{
										@Classname("unset")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											IBDIInternalAccess	scope	= (IBDIInternalAccess)ia;
											Object	fact	= scope.getBeliefbase().getBelief(name).getFact();
											if(cond!=null)
												fetcher.setValue("$fact", fact);
											
											if(cond==null || evaluate(cond, fetcher))
											{
												scope.getBeliefbase().getBelief(name).setFact(null);
//												System.out.println("unset: "+percept+" on: "+belset);
											}
											return IFuture.DONE;
										}
									});
								}
								else if(REMOVE_OUTDATED.equals(metainfos[i][0]) && percept.equals(avatar))
								{
									exta.scheduleStep(new IComponentStep<Void>()
									{
										@Classname("removeoutdated")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											IBDIInternalAccess	scope	= (IBDIInternalAccess)ia;
											Object[]	facts	= scope.getBeliefbase().getBeliefSet(name).getFacts();
											if(cond!=null)
												fetcher.setValue("$facts", facts);
											
											if(cond==null || evaluate(cond, fetcher))
											{
												IVector1 vision	= getRange(avatar);
												Space2D	space2d	= (Space2D)space;
												IVector2	mypos	= (IVector2)avatar.getProperty(Space2D.PROPERTY_POSITION);
												ISpaceObject[]	known	= (ISpaceObject[])facts;
												Set	seen = space2d.getNearObjects(mypos, vision);
												for(int j=0; j<known.length; j++)
												{
													IVector2	knownpos	= (IVector2)known[j].getProperty(Space2D.PROPERTY_POSITION);
													// Hack!!! Shouldn't react to knownpos==null
													if(!seen.contains(known[j]) && (knownpos==null || !vision.less(space2d.getDistance(mypos, knownpos))))
													{
//														System.out.println("Removing disappeared object: "+percept+", "+known[j]);
														scope.getBeliefbase().getBeliefSet(name).removeFact(known[j]);
													}
												}
											}
											return IFuture.DONE;
										}
									});
								}
							}
						}
					});
				}
				
				public void exceptionOccurred(Exception exception)
				{
					exception.printStackTrace();
				}
			});
		}
	}
	
	/**
	 *  Get the percept types defined for this generator.
	 *  @return The percept types.
	 */
	protected Object[] getPerceptTypes()
	{
		return (Object[])getProperty(PROPERTY_PERCEPTTYPES);
	}
	
	/**
	 *  Get meta infos about a percept type.
	 */
	protected String[][] getMetaInfos(String percepttype)
	{
		if(percepttypes==null)
		{
			this.percepttypes = new HashMap();
			Object[] percepttypes = getPerceptTypes();
			for(int i=0; i<percepttypes.length; i++)
			{
				String[]	per = (String[])percepttypes[i];
				String[][]	newmis	= per.length==3 ? new String[][]{{per[1], per[2]}} : new String[][]{{per[1], per[2], per[3]}};
				String[][]	oldmis	= (String[][])this.percepttypes.get(per[0]);
				if(oldmis!=null)
					newmis	= (String[][])SUtil.joinArrays(oldmis, newmis);
				this.percepttypes.put(per[0], newmis);
			}
		}
		return (String[][])percepttypes.get(percepttype);
	}

	// Todo: unify range handling!?
	
	/**
	 *  Get the range.
	 *  @return The range.
	 */
	protected IVector1 getRange(ISpaceObject avatar)
	{
		Object tmp = avatar.getProperty(getRangePropertyName());
		return tmp==null? getDefaultRange(): tmp instanceof Number? new Vector1Double(((Number)tmp).doubleValue()): (IVector1)tmp;
	}

	/**
	 *  Get the default range.
	 *  @return The range.
	 */
	protected IVector1 getDefaultRange()
	{
		Object tmp = getProperty(PROPERTY_MAXRANGE);
		return tmp==null? Vector1Double.ZERO: tmp instanceof Number? new Vector1Double(((Number)tmp).doubleValue()): (IVector1)tmp;
	}
	
	/**
	 *  Get the range property name.
	 *  @return The range property name.
	 */
	protected String getRangePropertyName()
	{
		Object tmp = getProperty(PROPERTY_RANGE);
		return tmp==null? "range": (String)tmp;
	}
	
	/**
	 *  Evaluate a condition.
	 *  @param exp	The expression.
	 *  @param fetcher	The value fetcher.
	 */
	protected boolean	evaluate(IParsedExpression exp, IValueFetcher fetcher)
	{
		boolean	ret	= false;
		try
		{
			ret	= ((Boolean)exp.getValue(fetcher)).booleanValue();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
}