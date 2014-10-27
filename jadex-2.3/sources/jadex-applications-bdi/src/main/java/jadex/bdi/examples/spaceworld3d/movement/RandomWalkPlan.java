package jadex.bdi.examples.spaceworld3d.movement;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.extension.envsupport.environment.space3d.Space3D;
import jadex.extension.envsupport.math.IVector3;
import jadex.extension.envsupport.math.Vector3Int;

/**
 *  Wander around randomly.
 */
public class RandomWalkPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public RandomWalkPlan()
	{
		//getLogger().info("Created: "+this+" for goal "+getRootGoal());
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
//		System.out.println("RandomWalk: "+getComponentIdentifier());
		IVector3	dest	= ((Space3D)getBeliefbase().getBelief("environment").getFact()).getRandomPosition(Vector3Int.ZERO);
		IGoal	moveto	= createGoal("move_dest");
		moveto.getParameter("destination").setValue(dest);
		dispatchSubgoalAndWait(moveto);
		getLogger().info("Reached point: "+dest);
	}
}
