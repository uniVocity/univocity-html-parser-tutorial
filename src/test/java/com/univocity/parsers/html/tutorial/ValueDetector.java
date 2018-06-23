/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
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
public class ValueDetector extends SimpleValueDetector {

	/**
	 * For the sake of the example, let's report all matched elements as well.
	 */
	private List<String> matchedReport = new ArrayList<>();

	@Override
	public void elementVisited(HtmlElement element, HtmlParsingContext context) {
		super.elementVisited(element, context); // super stores elements with a "value" class

		// it's easy to look for <input> and <select> elements as well.
		String tag = element.tagName();
		if ("input".equals(tag) || "select".equals(tag)) {
			visitedSet.add(element);
		}
	}

	@Override
	public void elementMatched(HtmlElement element, HtmlParsingContext context) {
		super.elementMatched(element, context); // super removes matched elements

		// let's report the matched elements
		reportMatchedElements(context);
	}

	private void reportMatchedElements(HtmlParsingContext context) {
		// Let's report the matched fields and the sequence of elements the parser found.
		String source = getDocumentSourceName(context);

		// The context object returns the sequence of elements matched for one or more fields.
		// This only works in the scope of the elementMatched() method.
		Map<String, HtmlElement[]> matchedFields = context.getMatchedElements();

		for (Map.Entry<String, HtmlElement[]> e : matchedFields.entrySet()) {
			// field names are used as the map keys.
			String fieldName = e.getKey();

			// append the file name parsed
			StringBuilder tmp = new StringBuilder(source.toString()).append(" - ");

			// then the field name
			tmp.append(fieldName).append(": ");

			// the sequence of elements matched for the given field come as values of the map.
			HtmlElement[] elementPath = e.getValue();

			// prints only the tag names of all matched elements, except the last one.
			int i = 0;
			for (; i < elementPath.length - 1; i++) {
				//print the matched HTML tag
				String tag = elementPath[i].tagName();
				tmp.append("<").append(tag).append(">");
			}
			// prints the outer HTML of the last element matched
			tmp.append(elementPath[i].toString());

			matchedReport.add(tmp.toString());
		}
	}

	public List<String> getMatchedElementReport() {
		return matchedReport;
	}
}