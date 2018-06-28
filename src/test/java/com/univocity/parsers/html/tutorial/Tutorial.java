/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;
import com.univocity.api.io.*;
import com.univocity.parsers.common.*;
import com.univocity.parsers.fixed.*;
import com.univocity.test.*;

import java.util.*;

public abstract class Tutorial extends OutputTester{

	/**
	 * Execute the main method to request a trial license.
	 */
	public static void main(String ... args){
		com.univocity.parsers.html.Main.runLicenseManager();
	}

	protected Tutorial() {
		super("documentation/tutorial/expected_results", "UTF-8");
	}

	protected Results<HtmlParserResult> parse(HtmlParser parser, String file) {
		// Now we can parse the input HTML file shown above. The FileProvider helps locating files in the classpath.
		FileProvider input = new FileProvider(file, "UTF-8");

		// Call the parse method to parse the input and get the results.
		Results<HtmlParserResult> results = parser.parse(input);

		return results;
	}

	protected void parseAndValidate(HtmlParser parser, String file, String... entities) {
		Results<HtmlParserResult> results = parse(parser, file);
		printResults("", results, entities);

		//updateExpectedOutput();
		printAndValidate();
	}

	protected void printResults(String indentation, Results<HtmlParserResult> results, String... entities) {
		printResults(indentation, results, true, entities);
	}

	protected void printResults(String indentation, Results<HtmlParserResult> results, boolean displayEntityName, String... entities) {
		Collection<String> entityNames = entities.length == 0 ? results.keySet() : Arrays.asList(entities);
		for (String entity : entityNames) {
			HtmlParserResult entityResults = results.get(entity);
			if (displayEntityName) {
				println(indentation + "[ " + entity + " ]");
			}
			printResult(indentation, entityResults, true);
			println();
		}
	}

	protected void printResultAndLinkedResults(HtmlParserResult result) {
		printResultAndLinkedResults(result, true);
	}

	protected void printResultAndLinkedResults(HtmlParserResult result, boolean printHeaders) {
		printResultAndLinkedResults("", result, printHeaders);
	}

	protected void printResultAndLinkedResults(String indentation, HtmlParserResult result, boolean printHeaders) {
		FixedWidthWriter writer = createFormattedWriter(result, printHeaders);

		if (printHeaders) {
			printHeaders(indentation.replace('|', '+'), writer);
		}

		String nextIndentationLevel = indentation.isEmpty() ? "  | " : "    " + indentation;

		for (HtmlRecord record : result.iterateRecords()) {
			String formattedRow = writer.writeRowToString(record.getValues());
			println(indentation + formattedRow);

			if (record.hasLinkedData()) {
				Results<HtmlParserResult> results = record.getLinkedEntityData();
				if (!results.isEmpty()) {
					for (String entityName : results.keySet()) {
						printResultAndLinkedResults(nextIndentationLevel, results.get(entityName), printHeaders);
					}
					println();
				}
			}
		}
	}

	protected void printResult(HtmlParserResult result) {
		printResult(result, true);
	}

	protected void printHeaders(String indentation, FixedWidthWriter writer) {
		// Let's print this out
		String formattedHeaders = writer.writeHeadersToString();
		println(indentation + formattedHeaders);
	}

	protected void printResult(HtmlParserResult result, boolean printHeaders) {
		printResult("", result, printHeaders);
	}

	protected void printResult(String indentation, HtmlParserResult result, boolean printHeaders) {
		// The results have headers, rows and other information
		List<String[]> rows = result.getRows();

		FixedWidthWriter writer = createFormattedWriter(result, printHeaders);

		if (printHeaders) {
			printHeaders(indentation, writer);
		}

		for (String[] row : rows) {
			String formattedRow = writer.writeRowToString(row);
			println(indentation + formattedRow);
		}
	}

	protected FixedWidthWriter createFormattedWriter(HtmlParserResult result, boolean printHeaders) {
		String[] headers = result.getHeaders();
		List<String[]> rows = result.getRows();

		int[] lengths = new int[headers.length];
		if (printHeaders) {
			for (int i = 0; i < headers.length; i++) {
				lengths[i] = headers[i].length();
			}
		}
		for (int i = 0; i < headers.length; i++) {
			for (String[] row : rows) {
				int length = row[i] == null ? 0 : row[i].length();
				if (lengths[i] < length) {
					lengths[i] = length;
				}
			}
		}
		for (int i = 0; i < headers.length; i++) {
			lengths[i] = lengths[i] + 2;
		}

		FixedWidthFields fields = new FixedWidthFields();
		for (int i = 0; i < headers.length; i++) {
			fields.addField(headers[i], lengths[i], ' ');
		}

		FixedWidthWriterSettings settings = new FixedWidthWriterSettings(fields);
		settings.setUseDefaultPaddingForHeaders(true);
		settings.getFormat().setPadding('_');
		settings.setEmptyValue("\"\"");
		settings.setNullValue("");
		FixedWidthWriter writer = new FixedWidthWriter(settings);

		return writer;
	}
}
