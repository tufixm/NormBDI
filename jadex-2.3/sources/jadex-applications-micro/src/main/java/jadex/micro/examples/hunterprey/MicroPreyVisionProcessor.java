package jadex.micro.examples.hunterprey;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.SimplePropertyObject;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.transformation.annotations.Classname;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.IPerceptProcessor;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.micro.IMicroExternalAccess;

/**
 *  Dumb prey vision processer.
 *  Updates the agent's "nearest_food" belief.
 */
public class MicroPreyVisionProcessor	extends	SimplePropertyObject	implements IPerceptProcessor
{
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
		SServiceProvider.getServiceUpwards(space.getExternalAccess().getServiceProvider(), IComponentManagementService.class).addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				IComponentManagementService ces = (IComponentManagementService)result;
				ces.getExternalAccess(agent.getName()).addResultListener(new IResultListener()
				{
					public void exceptionOccurred(Exception exception)
					{
						// May happen when agent has been killed concurrently.
//						exception.printStackTrace();
					}
					public void resultAvailable(Object result)
					{
						final Space2D	space2d	= (Space2D)space;
						final IMicroExternalAccess	exta	= (IMicroExternalAccess)result;
						exta.scheduleStep(new IComponentStep<Void>()
						{
							@Classname("food")
							public IFuture<Void> execute(IInternalAccess ia)
							{
								MicroPreyAgent	mp	= (MicroPreyAgent)ia;
								ISpaceObject	nearfood	= mp.getNearestFood();
								
								// Remember new food only if nearer than other known food (if any).
								if(type.equals("food_seen"))
								{
									if(nearfood==null
										|| space2d.getDistance((IVector2)avatar.getProperty(Space2D.PROPERTY_POSITION),
												(IVector2)nearfood.getProperty(Space2D.PROPERTY_POSITION))
										.greater(
											space2d.getDistance((IVector2)avatar.getProperty(Space2D.PROPERTY_POSITION),
												(IVector2)((ISpaceObject)percept).getProperty(Space2D.PROPERTY_POSITION))))
									{
										mp.setNearestFood((ISpaceObject)percept);
									}
								}
								// Remove disappeared food from belief.
								else if(percept.equals(nearfood) && (type.equals("food_out_of_sight") || type.equals("food_eaten")))
								{
									mp.setNearestFood(null);
								}
								
								return IFuture.DONE;
							}
						});
					}
				});
			}
		});
	}
}