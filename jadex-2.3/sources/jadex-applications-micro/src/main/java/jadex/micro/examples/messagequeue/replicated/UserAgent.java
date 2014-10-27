package jadex.micro.examples.messagequeue.replicated;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.examples.messagequeue.Event;


/**
 * 
 */
@Agent
@RequiredServices(@RequiredService(name = "mq", type = IMessageQueueReplicableService.class, binding = @Binding(scope = Binding.SCOPE_APPLICATION)))
@Arguments(@Argument(name = "topic", clazz = String.class, defaultvalue = "\"default_topic\""))
public class UserAgent
{
	// -------- attributes --------

	/** The agent. */
	@Agent
	protected MicroAgent						agent;

	/** The message queue. */
	@AgentService
	protected IMessageQueueReplicableService	mq;

	/** The topic argument. */
	@AgentArgument
	protected String							topic;

	// -------- methods --------

	/**
	 * The agent body.
	 */
	@AgentBody
	public void body()
	{
		final ISubscriptionIntermediateFuture<Event> fut = mq.subscribe(topic);
		fut.addResultListener(new IntermediateDefaultResultListener<Event>()
		{
			public void intermediateResultAvailable(Event event)
			{
				System.out.println("Received: "
						+ agent.getComponentIdentifier() + " " + event);
			}

			public void exceptionOccurred(Exception exception)
			{
				System.out.println("Ex: " + exception);
			}
		});

		IComponentStep<Void> step = new IComponentStep<Void>()
		{
			final int[]	cnt	= new int[1];

			public IFuture<Void> execute(IInternalAccess ia)
			{
				if(!(cnt[0] % 2 == 0))
				{
					// publish also remote
					mq.publish(topic, new Event("some type", cnt[0]++, agent.getComponentIdentifier()), true);
				}
				else
				{
					// just publish local
					mq.publish(topic,new Event("some type", cnt[0]++, agent.getComponentIdentifier()), false);
				}
				
				if(cnt[0] < 10)
				{
					agent.waitFor(1000, this);
				}
				else
				{
					fut.terminate();
				}
				return IFuture.DONE;
			}
		};
		agent.waitFor(1000, step);
	}
}
