package jadex.platform.service.awareness.discovery;


import jadex.micro.annotation.AgentBody;

/**
 *  Agent that sends multicasts to locate other Jadex awareness agents.
 */
public abstract class MasterSlaveDiscoveryAgent extends DiscoveryAgent
{
	//-------- attributes --------
		
	/** The local slaves. */
	protected LeaseTimeHandler locals;
	
	/** The local slaves. */
	protected LeaseTimeHandler remotes;
	
	//-------- methods --------
	
	/**
	 *  Execute the functional body of the agent.
	 *  Is only called once.
	 */
	@AgentBody
	public void executeBody()
	{
		this.locals = new LeaseTimeHandler(this);
		this.remotes = new LeaseTimeHandler(this)
		{
			public void entryDeleted(DiscoveryEntry entry)
			{
				// If master is lost, try to become master
				// If master is lost, try to become master
				String mid = entry.getInfo().getMasterId();
				String mymid = getMyMasterId();
//				System.out.println("mid:_"+mid+" "+mymid);
				if(mid!=null && mid.equals(mymid))
				{
//					System.out.println("Master deleted.");
					
					try
					{
						initNetworkRessource();
					}
					catch (Exception e) 
					{
						getMicroAgent().getLogger().warning("Receive problem: "+e);
//						e.printStackTrace();
					}
				}
			}
		};
		
		// Start sending/receiving
		super.executeBody();
	}
	
	/**
	 *  Get the locals.
	 *  @return the locals.
	 */
	public LeaseTimeHandler getLocals()
	{
		return locals;
	}

	/**
	 *  Get the remotes.
	 *  @return the remotes.
	 */
	public LeaseTimeHandler getRemotes()
	{
		return remotes;
	}
	
	/**
	 *  Test if is master.
	 */
	protected abstract boolean isMaster();
	
	/**
	 *  Create the master id.
	 */
	protected abstract String createMasterId();
	
	/**
	 *  Get the local master id.
	 */
	protected abstract String getMyMasterId();
	
}


