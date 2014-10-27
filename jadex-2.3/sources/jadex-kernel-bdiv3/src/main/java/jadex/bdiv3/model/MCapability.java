package jadex.bdiv3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 */
public class MCapability extends MElement
{
	protected List<MBelief> beliefs;;

	protected List<MGoal> goals;;
	
	protected List<MPlan> plans;;
	
	protected List<MConfiguration> configurations;
	
	/**
	 *  Create a capability.
	 */
	public MCapability(String name)
	{
		super(name);
	}

	/**
	 *  Get the beliefs.
	 *  @return The beliefs.
	 */
	public List<MBelief> getBeliefs()
	{
		return beliefs==null? Collections.EMPTY_LIST: beliefs;
	}

	/**
	 *  Set the beliefs.
	 *  @param beliefs The beliefs to set.
	 */
	public void setBeliefs(List<MBelief> beliefs)
	{
		this.beliefs = beliefs;
	}

	/**
	 *  Add a belief.
	 */
	public void addBelief(MBelief belief)
	{
		if(beliefs==null)
			beliefs = new ArrayList<MBelief>();
		beliefs.add(belief);
	}
	
	/**
	 *  Test if a belief is contained.
	 */
	public boolean hasBelief(String name)
	{
		boolean ret = false;
		
		if(beliefs!=null && name!=null)
		{
			for(MBelief bel: beliefs)
			{
				ret = name.equals(bel.getName());
				if(ret)
					break;
			}
		}
		
		return ret;
	}
	
	/**
	 *  Get a belief.
	 */
	public MBelief getBelief(String name)
	{
		MBelief ret = null;
		
		if(beliefs!=null && name!=null)
		{
			for(MBelief bel: beliefs)
			{
				if(name.equals(bel.getName()))
				{
					ret = bel;
					break;
				}
			}
		}
		
		return ret;
	}
	
	
	/**
	 *  Get the goals.
	 *  @return The goals.
	 */
	public List<MGoal> getGoals()
	{
		return goals==null? Collections.EMPTY_LIST: goals;
	}

	/**
	 *  Set the goals.
	 *  @param goals The goals to set.
	 */
	public void setGoals(List<MGoal> goals)
	{
		this.goals = goals;
	}
	
	/**
	 *  Add a goal.
	 */
	public void addGoal(MGoal goal)
	{
		if(goals==null)
			goals = new ArrayList<MGoal>();
		goals.add(goal);
	}
	
	/**
	 *  Get the goal for its name.
	 *  @return The goal.
	 */
	public MGoal getGoal(String name)
	{
		MGoal ret = null;
		if(goals!=null)
		{
			for(MGoal goal: goals)
			{
				if(goal.getName().equals(name))
				{
					ret = goal;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 *  Get the plans.
	 *  @return The plans.
	 */
	public List<MPlan> getPlans()
	{
		return plans==null? Collections.EMPTY_LIST: plans;
	}

	/**
	 *  Set the plans.
	 *  @param plans The plans to set.
	 */
	public void setPlans(List<MPlan> plans)
	{
		this.plans = plans;
	}
	
	/**
	 *  Add a plan.
	 */
	public void addPlan(MPlan plan)
	{
		if(plans==null)
			plans = new ArrayList<MPlan>();
		plans.add(plan);
	}
	
	/**
	 *  Get the plan for its name.
	 *  @return The plan.
	 */
	public MPlan getPlan(String name)
	{
		MPlan ret = null;
		if(plans!=null)
		{
			for(MPlan plan: plans)
			{
				if(plan.getName().equals(name))
				{
					ret = plan;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 *  Get the configurations.
	 *  @return The configurations.
	 */
	public List<MConfiguration> getConfigurations()
	{
		return configurations==null? Collections.EMPTY_LIST: configurations;
	}

	/**
	 *  Set the configurations.
	 *  @param configurations The configurations to set.
	 */
	public void setConfigurations(List<MConfiguration> configurations)
	{
		this.configurations = configurations;
	}
	
	/**
	 *  Add a configuration.
	 */
	public void addConfiguration(MConfiguration config)
	{
		if(configurations==null)
			configurations = new ArrayList<MConfiguration>();
		configurations.add(config);
	}
	
	/**
	 *  Get the configurations.
	 *  @return The configurations.
	 */
	public MConfiguration getConfiguration(String name)
	{
		MConfiguration ret = null;
		if(configurations!=null)
		{
			for(MConfiguration conf: configurations)
			{
				if(conf.getName().equals(name))
				{
					ret = conf;
					break;
				}
			}
		}
		return ret;
	}
}
