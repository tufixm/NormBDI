package jadex.bdi.examples.shop;

import jadex.bdi.runtime.AgentEvent;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IGoalListener;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

/**
 *  The shop for buying goods at the shop.
 */
@Service
public class ShopService implements IShopService 
{
	//-------- attributes --------
	
	/** The component. */
	@ServiceComponent
	protected IBDIInternalAccess comp;
	
	/** The shop name. */
	protected String name;
	
	//-------- constructors --------
	
	/**
	 *  Create a new shop service.
	 */
	public ShopService()
	{
		this.name = "noname-";
	}
	
	/**
	 *  Create a new shop service.
	 */
	public ShopService(String name)
	{
		this.name = name;
	}

	//-------- methods --------
	
	/**
	 *  Get the shop name. 
	 *  @return The name.
	 *  
	 *  @directcall (Is called on caller thread).
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 *  Buy an item.
	 *  @param item The item.
	 */
	public IFuture<ItemInfo> buyItem(final String item, final double price)
	{
		final Future<ItemInfo> ret = new Future<ItemInfo>();
		
		final IGoal sell = comp.getGoalbase().createGoal("sell");
		sell.getParameter("name").setValue(item);
		sell.getParameter("price").setValue(new Double(price));
		sell.addGoalListener(new IGoalListener()
		{
			public void goalFinished(AgentEvent ae)
			{
				if(sell.isSucceeded())
					ret.setResult((ItemInfo)sell.getParameter("result").getValue());
				else
					ret.setException(sell.getException());
			}
			
			public void goalAdded(AgentEvent ae)
			{
			}
		});
		comp.getGoalbase().dispatchTopLevelGoal(sell);
		
		return ret;
	}
	
	/**
	 *  Get the item catalog.
	 *  @return  The catalog.
	 */	
	public IFuture<ItemInfo[]> getCatalog()
	{
		final Future<ItemInfo[]> ret = new Future<ItemInfo[]>();
		ret.setResult((ItemInfo[])comp.getBeliefbase().getBeliefSet("catalog").getFacts());
		return ret;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return name;
	}
	
}
