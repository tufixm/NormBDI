package jadex.micro;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.LocalResourceIdentifier;
import jadex.bridge.ResourceIdentifier;
import jadex.bridge.modelinfo.ComponentInstanceInfo;
import jadex.bridge.modelinfo.ConfigurationInfo;
import jadex.bridge.modelinfo.IArgument;
import jadex.bridge.modelinfo.ModelInfo;
import jadex.bridge.modelinfo.SubcomponentTypeInfo;
import jadex.bridge.modelinfo.UnparsedExpression;
import jadex.bridge.service.ProvidedServiceImplementation;
import jadex.bridge.service.ProvidedServiceInfo;
import jadex.bridge.service.PublishInfo;
import jadex.bridge.service.RequiredServiceBinding;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.GuiClass;
import jadex.bridge.service.annotation.GuiClassName;
import jadex.bridge.service.annotation.Value;
import jadex.commons.FieldInfo;
import jadex.commons.IValueFetcher;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.javaparser.SJavaParser;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBreakpoint;
import jadex.micro.annotation.AgentResult;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Breakpoints;
import jadex.micro.annotation.Component;
import jadex.micro.annotation.ComponentType;
import jadex.micro.annotation.ComponentTypes;
import jadex.micro.annotation.Configuration;
import jadex.micro.annotation.Configurations;
import jadex.micro.annotation.CreationInfo;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.Imports;
import jadex.micro.annotation.NameValue;
import jadex.micro.annotation.Properties;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.Publish;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Reads micro agent classes and generates a model from metainfo and annotations.
 */
public class MicroClassReader
{
	/**
	 *  Load a  model.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return The loaded model.
	 */
	public MicroModel read(String model, String[] imports, ClassLoader classloader, IResourceIdentifier rid, IComponentIdentifier root)
	{
//		System.out.println("loading micro: "+model);
		String clname = model;
		
		// Note: it is ok if it is an absolute path with dots even it looks strange.
		// getMicroAgentClass will strip away parts until the model name is clear. 
		
		// Hack! for extracting clear classname
		if(clname.endsWith(".class"))
			clname = model.substring(0, model.indexOf(".class"));
		clname = clname.replace('\\', '.');
		clname = clname.replace('/', '.');
		
		Class cma = getMicroAgentClass(clname, imports, classloader);
		
		return read(model, cma, classloader, rid, root);
	}
	
	/**
	 *  Load the model.
	 */
	protected MicroModel read(String model, Class<?> cma, ClassLoader classloader, IResourceIdentifier rid, IComponentIdentifier root)
	{		ModelInfo modelinfo = new ModelInfo();
		MicroModel ret = new MicroModel(modelinfo);
		
//		System.out.println("read micro: "+cma);
		
		String name = SReflect.getUnqualifiedClassName(cma);
		if(name.endsWith("Agent"))
			name = name.substring(0, name.lastIndexOf("Agent"));
		String packagename = cma.getPackage()!=null? cma.getPackage().getName(): null;
		modelinfo.setName(name);
		modelinfo.setPackage(packagename);
		
		String src = cma.getProtectionDomain()!=null
			? SUtil.convertURLToString(cma.getProtectionDomain().getCodeSource().getLocation()) + File.separator: "/";
//			: ('/' + cma.getPackage().getName().replace('.', '/') + '/');
//		modelinfo.setFilename(src+File.separatorChar+model);
		modelinfo.setFilename(src+SReflect.getClassName(cma).replace('.', cma.getProtectionDomain()!=null? File.separatorChar: '/')+".class");
//		System.out.println("mircor: "+src+File.separatorChar+model);
		modelinfo.setType(MicroAgentFactory.FILETYPE_MICROAGENT);
		modelinfo.setStartable(true);
		if(rid==null)
		{
			URL url	= null;
			try
			{
				url	= cma.getProtectionDomain()!=null
					? cma.getProtectionDomain().getCodeSource().getLocation()
					: new URL("file://" + cma.getPackage().getName().replace('.', '/') + '/');
			}
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
			rid = new ResourceIdentifier(new LocalResourceIdentifier(root, url), null);
		}
		modelinfo.setResourceIdentifier(rid);
		ret.setClassloader(classloader);
		
		// not supported any longer
//		try
//		{
//			Method m = cma.getMethod("getMetaInfo", new Class[0]);
//			if(m!=null)
//			{
//				MicroAgentMetaInfo metainfo = (MicroAgentMetaInfo)m.invoke(null, new Object[0]);
//				fillMicroModelFromMetaInfo(ret, model, cma, classloader, metainfo);
//			}
//		}
//		catch(Exception e)
//		{
//		}
		
		fillMicroModelFromAnnotations(ret, model, cma, classloader);
		
		return ret;
	}
	
//	/**
//	 *  Fill the model details using meta info.
//	 */
//	protected void fillMicroModelFromMetaInfo(CacheableKernelModel micromodel, String model, Class cma, ClassLoader classloader, MicroAgentMetaInfo metainfo)
//	{
//		try
//		{
//			ModelInfo modelinfo = (ModelInfo)micromodel.getModelInfo();
////			Method m = cma.getMethod("getMetaInfo", new Class[0]);
////			if(m!=null)
////				metainfo = (MicroAgentMetaInfo)m.invoke(null, new Object[0]);
//			
//			String description = metainfo!=null && metainfo.getDescription()!=null? metainfo.getDescription(): null;
//			String[] configurations = metainfo!=null? metainfo.getConfigurations(): null;
//			IArgument[] arguments = metainfo!=null? metainfo.getArguments(): null;
//			IArgument[] results = metainfo!=null? metainfo.getResults(): null;
//			Map properties = metainfo!=null && metainfo.getProperties()!=null? new HashMap(metainfo.getProperties()): new HashMap();
//			RequiredServiceInfo[] required = metainfo!=null? metainfo.getRequiredServices(): null;
//			ProvidedServiceInfo[] provided = metainfo!=null? metainfo.getProvidedServices(): null;
//			IModelValueProvider master = metainfo!=null? metainfo.getMaster(): null;
//			IModelValueProvider daemon= metainfo!=null? metainfo.getDaemon(): null;
//			IModelValueProvider autosd = metainfo!=null? metainfo.getAutoShutdown(): null;
//			
//			// Add debugger breakpoints
//			List names = new ArrayList();
//			for(int i=0; metainfo!=null && i<metainfo.getBreakpoints().length; i++)
//				names.add(metainfo.getBreakpoints()[i]);
//			properties.put("debugger.breakpoints", names);
//			
//			ConfigurationInfo[] cinfo = null;
//			if(configurations!=null)
//			{
//				cinfo = new ConfigurationInfo[configurations.length];
//				for(int i=0; i<configurations.length; i++)
//				{
//					cinfo[i] = new ConfigurationInfo(configurations[i]);
//					cinfo[i].setMaster((Boolean)master.getValue(configurations[i]));
//					cinfo[i].setDaemon((Boolean)daemon.getValue(configurations[i]));
//					cinfo[i].setAutoShutdown((Boolean)autosd.getValue(configurations[i]));
//					// suspend?
//					// todo
////					cinfo[i].addArgument(argument)
//				}
//			}
//			modelinfo.setDescription(description);
//			modelinfo.setArguments(arguments);
//			modelinfo.setResults(results);
//			modelinfo.setProperties(properties);
//			modelinfo.setRequiredServices(required);
//			modelinfo.setProvidedServices(provided);
//			modelinfo.setConfigurations(cinfo);
//		}
//		catch(Exception e)
//		{
////			e.printStackTrace();
//		}
//	}
	
