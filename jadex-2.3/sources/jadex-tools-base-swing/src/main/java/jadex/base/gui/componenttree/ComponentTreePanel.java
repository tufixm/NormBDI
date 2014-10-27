package jadex.base.gui.componenttree;

import jadex.base.gui.CMSUpdateHandler;
import jadex.base.gui.ObjectInspectorPanel;
import jadex.base.gui.asynctree.AbstractSwingTreeNode;
import jadex.base.gui.asynctree.AsyncTreeCellRenderer;
import jadex.base.gui.asynctree.AsyncSwingTreeModel;
import jadex.base.gui.asynctree.ISwingNodeHandler;
import jadex.base.gui.asynctree.ISwingTreeNode;
import jadex.base.gui.asynctree.TreePopupListener;
import jadex.base.gui.componentviewer.IAbstractViewerPanel;
import jadex.base.gui.componentviewer.IComponentViewerPanel;
import jadex.base.gui.componentviewer.IServiceViewerPanel;
import jadex.base.gui.plugin.AbstractJCCPlugin;
import jadex.base.gui.plugin.IControlCenter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.security.ISecurityService;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.gui.CombiIcon;
import jadex.commons.gui.SGUI;
import jadex.commons.gui.TreeExpansionHandler;
import jadex.commons.gui.future.SwingDefaultResultListener;
import jadex.base.gui.asynctree.ITreeNode;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.TreePath;

/**
 *  A panel displaying components on the platform as tree.
 */
public class ComponentTreePanel extends JSplitPane
{
	//-------- constants --------

