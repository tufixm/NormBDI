package jadex.rules.test.rulesystem;import jadex.rules.rulesystem.IAction;import jadex.rules.rulesystem.ICondition;import jadex.rules.rulesystem.IVariableAssignments;import jadex.rules.rulesystem.RuleSystem;import jadex.rules.rulesystem.Rulebase;import jadex.rules.rulesystem.rete.RetePatternMatcherFunctionality;import jadex.rules.rulesystem.rules.AndCondition;import jadex.rules.rulesystem.rules.BoundConstraint;import jadex.rules.rulesystem.rules.NotCondition;import jadex.rules.rulesystem.rules.ObjectCondition;import jadex.rules.rulesystem.rules.Rule;import jadex.rules.rulesystem.rules.Variable;import jadex.rules.state.IOAVState;import jadex.rules.state.OAVJavaType;import jadex.rules.state.javaimpl.OAVStateFactory;import junit.framework.TestCase;

/** *  Test multiple object conditions of same type. *///Class doesn't end with 'Test', because test fails and would interrupt maven builds.public class BetaTest2 extends TestCase{	public static boolean showViewer = true;	public static boolean useTreat = false;		public static boolean singleMode = true;	public static boolean withRemove = true;
	private Rulebase rb;	private RuleSystem rs;	private IOAVState state;		public BetaTest2()	{		super("BetaTest2");	}		public void testSucess()	{		createRule(false);		Object fact = state.createObject(Blocks.block_type);		state.setAttributeValue(fact, Blocks.block_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 0);		}				Object fact2 = state.createObject(Blocks.ball_type);		state.setAttributeValue(fact2, Blocks.ball_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 1);		}				Object fact3 = state.createObject(Blocks.block_type);		state.setAttributeValue(fact3, Blocks.block_has_name, "klaus");		state.notifyEventListeners();		repaintViewer();				assertEquals(rs.getAgenda().getActivations().size(), 2);				if(withRemove)		{			state.dropObject(fact2);			state.notifyEventListeners();			repaintViewer();						assertEquals(rs.getAgenda().getActivations().size(), 0);		}	}		public void testFail()	{		createRule(false);		Object fact = state.createObject(Blocks.block_type);		state.setAttributeValue(fact, Blocks.block_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 0);		}				Object fact2 = state.createObject(Blocks.ball_type);		state.setAttributeValue(fact2, Blocks.ball_has_color, "blue");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 0);		}				Object fact3 = state.createObject(Blocks.block_type);		state.setAttributeValue(fact3, Blocks.block_has_name, "klaus");		state.notifyEventListeners();		repaintViewer();				assertEquals(rs.getAgenda().getActivations().size(), 0);	}		public void testNotSucessSeed()	{		createRule(true);		Object fact = state.createObject(Blocks.block_type);		state.setAttributeValue(fact, Blocks.block_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 1);		}				Object fact3 = state.createObject(Blocks.block_type);		state.setAttributeValue(fact3, Blocks.block_has_name, "klaus");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 4);		}				Object fact2 = state.createObject(Blocks.ball_type);		state.setAttributeValue(fact2, Blocks.ball_has_color, "blue");		state.notifyEventListeners();		repaintViewer();				System.out.println("BetaTest2.testNotSucessSeed:");		System.out.println("FOUND ACTS: " + rs.getAgenda().getActivations());		assertEquals(rs.getAgenda().getActivations().size(), 4);				if(withRemove)		{			state.dropObject(fact);			state.notifyEventListeners();			repaintViewer();						System.out.println("ACTS AFTER REMOVE: " + rs.getAgenda().getActivations() + "\n");			assertEquals(rs.getAgenda().getActivations().size(), 1);			// TODO: RETE semantic is 2, verify!!!		}	}		public void testNotSucessNotSeed()	{		createRule(true);		Object fact = state.createObject(Blocks.block_type);		state.setAttributeValue(fact, Blocks.block_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 1);		}				Object fact2 = state.createObject(Blocks.ball_type);		state.setAttributeValue(fact2, Blocks.ball_has_color, "blue");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 1);		}				Object fact3 = state.createObject(Blocks.block_type);		state.setAttributeValue(fact3, Blocks.block_has_name, "klaus");		state.notifyEventListeners();		repaintViewer();				System.out.println("BetaTest2.testNotSucessNotSeed:");		System.out.println("FOUND ACTS: " + rs.getAgenda().getActivations());		assertEquals(rs.getAgenda().getActivations().size(), 4);				if(withRemove)		{			state.dropObject(fact3);			state.notifyEventListeners();			repaintViewer();						System.out.println("ACTS AFTER REMOVE: " + rs.getAgenda().getActivations());			assertEquals(rs.getAgenda().getActivations().size(), 1); 			// TODO: RETE semantic is 2, verify!!		}	}		public void testNotFailSeed()	{		createRule(true);		Object fact = state.createObject(Blocks.block_type);		state.setAttributeValue(fact, Blocks.block_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 1);		}				Object fact3 = state.createObject(Blocks.block_type);		state.setAttributeValue(fact3, Blocks.block_has_name, "klaus");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 4);		}				Object fact2 = state.createObject(Blocks.ball_type);		state.setAttributeValue(fact2, Blocks.ball_has_color, "red");		state.notifyEventListeners();		repaintViewer();		//		System.out.println("Beta2Test.notfailseed:\nFOUND ACTS: " + rs.getAgenda().getActivations());		assertEquals(rs.getAgenda().getActivations().size(), 2);				if(withRemove)		{			state.dropObject(fact2);			state.notifyEventListeners();			repaintViewer();						// TODO: Treat will fail impl isActValid...			assertEquals(rs.getAgenda().getActivations().size(), 4);		}	}		public void testNotFailNotSeed()	{		createRule(true);		Object fact = state.createObject(Blocks.block_type);		state.setAttributeValue(fact, Blocks.block_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 1);		}				Object fact2 = state.createObject(Blocks.ball_type);		state.setAttributeValue(fact2, Blocks.ball_has_color, "red");		if(singleMode)		{			state.notifyEventListeners();			repaintViewer();			assertEquals(rs.getAgenda().getActivations().size(), 0);		}				Object fact3 = state.createObject(Blocks.block_type);		state.setAttributeValue(fact3, Blocks.block_has_name, "klaus");		state.notifyEventListeners();		repaintViewer();		//		System.out.println("Beta2Test.notfailnotseed:\nFOUND ACTS: " + rs.getAgenda().getActivations());		assertEquals(rs.getAgenda().getActivations().size(), 2);				if(withRemove)		{			state.dropObject(fact2);			state.notifyEventListeners();			repaintViewer();						// TODO: Treat will fail			assertEquals(rs.getAgenda().getActivations().size(), 4);		}	}		public void testMod()	{//		createRule(false);//		Object fact = state.createObject(Blocks.block_type);//		state.setAttributeValue(fact, Blocks.block_has_color, "red");//		if(singleMode)//		{//			state.notifyEventListeners();//			repaintViewer();//			assertEquals(rs.getAgenda().getActivations().size(), 0);//		}//		//		Object fact2 = state.createObject(Blocks.ball_type);//		state.setAttributeValue(fact2, Blocks.ball_has_color, "red");//		//		state.notifyEventListeners();//		repaintViewer();//		//		assertEquals(rs.getAgenda().getActivations().size(), 1);//		//		// Mod//		state.setAttributeValue(fact2, Blocks.ball_has_color, "blue");//		state.notifyEventListeners();//		repaintViewer();//		//		assertEquals(rs.getAgenda().getActivations().size(), 0);	}		private void createRule(boolean isNot)	{		state = OAVStateFactory.createOAVState(Blocks.blocksworld_type_model);		rb = new Rulebase();		// Rule		// (Block (color ?color))		// (Ball (color ?color))		// OR:		// not (Ball (color ?color))		// (Block (name ?name))		ObjectCondition condBlock = new ObjectCondition(Blocks.block_type);		condBlock.addConstraint(new BoundConstraint(Blocks.block_has_color, new Variable("color", OAVJavaType.java_string_type)));				ObjectCondition condBall = new ObjectCondition(Blocks.ball_type);		condBall.addConstraint(new BoundConstraint(Blocks.ball_has_color, new Variable("color", OAVJavaType.java_string_type)));				ObjectCondition condBlock2 = new ObjectCondition(Blocks.block_type);		condBlock2.addConstraint(new BoundConstraint(Blocks.block_has_name, new Variable("name", OAVJavaType.java_string_type)));				ICondition condition = (isNot) ? new AndCondition(new ICondition[]{condBlock, new NotCondition(condBall), condBlock2}) 				: new AndCondition(new ICondition[]{condBlock, condBall, condBlock2});				IAction action = new IAction()		{			public void execute(IOAVState state, IVariableAssignments assigments)			{				System.out.println("Executing Action of Beta2 Rule...");			}		};		rb.addRule(new Rule("Beta2 Test Rule", condition, action));				if(useTreat)		{//			TreatPatternMatcherFunctionality treat = new TreatPatternMatcherFunctionality(rb);	//			rs = new RuleSystem(state, rb, treat);		}		else		{			RetePatternMatcherFunctionality rete = new RetePatternMatcherFunctionality(rb);			rs = new RuleSystem(state, rb, rete);		}		rs.init();				// Show Treat Viewer		if(showViewer)		{//			if(useTreat)//				tw = new TreatViewer(rs);//			else//			{//				RuleSystemExecutor rse = new RuleSystemExecutor(rs, true);//				RuleEnginePanel.createRuleEngineFrame(rse, "Rete Structure");//			}		}	}		private void repaintViewer()	{		if(showViewer && useTreat)		{//			tw.repaint();		}	}}