	/**
	 *  Fill the model details using annotation.
	 */
	protected void fillMicroModelFromAnnotations(MicroModel micromodel, String model, Class clazz, ClassLoader cl)
	{
		ModelInfo modelinfo = (ModelInfo)micromodel.getModelInfo();
		Class cma = clazz;
		
		int cnt = 0;
		Map toset = new HashMap();
		boolean propdone = false;
		boolean reqsdone = false;
		boolean prosdone = false;
		boolean argsdone = false;
		boolean resudone = false;
		boolean confdone = false;
		boolean compdone = false;
		boolean breaksdone = false;
		
		while(cma!=null && !cma.equals(Object.class) && !cma.equals(getClass(MicroAgent.class, cl)))
		{
			// Description is set only once from upper most element.
			if(isAnnotationPresent(cma, Description.class, cl) && modelinfo.getDescription()==null)
			{
				Description val = (Description)getAnnotation(cma, Description.class, cl);
//				Description val = (Description)cma.getAnnotation(Description.class);
				modelinfo.setDescription(val.value());
			}
			
			// Take all, duplicates are eleminated
			if(isAnnotationPresent(cma, Imports.class, cl))
			{
				String[] tmp = ((Imports)getAnnotation(cma, Imports.class, cl)).value();
//				String[] tmp = ((Imports)cma.getAnnotation(Imports.class)).value();
				Set<String> imports = (Set<String>)toset.get("imports");
				if(imports==null)
				{
					imports = new LinkedHashSet<String>();
					toset.put("imports", imports);
				}
				for(int i=0; i<tmp.length; i++)
				{
					imports.add(tmp[i]);
				}
			}
			
			// Add package of current class to imports.
			// Is a little hack because getAllImports() of ModelInfo add package again.
			Set<String> imports = (Set<String>)toset.get("imports");
			if(imports==null)
			{
				imports = new LinkedHashSet<String>();
				toset.put("imports", imports);
			}
			
			// Take all, upper replace lower
			if(!propdone && isAnnotationPresent(cma, Properties.class, cl))
			{
				Properties val = (Properties)getAnnotation(cma, Properties.class, cl);
				NameValue[] vals = val.value();
				propdone = val.replace();
				
				Map props = (Map)toset.get("properties");
				if(props==null)
				{
					props = new LinkedHashMap();
					toset.put("properties", props);
				}
				for(int i=0; i<vals.length; i++)
				{
					// Todo: clazz, language
					if(!props.containsKey(vals[i].name()))
					{
						props.put(vals[i].name(), new UnparsedExpression(vals[i].name(), vals[i].clazz().getName(), vals[i].value(), null) );
					}
				}
			}
			
			// Take newest version
			// todo: move to be able to use the constant
			// jadex.base.gui.componentviewer.IAbstractViewerPanel.PROPERTY_VIEWERCLASS
			if(isAnnotationPresent(cma, GuiClass.class, cl))
			{
				GuiClass gui = (GuiClass)getAnnotation(cma, GuiClass.class, cl);
				Class gclazz = gui.value();
				
				Map props = (Map)toset.get("properties");
				if(props==null)
				{
					props = new LinkedHashMap();
					toset.put("properties", props);
				}
				
				if(!props.containsKey("componentviewer.viewerclass"))
				{
					props.put("componentviewer.viewerclass", gclazz);
				}
			}
			else if(isAnnotationPresent(cma, GuiClassName.class, cl))
			{
				GuiClassName gui = (GuiClassName)getAnnotation(cma, GuiClassName.class, cl);
				String clazzname = gui.value();
				
				Map props = (Map)toset.get("properties");
				if(props==null)
				{
					props = new LinkedHashMap();
					toset.put("properties", props);
				}
				
				if(!props.containsKey("componentviewer.viewerclass"))
				{
					props.put("componentviewer.viewerclass", clazzname);
				}
			}
			
			// Take all (if not replace)
			if(!breaksdone && isAnnotationPresent(cma, Breakpoints.class, cl))
			{
				Breakpoints val = (Breakpoints)getAnnotation(cma, Breakpoints.class, cl);
				breaksdone = val.replace();
				String[] vals = val.value();
				
				List bps = (List)toset.get("breakpoints");
				if(bps==null)
				{
					bps = new ArrayList();
					toset.put("breakpoints", bps);
				}
				
				for(int i=0; i<vals.length; i++)
				{
					if(!bps.contains(vals[i]))
						bps.add(vals[i]);
				}
			}
			
			// Take all but new overrides old
			if(!reqsdone && isAnnotationPresent(cma, RequiredServices.class, cl))
			{
				RequiredServices val = (RequiredServices)getAnnotation(cma, RequiredServices.class, cl);
				RequiredService[] vals = val.value();
				reqsdone = val.replace();
				
				Map rsers = (Map)toset.get("reqservices");
				if(rsers==null)
				{
					rsers = new LinkedHashMap();
					toset.put("reqservices", rsers);
				}
				
				for(int i=0; i<vals.length; i++)
				{
					RequiredServiceBinding binding = createBinding(vals[i].binding());
					RequiredServiceInfo rsis = new RequiredServiceInfo(vals[i].name(), vals[i].type(), 
						vals[i].multiple(), Object.class.equals(vals[i].multiplextype())? null: vals[i].multiplextype(), binding);
					if(rsers.containsKey(vals[i].name()))
					{
						RequiredServiceInfo old = (RequiredServiceInfo)rsers.get(vals[i].name());
						if(old.isMultiple()!=rsis.isMultiple() || !old.getType().getType(cl).equals(rsis.getType().getType(cl)))
							throw new RuntimeException("Extension hierarchy contains incompatible required service more than once: "+vals[i].name());
					}
					else
					{
						rsers.put(vals[i].name(), rsis);
					}
				}
			}
			
			// Take all but new overrides old
			if(!prosdone && isAnnotationPresent(cma, ProvidedServices.class, cl))
			{
				ProvidedServices val = (ProvidedServices)getAnnotation(cma, ProvidedServices.class, cl);
				ProvidedService[] vals = val.value();
				prosdone = val.replace();
				
				Map psers = (Map)toset.get("proservices");
				if(psers==null)
				{
					psers = new LinkedHashMap();
					toset.put("proservices", psers);
				}
				
				for(int i=0; i<vals.length; i++)
				{
					Implementation im = vals[i].implementation();
					Value[] inters = im.interceptors();
					UnparsedExpression[] interceptors = null;
					if(inters.length>0)
					{
						interceptors = new UnparsedExpression[inters.length];
						for(int j=0; j<inters.length; j++)
						{
							interceptors[j] = new UnparsedExpression(null, inters[j].clazz(), inters[j].value(), null);
						}
					}
					ProvidedServiceImplementation impl = createImplementation(im);
					Publish p = vals[i].publish();
					NameValue[] props = p.properties();
					UnparsedExpression[] exps = createUnparsedExpressions(props);
					
					PublishInfo pi = p.publishid().length()==0? null: new PublishInfo(p.publishid(), p.publishtype(), Object.class.equals(p.mapping())? null: p.mapping(), exps);
					ProvidedServiceInfo psis = new ProvidedServiceInfo(vals[i].name().length()>0? 
						vals[i].name(): null, vals[i].type(), impl, pi);
				
					if(vals[i].name().length()==0 || !psers.containsKey(vals[i].name()))
					{
						psers.put(vals[i].name().length()==0? ("#"+cnt++): vals[i].name(), psis);
					}
				}
			}
			
			// Take all but new overrides old
			if(!argsdone && isAnnotationPresent(cma, Arguments.class, cl))
			{
				Arguments val = (Arguments)getAnnotation(cma, Arguments.class, cl);
				Argument[] vals = val.value();
				argsdone = val.replace();
				
				Map args = (Map)toset.get("arguments");
				if(args==null)
				{
					args = new LinkedHashMap();
					toset.put("arguments", args);
				}
				
				for(int i=0; i<vals.length; i++)
				{
//					try
//					{
	//				Object arg = SJavaParser.evaluateExpression(vals[i].defaultvalue(), imports, null, classloader);
					IArgument tmparg = new jadex.bridge.modelinfo.Argument(vals[i].name(), 
						vals[i].description(), SReflect.getClassName(vals[i].clazz()),
						"".equals(vals[i].defaultvalue()) ? null : vals[i].defaultvalue());
					
					if(!args.containsKey(vals[i].name()))
					{
						args.put(vals[i].name(), tmparg);
					}
//					}
//					catch(Exception e)
//					{
						// Currently a type not present exception can occur with the applications.mixed.ShopAgent
//						e.printStackTrace();
//					}
				}
			}
			
			// Take all but new overrides old
			if(!resudone && isAnnotationPresent(cma, Results.class, cl))
			{
				Results val = (Results)getAnnotation(cma, Results.class, cl);
				Result[] vals = val.value();
				resudone = val.replace();
				
				Map res = (Map)toset.get("results");
				if(res==null)
				{
					res = new LinkedHashMap();
					toset.put("results", res);
				}
				
				IArgument[] tmpresults = new IArgument[vals.length];
				for(int i=0; i<vals.length; i++)
				{
	//				Object res = evaluateExpression(vals[i].defaultvalue(), imports, null, classloader);
					IArgument tmpresult = new jadex.bridge.modelinfo.Argument(vals[i].name(), 
						vals[i].description(), SReflect.getClassName(vals[i].clazz()),
						"".equals(vals[i].defaultvalue()) ? null : vals[i].defaultvalue());
					
					if(!res.containsKey(vals[i].name()))
					{
						res.put(vals[i].name(), tmpresult);
					}
				}
			}
			
			// Take all but new overrides old
			if(!compdone && isAnnotationPresent(cma, ComponentTypes.class, cl))
			{
				SubcomponentTypeInfo[] subinfos = null;
				ComponentTypes tmp = (ComponentTypes)getAnnotation(cma, ComponentTypes.class, cl);
				compdone = tmp.replace();
				ComponentType[] ctypes = tmp.value();
				
				Map res = (Map)toset.get("componenttypes");
				if(res==null)
				{
					res = new LinkedHashMap();
					toset.put("componenttypes", res);
				}
				
				for(int i=0; i<ctypes.length; i++)
				{
					SubcomponentTypeInfo subinfo = new SubcomponentTypeInfo(ctypes[i].name(), ctypes[i].filename());
					if(!toset.containsKey(ctypes[i].name()))
					{
						res.put(ctypes[i].name(), subinfo);
					}
				}
			}
			
			if(!confdone && isAnnotationPresent(cma, Configurations.class, cl))
			{
				Configurations val = (Configurations)getAnnotation(cma, Configurations.class, cl);
				Configuration[] configs = val.value();
				confdone = val.replace();
				
				Map confs = (Map)toset.get("configurations");
				if(confs==null)
				{
					confs = new LinkedHashMap();
					toset.put("configurations", confs);
				}
				
				for(int i=0; i<configs.length; i++)
				{
					if(!confs.containsKey(configs[i].name()))
					{
						ConfigurationInfo configinfo = new ConfigurationInfo(configs[i].name());
						confs.put(configs[i].name(), configinfo);
						
						configinfo.setMaster(configs[i].master());
						configinfo.setDaemon(configs[i].daemon());
						configinfo.setAutoShutdown(configs[i].autoshutdown());
						configinfo.setSuspend(configs[i].suspend());
						
						NameValue[] argvals = configs[i].arguments();
						for(int j=0; j<argvals.length; j++)
						{
							configinfo.addArgument(new UnparsedExpression(argvals[j].name(), argvals[j].clazz(), argvals[j].value(), null));
						}
						NameValue[] resvals = configs[i].results();
						for(int j=0; j<resvals.length; j++)
						{
							configinfo.addResult(new UnparsedExpression(resvals[j].name(), resvals[j].clazz(), resvals[j].value(), null));
						}
						
						ProvidedService[] provs = configs[i].providedservices();
						ProvidedServiceInfo[] psis = new ProvidedServiceInfo[provs.length];
						for(int j=0; j<provs.length; j++)
						{
							Implementation im = provs[j].implementation();
							Value[] inters = im.interceptors();
							UnparsedExpression[] interceptors = null;
							if(inters.length>0)
							{
								interceptors = new UnparsedExpression[inters.length];
								for(int k=0; k<inters.length; k++)
								{
									interceptors[k] = new UnparsedExpression(null, inters[k].clazz(), inters[k].value(), null);
								}
							}
							RequiredServiceBinding bind = createBinding(im.binding());
							ProvidedServiceImplementation impl = new ProvidedServiceImplementation(!im.value().equals(Object.class)? im.value(): null, 
								im.expression().length()>0? im.expression(): null, im.proxytype(), bind, interceptors);
							Publish p = provs[j].publish();
							PublishInfo pi = p.publishid().length()==0? null: new PublishInfo(p.publishid(), p.publishtype(), 
								p.mapping(), createUnparsedExpressions(p.properties()));
							psis[j] = new ProvidedServiceInfo(provs[j].name().length()>0? provs[j].name(): null, provs[j].type(), impl, pi);
							configinfo.setProvidedServices(psis);
						}
						
						RequiredService[] reqs = configs[i].requiredservices();
						RequiredServiceInfo[] rsis = new RequiredServiceInfo[reqs.length];
						for(int j=0; j<reqs.length; j++)
						{
							RequiredServiceBinding binding = createBinding(reqs[j].binding());
							rsis[j] = new RequiredServiceInfo(reqs[j].name(), reqs[j].type(), reqs[j].multiple(), 
								Object.class.equals(reqs[j].multiplextype())? null: reqs[j].multiplextype(), binding);
							configinfo.setRequiredServices(rsis);
						}
						
						Component[] comps = configs[i].components();
						for(int j=0; j<comps.length; j++)
						{
							configinfo.addComponentInstance(createComponentInstanceInfo(comps[j]));
						}
					}
				}
			}
			
			// Find injection targets by reflection (agent, arguments, services)
			Field[] fields = cma.getDeclaredFields();
			for(int i=0; i<fields.length; i++)
			{
				if(isAnnotationPresent(fields[i], Agent.class, cl))
				{
					micromodel.addAgentInjection(new FieldInfo(fields[i]));
				}
				else if(isAnnotationPresent(fields[i], AgentService.class, cl))
				{
					AgentService ser = getAnnotation(fields[i], AgentService.class, cl);
					String name = ser.name().length()>0? ser.name(): fields[i].getName();
					micromodel.addServiceInjection(name, new FieldInfo(fields[i]));
				}
				else
				{
					if(isAnnotationPresent(fields[i], AgentArgument.class, cl))
					{
						AgentArgument arg = getAnnotation(fields[i], AgentArgument.class, cl);
						String name = arg.value().length()>0? arg.value(): fields[i].getName();
						micromodel.addArgumentInjection(name, new FieldInfo(fields[i]), arg.convert());
					}
					if(isAnnotationPresent(fields[i], AgentResult.class, cl))
					{
						AgentResult res = getAnnotation(fields[i], AgentResult.class, cl);
						String name = res.value().length()>0? res.value(): fields[i].getName();
						if(micromodel.getResultInjection(name)==null)
						{
							micromodel.addResultInjection(name, new FieldInfo(fields[i]), res.convert(), res.convertback());
						}
					}
				}
			}
			
			if(micromodel.getBreakpointMethod()==null)
			{
				Method[] methods = cma.getDeclaredMethods();
				for(int i=0; i<methods.length; i++)
				{
					final Method method = methods[i];
					if(isAnnotationPresent(methods[i], AgentBreakpoint.class, cl))
					{
						micromodel.setBreakpointMethod(method);
					}
				}
			}
			
			cma = cma.getSuperclass();
		}
				
		Set imp = (Set)toset.get("imports");
		if(imp!=null)
			modelinfo.setImports((String[])imp.toArray(new String[imp.size()]));
		
		Map props = (Map)toset.get("properties");
		List bps = (List)toset.get("breakpoints");
		if(bps!=null)
		{
			if(props==null)
				props = new HashMap();
			props.put("debugger.breakpoints", bps);
		}
		if(props!=null)
			modelinfo.setProperties(props);
		
		Map rsers = (Map)toset.get("reqservices");
		if(rsers!=null)
			modelinfo.setRequiredServices((RequiredServiceInfo[])rsers.values().toArray(new RequiredServiceInfo[rsers.size()]));
		
		Map psers = (Map)toset.get("proservices");
		if(psers!=null)
			modelinfo.setProvidedServices((ProvidedServiceInfo[])psers.values().toArray(new ProvidedServiceInfo[psers.size()]));
//		System.out.println("provided services: "+psers);
		
		Map argus = (Map)toset.get("arguments");
		if(argus!=null)
			modelinfo.setArguments((IArgument[])argus.values().toArray(new IArgument[argus.size()]));
//		System.out.println("arguments: "+argus);
		
		Map res = (Map)toset.get("results");
		if(res!=null)
			modelinfo.setResults((IArgument[])res.values().toArray(new IArgument[res.size()]));
		
		Map cts = (Map)toset.get("componenttypes");
		if(cts!=null)
			modelinfo.setSubcomponentTypes((SubcomponentTypeInfo[])cts.values().toArray(new SubcomponentTypeInfo[cts.size()]));

		Map cfs = (Map)toset.get("configurations");
		if(cfs!=null)
			modelinfo.setConfigurations((ConfigurationInfo[])cfs.values().toArray(new ConfigurationInfo[cfs.size()]));

	}
	
//	/**
//	 *  Fill the model details using annotation.
//	 */
//	protected void fillMicroModelFromAnnotations(MicroModel micromodel, String model, Class clazz, ClassLoader classloader)
//	{
//		ModelInfo modelinfo = (ModelInfo)micromodel.getModelInfo();
//		Class cma = clazz;
//		
//		int cnt = 0;
//		Map toset = new HashMap();
//		boolean propdone = false;
//		boolean reqsdone = false;
//		boolean prosdone = false;
//		boolean argsdone = false;
//		boolean resudone = false;
//		boolean confdone = false;
//		boolean compdone = false;
//		boolean breaksdone = false;
//		
//		while(cma!=null && !cma.equals(Object.class) && !cma.equals(MicroAgent.class))
//		{
//			// Description is set only once from upper most element.
//			if(cma.isAnnotationPresent(Description.class) && modelinfo.getDescription()==null)
//			{
//				Description val = (Description)cma.getAnnotation(Description.class);
//				modelinfo.setDescription(val.value());
//			}
//			
//			// Take all, duplicates are eleminated
//			if(cma.isAnnotationPresent(Imports.class))
//			{
//				String[] tmp = ((Imports)cma.getAnnotation(Imports.class)).value();
//				Set imports = (Set)toset.get("imports");
//				if(imports==null)
//				{
//					imports = new LinkedHashSet();
//					toset.put("imports", imports);
//				}
//				for(int i=0; i<tmp.length; i++)
//				{
//					imports.add(tmp[i]);
//				}
//			}
//			
//			// Add package of current class to imports.
//			// Is a little hack because getAllImports() of ModelInfo add package again.
//			Set imports = (Set)toset.get("imports");
//			if(imports==null)
//			{
//				imports = new LinkedHashSet();
//				toset.put("imports", imports);
//			}
//			
//			// Take all, upper replace lower
//			if(!propdone && cma.isAnnotationPresent(Properties.class))
//			{
//				Properties val = (Properties)cma.getAnnotation(Properties.class);
//				NameValue[] vals = val.value();
//				propdone = val.replace();
//				
//				Map props = (Map)toset.get("properties");
//				if(props==null)
//				{
//					props = new LinkedHashMap();
//					toset.put("properties", props);
//				}
//				for(int i=0; i<vals.length; i++)
//				{
//					// Todo: clazz, language
//					if(!props.containsKey(vals[i].name()))
//					{
//						props.put(vals[i].name(), new UnparsedExpression(vals[i].name(), vals[i].clazz(), vals[i].value(), null) );
//					}
//				}
//			}
//			
//			// Take newest version
//			// todo: move to be able to use the constant
//			// jadex.base.gui.componentviewer.IAbstractViewerPanel.PROPERTY_VIEWERCLASS
//			if(cma.isAnnotationPresent(GuiClass.class))
//			{
//				GuiClass gui = (GuiClass)cma.getAnnotation(GuiClass.class);
//				Class gclazz = gui.value();
//				
//				Map props = (Map)toset.get("properties");
//				if(props==null)
//				{
//					props = new LinkedHashMap();
//					toset.put("properties", props);
//				}
//				
//				if(!props.containsKey("componentviewer.viewerclass"))
//				{
//					props.put("componentviewer.viewerclass", gclazz);
//				}
//			}
//			else if(cma.isAnnotationPresent(GuiClassName.class))
//			{
//				GuiClassName gui = (GuiClassName)cma.getAnnotation(GuiClassName.class);
//				String clazzname = gui.value();
//				
//				Map props = (Map)toset.get("properties");
//				if(props==null)
//				{
//					props = new LinkedHashMap();
//					toset.put("properties", props);
//				}
//				
//				if(!props.containsKey("componentviewer.viewerclass"))
//				{
//					props.put("componentviewer.viewerclass", clazzname);
//				}
//			}
//			
//			// Take all (if not replace)
//			if(!breaksdone && cma.isAnnotationPresent(Breakpoints.class))
//			{
//				Breakpoints val = (Breakpoints)cma.getAnnotation(Breakpoints.class);
//				breaksdone = val.replace();
//				String[] vals = val.value();
//				
//				List bps = (List)toset.get("breakpoints");
//				if(bps==null)
//				{
//					bps = new ArrayList();
//					toset.put("breakpoints", bps);
//				}
//				
//				for(int i=0; i<vals.length; i++)
//				{
//					if(!bps.contains(vals[i]))
//						bps.add(vals[i]);
//				}
//			}
//			
//			// Take all but new overrides old
//			if(!reqsdone && cma.isAnnotationPresent(RequiredServices.class))
//			{
//				RequiredServices val = (RequiredServices)cma.getAnnotation(RequiredServices.class);
//				RequiredService[] vals = val.value();
//				reqsdone = val.replace();
//				
//				Map rsers = (Map)toset.get("reqservices");
//				if(rsers==null)
//				{
//					rsers = new LinkedHashMap();
//					toset.put("reqservices", rsers);
//				}
//				
//				for(int i=0; i<vals.length; i++)
//				{
//					RequiredServiceBinding binding = createBinding(vals[i].binding());
//					RequiredServiceInfo rsis = new RequiredServiceInfo(vals[i].name(), vals[i].type(), 
//						vals[i].multiple(), Object.class.equals(vals[i].multiplextype())? null: vals[i].multiplextype(), binding);
//					if(rsers.containsKey(vals[i].name()))
//					{
//						RequiredServiceInfo old = (RequiredServiceInfo)rsers.get(vals[i].name());
//						if(old.isMultiple()!=rsis.isMultiple() || !old.getType().getType(classloader).equals(rsis.getType().getType(classloader)))
//							throw new RuntimeException("Extension hierarchy contains incompatible required service more than once: "+vals[i].name());
//					}
//					else
//					{
//						rsers.put(vals[i].name(), rsis);
//					}
//				}
//			}
//			
//			// Take all but new overrides old
//			if(!prosdone && cma.isAnnotationPresent(ProvidedServices.class))
//			{
//				ProvidedServices val = (ProvidedServices)cma.getAnnotation(ProvidedServices.class);
//				ProvidedService[] vals = val.value();
//				prosdone = val.replace();
//				
//				Map psers = (Map)toset.get("proservices");
//				if(psers==null)
//				{
//					psers = new LinkedHashMap();
//					toset.put("proservices", psers);
//				}
//				
//				for(int i=0; i<vals.length; i++)
//				{
//					Implementation im = vals[i].implementation();
//					Value[] inters = im.interceptors();
//					UnparsedExpression[] interceptors = null;
//					if(inters.length>0)
//					{
//						interceptors = new UnparsedExpression[inters.length];
//						for(int j=0; j<inters.length; j++)
//						{
//							interceptors[j] = new UnparsedExpression(null, inters[j].clazz(), inters[j].value(), null);
//						}
//					}
//					ProvidedServiceImplementation impl = createImplementation(im);
//					Publish p = vals[i].publish();
//					NameValue[] props = p.properties();
//					UnparsedExpression[] exps = createUnparsedExpressions(props);
//					
//					PublishInfo pi = p.publishid().length()==0? null: new PublishInfo(p.publishid(), p.publishtype(), Object.class.equals(p.mapping())? null: p.mapping(), exps);
//					ProvidedServiceInfo psis = new ProvidedServiceInfo(vals[i].name().length()>0? 
//						vals[i].name(): null, vals[i].type(), impl, pi);
//				
//					if(vals[i].name().length()==0 || !psers.containsKey(vals[i].name()))
//					{
//						psers.put(vals[i].name().length()==0? ("#"+cnt++): vals[i].name(), psis);
//					}
//				}
//			}
//			
//			// Take all but new overrides old
//			if(!argsdone && cma.isAnnotationPresent(Arguments.class))
//			{
//				Arguments val = (Arguments)cma.getAnnotation(Arguments.class);
//				Argument[] vals = val.value();
//				argsdone = val.replace();
//				
//				Map args = (Map)toset.get("arguments");
//				if(args==null)
//				{
//					args = new LinkedHashMap();
//					toset.put("arguments", args);
//				}
//				
//				for(int i=0; i<vals.length; i++)
//				{
////					try
////					{
//	//				Object arg = SJavaParser.evaluateExpression(vals[i].defaultvalue(), imports, null, classloader);
//					IArgument tmparg = new jadex.bridge.modelinfo.Argument(vals[i].name(), 
//						vals[i].description(), SReflect.getClassName(vals[i].clazz()),
//						"".equals(vals[i].defaultvalue()) ? null : vals[i].defaultvalue());
//					
//					if(!args.containsKey(vals[i].name()))
//					{
//						args.put(vals[i].name(), tmparg);
//					}
////					}
////					catch(Exception e)
////					{
//						// Currently a type not present exception can occur with the applications.mixed.ShopAgent
////						e.printStackTrace();
////					}
//				}
//			}
//			
//			// Take all but new overrides old
//			if(!resudone && cma.isAnnotationPresent(Results.class))
//			{
//				Results val = (Results)cma.getAnnotation(Results.class);
//				Result[] vals = val.value();
//				resudone = val.replace();
//				
//				Map res = (Map)toset.get("results");
//				if(res==null)
//				{
//					res = new LinkedHashMap();
//					toset.put("results", res);
//				}
//				
//				IArgument[] tmpresults = new IArgument[vals.length];
//				for(int i=0; i<vals.length; i++)
//				{
//	//				Object res = evaluateExpression(vals[i].defaultvalue(), imports, null, classloader);
//					IArgument tmpresult = new jadex.bridge.modelinfo.Argument(vals[i].name(), 
//						vals[i].description(), SReflect.getClassName(vals[i].clazz()),
//						"".equals(vals[i].defaultvalue()) ? null : vals[i].defaultvalue());
//					
//					if(!res.containsKey(vals[i].name()))
//					{
//						res.put(vals[i].name(), tmpresult);
//					}
//				}
//			}
//			
//			// Take all but new overrides old
//			if(!compdone && cma.isAnnotationPresent(ComponentTypes.class))
//			{
//				SubcomponentTypeInfo[] subinfos = null;
//				ComponentTypes tmp = (ComponentTypes)cma.getAnnotation(ComponentTypes.class);
//				compdone = tmp.replace();
//				ComponentType[] ctypes = tmp.value();
//				
//				Map res = (Map)toset.get("componenttypes");
//				if(res==null)
//				{
//					res = new LinkedHashMap();
//					toset.put("componenttypes", res);
//				}
//				
//				for(int i=0; i<ctypes.length; i++)
//				{
//					SubcomponentTypeInfo subinfo = new SubcomponentTypeInfo(ctypes[i].name(), ctypes[i].filename());
//					if(!toset.containsKey(ctypes[i].name()))
//					{
//						res.put(ctypes[i].name(), subinfo);
//					}
//				}
//			}
//			
//			if(!confdone && cma.isAnnotationPresent(Configurations.class))
//			{
//				Configurations val = (Configurations)cma.getAnnotation(Configurations.class);
//				Configuration[] configs = val.value();
//				confdone = val.replace();
//				
//				Map confs = (Map)toset.get("configurations");
//				if(confs==null)
//				{
//					confs = new LinkedHashMap();
//					toset.put("configurations", confs);
//				}
//				
//				for(int i=0; i<configs.length; i++)
//				{
//					if(!confs.containsKey(configs[i].name()))
//					{
//						ConfigurationInfo configinfo = new ConfigurationInfo(configs[i].name());
//						confs.put(configs[i].name(), configinfo);
//						
//						configinfo.setMaster(configs[i].master());
//						configinfo.setDaemon(configs[i].daemon());
//						configinfo.setAutoShutdown(configs[i].autoshutdown());
//						configinfo.setSuspend(configs[i].suspend());
//						
//						NameValue[] argvals = configs[i].arguments();
//						for(int j=0; j<argvals.length; j++)
//						{
//							configinfo.addArgument(new UnparsedExpression(argvals[j].name(), argvals[j].clazz(), argvals[j].value(), null));
//						}
//						NameValue[] resvals = configs[i].results();
//						for(int j=0; j<resvals.length; j++)
//						{
//							configinfo.addResult(new UnparsedExpression(resvals[j].name(), resvals[j].clazz(), resvals[j].value(), null));
//						}
//						
//						ProvidedService[] provs = configs[i].providedservices();
//						ProvidedServiceInfo[] psis = new ProvidedServiceInfo[provs.length];
//						for(int j=0; j<provs.length; j++)
//						{
//							Implementation im = provs[j].implementation();
//							Value[] inters = im.interceptors();
//							UnparsedExpression[] interceptors = null;
//							if(inters.length>0)
//							{
//								interceptors = new UnparsedExpression[inters.length];
//								for(int k=0; k<inters.length; k++)
//								{
//									interceptors[k] = new UnparsedExpression(null, inters[k].clazz(), inters[k].value(), null);
//								}
//							}
//							RequiredServiceBinding bind = createBinding(im.binding());
//							ProvidedServiceImplementation impl = new ProvidedServiceImplementation(!im.value().equals(Object.class)? im.value(): null, 
//								im.expression().length()>0? im.expression(): null, im.proxytype(), bind, interceptors);
//							Publish p = provs[j].publish();
//							PublishInfo pi = p.publishid().length()==0? null: new PublishInfo(p.publishid(), p.publishtype(), 
//								p.mapping(), createUnparsedExpressions(p.properties()));
//							psis[j] = new ProvidedServiceInfo(provs[j].name().length()>0? provs[j].name(): null, provs[j].type(), impl, pi);
//							configinfo.setProvidedServices(psis);
//						}
//						
//						RequiredService[] reqs = configs[i].requiredservices();
//						RequiredServiceInfo[] rsis = new RequiredServiceInfo[reqs.length];
//						for(int j=0; j<reqs.length; j++)
//						{
//							RequiredServiceBinding binding = createBinding(reqs[j].binding());
//							rsis[j] = new RequiredServiceInfo(reqs[j].name(), reqs[j].type(), reqs[j].multiple(), 
//								Object.class.equals(reqs[j].multiplextype())? null: reqs[j].multiplextype(), binding);
//							configinfo.setRequiredServices(rsis);
//						}
//						
//						Component[] comps = configs[i].components();
//						for(int j=0; j<comps.length; j++)
//						{
//							configinfo.addComponentInstance(createComponentInstanceInfo(comps[j]));
//						}
//					}
//				}
//			}
//			
//			// Find injection targets by reflection (agent, arguments, services)
//			Field[] fields = cma.getDeclaredFields();
//			for(int i=0; i<fields.length; i++)
//			{
//				if(fields[i].isAnnotationPresent(Agent.class))
//				{
//					micromodel.addAgentInjection(fields[i]);
//				}
//				else if(fields[i].isAnnotationPresent(AgentService.class))
//				{
//					AgentService ser = (AgentService)fields[i].getAnnotation(AgentService.class);
//					String name = ser.name().length()>0? ser.name(): fields[i].getName();
//					micromodel.addServiceInjection(name, fields[i]);
//				}
//				else
//				{
//					if(fields[i].isAnnotationPresent(AgentArgument.class))
//					{
//						AgentArgument arg = (AgentArgument)fields[i].getAnnotation(AgentArgument.class);
//						String name = arg.value().length()>0? arg.value(): fields[i].getName();
//						micromodel.addArgumentInjection(name, fields[i], arg.convert());
//					}
//					if(fields[i].isAnnotationPresent(AgentResult.class))
//					{
//						AgentResult res = (AgentResult)fields[i].getAnnotation(AgentResult.class);
//						String name = res.value().length()>0? res.value(): fields[i].getName();
//						if(micromodel.getResultInjection(name)==null)
//						{
//							micromodel.addResultInjection(name, fields[i], res.convert(), res.convertback());
//						}
//					}
//				}
//			}
//			
//			if(micromodel.getBreakpointMethod()==null)
//			{
//				Method[] methods = cma.getDeclaredMethods();
//				for(int i=0; i<methods.length; i++)
//				{
//					final Method method = methods[i];
//					if(methods[i].isAnnotationPresent(AgentBreakpoint.class))
//					{
//						micromodel.setBreakpointMethod(method);
//					}
//				}
//			}
//			
//			cma = cma.getSuperclass();
//		}
//				
//		Set imp = (Set)toset.get("imports");
//		if(imp!=null)
//			modelinfo.setImports((String[])imp.toArray(new String[imp.size()]));
//		
//		Map props = (Map)toset.get("properties");
//		List bps = (List)toset.get("breakpoints");
//		if(bps!=null)
//		{
//			if(props==null)
//				props = new HashMap();
//			props.put("debugger.breakpoints", bps);
//		}
//		if(props!=null)
//			modelinfo.setProperties(props);
//		
//		Map rsers = (Map)toset.get("reqservices");
//		if(rsers!=null)
//			modelinfo.setRequiredServices((RequiredServiceInfo[])rsers.values().toArray(new RequiredServiceInfo[rsers.size()]));
//		
//		Map psers = (Map)toset.get("proservices");
//		if(psers!=null)
//			modelinfo.setProvidedServices((ProvidedServiceInfo[])psers.values().toArray(new ProvidedServiceInfo[psers.size()]));
////		System.out.println("provided services: "+psers);
//		
//		Map argus = (Map)toset.get("arguments");
//		if(argus!=null)
//			modelinfo.setArguments((IArgument[])argus.values().toArray(new IArgument[argus.size()]));
////		System.out.println("arguments: "+argus);
//		
//		Map res = (Map)toset.get("results");
//		if(res!=null)
//			modelinfo.setResults((IArgument[])res.values().toArray(new IArgument[res.size()]));
//		
//		Map cts = (Map)toset.get("componenttypes");
//		if(cts!=null)
//			modelinfo.setSubcomponentTypes((SubcomponentTypeInfo[])cts.values().toArray(new SubcomponentTypeInfo[cts.size()]));
//
//		Map cfs = (Map)toset.get("configurations");
//		if(cfs!=null)
//			modelinfo.setConfigurations((ConfigurationInfo[])cfs.values().toArray(new ConfigurationInfo[cfs.size()]));
//
//	}
	
