package jadex.rules.rulesystem.rete.constraints;

import jadex.commons.SUtil;
import jadex.rules.rulesystem.rete.Tuple;
import jadex.rules.rulesystem.rete.extractors.AttributeSet;
import jadex.rules.state.IOAVState;
import jadex.rules.state.OAVAttributeType;

import java.util.Arrays;

/**
 *  A constraint evaluator for or-connected constraints.
 */
public class OrConstraintEvaluator implements IConstraintEvaluator
{
	//-------- attributes --------
	
	/** The constraint evaluator. */
	protected final IConstraintEvaluator[] evaluators;

	
	//-------- constructors --------
	
	/**
	 *  Create an OR constraint evaluator.
	 */
	public OrConstraintEvaluator(IConstraintEvaluator[] evaluators)
	{
		this.evaluators	= evaluators;
	}

	//-------- methods --------
	
	/**
	 *  Evaluate the constraints given the right object, left tuple 
	 *  (null for alpha nodes) and the state.
	 *  @param right The right input object.
	 *  @param left The left input tuple. 
	 *  @param state The working memory.
	 */
	public boolean evaluate(Object right, Tuple left, IOAVState state)
	{
		boolean ret = false;
		//IConstraintEvaluator[]	evals	= evaluators;
		for(int i=0; !ret && evaluators!=null && i<evaluators.length; i++)
			ret = evaluators[i].evaluate(right, left, state);
		return ret;
	}
	
	/**
	 *  Test if a constraint evaluator is affected from a 
	 *  change of a certain attribute.
	 *  @param tupleindex The tuple index.
	 *  @param attr The attribute.
	 *  @return True, if affected.
	 */
	public boolean isAffected(int tupleindex, OAVAttributeType attr)
	{
		boolean ret = false;
		
		for(int i=0; !ret && i<evaluators.length; i++)
		{
			ret = evaluators[i].isAffected(tupleindex, attr);
		}
		return ret;
	}
	
	/**
	 *  Get the set of relevant attribute types.
	 */
	public AttributeSet	getRelevantAttributes()
	{
		AttributeSet	ret	= new AttributeSet();
		for(int i=0; i<evaluators.length; i++)
		{
			ret.addAll(evaluators[i].getRelevantAttributes());
		}
		return ret;
	}
	
	/**
	 *  Get the set of indirect attribute types.
	 *  I.e. attributes of objects, which are not part of an object conditions
	 *  (e.g. for chained extractors) 
	 *  @return The relevant attribute types.
	 */
	public AttributeSet	getIndirectAttributes()
	{
		AttributeSet ret = new AttributeSet();
		for(int i=0; i<evaluators.length; i++)
		{
			ret.addAll(evaluators[i].getIndirectAttributes());
		}
		return ret;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation. 
	 */
	public String toString()
	{
		StringBuffer ret = new StringBuffer(" or ");
		for(int i=0; evaluators!=null && i<evaluators.length; i++)
			ret.append("(").append(evaluators[i]).append(")");
		return ret.toString();
	}
	
	/**
	 *  Get the constraint evaluators.
	 */
	public IConstraintEvaluator[] getConstraintEvaluators()
	{
		return evaluators;
	}

	/**
	 *  The hash code.
	 *  @return The hash code.
	 */
	public int hashCode()
	{
		// Arrays.hashCode(Object[]): JDK 1.5
		return 31 + SUtil.arrayHashCode(evaluators);
	}

	/**
	 *  Test for equality.
	 *  @param obj The object.
	 *  @return True, if equal.
	 */
	public boolean equals(Object obj)
	{
		if(this==obj)
			return true;

		return (obj instanceof OrConstraintEvaluator)
			&& Arrays.equals(evaluators, ((OrConstraintEvaluator)obj).getConstraintEvaluators());
	}
}

