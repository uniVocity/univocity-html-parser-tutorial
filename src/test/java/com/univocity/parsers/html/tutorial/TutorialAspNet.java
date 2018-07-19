/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;
import com.univocity.api.entity.html.builders.*;
import com.univocity.api.net.*;
import com.univocity.parsers.remote.*;

import java.util.*;

/**
 * This tutorial demonstrates how parse results with pagination implemented in ASP.NET and
 * it's meant as a reference only. You will have to adapt this code to your particular case.
 *
 * Here, it is expected that the page uses AJAX to obtain each page of results. When using a
 * browser, javascript is used to parse this result and update the page.
 *
 * Our example works by reading the response using Java instead to produce the same requests a browser would.
 *
 * Each POST request returns a pipe separated response with a mix of HTML and values for hidden elements
 * of the page. Every page of results will be saved under "{user.home}/Downloads/companies/" - look at these files to
 * help understand what the code is doing.
 */
public class TutorialAspNet extends Tutorial {

	public static void main(String ... args){
		//search for companies in Alabama
		new TutorialAspNet().findCompanies("AL");
	}

	public void findCompanies(String stateCode) {
		// Let's start by configuring the parser to save each page of results into a local directory
		HtmlEntityList entityList = new HtmlEntityList();
		entityList.getParserSettings().setDownloadContentDirectory("{user.home}/Downloads/companies/");

		FetchOptions fetchOptions = new FetchOptions();
		entityList.getParserSettings().fetchResourcesBeforeParsing(fetchOptions);

		// Then configure the "companies" entity to have fields "Name" and "City"
		configureCompanyFields(entityList);

		// Configure the paginator
		configurePagination(entityList);

		// Create a POST request to search for companies of a given state.
		// This request will return the first page of results. The paginator will then go
		// after the other pages for us.
		UrlReaderProvider search = generateSearchRequest(stateCode);

		// Finally we parse and print out the rows of entity "companies"
		HtmlParserResult result = new HtmlParser(entityList).parse(search).get("companies");
		printResult(result);
		printAndDontValidate();
	}

	private UrlReaderProvider generateSearchRequest(String stateCode) {
		// The first access URL has the same effect of opening the URL in your browser
		UrlReaderProvider firstAccess = new UrlReaderProvider("http://somePageThatUsesAsp.com/frmSearch.aspx");

		// We parse page available in the URL above into a HTML tree.
		HtmlElement pageRoot = HtmlParser.parseTree(firstAccess);

		// Now we clone the request configuration, and prepare a POST request to get the first page of results
		UrlReaderProvider search = firstAccess.clone();
		HttpRequest request = search.getRequest();
		request.setRequestMethod(RequestMethod.POST);

		// The pagination is controlled by a form with id = "aspnetForm" - we need to work with it.
		HtmlElement form = pageRoot.query().match("form").id("aspnetForm").getElement();

		// First we get the values of the input fields in this form
		Map<String, String[]> data = form.inputValues();

		// Then we add the following fields to the body of our POST request. These are NOT in the form - or won't have the
		// appropriate values - because this is populated with javascript. You must your browser to execute the search and
		// inspect the network activity - the POST request to "http://somePageThatUsesAsp.com/frmSearch.aspx" will display
		// these values (along with the form data obtained above)
		data.put("__ASYNCPOST", new String[]{"true"});
		data.put("ctl00$ScriptManager1", new String[]{"ctl00$ScriptManager1|ctl00$ContentPlaceHolder1$btnSearch"});
		data.put("ctl00$ContentPlaceHolder1$drpState", new String[]{stateCode});
		data.put("ctl00$ContentPlaceHolder1$btnSearch", new String[]{"Search"});

		request.setDataParameters(data);

		// Make sure the request is sending the same parameters your browser sends, by printing the details
		System.out.println(request.printDetails());

		return search;
	}