	/**
	 * The image icons.
	 */
	public static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"component_suspended", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_szzz.png"),
		"kill_component", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/new_killagent.png"),
		"suspend_component", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/new_agent_szzz_big.png"),
		"resume_component", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/resume_component.png"),
		"step_component", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/step_component.png"),
		"refresh", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/refresh_component.png"),
		"refresh_tree", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/refresh_tree.png"),
		"show_properties", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/new_agent_props.png"),
		"show_details", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/new_agent_details.png"),
		"overlay_kill", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_kill.png"),
		"overlay_suspend", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_szzz.png"),
		"overlay_resume", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_wakeup.png"),
		"overlay_step", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_step.png"),
		// no overlay icon for idle (default state)
//		"overlay_idle", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_trafficlight_red.png"),
		"overlay_ready", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_busy.png"),
		"overlay_running", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_gearwheel.png"),
		"overlay_refresh", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_refresh.png"),
		"overlay_refreshtree", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_refresh.png"),
		"overlay_showprops", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_doc.png"),
		"overlay_showobject", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_bean.png"),
		"overlay_lock", SGUI.makeIcon(ComponentTreePanel.class, "/jadex/base/gui/images/overlay_proxy_locked.png")
	});
	
	/** The kill action constant. */
	public static final String KILL_ACTION = "Kill component";
	
	/** The proxy kill action constant. */
	public static final String PROXYKILL_ACTION = "Kill also remote component";

	/** The suspend action constant. */
	public static final String SUSPEND_ACTION = "Suspend component";

	/** The resume action constant. */
	public static final String RESUME_ACTION = "Resume component";

	/** The step action constant. */
	public static final String STEP_ACTION = "Step component";

	/** The refresh action constant. */
	public static final String REFRESH_ACTION = "Refresh";

	/** The refreshtree action constant. */
	public static final String REFRESHSUBTREE_ACTION = "Refresh subtree";

	/** The show properties action constant. */
	public static final String SHOWPROPERTIES_ACTION = "Show properties";
	
	/** The remove service action constant. */
	public static final String REMOVESERVICE_ACTION = "Remove service";

	/** The remove service action constant. */
	public static final String SHOWDETAILS_ACTION = "Show object details";

	/** The set password action constant. */
	public static final String SET_PASSWD_ACTION = "Set/change remote platform password";

	
	//-------- attributes --------
	
	/** The external access. */
	protected final IExternalAccess	access;
	
	/** The component tree model. */
	protected final AsyncSwingTreeModel	model;
	
	/** The component tree. */
	protected final JTree	tree;
	
	/** The component management service. */
	protected IComponentManagementService	cms;

	/** The actions. */
	protected Map actions;
	
	/** The properties panel. */
	protected final JScrollPane	proppanel;
	
	
	//-------- constructors --------
	
	/**
	 *  Create a new component tree panel.
	 */
	public ComponentTreePanel(IExternalAccess access, CMSUpdateHandler cmshandler, ComponentIconCache cic)
	{
		this(access, cmshandler, cic, VERTICAL_SPLIT);
	}
	
	/**
	 *  Create a new component tree panel.
	 */
	public ComponentTreePanel(final IExternalAccess access, CMSUpdateHandler cmshandler, final ComponentIconCache cic, int orientation)
	{
		super(orientation);
		this.setOneTouchExpandable(true);
		
		this.actions = new HashMap();
		this.access	= access;
		this.model	= new AsyncSwingTreeModel();
		this.tree	= new JTree(model);
		tree.setCellRenderer(new AsyncTreeCellRenderer());
		tree.addMouseListener(new TreePopupListener());
		tree.setShowsRootHandles(true);
		tree.setToggleClickCount(0);
		tree.putClientProperty(CMSUpdateHandler.class, cmshandler);
		JScrollPane	scroll	= new JScrollPane(tree);
		this.add(scroll);
		// needed to show tooltips: http://info.michael-simons.eu/2008/08/12/enabling-tooltips-on-a-jtree/
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		this.proppanel	= new JScrollPane();
		proppanel.setMinimumSize(new Dimension(0, 0));
		proppanel.setPreferredSize(new Dimension(0, 0));
		this.add(proppanel);
		this.setResizeWeight(1.0);
				

		final Action kill = new AbstractAction(KILL_ACTION, icons.getIcon("kill_component"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if(cms!=null)
				{
					final TreePath[] paths	= tree.getSelectionPaths();
					for(int i=0; paths!=null && i<paths.length; i++)
					{
						// note: cannot use getComponentIdenfier() due to proxy components return their remote cid
//						final IActiveComponentTreeNode sel = (IActiveComponentTreeNode)paths[i].getLastPathComponent();
						final IComponentIdentifier cid = ((IActiveComponentTreeNode)paths[i].getLastPathComponent()).getDescription().getName();
//						final ISwingTreeNode sel = (ISwingTreeNode)paths[i].getLastPathComponent();
						cms.resumeComponent(cid).addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
						{
							public void customResultAvailable(Object result)
							{
								cms.destroyComponent(cid).addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
								{
									public void customResultAvailable(Object result)
									{
										// Done by CMS listener?
//										if(sel.getParent()!=null)
//										{
//											((AbstractSwingTreeNode)sel.getParent()).removeChild(sel);
//										}
									}
									
									public void customExceptionOccurred(Exception exception)
									{
										super.customExceptionOccurred(new RuntimeException("Could not kill component: "+cid, exception));
									}
								});
							}
						});
					}
				}
			}
		};
		actions.put(kill.getValue(Action.NAME), kill);
		
		final Action proxykill = new AbstractAction(PROXYKILL_ACTION, icons.getIcon("kill_component"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if(cms!=null)
				{
					TreePath[]	paths	= tree.getSelectionPaths();
					for(int i=0; paths!=null && i<paths.length; i++)
					{
						final ProxyComponentTreeNode sel = (ProxyComponentTreeNode)paths[i].getLastPathComponent();
						
						sel.getRemoteComponentIdentifier().addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
						{
							public void customResultAvailable(Object result)
							{
								final IComponentIdentifier cid = (IComponentIdentifier)result;
								
								killComponent(access, cid).addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
								{
									public void	customResultAvailable(Object o)
									{
										if(sel.getParent()!=null)
										{
											((AbstractSwingTreeNode)sel.getParent()).removeChild(sel);
										}										
									}
								});
							}
						});
					}
				}
			}
		};
		actions.put(proxykill.getValue(Action.NAME), proxykill);

		final Action suspend	= new AbstractAction(SUSPEND_ACTION, icons.getIcon("suspend_component"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if(cms!=null)
				{
					TreePath[]	paths	= tree.getSelectionPaths();
					for(int i=0; paths!=null && i<paths.length; i++)
					{
						final IComponentIdentifier cid = ((IActiveComponentTreeNode)paths[i].getLastPathComponent()).getDescription().getName();
						final ISwingTreeNode sel = (ISwingTreeNode)paths[i].getLastPathComponent();
						cms.suspendComponent(cid).addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
						{
							public void customResultAvailable(Object result)
							{
								// Required for remote nodes.
								sel.refresh(false);
							}
						});
					}
				}
			}
		};
		actions.put(suspend.getValue(Action.NAME), suspend);

		
		final Action resume = new AbstractAction(RESUME_ACTION, icons.getIcon("resume_component"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if(cms!=null)
				{
					TreePath[]	paths	= tree.getSelectionPaths();
					for(int i=0; paths!=null && i<paths.length; i++)
					{
						final IComponentIdentifier cid = ((IActiveComponentTreeNode)paths[i].getLastPathComponent()).getDescription().getName();
						final ISwingTreeNode sel = (ISwingTreeNode)paths[i].getLastPathComponent();
						cms.resumeComponent(cid).addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
						{
							public void customResultAvailable(Object result)
							{
								// Required for remote nodes.
								sel.refresh(false);
							}
						});
					}
				}
			}
		};
		actions.put(resume.getValue(Action.NAME), resume);

		final Action step	= new AbstractAction(STEP_ACTION, icons.getIcon("step_component"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if(cms!=null)
				{
					TreePath[]	paths	= tree.getSelectionPaths();
					for(int i=0; paths!=null && i<paths.length; i++)
					{
						final IComponentIdentifier cid = ((IActiveComponentTreeNode)paths[i].getLastPathComponent()).getDescription().getName();

						final ISwingTreeNode sel = (ISwingTreeNode)paths[i].getLastPathComponent();
						cms.stepComponent(cid).addResultListener(new SwingDefaultResultListener(ComponentTreePanel.this)
						{
							public void customResultAvailable(Object result)
							{
								// Required for remote nodes.
								sel.refresh(false);
							}
						});
					}
				}
			}
		};
		actions.put(step.getValue(Action.NAME), step);

		final Action refresh = new AbstractAction(REFRESH_ACTION, icons.getIcon("refresh"))
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath[]	paths	= tree.getSelectionPaths();
				for(int i=0; paths!=null && i<paths.length; i++)
				{
					((ISwingTreeNode)paths[i].getLastPathComponent()).refresh(false);
				}
			}
		};
		actions.put(refresh.getValue(Action.NAME), refresh);

		final Action refreshtree	= new AbstractAction(REFRESHSUBTREE_ACTION, icons.getIcon("refresh_tree"))
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath[]	paths	= tree.getSelectionPaths();
				for(int i=0; paths!=null && i<paths.length; i++)
				{
					((ISwingTreeNode)paths[i].getLastPathComponent()).refresh(true);
				}
			}
		};
		actions.put(refreshtree.getValue(Action.NAME), refreshtree);

		final Action showprops = new AbstractAction(SHOWPROPERTIES_ACTION, icons.getIcon("show_properties"))
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath	path	= tree.getSelectionPath();
				if(path!=null && ((ISwingTreeNode)path.getLastPathComponent()).hasProperties())
				{
					showProperties(((ISwingTreeNode)path.getLastPathComponent()).getPropertiesComponent());
				}
			}
		};
		actions.put(showprops.getValue(Action.NAME), showprops);

		final Action removeservice = new AbstractAction(REMOVESERVICE_ACTION, icons.getIcon("show_properties"))
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = tree.getSelectionPath();
				if(path!=null)
				{
					final ServiceContainerNode scn = (ServiceContainerNode)path.getPathComponent(path.getPathCount()-2);
					final ProvidedServiceInfoNode sn = (ProvidedServiceInfoNode)path.getLastPathComponent();
					scn.getContainer().removeService(sn.getServiceIdentifier()).addResultListener(new SwingDefaultResultListener(proppanel)
					{
						public void customResultAvailable(Object result)
						{
							scn.removeChild(sn);
						}
					});
				}
			}
		};
		actions.put(removeservice.getValue(Action.NAME), removeservice);

		final Action showobject = new AbstractAction(SHOWDETAILS_ACTION, icons.getIcon("show_details"))
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = tree.getSelectionPath();
				if(path!=null)
				{
					final ISwingTreeNode node = (ISwingTreeNode)path.getLastPathComponent();
					if(node instanceof ProvidedServiceInfoNode)
					{
//						Object obj = ((ProvidedServiceInfoNode)node).getService();
						IServiceIdentifier sid = ((ProvidedServiceInfoNode)node).getServiceIdentifier();
						IFuture<Object> fut = SServiceProvider.getService(access.getServiceProvider(), sid);
						fut.addResultListener(new SwingDefaultResultListener<Object>()
						{
							public void customResultAvailable(Object obj)
							{
								JPanel panel = new ObjectInspectorPanel(obj);
								showProperties(panel);
							}
						});
					}
					else if(node instanceof IActiveComponentTreeNode)
					{
						//IComponentDescription desc = ((IActiveComponentTreeNode)node).getDescription();
						IComponentIdentifier cid = ((IActiveComponentTreeNode)node).getDescription().getName();
						cms.getExternalAccess(cid).addResultListener(new SwingDefaultResultListener((Component)null)
						{
							public void customResultAvailable(Object result)
							{
								IExternalAccess	ea	= (IExternalAccess)result;
								JPanel panel = new ObjectInspectorPanel(ea);
								showProperties(panel);
							}
						});
					}
				}
			}
		};
		actions.put(showobject.getValue(Action.NAME), showobject);

		final Action setpasswd = new AbstractAction(SET_PASSWD_ACTION)
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = tree.getSelectionPath();
				if(path!=null)
				{
					final ProxyComponentTreeNode  pctn = (ProxyComponentTreeNode)path.getLastPathComponent();
					final IComponentIdentifier	cid	= pctn.getComponentIdentifier();
					if(cid!=null)
					{
						SServiceProvider.getService(access.getServiceProvider(), ISecurityService.class, RequiredServiceInfo.SCOPE_PLATFORM)
							.addResultListener(new SwingDefaultResultListener<ISecurityService>(ComponentTreePanel.this)
						{
							public void customResultAvailable(final ISecurityService sec)
							{
								sec.getPlatformPassword(cid)
									.addResultListener(new SwingDefaultResultListener<String>(ComponentTreePanel.this)
								{
									public void customResultAvailable(String pass)
									{
										JLabel	lbpass	= new JLabel("Password");
										final JPasswordField	tfpass	= new JPasswordField(10);
										tfpass.setText(pass);
										final char	echo	= tfpass.getEchoChar();
										final JCheckBox cbshowchars	= new JCheckBox("Show characters");
										cbshowchars.addChangeListener(new ChangeListener()
										{
											public void stateChanged(ChangeEvent e)
											{
												tfpass.setEchoChar(cbshowchars.isSelected() ? 0 : echo);
											}
										});
										
										lbpass.setToolTipText("The platform password");
										tfpass.setToolTipText("The platform password");
										cbshowchars.setToolTipText("Show / hide password characters in gui");
										
										// The local password settings.
										JPanel	ppass	= new JPanel(new GridBagLayout());
										GridBagConstraints	gbc	= new GridBagConstraints();
										gbc.insets	= new Insets(0, 3, 0, 3);
										gbc.weightx	= 0;
										gbc.weighty	= 0;
										gbc.anchor	= GridBagConstraints.WEST;
										gbc.fill	= GridBagConstraints.NONE;
										gbc.gridy	= 0;
										gbc.gridwidth	= 1;
										ppass.add(lbpass, gbc);
										gbc.weightx	= 1;
										ppass.add(tfpass, gbc);
										gbc.gridy++;
										gbc.gridwidth	= GridBagConstraints.REMAINDER;
										ppass.add(cbshowchars, gbc);
										int	ok	= JOptionPane.showConfirmDialog(SGUI.getWindowParent(ComponentTreePanel.this),
											ppass, "Please enter password.", JOptionPane.OK_CANCEL_OPTION,
											JOptionPane.PLAIN_MESSAGE, icons.getIcon("overlay_lock"));
										if(ok==JOptionPane.OK_OPTION)
										{
											sec.setPlatformPassword(cid, new String(tfpass.getPassword()))
												.addResultListener(new SwingDefaultResultListener<Void>()
											{
												public void	customResultAvailable(Void result)
												{
													pctn.refresh(false);
												}
											});
										}
									}
								});
							}
							public void customExceptionOccurred(Exception e)
							{
								JOptionPane.showMessageDialog(SGUI.getWindowParent(ComponentTreePanel.this), "No security service installed.");
							}
						});
					}
				}
			}
		};
		actions.put(setpasswd.getValue(Action.NAME), setpasswd);
		
		
