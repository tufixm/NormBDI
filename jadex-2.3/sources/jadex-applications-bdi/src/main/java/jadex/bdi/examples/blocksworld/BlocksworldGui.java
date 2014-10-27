package jadex.bdi.examples.blocksworld;

import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IInternalEvent;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.TerminationAdapter;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.gui.SGUI;
import jadex.commons.transformation.annotations.Classname;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *  Shows the gui for blocksworld.
 */
public class BlocksworldGui	extends JFrame
{
	//-------- constructures --------
	
	/**
	 *  Create the blocksworld gui.
	 */
	public BlocksworldGui(final IBDIExternalAccess agent)
	{
		super();
		initGui(agent);
	}
	
	/**
	 *  Init the gui.
	 *  Method runs on AWT thread.
	 */
	protected void	initGui(final IBDIExternalAccess agent)
	{
		// HACK!! ensure that agent is inited
//		try
//		{
//			Thread.sleep(1000);
//		}
//		catch(Exception e)
//		{
//		}
		
		setTitle(agent.getComponentIdentifier().getName());
		final JPanel worlds = new JPanel(new GridLayout(1, 2));
		
		agent.scheduleStep(new IComponentStep<Void>()
		{
			@Classname("start")
			public IFuture<Void> execute(IInternalAccess ia)
			{
				IBDIInternalAccess bia = (IBDIInternalAccess)ia;
				final Block[] blocks = (Block[])bia.getBeliefbase().getBeliefSet("blocks").getFacts();
				final Table table = (Table)bia.getBeliefbase().getBelief("table").getFact();
				final Object md = bia.getBeliefbase().getBelief("mode").getFact();
				final Table buck = (Table)bia.getBeliefbase().getBelief("bucket").getFact();
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						final BlocksworldPanel	bwp	= new BlocksworldPanel(table, false);
						final JScrollPane	sp	= new JScrollPane(bwp, ScrollPaneConstants
							.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
						sp.setBorder(new BevelBorder(BevelBorder.LOWERED));
						JPanel	bw	= new JPanel(new BorderLayout());
						bw.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Current Blocksworld"));
						bw.add(BorderLayout.CENTER, sp);

						// Create target configuration panel.
//						final Table	newtable	= new Table((Table)agent.getBeliefbase().getBelief("table").getFact());
						final Table	newtable	= new Table();
						final BlocksworldPanel	bwp2	= new BlocksworldPanel(newtable, true);
						final JScrollPane	sp2	= new JScrollPane(bwp2, ScrollPaneConstants
							.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
						sp2.setBorder(new BevelBorder(BevelBorder.LOWERED));
						JPanel	bw2	= new JPanel(new BorderLayout());
						bw2.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Target Configuration"));
						bw2.add(BorderLayout.CENTER, sp2);

						worlds.add(bw2);
						worlds.add(bw);
						getContentPane().add(BorderLayout.CENTER, worlds);

						// Create zoom slider.
						final JSlider	zoom	= new JSlider(JSlider.HORIZONTAL, 25, 200, bwp.getBlockSize());
						zoom.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Zoom"));
						zoom.setMajorTickSpacing(25);
						zoom.setMinorTickSpacing(5);
						zoom.setPaintTicks(true);
						zoom.setPaintLabels(true);
						zoom.addChangeListener(new ChangeListener()
						{
							public void	stateChanged(ChangeEvent ce)
							{
								bwp.setBlockSize(zoom.getModel().getValue());
								bwp2.setBlockSize(zoom.getModel().getValue());
				 			}			
						});

						// Create target option panel.
						final DefaultListModel	newblocks	= new DefaultListModel();
						for(int i=0; i<blocks.length; i++)
							newblocks.addElement(new Block(blocks[i].number, blocks[i].getColor(), null));
						final JList	selblocks	= new JList(newblocks);
						selblocks.setVisibleRowCount(3);
						selblocks.setCellRenderer(new BlockCellRenderer());
						JScrollPane	ssp	= new JScrollPane(selblocks);
						ssp.setBorder(new BevelBorder(BevelBorder.LOWERED));
						final DefaultComboBoxModel	addblocks	= new DefaultComboBoxModel();
						addblocks.addElement(newtable);
						final JComboBox	addtarget	= new JComboBox(addblocks);
						addtarget.setRenderer(new BlockCellRenderer());
						selblocks.addMouseListener(new MouseAdapter()
						{
							public void	mouseClicked(MouseEvent me)
							{
								if(me.getClickCount()==2)
								{
									Block	block	= (Block)newblocks.getElementAt(
										selblocks.locationToIndex(me.getPoint()));
									Block	target	= (Block)addtarget.getSelectedItem();
									block.stackOn(target);
									newblocks.removeElement(block);
									if(target!=newtable)
										addblocks.removeElement(target);
									addblocks.addElement(block);
									addtarget.setSelectedItem(block);
								}
							}
						});
						final JButton	clear	= new JButton("Clear table");
						clear.addActionListener(new ActionListener()
						{
							public void	actionPerformed(ActionEvent ae)
							{
								// Reset blocks.
								newtable.clear();
								// Reset list.
								newblocks.removeAllElements();
								agent.scheduleStep(new IComponentStep<Void>()
								{
									@Classname("clear")
									public IFuture<Void> execute(IInternalAccess ia)
									{
										IBDIInternalAccess bia = (IBDIInternalAccess)ia;
										final Block[]	blocks = (Block[])bia.getBeliefbase().getBeliefSet("blocks").getFacts();
										SwingUtilities.invokeLater(new Runnable()
										{
											public void run()
											{
												for(int i=0; i<blocks.length; i++)
													newblocks.addElement(new Block(blocks[i].number, blocks[i].getColor(), null));
												// Reset combo box.
												addblocks.removeAllElements();
												addblocks.addElement(newtable);
											}
										});
										return IFuture.DONE;
									}
								});
//								agent.getBeliefbase().getBeliefSetFacts("blocks").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//								{
//									public void customResultAvailable(Object source, Object result)
//									{
//										Block[]	blocks = (Block[])result;
//										for(int i=0; i<blocks.length; i++)
//											newblocks.addElement(new Block(blocks[i].number, blocks[i].getColor(), null));
//										// Reset combo box.
//										addblocks.removeAllElements();
//										addblocks.addElement(newtable);
//									}
//								});
							}
						});
						JButton	goal	= new JButton("Create goal");
						goal.addActionListener(new ActionListener()
						{
							public void	actionPerformed(ActionEvent ae)
							{
								agent.scheduleStep(new IComponentStep<Void>()
								{
									@Classname("configure")
									public IFuture<Void> execute(IInternalAccess ia)
									{
										IBDIInternalAccess bia = (IBDIInternalAccess)ia;
										IGoal achieve = bia.getGoalbase().createGoal("configure");
										achieve.getParameter("configuration").setValue(newtable);
										// Hack!!! Blocks must be in state directly.
										achieve.getParameterSet("blocks").addValues(newtable.getAllBlocks());
										bia.getGoalbase().dispatchTopLevelGoal(achieve);
										return IFuture.DONE;
									}
								});
										
//								agent.getGoalbase().createGoal("configure").addResultListener(new DefaultResultListener()
//								{
//									
//									public void resultAvailable(Object source, Object result)
//									{
//										IEAGoal achieve	= (IEAGoal)result;
//										achieve.setParameterValue("configuration", newtable);
//										// Hack!!! Blocks must be in state directly.
//										achieve.addParameterSetValues("blocks", newtable.getAllBlocks());
//										agent.getGoalbase().dispatchTopLevelGoal(achieve);
//									}
//								});
							}
						});
						JPanel	options = new JPanel(new GridBagLayout());
						bw2.add(BorderLayout.SOUTH, options);
						// First coloum (block list)
						options.add(new JLabel("Double-click to add blocks"), new GridBagConstraints(0, 0, 1, 1, 1, 1,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,0,2), 0, 0));
						options.add(ssp, new GridBagConstraints(0, 1, 1, GridBagConstraints.REMAINDER, 1, 1,
							GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,2), 0, 0));
						// Second column (choice and buttons)
						options.add(new JLabel("Stack on"), new GridBagConstraints(1, 0, 1, 1, 0, 0,
							GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,2), 0, 0));
						options.add(addtarget, new GridBagConstraints(2, 0, GridBagConstraints.REMAINDER, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,5), 5, 0));
						options.add(clear, new GridBagConstraints(1, 1, GridBagConstraints.REMAINDER, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,5), 0, 0));
						options.add(goal, new GridBagConstraints(1, 2, GridBagConstraints.REMAINDER, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,5), 0, 0));

