package jadex.bpmn.runtime.task;

import jadex.bpmn.model.task.ITaskContext;
import jadex.bpmn.task.info.ParameterMetaInfo;
import jadex.bpmn.task.info.TaskMetaInfo;
import jadex.bridge.IInternalAccess;

/**
 *  Writes a parameter value to the thread (or superthread).
 */
public class WriteParameterTask extends AbstractTask
{
	/**
	 *  Execute the task.
	 */
	public void doExecute(ITaskContext context, IInternalAccess instance) throws Exception
	{
		if(context.hasParameterValue("name"))
		{
			String name = (String)context.getParameterValue("name");
			Object val = context.getParameterValue("value");
			Object key = context.getParameterValue("key");
			context.setParameterValue(name, key, val);
		}
		
		for(int i=0; ; i++)
		{
			if(context.hasParameterValue("name"+i))
			{
				String name = (String)context.getParameterValue("name"+i);
				Object val = context.getParameterValue("value"+i);
				Object key = context.getParameterValue("key"+i);
				context.setParameterValue(name, key, val);
			}
			else
			{
				break;
			}
		}
	}
	
	/**
	 *  Get the meta-info.
	 *  @return The meta-info.
	 */
	public static TaskMetaInfo getMetaInfo()
	{
		String desc = "The write parameter task can be used to write values to parameters.";
		ParameterMetaInfo vnamemi = new ParameterMetaInfo(ParameterMetaInfo.DIRECTION_IN, 
			String.class, "parametername", null, "The name of the variable that is the target of the write operation.");
		ParameterMetaInfo keymi = new ParameterMetaInfo(ParameterMetaInfo.DIRECTION_IN, 
			Object.class, "key", null, "The key that is used to write the context variable.");
		ParameterMetaInfo valuemi = new ParameterMetaInfo(ParameterMetaInfo.DIRECTION_IN, 
			Object.class, "value", null, "The value that is written to the context variable.");
			
		return new TaskMetaInfo(desc, new ParameterMetaInfo[]{vnamemi, keymi, valuemi}); 
	}
}
