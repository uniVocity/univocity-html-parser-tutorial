/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.github.tomakehurst.wiremock.*;
import com.univocity.api.entity.html.*;
import com.univocity.api.entity.html.builders.*;
import com.univocity.api.net.*;
import com.univocity.parsers.common.*;
import com.univocity.parsers.mocks.*;
import com.univocity.parsers.remote.*;
import org.testng.annotations.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;

public class TutorialRemote extends Tutorial {

	public static final int PORT = 8086;

	private WireMockServer wireMockServer;
	private MouseSearchMock mouseSearchMock;
	private MemoryStickSearchMock memoryStickSearchMock;
	private RealEstateMock realEstateMock;

	@BeforeClass
	public void setUp() throws Exception {
		wireMockServer = new WireMockServer(wireMockConfig().port(PORT));
		wireMockServer.start();

		mouseSearchMock = new MouseSearchMock(PORT);
		mouseSearchMock.setup();

		memoryStickSearchMock = new MemoryStickSearchMock(PORT);
		memoryStickSearchMock.setup();

		realEstateMock = new RealEstateMock(PORT);
		realEstateMock.setup();
	}

	@AfterClass
	public void tearDown() {
		wireMockServer.stop();
	}

	@Test
	public void example001RemotePagination() {

		HtmlEntityList entities = new HtmlEntityList();

		//##CODE_START
		// Configures the paginator to follow through a list of search result pages
		HtmlPaginator paginator = entities.getPaginator();
		paginator.setNextPage().match("a").id("pagnNextLink")
				.classes("pagnNext").getAttribute("href");

		// Collect rows from up to 2 pages after the first search results page.
		paginator.setFollowCount(2);

		// Print out the details of the request to be made to the next page.
		// You can modify the request at will if you need.
		paginator.setPaginationHandler(new NextInputHandler<HtmlPaginationContext>() {
			@Override
			public void prepareNextCall(HtmlPaginationContext remoteContext) {
				//this is the request ready to go to the next page.
				UrlReaderProvider next = remoteContext.getNextRequest();

				//the context object has additional information.
				int pageNumber = remoteContext.getPageCount() + 1;

				println("Going to page " + pageNumber + ": " + next.getUrl());
				println("Headers: " + next.getRequest().getHeaders());
				println();
			}
		});

		// creates a new request
		UrlReaderProvider url = new UrlReaderProvider("http://localhost:8086/s/field-keywords=mouse");

		// configure the request
		url.getRequest().setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:49.0) Gecko/20100101 Firefox/49.0");
		url.getRequest().setHeader("Accept-Language", "en-US,en;q=0.5");

		// run the parser against the first page. The paginator will kick in and get the results of another 2 result pages.
		new HtmlParser(entities).parse(url);