						// Create blocksworld option panel
						JPanel	bwoptions = new JPanel(new GridBagLayout());
						bw.add(BorderLayout.SOUTH, bwoptions);
						// Create block components.
						final JLabel	showcol	= new JLabel(" color ");
						showcol.setOpaque(true);
						showcol.setBackground(new Color(240, 128, 16));
						final DefaultComboBoxModel	delblocks	= new DefaultComboBoxModel();
						for(int i=0; i<blocks.length; i++)
							delblocks.addElement(blocks[i]);
						final JComboBox	delblock	= new JComboBox(delblocks);
						delblock.setRenderer(new BlockCellRenderer());
						JButton	create	= new JButton("create block");
						create.addActionListener(new ActionListener()
						{
							public void	actionPerformed(ActionEvent ae)
							{
								agent.scheduleStep(new IComponentStep<Void>()
								{
									@Classname("createBlock")
									public IFuture<Void> execute(IInternalAccess ia)
									{
										IBDIInternalAccess bia = (IBDIInternalAccess)ia;
										Table table = (Table)bia.getBeliefbase().getBelief("table").getFact();
										final Block block = new Block(showcol.getBackground(), table);
										bia.getBeliefbase().getBeliefSet("blocks").addFact(block);
										
										SwingUtilities.invokeLater(new Runnable()
										{
											public void run()
											{
												delblocks.addElement(block);
												newblocks.addElement(new Block(block.number, showcol.getBackground(), null));
											}
										});
										return IFuture.DONE;
									}
								});
										
//								agent.getBeliefbase().getBeliefFact("table").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//								{
//									public void customResultAvailable(Object source, Object result)
//									{
//										Block block = new Block(showcol.getBackground(), (Block)result);
//										delblocks.addElement(block);
//										newblocks.addElement(new Block(block.number, showcol.getBackground(), null));
//										agent.getBeliefbase().addBeliefSetFact("blocks", block);
//									}
//								});
							}
						});
						final JButton	color	= new JButton("choose...");
						color.addActionListener(new ActionListener()
						{
							public void	actionPerformed(ActionEvent ae)
							{
								Color	newcol	= JColorChooser.showDialog(showcol, "Choose block color", showcol.getBackground());
								if(newcol!=null)
								{
									showcol.setBackground(newcol);
								}
							}
						});
						// Delete block components
						JButton	delete	= new JButton("delete block");
						delete.addActionListener(new ActionListener()
						{
							public void	actionPerformed(ActionEvent ae)
							{
								final Block	block	= (Block)delblock.getSelectedItem();
								Block	upper	= block.upper;
								Block	lower	= block.getLower();
								lower.removeBlock(block);
								if(upper!=null)
								{
									block.removeBlock(upper);
									lower.addBlock(upper);
									upper.setLower(lower);
								}
								delblocks.removeElement(block);
								agent.scheduleStep(new IComponentStep<Void>()
								{
									@Classname("deleteBlock")
									public IFuture<Void> execute(IInternalAccess ia)
									{
										IBDIInternalAccess bia = (IBDIInternalAccess)ia;
										bia.getBeliefbase().getBeliefSet("blocks").removeFact(block);
										clear.doClick();
										return IFuture.DONE;
									}
								});
//								agent.getBeliefbase().removeBeliefSetFact("blocks", block);
//								clear.doClick();	// Hack!!! Reads blocks from beliefbase.
							}
						});
						// Execution mode components
						final JComboBox	mode	= new JComboBox(new String[]
							{StackBlocksPlan.MODE_NORMAL, StackBlocksPlan.MODE_STEP, StackBlocksPlan.MODE_SLOW});
						
