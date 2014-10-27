package jadex.micro.testcases;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.ISearchConstraints;
import jadex.bridge.fipa.SFipa;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.df.IDF;
import jadex.bridge.service.types.df.IDFComponentDescription;
import jadex.bridge.service.types.df.IDFServiceDescription;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Test DF usage from micro agent.
 *  @author Dirk, Alex
 */
@Description("Test DF usage from micro agent.")
@Results(@Result(name="testresults", clazz=Testcase.class))
public class DFTestAgent extends MicroAgent
{
	//-------- attributes --------
	
	/** The reports of executed tests, used as result. */
	protected List	reports;
	
	//-------- methods --------
	
	/**
	 *  At startup register the agent at the DF.
	 */
	public IFuture<Void> executeBody()
	{
		this.reports	= new ArrayList();
		return registerDF();
	}
	
	/**
	 *  Called when agent finishes.
	 */
	public IFuture<Void>	agentKilled()
	{
		final Future<Void>	ret	= new Future<Void>();
		// Store test results.
		setResultValue("testresults", new Testcase(reports.size(), (TestReport[])reports.toArray(new TestReport[reports.size()])));

		// Deregister agent.
		// Todo: use fix component service container
		getServiceContainer().searchService(IDF.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IDF, Void>(ret)
		{
			public void customResultAvailable(IDF df)
			{
				IDFComponentDescription ad = df.createDFComponentDescription(getComponentIdentifier(), null);
				df.deregister(ad).addResultListener(new DelegationResultListener<Void>(ret));
			}
		});
		return IFuture.DONE;
	}
	
	/**
	 *  Register the agent at the DF.
	 */
	protected IFuture<Void> registerDF()
	{
		final Future<Void> ret = new Future<Void>();
		
		final TestReport tr	= new TestReport("#1", "Test DF registration.");
		reports.add(tr);

		getServiceContainer().searchService(IDF.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				IDF df = (IDF)result;
				IDFServiceDescription sd = df.createDFServiceDescription(null, "testType", null);
				IDFComponentDescription ad = df.createDFComponentDescription(getComponentIdentifier(), sd);

				IFuture re = df.register(ad); 
				re.addResultListener(createResultListener(new IResultListener()
				{
					public void resultAvailable(Object result)
					{
						// Set test success and continue test.
						tr.setSucceeded(true);
						searchDF().addResultListener(createResultListener(new DelegationResultListener<Void>(ret)));
					}
					
					public void exceptionOccurred(Exception e)
					{
						// Set test failure and kill agent.
						tr.setFailed(e.toString());
//						killAgent();
						ret.setResult(null);
					}
				}));
			}
		});
		
		return ret;
	}
	
	/**
	 *  Search for the agent at the DF.
	 */
	protected  IFuture<Void> searchDF()
	{
		final Future<Void> ret = new Future<Void>();
		
		final TestReport	tr	= new TestReport("#2", "Test DF search.");
		reports.add(tr);

		// Create a service description to search for.
		getServiceContainer().searchService(IDF.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				IDF df = (IDF)result;
				IDFServiceDescription sd = df.createDFServiceDescription(null, "testType", null);
				IDFComponentDescription ad = df.createDFComponentDescription(null, sd);
				ISearchConstraints	cons = df.createSearchConstraints(-1, 0);
				
				IFuture re = df.search(ad, cons); 
				re.addResultListener(createResultListener(new IResultListener() 
				{
					public void resultAvailable(Object result)
					{
						IDFComponentDescription[] agentDesc = (IDFComponentDescription[])result;
						if(agentDesc.length != 0)
						{
							// Set test success and continue test.
							tr.setSucceeded(true);
							IComponentIdentifier receiver = agentDesc[0].getName();
							sendMessageToReceiver(receiver);
						}
						else
						{
							// Set test failure and kill agent.
							tr.setFailed("No suitable service found.");
							killAgent();
							ret.setResult(null);
						}
					}
					
					public void exceptionOccurred(Exception e)
					{
						// Set test failure and kill agent.
						tr.setFailed(e.toString());
//						killAgent();
						ret.setResult(null);
					}
				}));
			}
		});
		
		return ret;
	}
	
	private IFuture<Void> sendMessageToReceiver(IComponentIdentifier cid)
	{
		final Future<Void> ret = new Future<Void>();
		
		final TestReport	tr	= new TestReport("#3", "Test sending message to service (i.e. myself).");
		reports.add(tr);

		Map hlefMessage = new HashMap();
		hlefMessage.put(SFipa.PERFORMATIVE, SFipa.INFORM);
		hlefMessage.put(SFipa.SENDER, getComponentIdentifier());
		hlefMessage.put(SFipa.RECEIVERS, cid);
		hlefMessage.put(SFipa.CONTENT, "testMessage");
		
		sendMessage(hlefMessage, SFipa.FIPA_MESSAGE_TYPE);
		
		waitFor(1000, new IComponentStep<Void>()
		{
			public IFuture<Void> execute(IInternalAccess ia)
			{
				// Set test failure and kill agent.
				tr.setFailed("No message received.");
//				killAgent();
				ret.setResult(null);
				return IFuture.DONE;
			}
		});
		
		return ret;
	}
	
	// todo: set body future?!
	public void messageArrived(Map msg, MessageType mt)
	{
		TestReport	tr	= (TestReport)reports.get(reports.size()-1);
		
		if("testMessage".equals(msg.get(SFipa.CONTENT)))
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setFailed("Wrong message received: "+msg);
		}

		// All tests done.
		killAgent();
	}

	
//	/**
//	 *  Add the 'testresults' marking this agent as a testcase. 
//	 */
//	public static Object getMetaInfo()
//	{
//		return new MicroAgentMetaInfo("Test DF usage from micro agent.", 
//			null, null, new IArgument[]{new Argument("testresults", null, "Testcase")});
//	}
}
