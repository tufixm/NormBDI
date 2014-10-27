package jadex.bpmn.model;

import jadex.bridge.ClassInfo;
import jadex.commons.SReflect;
import jadex.commons.collection.IndexMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *  Base class for all kinds of activities.
 */
public class MActivity extends MAssociationTarget
{
	//-------- attributes --------
	
	/** The lane description. */
	protected String lanedescription;

	/** The outgoing sequence edges description. */
	protected String outgoingsequenceedgesdescription;
	
	/** The incoming sequence edges description. */
	protected String incomingsequenceedgesdescription;
	
	/** The incoming messages description. */
	protected List incomingmessagesdescriptions;
	
	/** The outgoing messages description. */
	protected List outgoingmessagesdescriptions;

	
	/** The outgoing sequence edges. */
	protected List outseqedges;
	
	/** The incoming sequence edges. */
	protected List inseqedges;

	/** The outgoing message edges. */
	protected List outmsgedges;
	
	/** The incoming message edges. */
	protected List inmsgedges;
	
	/** The type. */
	protected String type;
	
	/** The activity type. */
	protected String activitytype;

	/** The looping flag. */
	protected boolean looping;
	
	/** The throwing flag. */
	protected boolean throwing;

	/** The event handlers. */
	protected List eventhandlers;
	
	/** The pool. */
	protected MPool pool;
		
	/** The lane (if any). */
	protected MLane lane;
		
	/** The flag if this activity is an event handler. */
	protected boolean eventhandler;
	
	//-------- added --------
	
	/** The parameters (name -> MParameter). */
	protected IndexMap	parameters;
	
	/** The class. */
	protected ClassInfo clazz;
	
	//-------- methods --------
	
	/**
	 *  Get the xml lane description.
	 *  @return The lane description.
	 */
	public String getLaneDescription()
	{
		return this.lanedescription;
	}

	/**
	 *  Set the xml lane description.
	 *  @param lanedescription The lane description to set.
	 */
	public void setLaneDescription(String lanedescription)
	{
		this.lanedescription = lanedescription;
	}
	
	/**
	 *  Get the xml outgoing sequence edges desription.
	 *  @return The outgoing sequence edges description.
	 */
	public String getOutgoingSequenceEdgesDescription()
	{
		return this.outgoingsequenceedgesdescription;
	}

	/**
	 *  Set the xml outgoing edges desription.
	 *  @param outgoingedges The outgoing edges to set.
	 */
	public void setOutgoingSequenceEdgesDescription(String outgoingedges)
	{
		this.outgoingsequenceedgesdescription = outgoingedges;
	}
	
	/**
	 *  Get the xml incoming edges description.
	 *  @return The incoming edges description.
	 */
	public String getIncomingSequenceEdgesDescription()
	{
		return this.incomingsequenceedgesdescription;
	}

	/**
	 *  Set the xml incoming edges description.
	 *  @param incomingedges The incoming edges to set.
	 */
	public void setIncomingSequenceEdgesDescription(String incomingedges)
	{
		this.incomingsequenceedgesdescription = incomingedges;
	}
	
	/**
	 *  Get the xml outgoing messages descriptions.
	 *  @return The outgoing messages descriptions. 
	 */
	public List getOutgoingMessagesDescriptions()
	{
		return outgoingmessagesdescriptions;
	}

	/**
	 *  Add an outgoing message description.
	 *  @param desc The description.
	 */
	public void addOutgoingMessageDescription(Object desc)
	{
		if(outgoingmessagesdescriptions==null)
			outgoingmessagesdescriptions = new ArrayList();
		outgoingmessagesdescriptions.add(desc);
	}
	
	/**
	 *  Remove an outgoing message description.
	 *  @param desc The description.
	 */
	public void removeOutgoingMessageDescription(Object desc)
	{
		if(outgoingmessagesdescriptions!=null)
			outgoingmessagesdescriptions.remove(desc);
	}
	
	/**
	 *  Get the incoming messages description.
	 *  @return The incoming messages descriptions.
	 */
	public List getIncomingMessagesDescriptions()
	{
		return incomingmessagesdescriptions;
	}

	/**
	 *  Add an incoming message description.
	 *  @param desc The description.
	 */
	public void addIncomingMessageDescription(Object desc)
	{
		if(incomingmessagesdescriptions==null)
			incomingmessagesdescriptions = new ArrayList();
		incomingmessagesdescriptions.add(desc);
	}
	
	/**
	 *  Remove an incoming message description.
	 *  @param desc The description.
	 */
	public void removeIncomingMessageDescription(Object desc)
	{
		if(incomingmessagesdescriptions!=null)
			incomingmessagesdescriptions.remove(desc);
	}
	
	
	/**
	 *  Get the outgoing sequence edges.
	 *  @return The outgoing edges.
	 */
	public List<MSequenceEdge> getOutgoingSequenceEdges()
	{
		return outseqedges;
	}

