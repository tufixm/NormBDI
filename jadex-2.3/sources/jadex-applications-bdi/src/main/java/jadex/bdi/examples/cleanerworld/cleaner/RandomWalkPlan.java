package jadex.bdi.examples.cleanerworld.cleaner;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.extension.envsupport.math.Vector2Double;


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
//		getLogger().info("Created: "+this+" for goal "+getRootGoal());
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		double x_dest = Math.random();
		double y_dest = Math.random();
		Vector2Double dest = new Vector2Double(x_dest, y_dest);
		IGoal moveto = createGoal("achievemoveto");
		moveto.getParameter("location").setValue(dest);
//		System.out.println("Created: "+dest+" "+this);
		dispatchSubgoalAndWait(moveto);
//		System.out.println("Reached: "+dest+" "+this);
//		getLogger().info("Reached point: "+dest);
	}
}
