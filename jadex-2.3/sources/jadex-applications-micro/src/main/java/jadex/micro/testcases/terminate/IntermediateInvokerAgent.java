package jadex.micro.testcases.terminate;


import jadex.base.test.TestReport;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.types.remote.RemoteException;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.FutureTerminatedException;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateResultListener;
import jadex.commons.future.ITerminableIntermediateFuture;
import jadex.commons.future.IntermediateExceptionDelegationResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Description;

import java.util.Collection;

/**
 *  The intermediate invoker agent tests if intermediate futures can be terminated
 *  in local and remote cases.
 */
@Agent
@Description("The intermediate invoker agent tests if intermediate futures can be terminated " +
	"in local and remote cases.")
public class IntermediateInvokerAgent	extends InvokerAgent
{
	/**
	 *  Test terminating a future.
	 */
	protected IFuture<TestReport>	testTerminate(int testno, ITerminableService service, long delay)
	{
		final Future<Void> tmp = new Future<Void>();
		
		int	max	= 3;
		final ITerminableIntermediateFuture<String> fut = service.getResults(delay, max);
		fut.addResultListener(agent.createResultListener(new IIntermediateResultListener<String>()
		{
			public void resultAvailable(Collection<String> result)
			{
				tmp.setException(new RuntimeException("Termination did not occur: "+result));
			}
			public void intermediateResultAvailable(String result)
			{
//				System.out.println("intermediate result: "+result);
			}
			public void finished()
			{
				tmp.setException(new RuntimeException("Termination did not occur"));
			}
			public void exceptionOccurred(Exception exception)
			{
				if(exception instanceof FutureTerminatedException)
				{
					tmp.setResult(null);
				}
				else
				{
					tmp.setException(new RuntimeException("Wrong exception occurred: "+exception));
				}
			}
		}));
		
		agent.waitFor(delay*(max-1)+delay/2, new IComponentStep<Void>()
		{
			public IFuture<Void> execute(IInternalAccess ia)
			{
				fut.terminate();
				return IFuture.DONE;
			}
		});
		
		final Future<TestReport>	ret	= new Future<TestReport>();
		final TestReport tr = new TestReport("#"+testno, "Tests if intermediate future is terminated");
		tmp.addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
		{
			public void customResultAvailable(Void result)
			{
				tr.setSucceeded(true);
				ret.setResult(tr);
			}
			public void exceptionOccurred(Exception exception)
			{
				tr.setFailed(exception.getMessage());
				ret.setResult(tr);
			}
		});
		return ret;
	}

	
	/**
	 *  Test if terminate action is called.
	 */
	protected IFuture<TestReport>	testTerminateAction(int testno, ITerminableService service, long delay)
	{
		final Future<Void> tmp = new Future<Void>();
		
		final ITerminableIntermediateFuture<String> fut = service.getResults(delay, 3);
		service.terminateCalled().addResultListener(new IntermediateExceptionDelegationResultListener<Void, Void>(tmp)
		{
			public void intermediateResultAvailable(Void result)
			{
				fut.terminate();
			}
			public void customResultAvailable(Collection<Void> result)
			{
				tmp.setResult(null);
			}
			public void finished()
			{
				tmp.setResult(null);
			}
		});

		final Future<TestReport>	ret	= new Future<TestReport>();
		final TestReport tr = new TestReport("#"+testno, "Tests if terminating action of intermediate future is called");
		tmp.addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
		{
			public void customResultAvailable(Void result)
			{
				tr.setSucceeded(true);
				ret.setResult(tr);
			}
			public void exceptionOccurred(Exception exception)
			{
				tr.setFailed(exception.getMessage());
				ret.setResult(tr);
			}
		});
		return ret;
	}
}
