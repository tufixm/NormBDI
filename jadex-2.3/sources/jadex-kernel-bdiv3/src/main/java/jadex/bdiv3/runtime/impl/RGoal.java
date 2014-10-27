package jadex.bdiv3.runtime.impl;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.actions.AdoptGoalAction;
import jadex.bdiv3.actions.DropGoalAction;
import jadex.bdiv3.actions.SelectCandidatesAction;
import jadex.bdiv3.model.MDeliberation;
import jadex.bdiv3.model.MGoal;
import jadex.bdiv3.model.MethodInfo;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bdiv3.runtime.IGoal;
import jadex.bridge.IInternalAccess;
import jadex.commons.SUtil;
import jadex.commons.future.IResultListener;
import jadex.rules.eca.Event;
import jadex.rules.eca.ICondition;
import jadex.rules.eca.IEvent;
import jadex.rules.eca.IRule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class RGoal extends RProcessableElement implements IGoal
{
	//-------- goal lifecycle states --------
	
	/** The lifecycle state "new" (just created). */
	public static final String	GOALLIFECYCLESTATE_NEW	= "new";

	/** The lifecycle state "adopted" (adopted, but not active). */
	public static final String	GOALLIFECYCLESTATE_ADOPTED	= "adopted";

	/** The lifecycle state "option" (adopted, but not active). */
	public static final String	GOALLIFECYCLESTATE_OPTION	= "option";

	/** The lifecycle state "active" (adopted and processed or monitored). */
	public static final String	GOALLIFECYCLESTATE_ACTIVE	= "active";

	/** The lifecycle state "active" (adopted and processed or monitored). */
	public static final String	GOALLIFECYCLESTATE_SUSPENDED	= "suspended";

	/** The lifecycle state "dropping" (just before finished, but still dropping its subgoals). */
	public static final String	GOALLIFECYCLESTATE_DROPPING	= "dropping";

	/** The lifecycle state "dropped" (goal and all subgoals finished). */
	public static final String	GOALLIFECYCLESTATE_DROPPED	= "dropped";
	
	public static Set<String> GOALLIFECYCLE_STATES = SUtil.createHashSet(new String[]
	{
		GOALLIFECYCLESTATE_NEW,
		GOALLIFECYCLESTATE_ADOPTED,
		GOALLIFECYCLESTATE_OPTION,
		GOALLIFECYCLESTATE_ACTIVE,
		GOALLIFECYCLESTATE_SUSPENDED,
		GOALLIFECYCLESTATE_DROPPING,
		GOALLIFECYCLESTATE_DROPPED
	});
	
	//-------- goal processing states --------
	
	/** The goal idle state. */
	public static final String	GOALPROCESSINGSTATE_IDLE	= "idle";
	
	/** The goal in-process state. */
	public static final String	GOALPROCESSINGSTATE_INPROCESS	= "in-process";

	/** The goal paused state. */
	public static final String	GOALPROCESSINGSTATE_PAUSED	= "paused";
	
	/** The goal succeeded state. */
	public static final String	GOALPROCESSINGSTATE_SUCCEEDED	= "succeeded";
	
	/** The goal failed state. */
	public static final String	GOALPROCESSINGSTATE_FAILED	= "failed";

	public static Set<String> GOALPROCESSINGSTATE_STATES = SUtil.createHashSet(new String[]
	{
		GOALPROCESSINGSTATE_IDLE,
		GOALPROCESSINGSTATE_INPROCESS,
		GOALPROCESSINGSTATE_PAUSED,
		GOALPROCESSINGSTATE_SUCCEEDED,
		GOALPROCESSINGSTATE_FAILED
	});
	
	/** The lifecycle state. */
	protected String lifecyclestate;

	/** The processing state. */
	protected String processingstate;

	/** The exception. */
	protected Exception exception;
	
//	/** The observing rules. */
//	protected List<String> rulenames;
	
	/** The goal listeners. */
	protected List<IResultListener<Void>> listeners;
	
	/** The parent plan. */
	protected RPlan parentplan;
	
	/** The child plan. */
	protected RPlan childplan;
	
	/** Flag if goal is declarative. */
//	protected boolean declarative;
//	protected boolean maintain; // hack remove me
	
	/** The set of inhibitors. */
	protected Set<RGoal> inhibitors;

	/** The internal access. */
	protected IInternalAccess ia;
	
	/**
	 *  Create a new rgoal. 
	 */
	public RGoal(IInternalAccess ia, MGoal mgoal, Object goal, RPlan parentplan)
	{
		super(mgoal, goal);
		this.ia = ia;
		this.parentplan = parentplan;
		this.lifecyclestate = GOALLIFECYCLESTATE_NEW;
		this.processingstate = GOALPROCESSINGSTATE_IDLE;
	}

	/**
	 * 
	 */
	public static void adoptGoal(RGoal rgoal, IInternalAccess ia)
	{
		BDIAgentInterpreter ip = (BDIAgentInterpreter)((BDIAgent)ia).getInterpreter();
		ip.scheduleStep(new AdoptGoalAction(rgoal));
	}
	
	/**
	 *  Get the parentplan.
	 *  @return The parentplan.
	 */
	public RPlan getParentPlan()
	{
		return parentplan;
	}

	/**
	 *  Get the lifecycleState.
	 *  @return The lifecycleState.
	 */
	public String getLifecycleState()
	{
		return lifecyclestate;
	}

	/**
	 *  Set the lifecycleState.
	 *  @param lifecycleState The lifecycleState to set.
	 */
	public void setLifecycleState(String lifecyclestate)
	{
		if(!GOALLIFECYCLE_STATES.contains(lifecyclestate))
			throw new IllegalArgumentException("Unknown state: "+lifecyclestate);

		this.lifecyclestate = lifecyclestate;
	}

	/**
	 *  Get the processingState.
	 *  @return The processingState.
	 */
	public String getProcessingState()
	{
		return processingstate;
	}

	/**
	 *  Set the processingState.
	 *  @param processingState The processingState to set.
	 */
	public void setProcessingState(String processingstate)
	{
		if(!GOALPROCESSINGSTATE_STATES.contains(processingstate))
			throw new IllegalArgumentException("Unknown state: "+processingstate);
		
		this.processingstate = processingstate;
	}
	
	/**
	 *  Set the processingState.
	 *  @param processingState The processingState to set.
	 */
	public void setProcessingState(IInternalAccess ia, String processingstate)
	{
//		this.processingstate = processingstate;
	
		// If was inprocess -> now stop processing.
//		Object	curstate	= state.getAttributeValue(rgoal, OAVBDIRuntimeModel.goal_has_processingstate);
//		System.out.println("changeprocstate: "+rgoal+" "+newstate+" "+curstate);

		if(!RGoal.GOALPROCESSINGSTATE_INPROCESS.equals(processingstate))
		{
			// todo: introduce some state for finished?!
//			state.setAttributeValue(rgoal, OAVBDIRuntimeModel.processableelement_has_state, null);
			setState(PROCESSABLEELEMENT_INITIAL);
			
			// Remove finished plans that would otherwise interfere with next goal processing (if any).
//			Collection	fplans	= state.getAttributeValues(rgoal, OAVBDIRuntimeModel.goal_has_finishedplans);
//			if(fplans!=null && !fplans.isEmpty())
//			{
//				Object[]	afplans	= fplans.toArray();
//				for(int i=0; i<afplans.length; i++)
//					state.removeAttributeValue(rgoal, OAVBDIRuntimeModel.goal_has_finishedplans, afplans[i]);
//			}
			
			// Reset event processing.
//			BDIInterpreter ip = BDIInterpreter.getInterpreter(state);
//			if(rgoal.equals(state.getAttributeValue(ip.getAgent(), OAVBDIRuntimeModel.agent_has_eventprocessing)))
//				state.setAttributeValue(ip.getAgent(), OAVBDIRuntimeModel.agent_has_eventprocessing, null);
			
			// Reset APL.
//			state.setAttributeValue(rgoal, OAVBDIRuntimeModel.processableelement_has_apl, null);
			setApplicablePlanList(null);
			
			// Clean tried plans if necessary.
			setTriedPlans(null);
//			Collection coll = state.getAttributeValues(rgoal, OAVBDIRuntimeModel.goal_has_triedmplans);
//			if(coll!=null)
//			{
//				Object[]	acoll	= coll.toArray();
//				for(int i=0; i<acoll.length; i++)
//				{
//					state.removeAttributeValue(rgoal, OAVBDIRuntimeModel.goal_has_triedmplans, acoll[i]);
//				}
//			}
			
//			// Remove timers.
//			ITimer retrytimer = (ITimer)state.getAttributeValue(rgoal, OAVBDIRuntimeModel.goal_has_retrytimer);
//			if(retrytimer!=null)
//			{
//				retrytimer.cancel();
//				((InterpreterTimedObject)retrytimer.getTimedObject()).getAction().setValid(false);
//			}
			
			
//			if(!OAVBDIRuntimeModel.GOALPROCESSINGSTATE_PAUSED.equals(newstate))
//			{
//				ITimer recurtimer = (ITimer)state.getAttributeValue(rgoal, OAVBDIRuntimeModel.goal_has_recurtimer);
//				if(recurtimer!=null)
//				{
//					recurtimer.cancel();
//					((InterpreterTimedObject)recurtimer.getTimedObject()).getAction().setValid(false);
//				}
//			}
		}
		
		setProcessingState(processingstate);
		BDIAgentInterpreter ip = (BDIAgentInterpreter)((BDIAgent)ia).getInterpreter();
		
//		state.setAttributeValue(rgoal, OAVBDIRuntimeModel.goal_has_processingstate, newstate);
		
		// If now is inprocess -> start processing
		if(RGoal.GOALPROCESSINGSTATE_INPROCESS.equals(processingstate))
		{
//			if(getId().indexOf("AchieveCleanup")!=-1)
//				System.out.println("activating: "+this);
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALINPROCESS, this));
			setState(ia, RProcessableElement.PROCESSABLEELEMENT_UNPROCESSED);
		}
		else
		{
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALNOTINPROCESS, this));
		}
		
		if(RGoal.GOALPROCESSINGSTATE_SUCCEEDED.equals(processingstate)
			|| RGoal.GOALPROCESSINGSTATE_FAILED.equals(processingstate))
		{
			setLifecycleState(ia, GOALLIFECYCLESTATE_DROPPING);
		}
		
