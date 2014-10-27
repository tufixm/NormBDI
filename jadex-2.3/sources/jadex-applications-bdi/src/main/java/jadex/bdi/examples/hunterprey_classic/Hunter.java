package jadex.bdi.examples.hunterprey_classic;

import jadex.bridge.IComponentIdentifier;


/**
 *  Editable Java class for concept Hunter of hunterprey ontology.
 */
//bean needs to be serializable due to highscore export for observers
public class Hunter extends Creature implements java.io.Serializable
{
	//-------- constructors --------

	/**
	 *  Create a new Hunter.
	 */
	public Hunter()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new Hunter.
	 */
	public Hunter(String name, IComponentIdentifier aid, Location location)
	{
		// Constructor using required slots (change if desired).
		setName(name);
		setAID(aid);
		setLocation(location);
	}

	//-------- custom code --------

	/**
	 *  Get a string representation of this Creature.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Hunter(");
		buf.append("location=");
		buf.append(getLocation());
		buf.append(", name=");
		buf.append(getName());
		buf.append(", points=");
		buf.append(getPoints());
		buf.append(", age=");
		buf.append(getAge());
		buf.append(", leaseticks=");
		buf.append(getLeaseticks());
		buf.append(")");
		return buf.toString();
	}
}
