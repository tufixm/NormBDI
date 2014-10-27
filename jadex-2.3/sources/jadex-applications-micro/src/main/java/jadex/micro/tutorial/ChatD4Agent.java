package jadex.micro.tutorial;

import jadex.bridge.service.annotation.Value;
import jadex.bridge.service.types.clock.IClockService;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 *  Chat micro agent with a spam interceptor. 
 */
@Description("This agent provides a basic chat service.")
@Agent
@ProvidedServices(@ProvidedService(type=IExtendedChatService.class, 
	implementation=@Implementation(value=ChatServiceD4.class, 
	interceptors=@Value(clazz=SpamInterceptorD4.class))))
@RequiredServices({
	@RequiredService(name="clockservice", type=IClockService.class, 
		binding=@Binding(scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name="chatservices", type=IExtendedChatService.class, multiple=true,
		binding=@Binding(dynamic=true, scope=Binding.SCOPE_PLATFORM))
})
public class ChatD4Agent
{
}