		//##CODE_END
		printAndValidate();
	}

	@Test
	public void example002RemotePaginationOnUrlParams() {
		HtmlEntityList entityList = new HtmlEntityList();
		entityList.getParserSettings().setRemoteInterval(0L);

		//##CODE_START
		HtmlPaginator paginator = entityList.getPaginator();

		// Let's control the pagination ourselves
		paginator.setPaginationHandler(new NextInputHandler<HtmlPaginationContext>() {
			@Override
			public void prepareNextCall(HtmlPaginationContext paginationContext) {
				UrlReaderProvider nextPage = paginationContext.getNextRequest();

				int nextPageNumber = paginationContext.getPageCount() + 1;

				if (nextPageNumber <= 5) {
					//update the page number parameter.
					nextPage.getRequest().setUrlParameter("page_number", nextPageNumber);

					//print the next URL
					println("Next page: " + nextPage.getRequest().getUrl());
				} else {
					println("Request not modified, pagination will stop: " + nextPage.getRequest().getUrl());
				}

			}
		});

		// This is our initial URL (with parameters)
		String url = "http://localhost:8086/sch/i.html?_from=R40&_sacat=0&_nkw={search_key}&_pgn={page_number}&_skc=50&rt=nc";
		UrlReaderProvider input = new UrlReaderProvider(url);

		// Set the page_number
		input.getRequest().setUrlParameter("page_number", 1);

		// Set the search_key
		input.getRequest().setUrlParameter("search_key", "memory stick");

		// Run the parser.
		new HtmlParser(entityList).parse(input);

		//##CODE_END
		printAndValidate();
	}

	private HtmlEntityList configureRealEstatePagination() {
		//##CODE_START
		HtmlEntityList entityList = new HtmlEntityList();

		HtmlParserSettings parserSettings = entityList.getParserSettings();
		parserSettings.setDownloadContentDirectory("{user.home}/Downloads/realEstate/");
		parserSettings.setFileNamePattern("{date, yyyy-MMM-dd}/location_{$location}_page_{page, 4}.html");

		// won't override local files. Allows stopping and resuming the process.
		parserSettings.setDownloadOverwritingEnabled(false);

		//configure the paginator
		HtmlPaginator paginator = entityList.getPaginator();
		paginator.setCurrentPageNumber()
				.match("div").id("pager")
				.match("li").classes("pagerCount")
				.matchFirst("span").getText();

		paginator.setNextPage()
				.match("li").classes("pagerNext")
				.matchFirst("a").getAttribute("href");

		//Configure the paginator to visit one page of results after the first
		paginator.setFollowCount(1);

		//##CODE_END

		return entityList;
	}

	@Test
	public void runRealEstateSearchDownload() {
		HtmlEntityList entityList = configureRealEstatePagination();

		//##CODE_START

		// The search query lists all properties at a given location
		UrlReaderProvider search = new UrlReaderProvider("http://localhost:8086/Property/Residential?search=&location={LOCATION_CODE}&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=1");

		HtmlParser parser = new HtmlParser(entityList);

		// We just need to set the location code and parse the URL. All files will be saved locally. Running the parser
		// again on the same day will simply run over the stored files instead of actually going to the remote site.
		search.getRequest().setUrlParameter("LOCATION_CODE", "22008");
		parser.parse(search);

		//##CODE_END
	}

	private void configureHouseDetailsParsing(HtmlEntityList entityList) {

		//##CODE_START
		HtmlEntitySettings houses = entityList.configureEntity("houses");

		HtmlLinkFollower houseDetails = houses.addField("propertyDetailsLink")
				.match("div").id("galleryView")
				.match("div").classes("listingContent")
				.matchNext("h2").matchNext("a").getAttribute("href")
				.followLink();
		houseDetails.setNesting(Nesting.JOIN);

		// We need to visit each link of the search results to get the details of each house available for sale.
		// The details page of each property will be saved along with the initial search results. The third element
		// of the URL path can be used to name the files.
		houseDetails.getParserSettings().setFileNamePattern("{parent}/../{url, 2}.html");

		houseDetails.addField("id").match("strong").withExactText("Listing Number:").getFollowingText();
		houseDetails.addField("address").match("h2").classes("detailAddress").getText();
		houseDetails.addField("price").match("h3").id("listingViewDisplayPrice").getText();

		PartialPath info = houseDetails.newPath().match("ul").id("detailFeatures");
		info.addField("bedrooms").match("li").classes("bdrm").matchNext("span").getText();
		info.addField("bathrooms").match("li").classes("bthrm").matchNext("span").getText();

		info = houseDetails.newPath().match("div").classes("property-information").match("li").matchNext("span").classes("heading");
		info.addField("landSize").matchCurrent().withText("Land size").getFollowingText();
		info.addField("propertyType").matchCurrent().withText("Property type").getFollowingText();

		//##CODE_END
	}

	@Test
	public void example003HistoricalDataProcessing() {
		HtmlEntityList entityList = configureRealEstatePagination();
		configureHouseDetailsParsing(entityList);

		// The search query lists all properties at a given location
		UrlReaderProvider search = new UrlReaderProvider("http://localhost:8086/Property/Residential?search=&location={LOCATION_CODE}&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=1");

		HtmlParser parser = new HtmlParser(entityList);

		// We just need to set the location code and parse the URL. All files will be saved locally. Running the parser
		// again on the same day will simply run over the stored files instead of actually going to the remote site.
		search.getRequest().setUrlParameter("LOCATION_CODE", "22008");

		HtmlParserResult result = parser.parse(search).get("houses");

		printResult(result);
		printAndValidate();
	}


	@Test
	public void example004FetchResources() {
		HtmlEntityList entityList = configureRealEstatePagination();

		//##CODE_START
		// configure the fetch operation
		FetchOptions fetchOptions = new FetchOptions();

		// all resources of all pages to be stored under a "cache" folder.
		fetchOptions.setSharedResourceDir("{user.home}/Downloads/realEstate/cache");

		// use a download handler to control what to download - the `DownloadContext` provides many options, check the javadoc
		fetchOptions.setDownloadHandler(new DownloadHandler() {
			@Override
			public void nextDownload(DownloadContext context) {
				print("[" + context.parentHtmlFile().getName() + "] ");
				//we don't want to fetch linked html pages.
				if ("html".equals(context.targetFileExtension())) {
					println("skip download from " + context.downloadUrl() + " into " + context.targetRelativePath());
					context.skipDownload();
					return;
				}

				println("download from " + context.downloadUrl() + " into " + context.targetRelativePath());
			}
		});

		// tell the parser to fetch the resources using our configuration.
		entityList.getParserSettings().fetchResourcesBeforeParsing(fetchOptions);

		// we also need to force the parser to overwrite the local files stored previously, otherwise it won't touch the existing files.
		entityList.getParserSettings().setDownloadOverwritingEnabled(true);
		//##CODE_END

		configureHouseDetailsParsing(entityList);

		// The search query lists all properties at a given location
		UrlReaderProvider search = new UrlReaderProvider("http://localhost:8086/Property/Residential?search=&location={LOCATION_CODE}&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=1");

		HtmlParser parser = new HtmlParser(entityList);

		// We just need to set the location code and parse the URL. All files will be saved locally. Running the parser
		// again on the same day will simply run over the stored files instead of actually going to the remote site.
		search.getRequest().setUrlParameter("LOCATION_CODE", "22008");

		HtmlParserResult result = parser.parse(search).get("houses");

		printAndValidate();
		println("\nParsed property details:");
		printResult(result);
		printAndDontValidate();

	}

	@Test
	public void example005SavePageLikeABrowser() {
		//##CODE_START
		UrlReaderProvider url = new UrlReaderProvider("http://www.univocity.com");

		//parse the web page into a HTML element tree.
		HtmlElement root = HtmlParser.parseTree(url);

		//configure fetch options as needed
		FetchOptions fetchOptions = new FetchOptions();

		// flatten directories (generates long file names, so subdirectories)
		fetchOptions.flattenDirectories(true);

		// you can use the fetch output object to list all downloaded files
		FetchOutput output = root.fetchResources("{user.home}/Downloads/univocity.html", "UTF-8", fetchOptions);

		// let's list the downloaded files
		Map<File, URL> downloadedFiles = output.getResourceMap();

		for (Map.Entry<File, URL> e : downloadedFiles.entrySet()) {
			println(e.getKey().getAbsolutePath() + " downloaded from " + e.getValue());
		}

		//##CODE_END
		printAndValidate();
	}
}