	/**
	 *  Evaluate an expression string (using "" -> null mapping) as annotations
	 *  do not support null values.
	 */
	protected Object evaluateExpression(String exp, String[] imports, IValueFetcher fetcher, ClassLoader classloader)
	{
		return exp.length()==0? null: SJavaParser.evaluateExpression(exp, imports, null, classloader);
	}
	
	/**
	 *  Create a service implementation.
	 */
	protected ProvidedServiceImplementation createImplementation(Implementation impl)
	{
		return new ProvidedServiceImplementation(!impl.value().equals(Object.class)? impl.value(): null, 
			impl.expression().length()>0? impl.expression(): null, impl.proxytype(), createBinding(impl.binding()), createUnparsedExpressions(impl.interceptors()));
	}
	
	/**
	 *  Create a service binding.
	 */
	protected RequiredServiceBinding createBinding(Binding bd)
	{
		return bd==null || Implementation.BINDING_NULL.equals(bd.name()) ? null: new RequiredServiceBinding(bd.name(), 
			bd.componentname().length()==0? null: bd.componentname(), bd.componenttype().length()==0? null: bd.componenttype(), 
			bd.dynamic(), bd.scope(), bd.create(), bd.recover(), createUnparsedExpressions(bd.interceptors()),
			bd.proxytype(), bd.creationinfo().type().length()>0? createComponentInstanceInfo(bd.creationinfo()): null);
	}
	
