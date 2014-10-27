package jadex.micro.testcases;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;
import jadex.component.ComponentInterpreter;

/**
 *  Behavior of the component result test.
 */
public class EndStepTestStep implements IComponentStep<Void>
{
	/**
	 *  Execute the test.
	 */
	public IFuture<Void> execute(final IInternalAccess ia)
	{
		return ia.waitForDelay(2000, new IComponentStep<Void>()
		{
			public IFuture<Void> execute(IInternalAccess ia)
			{
				((ComponentInterpreter)ia).setResultValue("testresults", new Testcase(1,
					new TestReport[]{new TestReport("#1", "Test if end step is executed", true, null)}));
				return IFuture.DONE;
			}
		});
	}
}
