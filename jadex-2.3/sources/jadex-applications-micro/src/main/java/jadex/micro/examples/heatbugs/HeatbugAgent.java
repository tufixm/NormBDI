package jadex.micro.examples.heatbugs;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.extension.envsupport.environment.ISpaceAction;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector1Int;
import jadex.micro.MicroAgent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  The heatbug agent.
 */
public class HeatbugAgent extends MicroAgent
{
	//-------- attributes --------
	
	/** The probability of a random move. */
	protected double randomchance;
	
	/** The desired temperature. */
	protected double ideal_temp;
	
	/** The current temperature. */
	protected double mytemp;
	
	/** The current unhappiness. */
	protected double unhappiness;


	//-------- methods --------
	
	/**
	 *  Execute an agent step.
	 */
	public IFuture<Void> executeBody()
	{
		getParentAccess().getExtension("mygc2dspace").addResultListener(createResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				if(result==null)
					return;
				
				final Grid2D grid = (Grid2D)result;
				ISpaceObject avatar = grid.getAvatar(getComponentDescription());
				
//						unhappiness = Math.abs(ideal_temp - temp);
				randomchance = ((Number)avatar.getProperty("random_move_chance")).doubleValue();
				ideal_temp = ((Number)avatar.getProperty("ideal_temp")).doubleValue();
//						System.out.println("ideal_temp: "+ideal_temp+" "+getArgument("ideal_temp"));
				
				IComponentStep com = new IComponentStep<Void>()
				{
					public IFuture<Void> execute(IInternalAccess ia)
					{
						ISpaceObject avatar = grid.getAvatar(getComponentDescription());
						IVector2 mypos = (IVector2)avatar.getProperty(Space2D.PROPERTY_POSITION);
						Collection coll	= grid.getSpaceObjectsByGridPosition(mypos, "patch");
						if(coll!=null)
						{
							ISpaceObject patch = (ISpaceObject)coll.iterator().next();
							mytemp = ((Number)patch.getProperty("heat")).doubleValue();
	
							unhappiness = ((Number)avatar.getProperty("unhappiness")).doubleValue();
							if(unhappiness>0)
							{
								Set tmp = grid.getNearObjects((IVector2)avatar.getProperty(
									Space2D.PROPERTY_POSITION), new Vector1Int(1), "patch");
								tmp.remove(patch);
								ISpaceObject[] neighbors = (ISpaceObject[])tmp.toArray(new ISpaceObject[tmp.size()]); 
								
								IVector2 target = null;
								if(Math.random()<randomchance)
								{
					//				for(int tries=0; target==null && tries<10; tries++)
					//				{
										int choice = (int)(Math.random()*neighbors.length);
										IVector2 choicepos = (IVector2)neighbors[choice].getProperty(Space2D.PROPERTY_POSITION);
					//					if(grid.getSpaceObjectsByGridPosition(choicepos, "heatbug")==null)
										target = choicepos;
					//				}
								}
								else
								{
									if(mytemp>ideal_temp)
									{
										ISpaceObject min = patch;
										double minheat = mytemp;
										for(int i=0; i<neighbors.length; i++)
										{
											double heat = ((Number)neighbors[i].getProperty("heat")).doubleValue();
											if(heat<minheat)
											{
												min = neighbors[i];
												minheat = heat;
											}
										}
										target = (IVector2)min.getProperty(Space2D.PROPERTY_POSITION);
									}
									else
									{
										ISpaceObject max = patch;
										double maxheat = mytemp;
										for(int i=0; i<neighbors.length; i++)
										{
											double heat = ((Number)neighbors[i].getProperty("heat")).doubleValue();
											if(heat>maxheat)
											{
												max = neighbors[i];
												maxheat = heat;
											}
										}
										target = (IVector2)max.getProperty(Space2D.PROPERTY_POSITION);
									}
								}
								
	//									if(!target.equals(mypos))
								{
	//										System.out.println("res: "+avatar.getProperty(ISpaceObject.PROPERTY_OWNER)+" "+target);
									Map params = new HashMap();
									params.put(ISpaceAction.OBJECT_ID, avatar.getId());
									params.put(MoveAction.PARAMETER_POSITION, target);
									grid.performSpaceAction("move", params, null);
								}
							}
						}
						
						waitForTick(this);
						return IFuture.DONE;
					}
					
					public String toString()
					{
						return "heatbug.body()";
					}
				};
				
				waitForTick(com);
			}
		}));
		
		return new Future<Void>(); // never kill?!
	}
}
