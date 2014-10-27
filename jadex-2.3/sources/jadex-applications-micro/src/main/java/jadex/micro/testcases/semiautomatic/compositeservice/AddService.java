package jadex.micro.testcases.semiautomatic.compositeservice;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.BasicService;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

/**
 *  Simple add service.
 */
public class AddService extends BasicService implements IAddService
{
	//-------- constructors --------
	
	/**
	 *  Create the service.
	 */
	public AddService(IInternalAccess comp)
	{
		super(comp.getServiceContainer().getId(), IAddService.class, null);
	}
	
	//-------- methods --------

	/**
	 *  Add two numbers.
	 *  @param a Number one.
	 *  @param b Number two.
	 *  @return The sum of a and b.
	 */
	public IFuture<Double> add(double a, double b)
	{
		System.out.println("add service called on: "+getServiceIdentifier().getProviderId());
		return new Future<Double>(new Double(a+b));
	}
}
