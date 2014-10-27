package jadex.micro.examples.mandelbrot;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Configuration;
import jadex.micro.annotation.Configurations;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.NameValue;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

/**
 *  Calculate agent allows calculating the colors of an area using a calculate service.
 */
@Description("Agent offering a calculate service.")
@ProvidedServices({
	@ProvidedService(type=ICalculateService.class, implementation=@Implementation(CalculateService.class)),
	@ProvidedService(type=IProgressService.class, implementation=@Implementation(value=ProgressService.class, proxytype=Implementation.PROXYTYPE_DIRECT))
	})
@Arguments(@Argument(name="delay", description="Agent kills itself when no job arrives in the delay interval.", clazz=Long.class, defaultvalue="new Long(1000)"))
@Configurations({
	@Configuration(name="default"),
	@Configuration(name="long lived", arguments={@NameValue(name="delay", value="-1")})
})
public class CalculateAgent extends MicroAgent
{
	//-------- attributes --------
	
	/** Flag indicating that the agent had a job. */
	protected boolean hadjob;
	
	/** Id of the current job. */
	protected Object	taskid;
	
	/** Progress of the current job. */
	protected int progress;
	
	//-------- methods --------
	
	/**
	 *  Execute the body.
	 */
	public IFuture<Void> executeBody()
	{
		final Future<Void> ret = new Future<Void>();
		
		final long delay = ((Number)getArgument("delay")).longValue();
		IComponentStep<Void> step = new IComponentStep<Void>()
		{
			public IFuture<Void> execute(IInternalAccess ia)
			{
				if(!isHadJob())
				{
//					System.out.println("killComponent: "+getComponentIdentifier());
//					killComponent();
					ret.setResult(null);
				}
				setHadJob(false);
				waitFor(delay, this);
				return IFuture.DONE;
			}
		};
		if(delay>0)
			waitFor(delay, step);
		
		return ret;
	}
	
	/**
	 *  Set the hadjob.
	 *  @param hadjob The hadjob to set.
	 */
	public void setHadJob(boolean hadjob)
	{
		this.hadjob = hadjob;
	}
	
	/**
	 *  Get the hadjob.
	 *  @return The hadjob.
	 */
	public boolean isHadJob()
	{
		return hadjob;
	}
	
	/**
	 *  Get the current task id.
	 */
	public Object	getTaskId()
	{
		return taskid;
	}
	
	/**
	 *  Set the current task id.
	 */
	public void	setTaskId(Object taskid)
	{
		this.taskid	= taskid;
	}
	
	/**
	 *  Get the current progress.
	 */
	public int	getProgress()
	{
		return progress;
	}
	
	/**
	 *  Set the current progress.
	 */
	public void	setProgress(int progress)
	{
		this.progress	= progress;
	}
}
