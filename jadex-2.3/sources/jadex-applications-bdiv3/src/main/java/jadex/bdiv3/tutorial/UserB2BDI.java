package jadex.bdiv3.tutorial;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goals;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.ServicePlan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.gui.PropertiesPanel;
import jadex.commons.gui.SGUI;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * 
 */
@Agent
@RequiredServices(@RequiredService(name="transser", type=ITranslationService.class, 
	binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)))
@Goals(@Goal(clazz=TranslationGoalB2.class))
@Plans(@Plan(trigger=@Trigger(goals=TranslationGoalB2.class), 
	body=@Body(service=@ServicePlan(name="transser", mapper=TranslationGoalMapperB2.class))))
public class UserB2BDI
{
	//-------- attributes --------

	@Agent
	protected BDIAgent agent;
	
	//-------- methods ---------

//	@Plan(trigger=@Trigger(goals=TranslationGoalB2.class), 
//		body=@Body(service=@ServicePlan(name="transser")))
//	public native IFuture<String> translateEnglishGerman(
//		@GoalMapping(clazz=TranslationGoalB2.class, val="gword") String eword);
	
	/**
	 * 
	 */
	@AgentBody
	public void body()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame f = new JFrame();
				
				PropertiesPanel pp = new PropertiesPanel();
				final JTextField tfe = pp.createTextField("English Word", "dog", true);
				final JTextField tfg = pp.createTextField("German Word");
				JButton bt = pp.createButton("Initiate", "Translate");
				
				bt.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						IFuture<String> fut = agent.dispatchTopLevelGoal(new TranslationGoalB2(tfe.getText()));
						fut.addResultListener(new IResultListener<String>()
						{
							public void resultAvailable(String res) 
							{
								tfg.setText(res);
							}
							
							public void exceptionOccurred(Exception exception)
							{
								exception.printStackTrace();
								tfg.setText(exception.getMessage());
							}
						});
					}
				});
				
				f.add(pp, BorderLayout.CENTER);
				
				f.pack();
				f.setLocation(SGUI.calculateMiddlePosition(f));
				f.setVisible(true);
			}
		});
	}
}

//<plan name="letotherpaintone">
//<parameter name="name" class="String">
//	<value>$scope.getComponentIdentifier().getName()</value>
//</parameter>
//<parameter name="result" class="String" direction="out"/>
//<body service="paintservices" method="paintOneEuro"/>
//<trigger>
//	<goal ref="getoneeuro"/>
//</trigger>
//</plan>
//
//<plan name="printrich">
//<body class="PrintRichPlan"/>
//<trigger>
//	<goalfinished ref="becomerich"/>
//</trigger>
//</plan>
//</plans>
//
//<services>
//<requiredservice name="paintservices" class="IPaintMoneyService" multiple="true">
//<binding dynamic="true" scope="platform"/>
//</requiredservice>
//</services>