//		final Action showview = new AbstractAction(SHOWVIEW_ACTION, icons.getIcon("show_details"))
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				TreePath path = tree.getSelectionPath();
//				if(path!=null)
//				{
//					final ISwingTreeNode node = (ISwingTreeNode)path.getLastPathComponent();
//					
//					if(ComponentViewerPlugin.)
//					
//					if(node instanceof ProvidedServiceInfoNode)
//					{
////						Object obj = ((ProvidedServiceInfoNode)node).getService();
//						IServiceIdentifier sid = ((ProvidedServiceInfoNode)node).getServiceIdentifier();
//						IFuture<Object> fut = SServiceProvider.getService(access.getServiceProvider(), sid);
//						fut.addResultListener(new SwingDefaultResultListener<Object>()
//						{
//							public void customResultAvailable(Object obj)
//							{
//								JPanel panel = new ObjectInspectorPanel(obj);
//								showProperties(panel);
//							}
//						});
//					}
//					else if(node instanceof IActiveComponentTreeNode)
//					{
//						//IComponentDescription desc = ((IActiveComponentTreeNode)node).getDescription();
//						IComponentIdentifier cid = ((IActiveComponentTreeNode)node).getDescription().getName();
//						cms.getExternalAccess(cid).addResultListener(new SwingDefaultResultListener((Component)null)
//						{
//							public void customResultAvailable(Object result)
//							{
//								IExternalAccess	ea	= (IExternalAccess)result;
//								JPanel panel = new ObjectInspectorPanel(ea);
//								showProperties(panel);
//							}
//						});
//					}
//				}
//			}
//		};
//		actions.put(showobject.getValue(Action.NAME), showobject);
		
		// Default overlays and popups.
		model.addNodeHandler(new ISwingNodeHandler()
		{
			@Override
			public byte[] getOverlay(ITreeNode node)
			{
				return null;
			}

			public Icon getSwingOverlay(ISwingTreeNode node)
			{
				return null;
			}

			public Action[] getPopupActions(ISwingTreeNode[] nodes)
			{
				List ret = new ArrayList();
				Icon	base	= ((ISwingTreeNode) nodes[0]).getSwingIcon();
				
				if(nodes.length==1)
				{
					if(nodes[0].hasProperties())
					{
						Action pshowprops = new AbstractAction((String)showprops.getValue(Action.NAME),
							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_showprops")}) : (Icon)showprops.getValue(Action.SMALL_ICON))
						{
							public void actionPerformed(ActionEvent e)
							{
								showprops.actionPerformed(e);
							}
						};
						ret.add(pshowprops);
					}
					
					if(nodes[0] instanceof ProvidedServiceInfoNode || nodes[0] instanceof IActiveComponentTreeNode)
					{
						Action pshowobject = new AbstractAction((String)showobject.getValue(Action.NAME),
							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_showobject")}) : (Icon)showprops.getValue(Action.SMALL_ICON))
						{
							public void actionPerformed(ActionEvent e)
							{
								showobject.actionPerformed(e);
							}
						};
						ret.add(pshowobject);
					}
					
//					if(nodes[0] instanceof ProvidedServiceInfoNode && !Proxy.isProxyClass(((ProvidedServiceNode)nodes[0]).getService().getClass()))
//					{
//						Action premoveservice = new AbstractAction((String)removeservice.getValue(Action.NAME),
//							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_kill")}) : (Icon)showprops.getValue(Action.SMALL_ICON))
//						{
//							public void actionPerformed(ActionEvent e)
//							{
//								removeservice.actionPerformed(e);
//							}
//						};
//						ret.add(premoveservice);
//					}
					
					if(nodes[0] instanceof ProxyComponentTreeNode)
					{
						Action psetpasswd = new AbstractAction((String)setpasswd.getValue(Action.NAME), base)
						{
							public void actionPerformed(ActionEvent e)
							{
								setpasswd.actionPerformed(e);
							}
						};
						ret.add(psetpasswd);
					}
				}
				
				Action	prefresh	= new AbstractAction((String)refresh.getValue(Action.NAME),
					base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_refresh")}) : (Icon)refresh.getValue(Action.SMALL_ICON))
				{
					public void actionPerformed(ActionEvent e)
					{
						refresh.actionPerformed(e);
					}
				};
				ret.add(prefresh);
				Action	prefreshtree	= new AbstractAction((String)refreshtree.getValue(Action.NAME),
					base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_refreshtree")}) : (Icon)refreshtree.getValue(Action.SMALL_ICON))
				{
					public void actionPerformed(ActionEvent e)
					{
						refreshtree.actionPerformed(e);
					}
				};
				ret.add(prefreshtree);
			
				return (Action[])ret.toArray(new Action[0]);
			}

			public Action getDefaultAction(final ISwingTreeNode node)
			{
				Action	ret	= null;
				if(node.hasProperties())
				{
					ret	= showprops;
				}
				return ret;
			}
		});
		
		model.addNodeHandler(new ISwingNodeHandler()
		{
			@Override
			public byte[] getOverlay(ITreeNode node)
			{
				return null;
			}

			public Icon getSwingOverlay(ISwingTreeNode node)
			{
				Icon	ret	= null;
				
				IComponentDescription	desc = null;
				if(node instanceof IActiveComponentTreeNode)
				{
					desc = ((IActiveComponentTreeNode)node).getDescription();
				
//					if(IComponentDescription.PROCESSINGSTATE_READY.equals(desc.getProcessingState()))
//					{
//						ret = icons.getIcon("overlay_ready");
//					}
//					else if(IComponentDescription.PROCESSINGSTATE_RUNNING.equals(desc.getProcessingState()))
//					{
//						ret = icons.getIcon("overlay_running");
//					}
//					else if(IComponentDescription.PROCESSINGSTATE_IDLE.equals(desc.getProcessingState()))
//					{
//						// -> susp
//					}		
					if(IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
					{
						ret = icons.getIcon("component_suspended");
					}
				}
				return ret;
			}
			
			public Action[] getPopupActions(final ISwingTreeNode[] nodes)
			{
				List ret = new ArrayList();
				
				boolean	allcomp	= true;
				for(int i=0; allcomp && i<nodes.length; i++)
				{
					allcomp	= nodes[i] instanceof IActiveComponentTreeNode;
				}
				boolean	allproxy = true;
				for(int i=0; allproxy && i<nodes.length; i++)
				{
					allproxy = nodes[i] instanceof ProxyComponentTreeNode && ((ProxyComponentTreeNode)nodes[i]).isConnected();
				}
				
				if(allcomp)
				{
					boolean	allsusp	= true;
					for(int i=0; allsusp && i<nodes.length; i++)
					{
						allsusp	= IComponentDescription.STATE_SUSPENDED.equals(((IActiveComponentTreeNode)nodes[i]).getDescription().getState());
					}
					boolean	allact	= true;
					for(int i=0; allact && i<nodes.length; i++)
					{
						allact	= IComponentDescription.STATE_ACTIVE.equals(((IActiveComponentTreeNode)nodes[i]).getDescription().getState());
					}
					
					// Todo: Large icons for popup actions?
					Icon	base	= (nodes[0]).getSwingIcon();
					Action	pkill	= new AbstractAction((String)kill.getValue(Action.NAME),
						base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_kill")}) : (Icon)kill.getValue(Action.SMALL_ICON))
					{
						public void actionPerformed(ActionEvent e)
						{
							kill.actionPerformed(e);
						}
					};
					ret.add(pkill);
					
					if(allproxy)
					{
						Action	prkill	= new AbstractAction((String)proxykill.getValue(Action.NAME),
							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_kill")}) : (Icon)proxykill.getValue(Action.SMALL_ICON))
						{
							public void actionPerformed(ActionEvent e)
							{
								proxykill.actionPerformed(e);
							}
						};
						ret.add(prkill);
					}
					
					if(allact)
					{
						Action	psuspend	= new AbstractAction((String)suspend.getValue(Action.NAME),
							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_suspend")}) : (Icon)suspend.getValue(Action.SMALL_ICON))
						{
							public void actionPerformed(ActionEvent e)
							{
								suspend.actionPerformed(e);
							}
						};
						ret.add(psuspend);
//						ret	= new Action[]{pkill, psuspend};
					}
					else if(allsusp)
					{
						Action	presume	= new AbstractAction((String)resume.getValue(Action.NAME),
							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_resume")}) : (Icon)resume.getValue(Action.SMALL_ICON))
						{
							public void actionPerformed(ActionEvent e)
							{
								resume.actionPerformed(e);
							}
						};
						Action	pstep	= new AbstractAction((String)step.getValue(Action.NAME),
							base!=null ? new CombiIcon(new Icon[]{base, icons.getIcon("overlay_step")}) : (Icon)step.getValue(Action.SMALL_ICON))
						{
							public void actionPerformed(ActionEvent e)
							{
								step.actionPerformed(e);
							}
						};
						ret.add(presume);
						ret.add(pstep);
//						ret	= new Action[]{pkill, presume, pstep};
					}
//					else
//					{
//						ret	= new Action[]{pkill};								
//					}
				}
				
				return (Action[])ret.toArray(new Action[ret.size()]);
			}
			
			public Action getDefaultAction(ISwingTreeNode node)
			{
				return null;
			}
		});

		SServiceProvider.getService(access.getServiceProvider(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new DefaultResultListener<IComponentManagementService>()
		{
			public void resultAvailable(IComponentManagementService result)
			{
				cms	= result;
				
				// Hack!!! How to find root node?
				cms.getComponentDescriptions().addResultListener(new SwingDefaultResultListener()
				{
					public void customResultAvailable(Object result)
					{
						IComponentDescription[]	descriptions	= (IComponentDescription[])result;
						if(descriptions.length!=0)
						{
							IComponentDescription	root	= null;
							for(int i=0; root==null && i<descriptions.length; i++)
							{
								if(descriptions[i].getName().getParent()==null)
								{
									root	= descriptions[i];
								}
							}
							if(root==null)
								throw new RuntimeException("No root node found: "+SUtil.arrayToString(descriptions));
							model.setRoot(new ComponentTreeNode(null, model, tree, root, cms, cic, access));
							// Expand root node.
							TreeExpansionHandler	teh	= new TreeExpansionHandler(tree);
							teh.treeExpanded(new TreeExpansionEvent(tree, new TreePath(model.getRoot())));
						}
					}
				});
			}
		});
		
		// Remove selection in tree, when user clicks in background.
		tree.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(tree.getPathForLocation(e.getX(), e.getY())==null)
				{
					tree.clearSelection();
				}
			}
		});
	}
	
	//-------- methods --------
	
	/**
	 *  Get the action.
	 *  @param name The action name.
	 *  @return The action.
	 */
	public Action getAction(String name)
	{
		return (Action)actions.get(name);
	}
	
	/**
	 *  Add a node handler.
	 */
	public void	addNodeHandler(ISwingNodeHandler handler)
	{
		model.addNodeHandler(handler);
	}

	/**
	 *  Get the tree model.
	 */
	public AsyncSwingTreeModel	getModel()
	{
		return model;
	}
	
	/**
	 *  Get the tree.
	 */
	public JTree	getTree()
	{
		return tree;
	}
	
	/**
	 *  Get the access.
	 *  @return the access.
	 */
	public IExternalAccess getExternalAccess()
	{
		return access;
	}

	/**
	 *  Dispose the tree.
	 *  Should be called to remove listeners etc.
	 */
	public void	dispose()
	{
		getModel().dispose();
	}
	
	/**
	 *  Set the title and contents of the properties panel.
	 */
	public void	showProperties(JComponent content)
	{
		proppanel.setViewportView(content);
		proppanel.repaint();

		// Code to simulate a one touch expandable click,
	 	// see BasicSplitPaneDivider.OneTouchActionHandler)
		
		Insets  insets = getInsets();
		int lastloc = getLastDividerLocation();
	    int currentloc = getUI().getDividerLocation(this);
		int newloc = currentloc;
		BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();

		boolean	adjust	= false;
		if(getOrientation()==VERTICAL_SPLIT)
		{
			if(currentloc >= (getHeight() - insets.bottom - divider.getHeight())) 
			{
				adjust	= true;
				int maxloc = getMaximumDividerLocation();
				newloc = lastloc>=0 && lastloc<maxloc? lastloc: maxloc*1/2;
	        }			
		}
		else
		{
			if(currentloc >= (getWidth() - insets.right - divider.getWidth())) 
			{
				adjust	= true;
				int maxloc = getMaximumDividerLocation();
				newloc = lastloc>=0 && lastloc<maxloc? lastloc: maxloc*1/2;
	        }			
		}

		if(adjust && currentloc!=newloc) 
		{
			setDividerLocation(newloc);
			setLastDividerLocation(currentloc);
		}
	}
	
	//--------------------------------------
	
	/**
	 * 
	 */
	public static IFuture<JComponent> createView(final IControlCenter jcc, TreePath path, Map<Object, Boolean> viewables)
	{
		final Future<JComponent> ret = new Future<JComponent>();
		final Object tmp = path.getLastPathComponent();
		
		if(isNodeViewable2((ISwingTreeNode)path.getLastPathComponent(), viewables, jcc))
		{
			if(tmp instanceof ProvidedServiceInfoNode)
			{
				final ProvidedServiceInfoNode node = (ProvidedServiceInfoNode)tmp;
	//					final IService service = node.getService();
				
				SServiceProvider.getService(jcc.getJCCAccess().getServiceProvider(), node.getServiceIdentifier())
					.addResultListener(new ExceptionDelegationResultListener<Object, JComponent>(ret)
				{
					public void customResultAvailable(Object result)
					{
						final IService service = (IService)result;
	
						AbstractJCCPlugin.getClassLoader(((IActiveComponentTreeNode)node.getParent().getParent()).getComponentIdentifier(), jcc)
							.addResultListener(new ExceptionDelegationResultListener<ClassLoader, JComponent>(ret)
						{
							public void customResultAvailable(ClassLoader cl)
							{
								final Object clid = service.getPropertyMap()!=null? service.getPropertyMap().get(IAbstractViewerPanel.PROPERTY_VIEWERCLASS) : null;
								final Class clazz = clid instanceof Class? (Class)clid: clid instanceof String? SReflect.classForName0((String)clid, cl): null;
								
								if(clid!=null)
								{
									try
									{
//										storeCurrentPanelSettings();
										final IServiceViewerPanel	panel = (IServiceViewerPanel)clazz.newInstance();
										panel.init(jcc, service).addResultListener(new ExceptionDelegationResultListener<Void, JComponent>(ret)
										{
											public void customResultAvailable(Void result)
											{
//												Properties	sub	= props!=null ? props.getSubproperty(panel.getId()) : null;
//												if(sub!=null)
//													panel.setProperties(sub);
												JComponent comp = panel.getComponent();
												
												ret.setResult(comp);
												// todo: help 
												//SHelp.setupHelp(comp, getHelpID());
//												panels.put(node.getServiceIdentifier(), panel);
//												detail.add(comp, node.getServiceIdentifier());
//												comptree.getModel().fireNodeChanged(node);
											}
										});
									}
									catch(Exception e)
									{
										e.printStackTrace();
										jcc.displayError("Error initializing service viewer panel.", "Component viewer panel class: "+clid, e);
									}
								}
							}
						});
					}
				});
			}
			else if(tmp instanceof IActiveComponentTreeNode)
			{
				final IActiveComponentTreeNode node = (IActiveComponentTreeNode)tmp;
				final IComponentIdentifier cid = node.getComponentIdentifier();
				
				SServiceProvider.getServiceUpwards(jcc.getJCCAccess().getServiceProvider(), IComponentManagementService.class)
					.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, JComponent>(ret)
				{
					public void customResultAvailable(final IComponentManagementService cms)
					{
						cms.getExternalAccess(cid).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, JComponent>(ret)
						{
							public void customResultAvailable(final IExternalAccess exta)
							{
								AbstractJCCPlugin.getClassLoader(cid, jcc)
									.addResultListener(new SwingDefaultResultListener<ClassLoader>()
								{
									public void customResultAvailable(ClassLoader cl)
									{
										Object clid = exta.getModel().getProperty(IAbstractViewerPanel.PROPERTY_VIEWERCLASS, cl);
										
										if(clid instanceof String)
										{
											clid = SReflect.classForName0((String)clid, cl);
										}
										
										try
										{
											final IComponentViewerPanel panel = (IComponentViewerPanel)((Class)clid).newInstance();
											panel.init(jcc, exta).addResultListener(new ExceptionDelegationResultListener<Void, JComponent>(ret)
											{
												public void customResultAvailable(Void result)
												{
	//													Properties	sub	= props!=null ? props.getSubproperty(panel.getId()) : null;
	//													if(sub!=null)
	//														panel.setProperties(sub);
													JComponent comp = panel.getComponent();
													ret.setResult(comp);
													// todo: help
													//SHelp.setupHelp(comp, getHelpID());
	//													panels.put(exta.getComponentIdentifier(), panel);
	//													detail.add(comp, exta.getComponentIdentifier());
	//													comptree.getModel().fireNodeChanged(node);
												}
											});
										}
										catch(Exception e)
										{
											ret.setException(e);
										}
									}
								});
							}
						});
					}
				});
			}
		}
		else
		{
			ret.setResult(null);
		}
		
		return ret;
	}
	
	/**
	 *  Test if a node is viewable.
	 *  @param node	The node.
	 *  @return True, if the node is viewable.
	 */
	public static boolean isNodeViewable2(final ISwingTreeNode node, final Map<Object, Boolean> viewables, final IControlCenter jcc)
	{
//		System.out.println("called isVis: "+node.getId());
		boolean ret = false;
		if(node instanceof ProvidedServiceInfoNode)
		{
			final IServiceIdentifier sid = ((ProvidedServiceInfoNode)node).getServiceIdentifier();
			if(sid!=null)
			{
				Boolean viewable = (Boolean)viewables.get(sid);
				if(viewable!=null)
				{
					ret = viewable.booleanValue();
//					System.out.println("isVis result: "+node.getId()+" "+ret);
				}
				else
				{
					// Unknown -> start search to find out asynchronously
					SServiceProvider.getService(jcc.getJCCAccess().getServiceProvider(), sid)
						.addResultListener(new SwingDefaultResultListener<Object>()
					{
						public void customResultAvailable(Object result)
						{
							IService service = (IService)result;
							Map	props = service.getPropertyMap();
							boolean vis = props!=null && props.get(IAbstractViewerPanel.PROPERTY_VIEWERCLASS)!=null;
							viewables.put(sid, vis? Boolean.TRUE: Boolean.FALSE);
//							System.out.println("isVis first res: "+viewables.get(sid));
							node.refresh(false);
						}
					});
				}
			}
//			Map	props	= ((ProvidedServiceInfoNode)node).getServiceIdentifier().get.getPropertyMap();
//			ret = props!=null && props.get(IAbstractViewerPanel.PROPERTY_VIEWERCLASS)!=null;
		}
		else if(node instanceof IActiveComponentTreeNode)
		{
			final IComponentIdentifier cid = ((IActiveComponentTreeNode)node).getComponentIdentifier();
			
			// For proxy components the cid could be null if the remote cid has not yet been retrieved
			// Using a IFuture as return value in not very helpful because this method can't directly
			// return a result, even if known.
			// todo: how to initiate a repaint in case the the cid is null
			if(cid!=null)
			{
				Boolean viewable = (Boolean)viewables.get(cid);
				if(viewable!=null)
				{
					ret = viewable.booleanValue();
//					System.out.println("isVis result: "+node.getId()+" "+ret);
				}
				else
				{
					// Unknown -> start search to find out asynchronously
					IFuture<IComponentManagementService> fut = SServiceProvider.getServiceUpwards(jcc.getJCCAccess().getServiceProvider(), IComponentManagementService.class);
					fut.addResultListener(new DefaultResultListener<IComponentManagementService>()
					{
						public void resultAvailable(IComponentManagementService result)
						{
							final IComponentManagementService cms = (IComponentManagementService)result;
							
							cms.getExternalAccess(cid).addResultListener(new DefaultResultListener<IExternalAccess>()
							{
								public void resultAvailable(final IExternalAccess exta)
								{
									jcc.getClassLoader(exta.getModel().getResourceIdentifier())
										.addResultListener(new SwingDefaultResultListener<ClassLoader>()
									{
										public void customResultAvailable(ClassLoader cl)
										{
											final Object clid = exta.getModel().getProperty(IAbstractViewerPanel.PROPERTY_VIEWERCLASS, cl);
											viewables.put(cid, clid==null? Boolean.FALSE: Boolean.TRUE);
//											System.out.println("isVis first res: "+viewables.get(cid));
											node.refresh(false);
										}										
									});
								}
								
								public void exceptionOccurred(Exception exception)
								{
									// Happens e.g. when remote classes not locally available.
//									exception.printStackTrace();
								}
							});
						}
					});
				}
			}
		}
		return ret;
	}

	
	/**
	 *  Kill a proxy
	 */
	protected static IFuture<Void>	killComponent(IExternalAccess access, final IComponentIdentifier cid)
	{
		final Future<Void>	ret	= new Future<Void>();
		SServiceProvider.getService(access.getServiceProvider(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
		{
			public void customResultAvailable(IComponentManagementService cms)
			{
				cms.destroyComponent(cid)
					.addResultListener(new ExceptionDelegationResultListener<Map<String,Object>, Void>(ret)
				{
					public void customResultAvailable(Map<String, Object> result)
					{
						ret.setResult(null);
					}
				});
			}
		});
		return ret;
	}
}
