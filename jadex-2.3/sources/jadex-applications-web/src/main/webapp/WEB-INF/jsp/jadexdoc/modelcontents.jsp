<%@page import="jadex.commons.future.IFuture"%>
<%@page import="jadex.bridge.modelinfo.ConfigurationInfo"%>
<%@page import="jadex.commons.SUtil"%>
<%@page import="jadex.bridge.modelinfo.IArgument"%>
<%@page import="jadex.bridge.modelinfo.IModelInfo"%>
<%
	IModelInfo	model	= ((IFuture<IModelInfo>)request.getAttribute("model")).get(null);
	boolean	jaxcent	= ((Boolean)request.getAttribute("jaxcent")).booleanValue();
%>
<h2>Package <%= model.getPackage() %></h2>
<div class="desc"><%= model.getDescription()!=null ? model.getDescription() : "No description." %></div>

<% 	ConfigurationInfo[]	confs	=	model.getConfigurations();
	if(confs.length>0) { %>
<h2>Configurations</h2>
<%/* <div class="desc">Click on a configuration to show corresponding argument and result values.</div> */%>
<table class="printtable">
	<tr>
		<th>Name</th>
		<th>Description</th>
	</tr>
<%
	for(int i=0; i<confs.length; i++)
	{
%>
	<tr <%= jaxcent ? "id=\"config"+i+"\"" : "class=\""+ (i%2==0 ? "even\"" : "odd\"") %>>
		<td class="name" <%/* style="cursor:pointer;" */%>><%= confs[i].getName() %></td>
		<td class="desc" <%/* style="cursor:pointer;" */%>><%= confs[i].getDescription() %></td>
	</tr>
<%	} %>
</table>
<% } %>

<%
	boolean	hasflags	= model.getAutoShutdown(null)!=null || model.getDaemon(null)!=null
		|| model.getMaster(null)!=null || model.getSuspend(null)!=null;
	for(int i=0; !hasflags && i<confs.length; i++)
	{
		hasflags	= confs[i].getAutoShutdown()!=null || confs[i].getDaemon()!=null
			|| confs[i].getMaster()!=null || confs[i].getSuspend()!=null;		
	}
	if(hasflags) {
%>
<h2 id="flags">Flags</h2>
<table class="printtable">
	<tr class="even">
	<td class="name">Auto Shutdown</td>
	<td class="value"><div id="autoshutdown"><%= model.getAutoShutdown(null)!=null && model.getAutoShutdown(null).booleanValue() ? "true" : "false" %></div></td>
	<td class="desc">Destroy the component, when the last non-daemon subcomponent has been removed.</td>
	</tr><tr class="odd">			
	<td class="name">Daemon</td>
	<td class="value"><div id="daemon"><%= model.getDaemon(null)!=null && model.getDaemon(null).booleanValue() ? "true" : "false" %></div></td>
	<td class="desc">A daemon component does not prevent auto-shutdown of its parent.
	</tr><tr class="even">
	<td class="name">Master</td>
	<td class="value"><div id="master"><%= model.getMaster(null)!=null && model.getMaster(null).booleanValue() ? "true" : "false" %></div></td>
	<td class="desc">Destroy the parent component, when this component is destroyed.
	</tr><tr class="odd">			
	<td class="name">Suspend</td>
	<td class="value"><div id="suspend"><%= model.getSuspend(null)!=null && model.getSuspend(null).booleanValue() ? "true" : "false" %></div></td>
	<td class="desc">Start the component in suspended state.</td>
	</tr>
</table>
<% } %>

<% if(model.getArguments().length>0) { %>
<h2>Arguments</h2>
<table class="printtable">
	<tr>
		<th>Name</th>
		<th>Type</th>
		<th id="argdef">Default Value</th>
	</tr>
<%
	IArgument[]	args	=	model.getArguments();
	for(int i=0; i<args.length; i++)
	{
%>
	<tr class="<%= i%2==0 ? "even" : "odd"%>">
		<td class="desc" colspan="3"><%= args[i].getDescription() %></td>
	</tr>
	<tr class="<%= i%2==0 ? "even" : "odd"%>">
		<td class="name"><%= args[i].getName() %></td>
		<td class="type"><%= args[i].getClazz()!=null ? args[i].getClazz().getTypeName() : "undefined" %></td>
		<td class="value"><div id="<%= "arg"+i %>"><%= args[i].getDefaultValue().getValue() %></div></td>
	</tr>
<%	} %>
</table>
<% } %>

<% if(model.getResults().length>0) { %>
<h2>Results</h2>		
<table class="printtable">
	<tr>
		<th>Name</th>
		<th>Type</th>
		<th>Description</th>
		<th id="resdef">Default Value</th>
	</tr>
<%
	IArgument[]	args	=	model.getResults();
	for(int i=0; i<args.length; i++)
	{
%>
	<tr class="<%= i%2==0 ? "even" : "odd"%>">
		<td class="desc" colspan="3"><%= args[i].getDescription() %></td>
	</tr>
	<tr class="<%= i%2==0 ? "even" : "odd"%>">
		<td class="name"><%= args[i].getName() %></td>
		<td class="type"><%= args[i].getClazz()!=null ? args[i].getClazz().getTypeName() : "undefined" %></td>
		<td class="value"><div id="<%= "arg"+i %>"><%= args[i].getDefaultValue().getValue() %></div></td>
	</tr>
<%	} %>
</table>
<% } %>		
