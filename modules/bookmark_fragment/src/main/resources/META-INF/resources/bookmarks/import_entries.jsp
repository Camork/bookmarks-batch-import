<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>
<%@ include file="/bookmarks/init.jsp" %>

<%
	String redirect = ParamUtil.getString(request, "redirect");
	String backURL = ParamUtil.getString(request, "backURL");
	 
	BookmarksFolder folder = (BookmarksFolder) request.getAttribute(BookmarksWebKeys.BOOKMARKS_FOLDER);

	long folderId = BeanParamUtil.getLong(folder, request, "folderId");
	
	boolean portletTitleBasedNavigation =
		GetterUtil.getBoolean(portletConfig.getInitParameter("portlet-title-based-navigation"));

	if (portletTitleBasedNavigation) {
		portletDisplay.setShowBackIcon(true);
		portletDisplay.setURLBack(redirect);
		renderResponse.setTitle("Import Bookmarks");
	}
%>
<div>
	<portlet:actionURL name="/bookmarks/import_entries" var="importBookmarks">
		<portlet:param name="mvcRenderCommandName" value="/bookmarks/import_entries" />
	</portlet:actionURL>

	<portlet:defineObjects />

	<h3>Please Upload the exported bookmarks from Google Chrome</h3>
	<aui:form action="<%=importBookmarks%>" method="post" enctype="multipart/form-data">
		<aui:input name="folderId" type="hidden" value="<%= folderId %>" />
		<aui:input type="file" name="bookmark_file" />
		<button type="submit" class="defaultButton" style="margin-left: 10px;">Upload</button>
	</aui:form>
</div>