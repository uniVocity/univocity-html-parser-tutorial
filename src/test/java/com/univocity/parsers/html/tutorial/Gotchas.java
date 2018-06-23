/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;
import com.univocity.api.io.*;
import org.testng.annotations.*;

import java.util.*;

public class Gotchas extends Tutorial {

	@Test
	public void example001MatchingEverything() {

		HtmlEntityList entityList = new HtmlEntityList();

		//##CODE_START
		HtmlEntitySettings entity = entityList.configureEntity("company");

		entity.addField("id")
				.match("span").withText("company no:")
				.match("span").getText(); // match any <span> after finding a <span> with text "company no"

		entity.addField("name")
				.match("span").withText("Legal name:")
				.match("b").getText(); // match any <b> after finding a <span> with text "company no"
		//##CODE_END

		HtmlParser parser = new HtmlParser(entityList);

		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_001.html", "UTF-8");
		HtmlParserResult result = parser.parse(input).get("company");

		printResult(result);
		printAndValidate();
	}

	@Test
	public void example001MatchingEverythingFixed() {
		//##CODE_START
		// entities are defined in an entity list.
		HtmlEntityList entityList = new HtmlEntityList();

		// here we define the company entity.
		HtmlEntitySettings company = entityList.configureEntity("company");

		// creates a field "id" for company numbers
		company.addField("id")
				.match("span").withText("company no:") // match any <span> with text "company no"
				.matchNext("span").getText(); //match only the next <span> after finding a <span> with text "company no"

		// creates a field "name" for company names
		company.addField("name")
				.match("span").withText("Legal name:") // match any <span> with text "company no"
				.matchNext("b").getText(); //match only the next <b> after finding a <span> with text "legal name"

		// create a parser instance
		HtmlParser parser = new HtmlParser(entityList);

		// define the input file to parse
		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_001.html", "UTF-8");

		// then parse to get the results
		HtmlParserResult result = parser.parse(input).get("company");
		//##CODE_END
		printResult(result);
		printAndValidate();
	}

	@Test
	public void example002MatchingMixed() {
		HtmlEntityList entityList = new HtmlEntityList();
		HtmlEntitySettings entity = entityList.configureEntity("company");

		//##CODE_START
		entity.addField("id")
				.match("span").withText("company no:")
				.matchNext("span").getText();

		entity.addField("name")
				.match("span").withText("Legal name:")
				.matchNext("b").getText();

		HtmlParser parser = new HtmlParser(entityList);

		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_002.html", "UTF-8");
		HtmlParserResult result = parser.parse(input).get("company");
		//##CODE_END

		printResult(result);
		printAndValidate();
	}

	@Test
	public void example002MatchingMixedFixed() {
		HtmlEntityList entityList = new HtmlEntityList();

		HtmlEntitySettings entity = entityList.configureEntity("company");

		entity.addField("id")
				.match("span").withText("company no:")
				.matchNext("span").getText();

		entity.addField("name")
				.match("span").withText("Legal name:")
				.matchNext("b").getText();

		//##CODE_START

		// uses a record trigger to notify the parser this is the end of the record and a new one will may be created.
		entity.addRecordTrigger().match("hr");

		//##CODE_END

		HtmlParser parser = new HtmlParser(entityList);

		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_002.html", "UTF-8");
		HtmlParserResult result = parser.parse(input).get("company");

		printResult(result);
		printAndValidate();
	}

	@Test
	public void example003MatchingMultiple() {
		HtmlEntityList entityList = new HtmlEntityList();

		HtmlEntitySettings entity = entityList.configureEntity("company");
		//##CODE_START
		entity.addField("id")
				.match("span").withText("company no:")
				.matchNext("span").getText();

		entity.addField("name")
				.match("span").withText("Legal name:")
				.matchNext("b").getText();

		entity.addRecordTrigger().match("hr");
		//##CODE_END
		HtmlParser parser = new HtmlParser(entityList);

		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_003.html", "UTF-8");
		HtmlParserResult result = parser.parse(input).get("company");

		printResult(result);
		printAndValidate();
	}

	@Test
	public void example003MatchingMultipleFixed() {
		HtmlEntityList entityList = new HtmlEntityList();

		HtmlEntitySettings entity = entityList.configureEntity("company");
		//##CODE_START
		entity.addPersistentField("id") //persistent fields don't lose their values until they are overwritten
				.match("span").withText("company no:")
				.matchNext("span").getText();
		//##CODE_END

		entity.addField("name").match("span").withText("Legal name:").matchNext("b").getText();
		entity.addRecordTrigger().match("hr");

		HtmlParser parser = new HtmlParser(entityList);

		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_003.html", "UTF-8");
		HtmlParserResult result = parser.parse(input).get("company");

		printResult(result);
		printAndValidate();
	}

	@Test
	public void example004MatchingEverythingOnTree() {
		//##CODE_START
		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_001.html", "UTF-8");

		// Parse an input HTML into a tree structure.
		HtmlElement root = HtmlParser.parseTree(input);

		// You can run query the nodes of the tree using the matching rule API used when defining fields of an entity.
		List<HtmlElement> unexpectedElements = root.query()
				.match("span").withText("company no:")
				.match("span")
				.getElements();

		// Which is useful to identify any issues in your matching rules
		for (HtmlElement e : unexpectedElements) {
			println(e);
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void example004MatchingEverythingOnTreeFixed() {
		FileProvider input = new FileProvider("documentation/tutorial/html/gotchas/example_001.html", "UTF-8");

		// Parse an input HTML into a tree structure.
		HtmlElement root = HtmlParser.parseTree(input);

		//##CODE_START
		List<HtmlElement> expectedElements = root.query()
				.match("span").withText("company no:")
				.matchNext("span").getElements();

		for (HtmlElement e : expectedElements) {
			println(e);
		}
		//##CODE_END
		printAndValidate();
	}
}