						mode.setSelectedItem(md);
//						agent.getBeliefbase().getBeliefFact("mode").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//						{
//							public void customResultAvailable(Object source, Object result)
//							{
//								mode.setSelectedItem(result);
//							}
//						});
						final JButton	step	= new JButton("step");
						step.setEnabled(mode.getSelectedItem().equals(StackBlocksPlan.MODE_STEP));
						mode.addItemListener(new ItemListener()
						{
							public void	itemStateChanged(ItemEvent ie)
							{
								step.setEnabled(mode.getSelectedItem().equals(StackBlocksPlan.MODE_STEP));
								final Object sel = mode.getSelectedItem();
								agent.scheduleStep(new IComponentStep<Void>()
								{
									@Classname("mode")
									public IFuture<Void> execute(IInternalAccess ia)
									{
										IBDIInternalAccess bia = (IBDIInternalAccess)ia;
										bia.getBeliefbase().getBelief("mode").setFact(sel);
										return IFuture.DONE;
									}
								});
//								agent.getBeliefbase().setBeliefFact("mode", mode.getSelectedItem());
							}
						});
						step.addActionListener(new ActionListener()
						{
							public void	actionPerformed(ActionEvent ae)
							{
								agent.scheduleStep(new IComponentStep<Void>()
								{
									@Classname("step")
									public IFuture<Void> execute(IInternalAccess ia)
									{
										IBDIInternalAccess bia = (IBDIInternalAccess)ia;
										IInternalEvent ie = bia.getEventbase().createInternalEvent("step");
										bia.getEventbase().dispatchInternalEvent(ie);
										return IFuture.DONE;
									}
								});
								
//								agent.getEventbase().createInternalEvent("step").addResultListener(new DefaultResultListener()
//								{
//									public void resultAvailable(Object source, Object result)
//									{
//										agent.getEventbase().dispatchInternalEvent((IEAInternalEvent)result);
//									}
//								});
							}
						});
						// Bucket components
						final JList bucket = new JList();
						bucket.setModel(new BlocksListModel(buck));
//						agent.getBeliefbase().getBeliefFact("bucket").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//						{
//							public void customResultAvailable(Object source, Object result)
//							{
//								bucket.setModel(new BlocksListModel((Table)result));
//							}
//						});
						bucket.setVisibleRowCount(3);
						bucket.setCellRenderer(new BlockCellRenderer());
						JScrollPane	bsp	= new JScrollPane(bucket);
						bsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
						bsp.setPreferredSize(new Dimension(100,0));
						// Create-block components
						bwoptions.add(create, new GridBagConstraints(0, 0, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,2), 0, 0));
						bwoptions.add(showcol, new GridBagConstraints(1, 0, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
						bwoptions.add(color, new GridBagConstraints(2, 0, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
						// Delete-block components
						bwoptions.add(delete, new GridBagConstraints(0, 1, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,2), 0, 0));
						bwoptions.add(delblock, new GridBagConstraints(1, 1, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 5, 0));
						// Execution mode components
						bwoptions.add(new JLabel("Execution mode"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,2,2), 0, 0));
						bwoptions.add(mode, new GridBagConstraints(1, 2, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
						bwoptions.add(step, new GridBagConstraints(2, 2, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
						// Bucket components
						bwoptions.add(new JLabel("Bucket"), new GridBagConstraints(3, 0, 1, 1, 1, 0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,0,2), 0, 0));
						bwoptions.add(bsp, new GridBagConstraints(3, 1, 1, GridBagConstraints.REMAINDER, 1, 1,
							GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,2,2,2), 0, 0));

						Dimension	dim1	= bwoptions.getPreferredSize();
						Dimension	dim2	= options.getPreferredSize();
						dim1.height	= dim2.height	= Math.max(dim1.height, dim2.height);
						bwoptions.setPreferredSize(dim1);
						options.setPreferredSize(dim2);

						getContentPane().add(BorderLayout.SOUTH, zoom);

						pack();
						setLocation(SGUI.calculateMiddlePosition(BlocksworldGui.this));
						setVisible(true);
						addWindowListener(new WindowAdapter()
						{
							public void	windowClosing(WindowEvent we)
							{
								agent.killComponent();
							}
						});
						
						agent.scheduleStep(new IComponentStep<Void>()
						{
							@Classname("disp")
							public IFuture<Void> execute(IInternalAccess ia)
							{
								IBDIInternalAccess bia = (IBDIInternalAccess)ia;
								bia.addComponentListener(new TerminationAdapter()
								{
									public void componentTerminated()
									{
										SwingUtilities.invokeLater(new Runnable()
										{
											public void run()
											{
												BlocksworldGui.this.dispose();
											}
										});
									}
								});
								return IFuture.DONE;
							}
						}).addResultListener(new IResultListener<Void>()
						{
							public void resultAvailable(Void result)
							{
							}
							
							public void exceptionOccurred(Exception exception)
							{
								BlocksworldGui.this.dispose();
							}
						});
					}
				});
				return IFuture.DONE;
			}
		});
		
