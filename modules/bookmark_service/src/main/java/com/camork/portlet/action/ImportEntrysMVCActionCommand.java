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
import com.liferay.bookmarks.model.BookmarksEntry;
import com.liferay.bookmarks.service.BookmarksEntryService;
import com.liferay.bookmarks.service.BookmarksFolderService;
import com.liferay.portal.kernel.io.ByteArrayFileInputStream;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Charles Wu
 */
@Component(immediate = true, property = {
	"javax.portlet.name=" + BookmarksPortletKeys.BOOKMARKS,
	"javax.portlet.name=" + BookmarksPortletKeys.BOOKMARKS_ADMIN, 
	"mvc.command.name=/bookmarks/import_entries"
	}, service = MVCActionCommand.class
)
public class ImportEntrysMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) 
		throws Exception {

		UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);
		ByteArrayFileInputStream inputStream = null;
		try {
			File file = uploadPortletRequest.getFile("bookmark_file");
			if (!file.exists()) {
				System.out.println("Empty File");
			}
			String content = null;
			if ((file != null) && file.exists()) {

				inputStream = new ByteArrayFileInputStream(file, 1024);
				byte[] data;
				try {
					data = FileUtil.getBytes(inputStream);
					content = new String(data);

					Document doc = Jsoup.parse(content);

					Elements linkElements = doc.select("dt a");

					Map<String, String> bookmarks = new HashMap<>();

					for (Element e : linkElements)
						bookmarks.put(e.text(), e.attr("abs:href"));

					updateEntry(actionRequest, bookmarks);

				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		finally {
			StreamUtil.cleanUp(inputStream);
		}
	}

	protected void updateEntry(ActionRequest actionRequest, Map<String, String> data) throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getScopeGroupId();
		long folderId = ParamUtil.getLong(actionRequest, "folderId");

		ServiceContext serviceContext =
			ServiceContextFactory.getInstance(BookmarksEntry.class.getName(), actionRequest);

		for (Map.Entry<String, String> mapEntry : data.entrySet()) {
			_bookmarksEntryService.addEntry(
				groupId, folderId, mapEntry.getKey(), mapEntry.getValue(), "", serviceContext
			);
		}

	}

	@Reference(unbind = "-")
	protected void setBookmarksEntryService(BookmarksEntryService bookmarksEntryService) {

		_bookmarksEntryService = bookmarksEntryService;
	}

	private BookmarksEntryService _bookmarksEntryService;

	@Reference
	private Http _http;

	@Reference
	private Portal _portal;

}