	/**
	 *  Add an outgoing edge.
	 *  @param edge The edge.
	 */
	public void addOutgoingSequenceEdge(MSequenceEdge edge)
	{
		if(outseqedges==null)
			outseqedges = new ArrayList();
		outseqedges.add(edge);
	}
	
	/**
	 *  Remove an outgoing edge.
	 *  @param edge The edge.
	 */
	public void removeOutgoingSequenceEdge(MSequenceEdge edge)
	{
		if(outseqedges!=null)
			outseqedges.remove(edge);
	}
	
	/**
	 *  Get the incoming edges.
	 *  @return The incoming edges.
	 */
	public List<MSequenceEdge> getIncomingSequenceEdges()
	{
		return inseqedges;
	}
	
	/**
	 *  Add an incoming edge.
	 *  @param edge The edge.
	 */
	public void addIncomingSequenceEdge(MSequenceEdge edge)
	{
		if(inseqedges==null)
			inseqedges = new ArrayList();
		inseqedges.add(edge);
	}
	
	/**
	 *  Remove an incoming edge.
	 *  @param edge The edge.
	 */
	public void removeIncomingSequenceEdge(MSequenceEdge edge)
	{
		if(inseqedges!=null)
			inseqedges.remove(edge);
	}
	
	/**
	 *  Get the outgoing message edges.
	 *  @return The outgoing message edges.
	 */
	public List getOutgoingMessagingEdges()
	{
		return outmsgedges;
	}

	/**
	 *  Add an outgoing message edge.
	 *  @param edge The edge.
	 */
	public void addOutgoingMessagingEdge(MMessagingEdge edge)
	{
		if(outmsgedges==null)
			outmsgedges = new ArrayList();
		outmsgedges.add(edge);
	}
	
	/**
	 *  Remove an outgoing message edge.
	 *  @param edge The edge.
	 */
	public void removeOutgoingMessagingEdge(MMessagingEdge edge)
	{
		if(outmsgedges!=null)
			outmsgedges.remove(edge);
	}
	
	/**
	 *  Get the inconimg message edges.
	 *  @return the incoming message edges.
	 */
	public List getIncomingMessagingEdges()
	{
		return inmsgedges;
	}
	
	/**
	 *  Add an incoming message edge.
	 *  @param edge The edge.
	 */
	public void addIncomingMessagingEdge(MMessagingEdge edge)
	{
		if(inmsgedges==null)
			inmsgedges = new ArrayList();
		inmsgedges.add(edge);
	}
	
	/**
	 *  Remove an incoming message edge.
	 *  @param edge The edge.
	 */
	public void removeIncomingMessagingEdge(MMessagingEdge edge)
	{
		if(inmsgedges!=null)
			inmsgedges.remove(edge);
	}

	/**
	 *  Get the type.
	 *  @return The type.
	 */
	public String getType()
	{
		return this.type;
	}

	/**
	 *  Set the type.
	 *  @param type The type to set.
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 *  Get the activity type.
	 *  @return The activity type.
	 */
	public String getActivityType()
	{
		return this.activitytype;
	}

	/**
	 *  Set the activity type.
	 *  @param activitytype The activity type to set.
	 */
	public void setActivityType(String activitytype)
	{
		this.activitytype = activitytype;
	}
	
	/**
	 *  Test if the activity is looping.
	 *  @return True, if looping.
	 */
	public boolean isLooping()
	{
		return this.looping;
	}

	/**
	 *  Set the looping state.
	 *  @param looping The looping state to set.
	 */
	public void setLooping(boolean looping)
	{
		this.looping = looping;
	}
	
	/**
	 *  Test if the activity is throwing.
	 *  @return True, if throwing.
	 */
	public boolean isThrowing()
	{
		return this.throwing;
	}

	/**
	 *  Set the throwing state.
	 *  @param throwing The throwing state to set.
	 */
	public void setThrowing(boolean throwing)
	{
		this.throwing = throwing;
	}
	
	
	/**
	 *  Get the event handlers.
	 *  @return The event handlers.
	 */
	public List<MActivity> getEventHandlers()
	{
		return eventhandlers;
	}
	
	/**
	 *  Add an event handler.
	 *  @param eventhandler The event handler.
	 */
	public void addEventHandler(MActivity eventhandler)
	{
		if(eventhandlers==null)
			eventhandlers = new ArrayList();
		eventhandlers.add(eventhandler);
	}
	
	/**
	 *  Remove an event handler.
	 *  @param eventhandler The event handler.
	 */
	public void removeEventHandler(MActivity eventhandler)
	{
		if(eventhandlers!=null)
			eventhandlers.remove(eventhandler);
	}
	
	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 */
	public IndexMap	getParameters()
	{
		return parameters;
	}
		
