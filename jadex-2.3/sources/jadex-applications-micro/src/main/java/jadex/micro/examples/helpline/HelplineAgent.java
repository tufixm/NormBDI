package jadex.micro.examples.helpline;

import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.GuiClass;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SReflect;
import jadex.commons.collection.MultiCollection;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.IMicroExternalAccess;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.SwingUtilities;

/**
 *  Helpline micro agent. 
 */
@Description("This agent offers a helpline for getting information about missing persons.")
@RequiredServices({
	@RequiredService(name="clockservice", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="remotehelplineservices", type=IHelpline.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_GLOBAL)),
	@RequiredService(name="localhelplineservices", type=IHelpline.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
@ProvidedServices(@ProvidedService(type=IHelpline.class, implementation=@Implementation(HelplineService.class)))
@GuiClass(HelplineViewerPanel.class)
public class HelplineAgent extends MicroAgent
{
	//-------- attributes --------
	
	/** The map of information. */
	protected MultiCollection infos;
	
	//-------- methods --------
	
	/**
	 *  Called once after agent creation.
	 */
	public IFuture	agentCreated()
	{
//		this.infos = new MultiCollection(new HashMap(), TreeSet.class);
		this.infos = new MultiCollection();
		Object ini = getArgument("infos");
		if(ini!=null && SReflect.isIterable(ini))
		{
			for(Iterator it=SReflect.getIterator(ini); it.hasNext(); )
			{
				InformationEntry ie = (InformationEntry)it.next();
				infos.put(ie.getName(), ie);
			}
		}
//		addService(new HelplineService(this));
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				HelplinePanel.createHelplineGui((IMicroExternalAccess)getExternalAccess());
			}
		});
		return IFuture.DONE;
	}
		
	/**
	 *  Add an information about a person.
	 *  @param name The person's name.
	 *  @param info The information.
	 */
	public void addInformation(final String name, final String info)
	{
//		SServiceProvider.getService(getServiceProvider(), IClockService.class)
		getRequiredService("clockservice")
//			.addResultListener(createResultListener(new DefaultResultListener()
			.addResultListener(new DefaultResultListener() // not needed as decoupled service
		{
			public void resultAvailable(Object result)
			{
				IClockService cs = (IClockService)result;
				infos.put(name, new InformationEntry(name, info, cs.getTime()));
			}
		});
	}
	
	/**
	 *  Get all locally stored information about a person.
	 *  @param name The person's name.
	 *  @return Future that contains the information.
	 */
	public Collection<InformationEntry> getInformation(String name)
	{
		Collection ret	= (Collection)infos.get(name); 
		return ret!=null ? ret : Collections.EMPTY_LIST;
	}
	
	//-------- static methods --------

//	/**
//	 *  Get the meta information about the agent.
//	 */
//	public static MicroAgentMetaInfo getMetaInfo()
//	{
//		return new MicroAgentMetaInfo("This agent offers a helpline for getting information about missing persons.", null, 
//			new IArgument[]{new Argument("infos", "Initial information records.", "InformationEntry[]")}
//			, null, null, SUtil.createHashMap(new String[]{"componentviewer.viewerclass"}, new Object[]{"jadex.micro.examples.helpline.HelplineViewerPanel"}),
//			new RequiredServiceInfo[]{new RequiredServiceInfo("clockservice", IClockService.class, RequiredServiceInfo.SCOPE_PLATFORM),
//			new RequiredServiceInfo("remotehelplineservices", IHelpline.class, true, true, RequiredServiceInfo.SCOPE_GLOBAL),
//			new RequiredServiceInfo("localhelplineservices", IHelpline.class, true, true, RequiredServiceInfo.SCOPE_PLATFORM)}, 
//			new ProvidedServiceInfo[]{new ProvidedServiceInfo(IHelpline.class)});
//	}
}
