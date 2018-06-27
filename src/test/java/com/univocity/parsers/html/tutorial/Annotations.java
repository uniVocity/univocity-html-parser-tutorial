/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;
import com.univocity.api.entity.html.builders.*;
import com.univocity.api.io.*;
import com.univocity.parsers.common.*;
import com.univocity.parsers.html.tutorial.beans.*;
import org.testng.annotations.*;

import java.util.*;

public class Annotations extends Tutorial {


	public Results<HtmlParserResult> parsePetrolPriceTable() {
		//##CODE_START
		HtmlEntityList htmlEntityList = new HtmlEntityList();

		// captures all days of the week listed
		HtmlEntitySettings dayOfWeek = htmlEntityList.configureEntity("dayOfWeek");
		dayOfWeek.addField("day") // any <th> with at least 3 characters of text in them in the first <thead> of the document.
				.matchFirst("thead").match("th").withText("???").getText();

		// reads all pretrol station names
		HtmlEntitySettings petrolStation = htmlEntityList.configureEntity("petrolStation");
		petrolStation.addField("petrol_station_name") //any <td> somewhere after a <th> with text "Retailer"
				.match("td").precededBy("th").withText("Retailer").getText();

		// collects all reviewer names and their IDs
		HtmlEntitySettings reviewer = htmlEntityList.configureEntity("reviewer");
		reviewer.addField("reviewer_name") //first <span> inside a <td> of a <tfoot>
				.match("tfoot").match("td").matchFirst("span").getText();
		reviewer.addField("reviewer_id") //last <span> inside a <td> of a <tfoot>, removes values within '(' and ')'
				.match("tfoot").match("td").matchLast("span").getText().transform(s -> s.substring(1, s.length() - 1));

		// collects fuel prices
		HtmlEntitySettings fuel = htmlEntityList.configureEntity("fuel");
		// creates a reusable path: matches every <tr> of any <table> that is contained in an outer <table>
		PartialPath fuelPricePath = fuel.newPath().match("table").match("table").match("tr");
		fuelPricePath.addField("fuel_type") // fields are added to the partial path
				.matchFirst("td").getText(); // continue matching from where the path ends. Gets the text from the first <td>
		fuelPricePath.addField("price")
				.matchLast("td").getText(); // The fuel price is in the the last <td> of the <tr> matched by the partial path.

		// Now we add fields to our "fuel" that already exist in the other entities created above.
		// We will use these fields to join records of each entity ('dayOfWeek', 'petrolStation', 'reviewer' and 'fuel')

		fuel.addPersistentField("day") // captures the day of week of each fuel price listed.
				//matches a <table> contained by an outer <table>. Then goes up to the first <th> that is above the inner <table> and grabs its text.
				.match("table").match("table").upToHeader("th").getText();

		fuel.addPersistentField("petrol_station_name")// captures the petrol station name of each fuel price listed.
				// match a <td> that contains a <table>, then collects the text in the row above this <td>, from the same column.
				.match("td").parentOf("table").getTextAbove();

		fuel.addPersistentField("reviewer_id")// collects the reviewer ID under each fuel price listed.
				// finds a <table> contained by a <table>. From the inner <table> look down to a <td> inside a <tfoot>
				// that <td> should be in the same column of the inner <table>.
				.match("table").match("table").downToFooter("td").containedBy("tfoot")
				// From that <td>, gets the last <span> then collect the text between '(' and ')'
				.matchLast("span").getText().transform(s -> s.substring(1, s.length() - 1));

		//That's it, let's parse the HTML and get the results to see what data we get.
		FileProvider inputFile = new FileProvider("documentation/tutorial/html/annotations/linkedEntityTest.html");

		Results<HtmlParserResult> result = new HtmlParser(htmlEntityList).parse(inputFile);

		//##CODE_END
		return result;
	}

	@Test
	public void parseInput() {
		Results<HtmlParserResult> result = parsePetrolPriceTable();
		printResults("", result);
		printAndValidate();
	}

	public Results<HtmlParserResult> getLinkedResults() {
		Results<HtmlParserResult> result = parsePetrolPriceTable();

		//##CODE_START

		// fuel records are linked to each day of week
		// from each "dayOfWeek" record we can now get the corresponding "fuel" records
		result.link("dayOfWeek", "fuel");

		// petrol station and reviewer records are linked to each fuel record
		// from each "fuel" record we can now obtain the corresponding "petrolStation" and "reviewer" record.
		result.link("fuel", "petrolStation", "reviewer");

		//##CODE_END
		return result;
	}

	@Test
	public void parseIntoNestedBeans() {
		Results<HtmlParserResult> result = getLinkedResults();

		//##CODE_START
		// As each "dayOfWeek" record has "fuel" records, we can obtain a list of `DailyPriceList` beans
		List<DailyPriceList> pricesPerDay = result.get("dayOfWeek").getBeans(DailyPriceList.class);

		// Now we can print out the price details of each day.
		for (DailyPriceList priceList : pricesPerDay) {
			println("* Petrol prices on " + priceList.dayOfWeek);
			for (PriceDetails petrolStation : priceList.priceDetails) {
				print("\t" + petrolStation.name + " -> " + petrolStation.price);
				println(" | Reviewed by: " + petrolStation.reviewer.name + " (" + petrolStation.reviewer.id + ")");
			}
			println("----------------------");
		}
		//##CODE_END
		printAndValidate();
	}


	@Test
	public void parseIntoNestedPriceDetails() {
		Results<HtmlParserResult> result = getLinkedResults();

		//##CODE_START
		// Let's convert the records of entity "fuel" into `DailyPriceList` beans
		List<PriceDetails> prices = result.get("fuel").getBeans(PriceDetails.class);

		// Now we can print out all price details
		for (PriceDetails petrolStation : prices) {
			print(petrolStation.name + " -> " + petrolStation.price);
			println(" | Reviewed by: " + petrolStation.reviewer.name + " (" + petrolStation.reviewer.id + ")");
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void parseIntoNestedBeanMap() {
		Results<HtmlParserResult> result = getLinkedResults();

		//##CODE_START
		// Here, each `DailyPriceMap` instance has the day of the week and a Map<PetrolStation, List<Price>>
		List<DailyPriceMap> pricesPerDay = result.get("dayOfWeek").getBeans(DailyPriceMap.class);
		for (DailyPriceMap priceList : pricesPerDay) {
			println("* Petrol prices on " + priceList.dayOfWeek);
			for (Map.Entry<PetrolStation, List<Price>> e : priceList.pricesPerStation.entrySet()) {
				PetrolStation petrolStation = e.getKey();
				List<Price> prices = e.getValue();

				println("\t" + petrolStation.name + " | Reviewed by: " + petrolStation.reviewer.name + "(" + petrolStation.reviewer.id + ")");
				for (Price price : prices) {
					println("\t\tPrice of " + price.fuelType.name() + ": $" + price.price);
				}
			}
			println("----------------------");
		}
		//##CODE_END
		printAndValidate();
	}
}