//		agent.getBeliefbase().getBeliefSetFacts("blocks").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//		{
//			public void customResultAvailable(Object source, Object result)
//			{
//				final Block[] blocks = (Block[])result;
//				final JPanel	worlds	= new JPanel(new GridLayout(1, 2));
//				// Create blocksworld panel.
//				agent.getBeliefbase().getBeliefFact("table").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//				{
//					public void customResultAvailable(Object source, Object result)
//					{
//						final BlocksworldPanel	bwp	= new BlocksworldPanel((Table)result, false);
//						final JScrollPane	sp	= new JScrollPane(bwp, ScrollPaneConstants
//							.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//						sp.setBorder(new BevelBorder(BevelBorder.LOWERED));
//						JPanel	bw	= new JPanel(new BorderLayout());
//						bw.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Current Blocksworld"));
//						bw.add(BorderLayout.CENTER, sp);
//
//						// Create target configuration panel.
////						final Table	newtable	= new Table((Table)agent.getBeliefbase().getBelief("table").getFact());
//						final Table	newtable	= new Table();
//						final BlocksworldPanel	bwp2	= new BlocksworldPanel(newtable, true);
//						final JScrollPane	sp2	= new JScrollPane(bwp2, ScrollPaneConstants
//							.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//						sp2.setBorder(new BevelBorder(BevelBorder.LOWERED));
//						JPanel	bw2	= new JPanel(new BorderLayout());
//						bw2.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Target Configuration"));
//						bw2.add(BorderLayout.CENTER, sp2);
//
//						worlds.add(bw2);
//						worlds.add(bw);
//						getContentPane().add(BorderLayout.CENTER, worlds);
//
//						// Create zoom slider.
//						final JSlider	zoom	= new JSlider(JSlider.HORIZONTAL, 25, 200, bwp.getBlockSize());
//						zoom.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Zoom"));
//						zoom.setMajorTickSpacing(25);
//						zoom.setMinorTickSpacing(5);
//						zoom.setPaintTicks(true);
//						zoom.setPaintLabels(true);
//						zoom.addChangeListener(new ChangeListener()
//						{
//							public void	stateChanged(ChangeEvent ce)
//							{
//								bwp.setBlockSize(zoom.getModel().getValue());
//								bwp2.setBlockSize(zoom.getModel().getValue());
//				 			}			
//						});
//
//						// Create target option panel.
//						final DefaultListModel	newblocks	= new DefaultListModel();
//						for(int i=0; i<blocks.length; i++)
//							newblocks.addElement(new Block(blocks[i].number, blocks[i].getColor(), null));
//						final JList	selblocks	= new JList(newblocks);
//						selblocks.setVisibleRowCount(3);
//						selblocks.setCellRenderer(new BlockCellRenderer());
//						JScrollPane	ssp	= new JScrollPane(selblocks);
//						ssp.setBorder(new BevelBorder(BevelBorder.LOWERED));
//						final DefaultComboBoxModel	addblocks	= new DefaultComboBoxModel();
//						addblocks.addElement(newtable);
//						final JComboBox	addtarget	= new JComboBox(addblocks);
//						addtarget.setRenderer(new BlockCellRenderer());
//						selblocks.addMouseListener(new MouseAdapter()
//						{
//							public void	mouseClicked(MouseEvent me)
//							{
//								if(me.getClickCount()==2)
//								{
//									Block	block	= (Block)newblocks.getElementAt(
//										selblocks.locationToIndex(me.getPoint()));
//									Block	target	= (Block)addtarget.getSelectedItem();
//									block.stackOn(target);
//									newblocks.removeElement(block);
//									if(target!=newtable)
//										addblocks.removeElement(target);
//									addblocks.addElement(block);
//									addtarget.setSelectedItem(block);
//								}
//							}
//						});
//						final JButton	clear	= new JButton("Clear table");
//						clear.addActionListener(new ActionListener()
//						{
//							public void	actionPerformed(ActionEvent ae)
//							{
//								// Reset blocks.
//								newtable.clear();
//								// Reset list.
//								newblocks.removeAllElements();
//								agent.getBeliefbase().getBeliefSetFacts("blocks").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//								{
//									public void customResultAvailable(Object source, Object result)
//									{
//										Block[]	blocks = (Block[])result;
//										for(int i=0; i<blocks.length; i++)
//											newblocks.addElement(new Block(blocks[i].number, blocks[i].getColor(), null));
//										// Reset combo box.
//										addblocks.removeAllElements();
//										addblocks.addElement(newtable);
//									}
//								});
//							}
//						});
//						JButton	goal	= new JButton("Create goal");
//						goal.addActionListener(new ActionListener()
//						{
//							public void	actionPerformed(ActionEvent ae)
//							{
//								agent.getGoalbase().createGoal("configure").addResultListener(new DefaultResultListener()
//								{
//									
//									public void resultAvailable(Object source, Object result)
//									{
//										IEAGoal achieve	= (IEAGoal)result;
//										achieve.setParameterValue("configuration", newtable);
//										// Hack!!! Blocks must be in state directly.
//										achieve.addParameterSetValues("blocks", newtable.getAllBlocks());
//										agent.getGoalbase().dispatchTopLevelGoal(achieve);
//									}
//								});
//							}
//						});
//						JPanel	options = new JPanel(new GridBagLayout());
//						bw2.add(BorderLayout.SOUTH, options);
//						// First coloum (block list)
//						options.add(new JLabel("Double-click to add blocks"), new GridBagConstraints(0, 0, 1, 1, 1, 1,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,0,2), 0, 0));
//						options.add(ssp, new GridBagConstraints(0, 1, 1, GridBagConstraints.REMAINDER, 1, 1,
//							GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,2), 0, 0));
//						// Second column (choice and buttons)
//						options.add(new JLabel("Stack on"), new GridBagConstraints(1, 0, 1, 1, 0, 0,
//							GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,2), 0, 0));
//						options.add(addtarget, new GridBagConstraints(2, 0, GridBagConstraints.REMAINDER, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,5), 5, 0));
//						options.add(clear, new GridBagConstraints(1, 1, GridBagConstraints.REMAINDER, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,5), 0, 0));
//						options.add(goal, new GridBagConstraints(1, 2, GridBagConstraints.REMAINDER, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,5), 0, 0));
//
//						// Create blocksworld option panel
//						JPanel	bwoptions = new JPanel(new GridBagLayout());
//						bw.add(BorderLayout.SOUTH, bwoptions);
//						// Create block components.
//						final JLabel	showcol	= new JLabel(" color ");
//						showcol.setOpaque(true);
//						showcol.setBackground(new Color(240, 128, 16));
//						final DefaultComboBoxModel	delblocks	= new DefaultComboBoxModel();
//						for(int i=0; i<blocks.length; i++)
//							delblocks.addElement(blocks[i]);
//						final JComboBox	delblock	= new JComboBox(delblocks);
//						delblock.setRenderer(new BlockCellRenderer());
//						JButton	create	= new JButton("create block");
//						create.addActionListener(new ActionListener()
//						{
//							public void	actionPerformed(ActionEvent ae)
//							{
//								agent.getBeliefbase().getBeliefFact("table").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//								{
//									public void customResultAvailable(Object source, Object result)
//									{
//										Block block = new Block(showcol.getBackground(), (Block)result);
//										delblocks.addElement(block);
//										newblocks.addElement(new Block(block.number, showcol.getBackground(), null));
//										agent.getBeliefbase().addBeliefSetFact("blocks", block);
//									}
//								});
//							}
//						});
//						final JButton	color	= new JButton("choose...");
//						color.addActionListener(new ActionListener()
//						{
//							public void	actionPerformed(ActionEvent ae)
//							{
//								Color	newcol	= JColorChooser.showDialog(showcol, "Choose block color", showcol.getBackground());
//								if(newcol!=null)
//								{
//									showcol.setBackground(newcol);
//								}
//							}
//						});
//						// Delete block components
//						JButton	delete	= new JButton("delete block");
//						delete.addActionListener(new ActionListener()
//						{
//							public void	actionPerformed(ActionEvent ae)
//							{
//								final Block	block	= (Block)delblock.getSelectedItem();
//								Block	upper	= block.upper;
//								Block	lower	= block.getLower();
//								lower.removeBlock(block);
//								if(upper!=null)
//								{
//									block.removeBlock(upper);
//									lower.addBlock(upper);
//									upper.setLower(lower);
//								}
//								delblocks.removeElement(block);
//								agent.getBeliefbase().removeBeliefSetFact("blocks", block);
//								clear.doClick();	// Hack!!! Reads blocks from beliefbase.
//							}
//						});
//						// Execution mode components
//						final JComboBox	mode	= new JComboBox(new String[]
//							{StackBlocksPlan.MODE_NORMAL, StackBlocksPlan.MODE_STEP, StackBlocksPlan.MODE_SLOW});
//						agent.getBeliefbase().getBeliefFact("mode").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//						{
//							public void customResultAvailable(Object source, Object result)
//							{
//								mode.setSelectedItem(result);
//							}
//						});
//						final JButton	step	= new JButton("step");
//						step.setEnabled(mode.getSelectedItem().equals(StackBlocksPlan.MODE_STEP));
//						mode.addItemListener(new ItemListener()
//						{
//							public void	itemStateChanged(ItemEvent ie)
//							{
//								step.setEnabled(mode.getSelectedItem().equals(StackBlocksPlan.MODE_STEP));
//								agent.getBeliefbase().setBeliefFact("mode", mode.getSelectedItem());
//							}
//						});
//						step.addActionListener(new ActionListener()
//						{
//							public void	actionPerformed(ActionEvent ae)
//							{
//								agent.getEventbase().createInternalEvent("step").addResultListener(new DefaultResultListener()
//								{
//									public void resultAvailable(Object source, Object result)
//									{
//										agent.getEventbase().dispatchInternalEvent((IEAInternalEvent)result);
//									}
//								});
//							}
//						});
//						// Bucket components
//						final JList bucket = new JList();
//						agent.getBeliefbase().getBeliefFact("bucket").addResultListener(new SwingDefaultResultListener(BlocksworldGui.this)
//						{
//							public void customResultAvailable(Object source, Object result)
//							{
//								bucket.setModel(new BlocksListModel((Table)result));
//							}
//						});
//						bucket.setVisibleRowCount(3);
//						bucket.setCellRenderer(new BlockCellRenderer());
//						JScrollPane	bsp	= new JScrollPane(bucket);
//						bsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
//						bsp.setPreferredSize(new Dimension(100,0));
//						// Create-block components
//						bwoptions.add(create, new GridBagConstraints(0, 0, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,2), 0, 0));
//						bwoptions.add(showcol, new GridBagConstraints(1, 0, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
//						bwoptions.add(color, new GridBagConstraints(2, 0, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
//						// Delete-block components
//						bwoptions.add(delete, new GridBagConstraints(0, 1, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,2,2), 0, 0));
//						bwoptions.add(delblock, new GridBagConstraints(1, 1, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 5, 0));
//						// Execution mode components
//						bwoptions.add(new JLabel("Execution mode"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
//							GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,2,2), 0, 0));
//						bwoptions.add(mode, new GridBagConstraints(1, 2, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
//						bwoptions.add(step, new GridBagConstraints(2, 2, 1, 1, 0, 0,
//							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,2,2,2), 0, 0));
//						// Bucket components
//						bwoptions.add(new JLabel("Bucket"), new GridBagConstraints(3, 0, 1, 1, 1, 0,
//							GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,0,2), 0, 0));
//						bwoptions.add(bsp, new GridBagConstraints(3, 1, 1, GridBagConstraints.REMAINDER, 1, 1,
//							GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,2,2,2), 0, 0));
//
//						Dimension	dim1	= bwoptions.getPreferredSize();
//						Dimension	dim2	= options.getPreferredSize();
//						dim1.height	= dim2.height	= Math.max(dim1.height, dim2.height);
//						bwoptions.setPreferredSize(dim1);
//						options.setPreferredSize(dim2);
//
//						getContentPane().add(BorderLayout.SOUTH, zoom);
//
//						pack();
//						setLocation(SGUI.calculateMiddlePosition(BlocksworldGui.this));
//						setVisible(true);
//						addWindowListener(new WindowAdapter()
//						{
//							public void	windowClosing(WindowEvent we)
//							{
//								agent.killComponent();
//							}
//						});
//						
//						agent.addAgentListener(new IAgentListener()
//						{
//							public void agentTerminating(AgentEvent ae)
//							{
//								SwingUtilities.invokeLater(new Runnable()
//								{
//									public void run()
//									{
//										BlocksworldGui.this.dispose();
//									}
//								});
//							}
//							
//							public void agentTerminated(AgentEvent ae)
//							{
//							}
//						});
//					}
//				});
//			}
//		});
	}
}