	/**
	 *  Create component instance info from component annotation.
	 */
	protected ComponentInstanceInfo createComponentInstanceInfo(Component comp)
	{
		ComponentInstanceInfo ret = new ComponentInstanceInfo();
		
		ret.setSuspend(comp.suspend());
		ret.setMaster(comp.master());
		ret.setDaemon(comp.daemon());
		ret.setAutoShutdown(comp.autoshutdown());
		
		if(comp.name().length()>0)
			ret.setName(comp.name());
		if(comp.type().length()>0)
			ret.setTypeName(comp.type());
		if(comp.configuration().length()>0)
			ret.setConfiguration(comp.configuration());
		if(comp.number().length()>0)
			ret.setNumber(comp.number());
		
		NameValue[] args = comp.arguments();
		if(args.length>0)
		{
			UnparsedExpression[] exps = createUnparsedExpressions(args);
			ret.setArguments(exps);
		}
		
		Binding[] binds = comp.bindings();
		if(binds.length>0)
		{
			RequiredServiceBinding[] bds = new RequiredServiceBinding[binds.length];
			for(int k=0; k<binds.length; k++)
			{
				bds[k] = createBinding(binds[k]);
			}
			ret.setBindings(bds);
		}
		
		return ret;
	}
	
	/**
	 *  Create component instance info from creation info annotation.
	 */
	protected ComponentInstanceInfo createComponentInstanceInfo(CreationInfo comp)
	{
		ComponentInstanceInfo ret = new ComponentInstanceInfo();
		
		ret.setSuspend(comp.suspend());
		ret.setMaster(comp.master());
		ret.setDaemon(comp.daemon());
		ret.setAutoShutdown(comp.autoshutdown());
		
		if(comp.name().length()>0)
			ret.setName(comp.name());
		if(comp.type().length()>0)
			ret.setTypeName(comp.type());
		if(comp.configuration().length()>0)
			ret.setConfiguration(comp.configuration());
		if(comp.number().length()>0)
			ret.setNumber(comp.number());
		
		NameValue[] args = comp.arguments();
		if(args.length>0)
		{
			UnparsedExpression[] exps = createUnparsedExpressions(args);
			ret.setArguments(exps);
		}
		
		return ret;
	}
	
