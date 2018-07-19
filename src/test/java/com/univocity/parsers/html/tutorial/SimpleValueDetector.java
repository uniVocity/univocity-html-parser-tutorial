/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;

import java.io.*;
import java.util.*;

/**
 * This class is a simple example of how to use a {@link HtmlParserListener} to observe the parser as it walks through
 * the HTML structure searching for elements to be matched.
 *
 * The purpose of this implementation is to detect data points in a given HTML that should have been captured. This can
 * be very useful to identify whether a website had some of its structure change. Suppose the HTML you parse always
 * displays the relevant values you need in elements with the CSS class "value".
 *
 * Every time the parser visits a {@link HtmlElement} that has the CSS class "value", we store it in a set
 *
 * Every time the parser matches a path to a field of your entity, assuming your matching rules are looking for
 * a {@link HtmlElement} with the CSS class "value", we remove the matched {@link HtmlElement} from the set.
 *
 * At the end of the parsing process, any {@link HtmlElement} still in the set represents a data point that has not
 * been matched by the parser. A report is generated to print out the source file/url and the tags that were not matched.
 *
 */
public class SimpleValueDetector extends HtmlParserListener {

	/**
	 * Keeps track of all possible relevant HTML elements that could contain data.
	 */
	protected Set<HtmlElement> visitedSet = new HashSet<>();

	/**
	 * The report is generated at the end of the parsing process, listing any data point that might have been missed by
	 * the parser.
	 */
	private List<String> unmatchedReport = new ArrayList<>();

	@Override
	public void parsingStarted(HtmlParsingContext context) {
		visitedSet.clear();
	}

	@Override
	public void elementVisited(HtmlElement element, HtmlParsingContext context) {
		// the parser will visit every element of the input HTML and call your elementVisited() implementation.
		// let's collect any possible elements of interest.
		if (element.classes().contains("value")) {
			visitedSet.add(element);
		}
	}

	@Override
	public void elementMatched(HtmlElement element, HtmlParsingContext context) {
		// when an element is matched, it means the parser matched a path to a field and collected its value.
		// We can remove the element from the set because our code only matches elements with CSS class="value".
		visitedSet.remove(element);
	}

	@Override
	public void parsingEnded(HtmlParsingContext context) {
		String source = getDocumentSourceName(context);
		for (HtmlElement unmatchedElement : visitedSet) {
			unmatchedReport.add(source + ": " + unmatchedElement.toString());
		}
	}

	protected String getDocumentSourceName(HtmlParsingContext context) {
		// gets the source URL or File.
		Object source = context.documentSource();
		if (source instanceof File) {
			source = ((File) source).getName();
		}
		return source.toString();
	}

	public List<String> getUnmatchedElementReport() {
		return unmatchedReport;
	}
}