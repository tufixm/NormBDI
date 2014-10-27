package jadex.micro.testcases;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.clock.IClock;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SUtil;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Imports;
import jadex.micro.annotation.NameValue;
import jadex.micro.annotation.Properties;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;
import jadex.micro.testcases.LoggerAgent.TestLogHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *  A minimal test case agent serving as a demonstrator.
 */
@Imports({"java.util.logging.*"})
@Description("Tests the logger.")
@Results(@Result(name="testresults", description= "The test results.", clazz=Testcase.class))
@RequiredServices({@RequiredService(name="clockservice", type=IClockService.class, 
	binding=@Binding(scope=RequiredServiceInfo.SCOPE_GLOBAL))})
@Properties({
	@NameValue(name="logging.level", value="Level.FINEST"),
//	@NameValue(name="logging.useParentHandlers", value="true"),
//	@NameValue(name="logging.addConsoleHandler", value="true"),
//	@NameValue(name="logging.file", value="log.txt"),
	@NameValue(name="logging.handlers", clazz=TestLogHandler.class)
//	@NameValue(name="logging.handlers", value="new LoggerAgent$TestLogHandler()")
})
public class LoggerAgent extends MicroAgent
{
	/**
	 *  Just finish the test by setting the result and killing the agent.
	 */
	public IFuture<Void> executeBody()
	{
		final Future<Void> ret = new Future<Void>();
		
		getRequiredService("clockservice").addResultListener(new DelegationResultListener(ret)
		{
			public void customResultAvailable(Object result)
			{
				final IClockService clock = (IClockService)result;
				final long start = clock.getTime();
				String ct = clock.getClockType();
				final boolean simclock = IClock.TYPE_EVENT_DRIVEN.equals(ct) || IClock.TYPE_TIME_DRIVEN.equals(ct);
				
				List<TestReport> reports = new ArrayList<TestReport>();
				
				final TestReport tr = new TestReport("#1", "Test logging.");
				getLogger().setLevel(Level.FINEST);
				getLogger().addHandler(new Handler()
				{
					public void publish(LogRecord record)
					{
//						System.out.println("log: "+record.getMillis());
						long end = clock.getTime();
						long diff = end-start;
						
						if(simclock && diff==0 || !simclock && diff<1000)
						{
							tr.setSucceeded(true);
						}
						else
						{
							tr.setReason("Time in log record differs substantially: "+end+" "+start+" "+diff);
						}
					}
					
					public void flush()
					{
					}
					
					public void close() throws SecurityException
					{
					}
				});
				reports.add(tr);
				
				getLogger().info("test log message");
				
				TestReport tr2 = new TestReport("#2", "Test logging handler.");
				
				Handler[] handlers = getLogger().getHandlers();
				for(int i=0; i<handlers.length; i++)
				{
					if(handlers[i] instanceof TestLogHandler)
					{
						tr2.setSucceeded(true);
						break;
					}
				}
				if(!tr2.isSucceeded())
					tr2.setReason("TestLogHandler was not found: "+SUtil.arrayToString(handlers));
				reports.add(tr2);
				
				setResultValue("testresults", new Testcase(reports.size(), (TestReport[])reports.toArray(new TestReport[reports.size()])));
//				killAgent();
				ret.setResult(null);
			}
		});
		
		return ret;
	}
	
	/**
	 * 
	 */
	public static class TestLogHandler extends Handler
	{
		public void publish(LogRecord record)
		{
//			System.out.println("Received log record: "+record);
		}
		
		public void close() throws SecurityException
		{
		}
		
		public void flush()
		{
		}
	}
}