	/**
	 *  Create unparsed expressions.
	 */
	protected UnparsedExpression[] createUnparsedExpressions(Value[] values)
	{
		UnparsedExpression[] ret = null;
		if(values.length>0)
		{
			ret = new UnparsedExpression[values.length];
			for(int i=0; i<values.length; i++)
			{
				ret[i] = new UnparsedExpression(null, values[i].clazz().getName(), values[i].value(), null);
			}
		}
		return ret;
	}
	
	/**
	 *  Create unparsed expressions.
	 */
	protected UnparsedExpression[] createUnparsedExpressions(NameValue[] values)
	{
		UnparsedExpression[] ret = null;
		if(values.length>0)
		{
			ret = new UnparsedExpression[values.length];
			for(int i=0; i<values.length; i++)
			{
				ret[i] = new UnparsedExpression(values[i].name(), values[i].clazz().getName(), values[i].value(), null);
			}
		}
		return ret;
	}
	
	/**
	 *  Create unparsed expressions.
	 */
	protected List<UnparsedExpression> createUnparsedExpressionsList(NameValue[] values)
	{
		List<UnparsedExpression>  ret = null;
		if(values.length>0)
		{
			ret = new ArrayList<UnparsedExpression>();
			for(int i=0; i<values.length; i++)
			{
				ret.add(new UnparsedExpression(values[i].name(), values[i].clazz().getName(), values[i].value(), null));
			}
		}
		return ret;
	}
	
	
	/**
	 * Get the mirco agent class.
	 */
	// todo: make use of cache
	protected Class getMicroAgentClass(String clname, String[] imports, ClassLoader classloader)
	{
		Class ret = SReflect.findClass0(clname, imports, classloader);
//		System.out.println(clname+" "+ret+" "+classloader);
		int idx;
		while(ret == null && (idx = clname.indexOf('.')) != -1)
		{
			clname = clname.substring(idx + 1);
			try
			{
				ret = SReflect.findClass0(clname, imports, classloader);
			}
			catch(IllegalArgumentException iae)
			{
				// Hack!!! Sun URL class loader doesn't like if classnames start
				// with (e.g.) 'C:'.
			}
			// System.out.println(clname+" "+cma+" "+ret);
		}
		if(ret == null)
		{
			throw new RuntimeException("Micro agent class not found: " + clname);
		}
		else if(!MicroAgent.class.isAssignableFrom(ret))
		{
			boolean	found	= false;
			Class	cma	= ret;
			while(!found && cma!=null)
			{
				found = isAnnotationPresent(cma, Agent.class, classloader); 
//				found	=  cma.isAnnotationPresent(Agent.class);
				cma	= cma.getSuperclass();
			}

			if(!found)
			{
				throw new RuntimeException("Not a Micro agent class: " + clname);
			}
		}
		return ret;
	}
	
