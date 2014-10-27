package jadex.micro;

import jadex.bridge.service.types.factory.IComponentFactory;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.Properties;
import jadex.micro.annotation.NameValue;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

/**
 *  Component kernel.
 */
@Properties({
	@NameValue(name="kernel.types", value="new String[] { \"component.xml\"}")
})
@ProvidedServices({
	@ProvidedService(type=IComponentFactory.class, implementation=@Implementation(expression="new jadex.component.ComponentComponentFactory($component.getServiceProvider())"))
})
public class KernelComponentAgent extends MicroAgent
{
}