//		System.out.println("exit: "+rgoal+" "+state.getAttributeValue(rgoal, OAVBDIRuntimeModel.processableelement_has_state));
	}
	
	/**
	 *  Set the lifecycle state.
	 *  @param processingState The processingState to set.
	 */
	public void setLifecycleState(IInternalAccess ia, String lifecyclestate)
	{
		if(lifecyclestate.equals(getLifecycleState()))
			return;
		
//		System.out.println("goal state change: "+this.getId()+" "+getLifecycleState()+" "+lifecyclestate);
//		if(RGoal.GOALLIFECYCLESTATE_DROPPING.equals(lifecyclestate) && RGoal.GOALLIFECYCLESTATE_NEW.equals(getLifecycleState()))
//			Thread.dumpStack();
//		if(RGoal.GOALLIFECYCLESTATE_ADOPTED.equals(lifecyclestate) && RGoal.GOALLIFECYCLESTATE_DROPPING.equals(getLifecycleState()))
//			Thread.dumpStack();
//		if(RGoal.GOALLIFECYCLESTATE_DROPPED.equals(getLifecycleState()))
//			Thread.dumpStack();
		
//		if(getId().indexOf("QueryCharging")!=-1 && GOALLIFECYCLESTATE_DROPPING.equals(lifecyclestate))
//			System.out.println("goal state change: "+this.getId()+" "+getLifecycleState()+" "+lifecyclestate);
//		if(getId().indexOf("Battery")!=-1 && GOALLIFECYCLESTATE_DROPPING.equals(lifecyclestate))
//			System.out.println("goal state change: "+this.getId()+" "+getLifecycleState()+" "+lifecyclestate);
//		if(getId().indexOf("AchieveCleanup")!=-1)
//			System.out.println("goal state change: "+this.getId()+" "+getLifecycleState()+" "+lifecyclestate);

		BDIAgentInterpreter ip = (BDIAgentInterpreter)((BDIAgent)ia).getInterpreter();
		setLifecycleState(lifecyclestate);
		
		if(GOALLIFECYCLESTATE_ADOPTED.equals(lifecyclestate))
		{
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALADOPTED, this));
			setLifecycleState(ia, GOALLIFECYCLESTATE_OPTION);
		}
		else if(GOALLIFECYCLESTATE_ACTIVE.equals(lifecyclestate))
		{
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALACTIVE, this));

			// start means-end reasoning
			if(onActivate())
			{
				setProcessingState(ia, GOALPROCESSINGSTATE_INPROCESS);
			}
			else
			{
				setProcessingState(ia, GOALPROCESSINGSTATE_IDLE);
			}
		}
		else if(GOALLIFECYCLESTATE_OPTION.equals(lifecyclestate))
		{
			// ready to be activated via deliberation
//			if(getId().indexOf("AchieveCleanup")!=-1)
//				System.out.println("option: "+ChangeEvent.GOALOPTION+"."+getId());
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALOPTION, this));
			abortPlans();
		}
		else if(GOALLIFECYCLESTATE_SUSPENDED.equals(lifecyclestate))
		{
			// goal is suspended (no more plan executions)
//			if(getId().indexOf("PerformLook")==-1)
//				System.out.println("suspending: "+getId());
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALSUSPENDED, this));
			abortPlans();
		}
		
		if(GOALLIFECYCLESTATE_DROPPING.equals(lifecyclestate))
		{
//			if(getId().indexOf("AchieveCleanup")!=-1)
//				System.out.println("dropping achievecleanup");
			
//			if(getId().indexOf("GetVisionAction")==-1)
//				System.out.println("dropping: "+getId());
			
//			System.out.println("dropping: "+getId());
			
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALDROPPED, this));
			// goal is dropping (no more plan executions)
			abortPlans();
			setState(PROCESSABLEELEMENT_INITIAL);
			ia.getExternalAccess().scheduleStep(new DropGoalAction(this));
		}
		else if(GOALLIFECYCLESTATE_DROPPED.equals(lifecyclestate))
		{
			if(listeners!=null)
			{
				for(IResultListener<Void> lis: listeners)
				{
					if(isSucceeded())
					{
						lis.resultAvailable(null);
					}
					else if(isFailed())
					{
						lis.exceptionOccurred(exception);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	protected void abortPlans()
	{
		if(childplan!=null)
			childplan.abortPlan();
	}
	
	/**
	 * 
	 */
	protected MGoal getMGoal()
	{
		return (MGoal)getModelElement();
	}
	
	/**
	 * 
	 */
	public boolean isSucceeded()
	{
		return RGoal.GOALPROCESSINGSTATE_SUCCEEDED.equals(processingstate);
	}
	
	/**
	 * 
	 */
	public boolean isFailed()
	{
		return RGoal.GOALPROCESSINGSTATE_FAILED.equals(processingstate);
	}
	
	/**
	 * 
	 */
	public boolean isFinished()
	{
		return isSucceeded() || isFailed();
	}
	
	/**
	 *  Get the exception.
	 *  @return The exception.
	 */
	public Exception getException()
	{
		return exception;
	}

	/**
	 *  Set the exception.
	 *  @param exception The exception to set.
	 */
	public void setException(Exception exception)
	{
		assert this.exception==null;
		this.exception = exception;
	}

//	/**
//	 *  Add a rule.
//	 */
//	protected void addRule(IRule<?> rule)
//	{
//		if(rulenames==null)
//			rulenames = new ArrayList<String>();
//		rulenames.add(rule.getName());
//	}
	
//	/**
//	 *  Unobserve a runtime goal.
//	 */
//	public void unobserveGoal(final IInternalAccess ia)
//	{
//		BDIAgentInterpreter ip = (BDIAgentInterpreter)((BDIAgent)ia).getInterpreter();
//			
//		ip.getRuleSystem().unobserveObject(getPojoElement());
//		
//		if(rulenames!=null)
//		{
//			for(String rulename: rulenames)
//			{
//				ip.getRuleSystem().getRulebase().removeRule(rulename);
//			}
//		}
//	}

	/**
	 * 
	 */
	public void addGoalListener(IResultListener<Void> listener)
	{
		if(listeners==null)
			listeners = new ArrayList<IResultListener<Void>>();
		
		if(isSucceeded())
		{
			listener.resultAvailable(null);
		}
		else if(isFailed())
		{
			listener.exceptionOccurred(exception);
		}
		else
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * 
	 */
	public void removeGoalListener(IResultListener<Void> listener)
	{
		if(listeners!=null)
			listeners.remove(listener);
	}
	
	/**
	 *  Get the childplan.
	 *  @return The childplan.
	 */
	public RPlan getChildPlan()
	{
		return childplan;
	}

	/**
	 *  Set the childplan.
	 *  @param childplan The childplan to set.
	 */
	public void setChildPlan(RPlan childplan)
	{
		this.childplan = childplan;
	}
	
	/**
	 * 
	 */
	protected void addInhibitor(RGoal inhibitor, IInternalAccess ia)
	{		
		if(inhibitors==null)
			inhibitors = new HashSet<RGoal>();
		
		if(inhibitors.add(inhibitor) && inhibitors.size()==1)
		{
			BDIAgentInterpreter ip = (BDIAgentInterpreter)((BDIAgent)ia).getInterpreter();
			ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALINHIBITED, this));
		}
		
//		if(inhibitor.getId().indexOf("AchieveCleanup")!=-1)
//			System.out.println("add inhibit: "+getId()+" "+inhibitor.getId()+" "+inhibitors);
	}
	
	/**
	 * 
	 */
	protected void removeInhibitor(RGoal inhibitor, IInternalAccess ia)
	{
//		System.out.println("rem inhibit: "+getId()+" "+inhibitor.getId()+" "+inhibitors);
		
//		if(inhibitor.getId().indexOf("AchieveCleanup")!=-1)
//			System.out.println("kokoko: "+inhibitor);
		
		if(inhibitors!=null)
		{
			if(inhibitors.remove(inhibitor) && inhibitors.size()==0)
			{
//				System.out.println("goal not inhibited: "+this);
				BDIAgentInterpreter ip = (BDIAgentInterpreter)((BDIAgent)ia).getInterpreter();
				ip.getRuleSystem().addEvent(new Event(ChangeEvent.GOALNOTINHIBITED, this));
			}
		}
	}
	
	/**
	 * 
	 */
	protected boolean isInhibited()
	{
		return inhibitors!=null && !inhibitors.isEmpty();
	}
	
	/**
	 * 
	 */
	protected boolean isInhibitedBy(RGoal other)
	{
		return !isFinished() && inhibitors!=null && inhibitors.contains(other);
	}
	
	/**
	 *  Get the inhibitors.
	 *  @return The inhibitors.
	 */
	public Set<RGoal> getInhibitors()
	{
		return inhibitors;
	}

	/**
	 *  Get the hashcode.
	 */
	public int hashCode()
	{
		return getMGoal().isUnique()? getPojoElement().hashCode(): super.hashCode();
	}

	/**
	 *  Test if equal to other object.
	 */
	public boolean equals(Object obj)
	{
		boolean ret = false;
		if(obj instanceof RGoal)
		{
			ret = getMGoal().isUnique()? getPojoElement().equals(((RGoal)obj).getPojoElement()): super.equals(obj);
		}
		return ret;
	}

	/** 
	 * 
	 */
	public String toString()
	{
//		return "RGoal(lifecyclestate=" + lifecyclestate + ", processingstate="
//			+ processingstate + ", state=" + state + ", id=" + id + ")";
		return id+" "+getPojoElement();
	}
	
	/**
	 * 
	 */
	public void planFinished(IInternalAccess ia, RPlan rplan)
	{
		super.planFinished(ia, rplan);
		childplan = null;
		
//		if(getPojoElement().getClass().getName().indexOf("PatrolPlan")!=-1)
//			System.out.println("pips");
		
		// create reasoning step depending on the processable element type

		// Check procedural success semantics
		if(isProceduralSucceeded())
		{
			setProcessingState(ia, RGoal.GOALPROCESSINGSTATE_SUCCEEDED);
		}
		
		if(RGoal.GOALLIFECYCLESTATE_ACTIVE.equals(getLifecycleState()))
		{
			if(!isSucceeded() && !isFailed())
			{
				// Test if is retry
				if(isRetry() && rplan!=null)
				{
					if(RProcessableElement.PROCESSABLEELEMENT_CANDIDATESSELECTED.equals(getState()))
					{
						if(getMGoal().getRetryDelay()>-1)
						{
							ia.getExternalAccess().scheduleStep(new SelectCandidatesAction(this), getMGoal().getRetryDelay());
						}
						else
						{
							ia.getExternalAccess().scheduleStep(new SelectCandidatesAction(this));
						}
					}
					else if(RProcessableElement.PROCESSABLEELEMENT_NOCANDIDATES.equals(getState()))
					{
						setException(new GoalFailureException("No canditates."));
						setProcessingState(ia, GOALPROCESSINGSTATE_FAILED);
					}
//					else
//					{
//						System.out.println("??? "+getState());
//					}
				}
				else
				{
					if(isRecur())
					{
						setProcessingState(ia, GOALPROCESSINGSTATE_PAUSED);
					}
					else
					{
						setException(new GoalFailureException("No canditates 2."));
						setProcessingState(ia, GOALPROCESSINGSTATE_FAILED);
					}
				}
			}
		}
	}
	
	//-------- methods that are goal specific --------

	// todo: implement those methods in goal types
	
	/**
	 * 
	 */
	public boolean onActivate()
	{
		return !getMGoal().isMaintain(); // for perform, achieve, query
	}
	
	/**
	 * 
	 */
	public boolean isRetry()
	{
		return getMGoal().isRetry();
	}
	
	/**
	 * 
	 */
	public boolean isRecur()
	{
		return getMGoal().isRecur();
	}
	
	/**
	 * 
	 */
	public boolean isProceduralSucceeded()
	{
		boolean ret = false;
		
		// todo: perform goals
		if(isProceduralGoal() && getMGoal().isSucceedOnPassed() 
			&& !getTriedPlans().isEmpty())
		{
			RPlan rplan = getTriedPlans().get(getTriedPlans().size()-1);
			ret = rplan.isPassed();
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public boolean isProceduralGoal()
	{
		return !getMGoal().isDeclarative();
	}
	
	/**
	 *  Get the goal result of the pojo element.
	 *  Searches @GoalResult and delivers value.
	 */
	public static Object getGoalResult(Object pojo, MGoal mgoal, ClassLoader cl)
	{
		Object ret = pojo;
		Object pac = mgoal.getPojoResultAccess(cl);
		if(pac instanceof Field)
		{
			try
			{
				Field f = (Field)pac;
				f.setAccessible(true);
				ret = f.get(pojo);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(pac instanceof Method)
		{
			try
			{
				Method m = (Method)pac;
				m.setAccessible(true);
				ret = m.invoke(pojo, new Object[0]);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
	 * 
	 */
	public void drop()
	{
		if(!RGoal.GOALLIFECYCLESTATE_NEW.equals(getLifecycleState())
			&& !RGoal.GOALLIFECYCLESTATE_DROPPING.equals(getLifecycleState()) 
			&& !RGoal.GOALLIFECYCLESTATE_DROPPED.equals(getLifecycleState()))
		{
			setLifecycleState(ia, RGoal.GOALLIFECYCLESTATE_DROPPING);
		}
	}
	
	/**
	 * 
	 */
	public void targetConditionTriggered(IInternalAccess ia, IEvent event, IRule<Void> rule, Object context)
	{
//		System.out.println("Goal target triggered: "+RGoal.this);
		if(getMGoal().isMaintain())
		{
			setProcessingState(ia, RGoal.GOALPROCESSINGSTATE_IDLE);
		}
		else
		{
			setProcessingState(ia, RGoal.GOALPROCESSINGSTATE_SUCCEEDED);
		}
	}
	
	/**
	 *  Test if this goal inhibits the other.
	 */
	protected boolean inhibits(RGoal other, IInternalAccess ia)
	{
		if(this.equals(other))
			return false;
		
		// todo: cardinality
		
		boolean ret = false;
		
		if(getLifecycleState().equals(RGoal.GOALLIFECYCLESTATE_ACTIVE) && getProcessingState().equals(RGoal.GOALPROCESSINGSTATE_INPROCESS))
		{
			MDeliberation delib = getMGoal().getDeliberation();
			if(delib!=null)
			{
				Set<MGoal> minh = delib.getInhibitions();
				MGoal mother = other.getMGoal();
				if(minh.contains(mother))
				{
					ret = true;
					
					// check if instance relation
					Map<String, MethodInfo> dms = delib.getInhibitionMethods();
					if(dms!=null)
					{
						MethodInfo mi = dms.get(mother.getName());
						if(mi!=null)
						{
							Method dm = mi.getMethod(ia.getClassLoader());
							try
							{
								dm.setAccessible(true);
								ret = ((Boolean)dm.invoke(getPojoElement(), new Object[]{other.getPojoElement()})).booleanValue();
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	class LifecycleStateCondition implements ICondition
	{
		/** The allowed states. */
		protected Set<String> states;
		
		/** The flag if state is allowed or disallowed. */
		protected boolean allowed;
		
		/**
		 * 
		 */
		public LifecycleStateCondition(String state)
		{
			this(SUtil.createHashSet(new String[]{state}));
		}
		
		/**
		 * 
		 */
		public LifecycleStateCondition(Set<String> states)
		{
			this(states, true);
		}
		
		/**
		 * 
		 */
		public LifecycleStateCondition(String state, boolean allowed)
		{
			this(SUtil.createHashSet(new String[]{state}), allowed);
		}
		
		/**
		 * 
		 */
		public LifecycleStateCondition(Set<String> states, boolean allowed)
		{
			this.states = states;
			this.allowed = allowed;
		}
		
		/**
		 * 
		 */
		public boolean evaluate(IEvent event)
		{
			boolean ret = states.contains(getLifecycleState());
			if(!allowed)
				ret = !ret;
			return ret;
		}
	}
	
	/**
	 * 
	 */
	class ProcessingStateCondition implements ICondition
	{
		/** The allowed states. */
		protected Set<String> states;
		
		/** The flag if state is allowed or disallowed. */
		protected boolean allowed;
		
		/**
		 * 
		 */
		public ProcessingStateCondition(String state)
		{
			this(SUtil.createHashSet(new String[]{state}));
		}
		
		/**
		 * 
		 */
		public ProcessingStateCondition(Set<String> states)
		{
			this(states, true);
		}
		
		/**
		 * 
		 */
		public ProcessingStateCondition(String state, boolean allowed)
		{
			this(SUtil.createHashSet(new String[]{state}), allowed);
		}
		
		/**
		 * 
		 */
		public ProcessingStateCondition(Set<String> states, boolean allowed)
		{
			this.states = states;
			this.allowed = allowed;
		}
		
		/**
		 * 
		 */
		public boolean evaluate(IEvent event)
		{
			boolean ret = states.contains(getProcessingState());
			if(!allowed)
				ret = !ret;
			return ret;
		}
	}
}