	/**
	 * 
	 */
	public static boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> anclazz, ClassLoader cl)
	{
		return clazz.isAnnotationPresent((Class<? extends Annotation>)getClass(anclazz, cl));
	}
	
	/**
	 * 
	 */
	public static boolean isAnnotationPresent(Field f, Class<? extends Annotation> anclazz, ClassLoader cl)
	{
		return f.isAnnotationPresent((Class<? extends Annotation>)getClass(anclazz, cl));
	}
	
	/**
	 * 
	 */
	public static boolean isAnnotationPresent(Method m, Class<? extends Annotation> anclazz, ClassLoader cl)
	{
		return m.isAnnotationPresent((Class<? extends Annotation>)getClass(anclazz, cl));
	}
	
	/**
	 * 
	 */
	public <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> anclazz, ClassLoader cl)
	{
		ClassLoader cl2 = cl instanceof DummyClassLoader? ((DummyClassLoader)cl).getOriginal(): cl;
		return getProxyAnnotation(clazz.getAnnotation((Class<T>)getClass(anclazz, cl)), cl2);
	}
	
	/**
	 * 
	 */
	public <T extends Annotation> T getAnnotation(Field f, Class<T> anclazz, ClassLoader cl)
	{
		ClassLoader cl2 = cl instanceof DummyClassLoader? ((DummyClassLoader)cl).getOriginal(): cl;
		return getProxyAnnotation(f.getAnnotation((Class<T>)getClass(anclazz, cl)), cl2);
	}
	
	/**
	 * 
	 */
	public <T extends Annotation> T getAnnotation(Method m, Class<T> anclazz, ClassLoader cl)
	{
		ClassLoader cl2 = cl instanceof DummyClassLoader? ((DummyClassLoader)cl).getOriginal(): cl;
		return getProxyAnnotation(m.getAnnotation((Class<T>)getClass(anclazz, cl)), cl2);
	}
	
	/**
	 * 
	 */
	public <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> anclazz, ClassLoader cl1, ClassLoader cl2)
	{
		return getProxyAnnotation(clazz.getAnnotation((Class<T>)getClass(anclazz, cl1)), cl2);
	}
	
	/**
	 * 
	 */
	public static Class<?> getClass(Class<?> clazz, ClassLoader cl)
	{
		Class<?> ret = clazz;
		
		try
		{
			if(!clazz.getClassLoader().equals(cl))
			{
				ret = cl.loadClass(clazz.getName());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public static Class<?>[] getClassArray(Class<?>[] clazzes, ClassLoader cl)
	{
		Class<?>[] ret = new Class[clazzes.length];
		for(int i=0; i<clazzes.length; i++)
		{
			ret[i] = getClass(clazzes[i], cl);
		}
		return ret;
	}
	
	/**
	 *  Gets proxy annotation that can be invoked by corresponding classloader.
	 * @return ret
	 */
	public <T extends Annotation> T getProxyAnnotation(final T an, final ClassLoader cl)
	{
		T ret = null;
		
		if(isClassLoaderCompatible(an.getClass(), cl))
		{
			ret = an;
		}
		else
		{
//			System.out.println("reloading: "+an);
			
			try
			{
				Class<?>[] in = an.getClass().getInterfaces();
				Class<?>[] nin = new Class[in.length];
				for(int i=0; i<in.length; i++)
				{
					nin[i] = getClass(in[i], cl);
				}
				
				ret = (T)Proxy.newProxyInstance(cl, nin, new InvocationHandler()
				{
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
					{
						Object[] nargs = args==null? new Object[0]: args;
						Class[] cls = method.getParameterTypes();
						Method m = an.getClass().getMethod(method.getName(), getClassArray(method.getParameterTypes(), cl));
						Object ret = m.invoke(an, nargs);
						
						if(ret!=null && !isClassLoaderCompatible(ret.getClass(), cl))
						{
							if(ret instanceof Annotation)
							{
								ret = getProxyAnnotation((Annotation)ret, cl);
							}
							else if(ret.getClass().isArray() && ret.getClass().getComponentType().isAnnotation())
							{
								int len =  Array.getLength(ret);
								Class<?> arclazz = MicroClassReader.this.getClass(ret.getClass().getComponentType(), cl);
								Object nret = Array.newInstance(arclazz, len);
								for(int i=0; i<len; i++)
								{
									Array.set(nret, i, getProxyAnnotation((Annotation)Array.get(ret, i), cl));
								}
								ret = nret;
							}
						}
						return ret;
					}
				});
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public static class DummyClassLoader extends URLClassLoader
	{
		protected ClassLoader orig;
		
		/**
		 * 
		 */
		public DummyClassLoader(URL[] urls, ClassLoader parent, ClassLoader orig) 
		{
			super(urls, parent);
			this.orig = orig;
		}
		
		/**
		 *  Get the orig.
		 *  @return The orig.
		 */
		public ClassLoader getOriginal()
		{
			return orig;
		}
		
		/**
		 *  Set the orig.
		 *  @param orig The orig to set.
		 */
		public void setOriginal(ClassLoader orig)
		{
			this.orig = orig;
		}
		
		/**
		 * 
		 */
		public String toString()
		{
			String ret = super.toString();
			ClassLoader pa = getParent();
			while(pa!=null)
			{
				ret += " "+pa.toString();
				pa = pa.getParent();
			}
			return ret;
		}
	}
	
	/**
	 * 
	 */
	protected static boolean isClassLoaderCompatible(Class<?> clazz, ClassLoader cl)
	{
		ClassLoader clcl = clazz.getClassLoader();
		boolean ret = clcl==null || clcl.equals(cl);
		if(!ret)
		{
			ClassLoader tst = cl.getParent();
			while(tst!=null && !ret)
			{
				ret = clcl.equals(tst);
				tst = tst.getParent();
			}
			if(!ret)
			{
				try
				{
					Method m = cl.getClass().getMethod("isClassLoaderCompatible", new Class[]{Class.class});
					ret = ((Boolean)m.invoke(cl, new Object[]{clazz})).booleanValue();
				}
				catch(Exception e)
				{
//					e.printStackTrace();
				}
			}
		}
		return ret;
	}
}
