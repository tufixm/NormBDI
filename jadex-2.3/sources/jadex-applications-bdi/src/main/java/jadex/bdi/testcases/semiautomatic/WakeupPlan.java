package jadex.bdi.testcases.semiautomatic;

import jadex.bdi.runtime.Plan;

import java.util.Calendar;
import java.util.Date;

/**
 *  Plan to wake up at every full minute.
 */
public class WakeupPlan extends Plan
{
	public void body()
	{
		// Get date of full minute.
		Calendar now = Calendar.getInstance();
		Calendar wakeup = Calendar.getInstance();
		wakeup.clear();
		wakeup.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE),
			now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 0);
		if(wakeup.before(now))
		{
			wakeup.add(Calendar.MINUTE, 1);
		}

		while (true)
		{
			// Wait until next full minute
			waitFor(wakeup.getTime().getTime() - getTime());

			// Do the action...
			getLogger().info("Time is now: "+new Date(getTime()));

			// Increment wakeup time one day.
			wakeup.add(Calendar.MINUTE, 1);
		}
	}
}