	private void configureCompanyFields(HtmlEntityList entityList){
		//##CODE_START
		// Let's create our "companies" entity:
		HtmlEntitySettings companies = entityList.configureEntity("companies");

		// The results come from a table with an ID like "ctl00_ContentPlaceHolder1_grdSearchResult"
		// Let's create a partial path that matches this table.
		PartialPath table = companies.newPath().match( "table").id("ctl00_*_grdSearchResult"); //notice the wildcard

		// Names are in <a> elements with crazy ID's such as "ctl00_ContentPlaceHolder1_grdSearchResult_ctl02_hpFirmName"
		// We use a wildcard to match any <a> in the table, with ID ending with "_hpFirmName"
		table.addField("Name").match("a").id("*_hpFirmName").getText();

		// Same story for city names
		table.addField("City").match("span").id("*_lblCity").getText();
		//##CODE_END
	}

	private void configurePagination(HtmlEntityList entityList){
		// On this website, the page numbers are displayed as a sequence of <a> elements, where the current page
		// has is highlighted with color #E2E2E2. To go to the next page, we match the <a> element that follows the
		// highlighted
		HtmlPaginator paginator = entityList.getPaginator();
		paginator.setFollowCount(2); //reads up to 2 pages of results after the first.

		paginator.addField("nextPageTarget") // we use this custom paginator field in the pagination handler
				.match("tr").classes("footer_grid") // looks only at <a> elements of the pager row
				.match("a").attribute("style", "*background-color:#E2E2E2*") // finds the highlighted page - wildcards help a lot.
				.matchFirst("a").classes("LinkPaging") // matches the link the comes after
				.getAttribute("href") // the href looks like "javascript:__doPostBack('ctl00$ContentPlaceHolder1$grdSearchResult$ctl23$ctl03','')"
				.transform(target-> substringBetween(target, "'", "'")); //we want to get "ctl00$ContentPlaceHolder1$grdSearchResult$ctl23$ctl03"

		paginator.setPaginationHandler(new NextInputHandler<HtmlPaginationContext>() {
			@Override
			public void prepareNextCall(HtmlPaginationContext pagination) {

				String nextPageTarget = pagination.readField("nextPageTarget");
				if(nextPageTarget == null){ // after going through all result pages, our custom "nextPageTarget" field will be null
					pagination.stop(); //so we stop the pagination
					return;
				}

				//The following code simply produces the same request that your browser would with javascript.

				// We need to get values from the result of the POST request. Check the downloaded files under
				// "{user.home}/Downloads/companies/" to see what the server returns: it's a pipe separated string.
				// As downloads are enabled, calling `getContent()` will return the text from the downloaded file instead of generating a new request.
				String ajaxResponse = pagination.getCurrentResponse().getContent();

				// From that pipe separated string, we need to get the updated value for "__VIEWSTATE"
				String viewState = substringBetween(ajaxResponse, "__VIEWSTATE|", "|");

				// In our request for the next page, we must send the updated __VIEWSTATE
				HttpRequest request = pagination.getNextRequest().getRequest();
				request.setDataParameter("__VIEWSTATE", viewState);

				// The value collected by our field "nextPageTarget" is also required to build the next page request.
				request.setDataParameter("__EVENTTARGET", nextPageTarget);
				request.setDataParameter("ctl00$ScriptManager1", "ctl00$ContentPlaceHolder1$pnlgrdSearchResult|" + nextPageTarget);

				// These parameters must be removed from the POST request or else the server will return the first page again.
				request.removeDataParameter("ctl00$ContentPlaceHolder1$btnAccept");
				request.removeDataParameter("ctl00$ContentPlaceHolder1$btnfrmSearch");
				request.removeDataParameter("ctl00$ContentPlaceHolder1$btnSearch");

				// Compare the request made via code against what your browser sends. The body of the POST request must
				// have the same keys and values.
				System.out.println(request.printDetails());
			}
		});
	}

	private String substringBetween(String string, String open, String close){
		int openIndex = string.indexOf(open) + open.length();
		int closeIndex = string.indexOf(close, openIndex + 1);
		String result = string.substring(openIndex, closeIndex);
		return result;
	}
}
