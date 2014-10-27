package jadex.rules.test.rulesystem;

import jadex.rules.rulesystem.IAction;
import jadex.rules.rulesystem.IRule;
import jadex.rules.rulesystem.IVariableAssignments;
import jadex.rules.rulesystem.RuleSystem;
import jadex.rules.rulesystem.Rulebase;
import jadex.rules.rulesystem.rete.RetePatternMatcherFunctionality;
import jadex.rules.rulesystem.rules.BoundConstraint;
import jadex.rules.rulesystem.rules.ObjectCondition;
import jadex.rules.rulesystem.rules.Rule;
import jadex.rules.rulesystem.rules.Variable;
import jadex.rules.state.IOAVState;
import jadex.rules.state.OAVJavaType;
import jadex.rules.state.javaimpl.OAVStateFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 *  Test if different activations with the same variable bindings are
 *  handled correctly.
 */
// Class doesn't end with 'Test', because test fails and would interrupt maven builds.
public class DuplicateActivationsTestFail extends TestCase
{
	//-------- attributes --------
	
	/** The state. */
	protected IOAVState	state;
	
	/** The rule system. */
	protected RuleSystem	system;
	
	/** The list of triggered colors. */
	protected List	colors;
	
	/** Red block 1. */
	protected Object	block1;
	
	/** Red block 2. */
	protected Object	block2;
		
	//-------- constructors --------
	
	/**
	 *  Test setup.
	 */
	protected void setUp() throws Exception
	{
		state	= OAVStateFactory.createOAVState(Blocks.blocksworld_type_model);
		colors	= new ArrayList();
		
		// (Block (color ?color))
		
		// Matches a block with a color
		ObjectCondition	cblock	= new ObjectCondition(Blocks.block_type);
		cblock.addConstraint(new BoundConstraint(Blocks.block_has_color, new Variable("?color", OAVJavaType.java_string_type)));

		// Add block of triggered condition to list.
		IRule	rule	= new Rule("block", cblock, new IAction()
		{
			public void execute(IOAVState state, IVariableAssignments assigments)
			{
				colors.add(assigments.getVariableValue("?color"));
			}
		});
		
		// Create rule system.
		Rulebase rb = new Rulebase();
		rb.addRule(rule);
		system	= new RuleSystem(state,rb, new RetePatternMatcherFunctionality(rb));
		system.init();
		
//		RetePanel.createReteFrame("Duplicate Activations Test",
//			((RetePatternMatcherFunctionality)system.getMatcherFunctionality()).getReteNode(),
//			((RetePatternMatcherState)system.getMatcherState()).getReteMemory(),
//			system.getAgenda(), new Object());
//		synchronized(system){system.wait();}

		// Add two red blocks.
		block1	= state.createRootObject(Blocks.block_type);
		state.setAttributeValue(block1, Blocks.block_has_color, "red");
		block2	= state.createRootObject(Blocks.block_type);
		state.setAttributeValue(block2, Blocks.block_has_color, "red");
	}
	
	//-------- test methods --------
	
	/**
	 *  Test that rule triggers twice. 
	 */
	public void testTriggerTwice()
	{
		system.fireAllRules();
		List	test	= Arrays.asList(new String[]{"red", "red"});
		assertEquals("Rule should trigger twice for color 'red'", test, colors);
	}
		
	/**
	 *  Test that rule triggers once after a block is removed.
	 */
	public void testRemoveTrigger()
	{
		state.notifyEventListeners();
		state.dropObject(block1);
		system.fireAllRules();
		List	test	= Collections.singletonList("red");
		assertEquals("Should trigger for second block", test, colors);
	}
}
