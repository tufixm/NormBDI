package jadex.bpmn.task.info;

import jadex.commons.transformation.annotations.IncludeFields;

/**
 *  Meta information about a task.
 *  Should contain a description of what the task is useful for
 *  and which parameters it has.
 */
@IncludeFields
public class TaskMetaInfo
{
	//-------- attributes --------
	
	/** The description. */
	protected String description;
	
	/** The parameter descriptions. */
	protected ParameterMetaInfo[] parammetainfos;
	
	//-------- constructors --------
	
	/**
	 *  Create a task meta info.
	 */
	public TaskMetaInfo()
	{
	}
	
	/**
	 *  Create a task meta info.
	 */
	public TaskMetaInfo(String description, ParameterMetaInfo[] parammetainfos)
	{
		this.description = description;
		this.parammetainfos = parammetainfos;
	}
	
	//-------- methods --------
	
	/**
	 *  Get the description.
	 *  @return The description.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 *  Sets the description.
	 *
	 *  @param description The description.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 */
	public ParameterMetaInfo[] getParameterMetaInfos()
	{
		return this.parammetainfos;
	}

	/**
	 *  Set the parameters.
	 *
	 *  @param parammetainfos The parameters.
	 */
	public void setParameterMetaInfos(ParameterMetaInfo[] parammetainfos)
	{
		this.parammetainfos = parammetainfos;
	}
}
