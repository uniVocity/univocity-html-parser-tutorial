/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;
import com.univocity.api.io.*;
import org.testng.annotations.*;

import java.util.*;

public class MatchingRules extends Tutorial {

	@Test
	public void matchingRules() {
		//##CODE_START
		FileProvider input = new FileProvider("documentation/tutorial/html/matching_rules/input.html", "UTF-8");

		// Parse an input HTML into a tree structure.
		HtmlElement root = HtmlParser.parseTree(input);
		List<HtmlElement> expectedElements = root.query()
				.match("tr").match("span").match("b")
				.getElements();

		for (HtmlElement e : expectedElements) {
			println(e);
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void matchingRulesWithConstraints() {

		FileProvider input = new FileProvider("documentation/tutorial/html/matching_rules/input.html", "UTF-8");

		// Parse an input HTML into a tree structure.
		HtmlElement root = HtmlParser.parseTree(input);

		//##CODE_START
		List<HtmlElement> expectedElements = root.query()
				.match("tr").under("tr").withText("* a bold")
				.match("span").match("b").withText("wow")
				.getElements();
		//##CODE_END

		for (HtmlElement e : expectedElements) {
			println(e);
		}
		printAndValidate();
	}
}
