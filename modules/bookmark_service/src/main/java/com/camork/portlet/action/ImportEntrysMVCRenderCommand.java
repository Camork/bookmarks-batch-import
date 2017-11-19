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

package com.camork.portlet.action;

import com.liferay.bookmarks.constants.BookmarksPortletKeys;
import com.liferay.bookmarks.constants.BookmarksWebKeys;
import com.liferay.bookmarks.exception.NoSuchFolderException;
import com.liferay.bookmarks.model.BookmarksFolder;
import com.liferay.bookmarks.model.BookmarksFolderConstants;
import com.liferay.bookmarks.service.BookmarksFolderServiceUtil;
import com.liferay.bookmarks.service.permission.BookmarksResourcePermissionChecker;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Charles Wu
 */
@Component(
	immediate = true,
	property = {
	"javax.portlet.name=" + BookmarksPortletKeys.BOOKMARKS,
	"javax.portlet.name=" + BookmarksPortletKeys.BOOKMARKS_ADMIN, 
	"mvc.command.name=/bookmarks/import_entries"
	}, service = MVCRenderCommand.class
)
public class ImportEntrysMVCRenderCommand implements MVCRenderCommand {

	protected String getPath() {
		return "/bookmarks/import_entries.jsp";
	}

	@Override
	public String render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException {
		try {
			BookmarksFolder folder = getFolder(renderRequest);

			renderRequest.setAttribute(BookmarksWebKeys.BOOKMARKS_FOLDER, folder);
		}
		catch (Exception e) {
			if (e instanceof NoSuchFolderException ||
				e instanceof PrincipalException) {

				SessionErrors.add(renderRequest, e.getClass());

				return "/bookmarks/error.jsp";
			}
			else {
				throw new PortletException(e);
			}
		}

		return getPath();
	}
	
	public static BookmarksFolder getFolder(HttpServletRequest request)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		long folderId = ParamUtil.getLong(request, "folderId");

		BookmarksFolder folder = null;

		if ((folderId > 0) &&
			(folderId != BookmarksFolderConstants.DEFAULT_PARENT_FOLDER_ID)) {

			folder = BookmarksFolderServiceUtil.getFolder(folderId);

			if (folder.isInTrash()) {
				throw new NoSuchFolderException("{folderId=" + folderId + "}");
			}
		}
		else {
			BookmarksResourcePermissionChecker.check(
				themeDisplay.getPermissionChecker(),
				themeDisplay.getScopeGroupId(), ActionKeys.VIEW);
		}

		return folder;
	}
	
	public static BookmarksFolder getFolder(PortletRequest portletRequest)
		throws Exception {

		HttpServletRequest request = PortalUtil.getHttpServletRequest(
			portletRequest);

		return getFolder(request);
	}

}