	/**
	 *  Get the in and inout parameters.
	 *  @return The in parameters.
	 * /
	public List getInParameters()
	{
		List inparams = new ArrayList();
		if(parameters!=null)
		{
			for(Iterator it=parameters.values().iterator(); it.hasNext(); )
			{
				MParameter param = (MParameter)it.next();
				if(MParameter.DIRECTION_IN.equals(param.getDirection())
					|| MParameter.DIRECTION_INOUT.equals(param.getDirection()))
				{
					inparams.add(param);
				}
			}
		}
		return inparams;
	}*/
	
	/**
	 *  Get the out and inout parameters.
	 *  @return The out parameters.
	 * /
	public List getOutParameters()
	{
		List outparams = new ArrayList();
		if(parameters!=null)
		{
			for(Iterator it=parameters.values().iterator(); it.hasNext(); )
			{
				MParameter param = (MParameter)it.next();
				if(MParameter.DIRECTION_OUT.equals(param.getDirection())
					|| MParameter.DIRECTION_INOUT.equals(param.getDirection()))
				{
					outparams.add(param);
				}
			}
		}
		return outparams;
	}*/
	
	/**
	 *  Get parameters of specific direction(s).
	 *  @return The in parameters.
	 */
	public List<MParameter> getParameters(String[] dirs)
	{
		Set test = new HashSet();
		if(dirs!=null)
		{
			for(int i=0; i<dirs.length; i++)
			{
				test.add(dirs[i]);
			}
		}
		List inparams = new ArrayList();
		if(parameters!=null)
		{
			for(Iterator it=parameters.values().iterator(); it.hasNext(); )
			{
				MParameter param = (MParameter)it.next();
				if(test.contains(param.getDirection()))
				{
					inparams.add(param);
				}
			}
		}
		return inparams;
	}
	
	/**
	 *  Test if a parameter exists.
	 */
	public boolean hasParameter(String name)
	{
		return parameters!=null && parameters.containsKey(name);
	}
	
	/**
	 *  Add a parameter.
	 *  @param param The parameter.
	 */
	public void addParameter(MParameter param)
	{
		if(parameters==null)
			parameters = new IndexMap();
		parameters.put(param.getName(), param);
	}
	
	/**
	 *  Remove a parameter.
	 *  @param param The parameter.
	 */
	public void removeParameter(MParameter param)
	{
		if(parameters!=null)
			parameters.removeKey(param.getName());
	}
	
	/**
	 *  Create a string representation of this activity.
	 *  @return A string representation of this activity.
	 */
	public String	toString()
	{		
		StringBuffer buf = new StringBuffer();
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(name=");
		buf.append(getName());
		buf.append(", activityType=");
		buf.append(getActivityType());
		buf.append(")");
		return buf.toString();
	}

	/**
	 *  Get the pool of the activity.
	 *  @return The pool of the activity.
	 */
	public MPool getPool()
	{
		return pool;
	}

	/**
	 *  Set the pool of the activity.
	 *  @param pool The pool of the activity.
	 */
	public void setPool(MPool pool)
	{
		this.pool	= pool;
	}

	/**
	 *  Get the lane of the activity.
	 *  @return The lane of the activity.
	 */
	public MLane getLane()
	{
		return lane;
	}

	/**
	 *  Set the lane of the activity.
	 *  @param lane The lane of the activity.
	 */
	public void setLane(MLane lane)
	{
		// eclipse STP has bugs regarding lanes.
		// The following at least identifies some inconsistencies (activity in multiple lanes).
		if(this.lane!=null && lane!=this.lane && lane!=null)
			throw new RuntimeException("Cannot add activity "+this+" to lane '"+lane.getName()+"'. Already contained in '"+this.lane.getName()+"'");
		
		this.lane	= lane;
	}

	/**
	 *  Get a string to identify this activity in a tool such as the debugger.
	 *  @return A unique but nicely readable name.
	 */
	public String getBreakpointId()
	{
		String	name	= getName();
		if(name==null)
			name	= getActivityType()+"("+getId()+")";
		return name;
	}

	/**
	 *  Get the eventhandler.
	 *  @return The eventhandler.
	 */
	public boolean isEventHandler()
	{
		return this.eventhandler;
	}

	/**
	 *  Set the eventhandler.
	 *  @param eventhandler The eventhandler to set.
	 */
	public void setEventHandler(boolean eventhandler)
	{
		this.eventhandler = eventhandler;
	}
	

	/**
	 *  Get the class.
	 *  @return The class.
	 */
	public ClassInfo getClazz()
	{
		return this.clazz;
	}

	/**
	 *  Set the class.
	 *  @param clazz The class to set.
	 */
	public void setClazz(ClassInfo clazz)
	{
		this.clazz = clazz;
	}
	
}
