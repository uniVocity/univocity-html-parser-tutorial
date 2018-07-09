/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial;

import com.univocity.api.entity.html.*;
import com.univocity.api.entity.html.builders.*;
import com.univocity.api.io.*;
import com.univocity.api.net.*;
import com.univocity.parsers.common.*;
import com.univocity.parsers.html.tutorial.beans.*;
import org.testng.annotations.*;

import java.util.*;

public class TutorialLocal extends Tutorial {

	@Test
	public void example001ParseStructuredSearchResults() {

		// The first step is to create a list of entities. Each entity will hold their own rows of data.
		// Multiple entities can have multiple fields added to them.
		HtmlEntityList entityList = new HtmlEntityList();

		// Configure an entity named "items"
		HtmlEntitySettings items = entityList.configureEntity("items");

		//##CODE_START
		// Let's add a few fields to the "items" entity.
		// A field must have a name and matching rules associated with them
		items.addField("name")
				.match("span")         // match any span
				.classes("prodName") // with a class "prodName"
				.getText(); //if a <span> with the "prodName" class is found, get the text from inside the <span>.

		// Next we add a "description" field, now matching <span> elements with class "prodDesc"
		items.addField("description").match("span").classes("prodDesc").getText();

		// Next we create a "price" field, and we clean up the price to remove the unwanted dollar sign
		items.addField("price").match("span").classes("prodPrice").getText()
				.transform(price -> price.trim().replaceAll("\\$", ""));
		//##CODE_END

		// Just create a HTML parser instance for the given entity list.
		HtmlParser parser = new HtmlParser(entityList);

		// Now we can parse the input HTML file shown above. The FileProvider helps locating files in the classpath.
		FileProvider input = new FileProvider("documentation/tutorial/html/example_001.html", "UTF-8");

		// Call the parse method to parse the input and get the results.
		Results<HtmlParserResult> results = parser.parse(input);

		// The Results maps entity names to their corresponding data. Let's get the results of our "items" entity.
		HtmlParserResult itemResults = results.get("items");

		printResult(itemResults);

		printAndValidate();
	}

	private void configureCompany(HtmlEntityList entityList) {
		//##CODE_START

		// We just need company ID and name.
		HtmlEntitySettings company = entityList.configureEntity("company");

		company.addField("company_id")
				.match("div")  //look for a <div>
				.match("span") //inside or after that <div>, find a <span>
				.precededImmediatelyBy("span").withText("company no") //see if it is preceded by another <span>
				// containing the text "company no"
				.getText(); //returns the text of the last matched element in the path (a <span>).

		company.addField("name")
				.match("span") //look for any <span>
				.childOf("b") //the <span> must be a child of a <b> element
				.childOf("td").withText("legal name") //the <b> element must be a child of a
				// <td> element with text "legal name"
				.getText();  //returns the text of the last matched element in the path (a <span>).
		//##CODE_END
	}

	private void configureCompanyAddress(HtmlEntityList entityList) {
		//##CODE_START

		// The address entity has quite a bit of fields.
		HtmlEntitySettings address = entityList.configureEntity("address");

		// We want to know the ID of the company that "owns" each address.
		address.addPersistentField("company_id") //a "persistent" field retains its value across all rows.
				.match("div")
				.match("span")
				.precededBy("span").withText("company no")
				.getText();

		address.addField("type")
				.match("tr").withText("* address") //look, a wildcard
				.getText();

		// The HTML structure is the same for street 1 & 2. Different rules can get to the same content, as demonstrated
		address.addField("street1").match("td").withText("Street 1").match("span").getText();
		address.addField("street2").match("span").precededByText("Street 2").getText();

		// We created a method to configure the remaining fields as the structure is the same
		// and only the text preceding each value changes
		addField(address, "city", "City:");
		addField(address, "state", "State:");
		addField(address, "country", "Country:");
		addField(address, "zip", "Postal Code:");
		//##CODE_END
	}

	private void addField(FieldDefinition address, String fieldName, String textToMatch) {
		address.addField(fieldName).match("td").withText(textToMatch).match("span").getText();
	}

	@Test
	public void example002ParseBusinessAndAddresses() {
		// Let's setup two entities: one for companies another for their addresses.
		HtmlEntityList entityList = new HtmlEntityList();

		configureCompany(entityList);
		configureCompanyAddress(entityList);

		// Just create a HTML parser instance for the given entity list.
		HtmlParser parser = new HtmlParser(entityList);
		parseAndValidate(parser, "documentation/tutorial/html/example_002.html");
	}

	@Test
	public void example003UsingGroups() {
		HtmlEntityList entityList = new HtmlEntityList();

		configureCompany(entityList);
		configureAddressFieldsInGroup(entityList);

		HtmlParser parser = new HtmlParser(entityList);
		parseAndValidate(parser, "documentation/tutorial/html/example_002.html");
	}

	@Test
	public void example004LinkingResults() {
		HtmlEntityList entityList = new HtmlEntityList();

		//parse company and address as before
		configureCompany(entityList);
		configureAddressFieldsInGroup(entityList);

		HtmlParser parser = new HtmlParser(entityList);
		Results<HtmlParserResult> results = parse(parser, "documentation/tutorial/html/example_002.html");

		//##CODE_START
		// links rows of address to company based on values of fields with the same name - "company_id" in this example.
		results.link("company", "address");

		HtmlParserResult companies = results.get("company"); // each company record will now have linked results
		for (HtmlRecord company : companies.iterateRecords()) { // iterate over each company record
			String companyName = company.getString("name"); // using the record, we can get fields by name
			Long companyId = company.getLong("company_id"); // values can be read with the appropriate type

			println("Addresses of company: " + companyName + " (" + companyId + ")");
			Results<HtmlParserResult> linkedEntities = company.getLinkedEntityData(); // returns all results linked
			// the current "company" record

			HtmlParserResult companyAddresses = linkedEntities.get("address"); //get the addresses linked to the company.
			printResult(companyAddresses, false); //##REWRITE_AS// print company addresses ...
			println(); //##REWRITE_AS
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void example005JoiningResults() {
		HtmlEntityList entityList = new HtmlEntityList();

		//parse company and address as before
		configureCompany(entityList);
		configureAddressFieldsInGroup(entityList);

		HtmlParser parser = new HtmlParser(entityList);
		Results<HtmlParserResult> results = parse(parser, "documentation/tutorial/html/example_002.html");

		//##CODE_START
		// Joins rows of company and address based on values of fields with the same name - "company_id" in this example.
		HtmlParserResult joinedResult = results.join("company", "address");

		printResult(joinedResult);

		printAndValidate();
	}

	@Test
	public void example006ResultsToJavaBeans() {
		HtmlEntityList entityList = new HtmlEntityList();

		//parse company and address as before
		configureCompany(entityList);
		configureAddressFieldsInGroup(entityList);

		HtmlParser parser = new HtmlParser(entityList);
		Results<HtmlParserResult> results = parse(parser, "documentation/tutorial/html/example_002.html");

		//##CODE_START
		HtmlParserResult companies = results.get("company");
		HtmlParserResult addresses = results.get("address");

		companies.link(addresses); //links addresses to companies

		List<Company> companyList = companies.getBeans(Company.class);

		for (Company company : companyList) {
			println(company);
		}
		//##CODE_END

		printAndValidate();
	}

	private void configureAddressFieldsInGroup(HtmlEntityList entityList) {
		//##CODE_START
		HtmlEntitySettings address = entityList.configureEntity("address");

		address.addPersistentField("company_id")
				.match("div").match("span").precededBy("span").withText("company no")
				.getText();

		// business address group starts at <tr> with text "Business address"
		// the group ends when the <tr> with "Mailing address" is reached.
		Group businessAdressGroup = address.newGroup()
				.startAt("tr").withExactTextMatchCase("Business address")
				.endAt("tr").withTextMatchCase("Mailing address");

		// Any rows produced from within this group will have field "type" set to "B" (for business addresses)
		businessAdressGroup.addField("type", "B");

		// Add fields to the group directly so their matching rules execute only when the group is entered.
		addAddressFieldsToGroup(businessAdressGroup);

		// Mailing address group starts from <tr> with text "Business address"
		// Here we identify the group end using the closing tag </tr>, i.e. the group ends when the <tr> that
		// follows the "Mailing address" heading is closed.
		Group mailingAddressGroup = address.newGroup()
				.startAt("tr").withExactTextMatchCase("Mailing address")
				.endAtClosing("tr").precededBy("tr").withExactTextMatchCase("Mailing address");

		//Any rows produced from within this group will have field "type" set to "M" (for mailing addresses)
		mailingAddressGroup.addField("type", "M");

		// Now we can add the address fields to this group too.
		addAddressFieldsToGroup(mailingAddressGroup);
		//##CODE_END
	}

	private void addAddressFieldsToGroup(Group group) {
		addField(group, "street1", "Street 1:");
		addField(group, "street2", "Street 2:");
		addField(group, "city", "City:");
		addField(group, "state", "State:");
		addField(group, "country", "Country:");
		addField(group, "zip", "Postal Code:");
	}


	@DataProvider
	public Object[][] nestingProvider() {
		return new Object[][]{
				{Nesting.LINK},
				{Nesting.JOIN},
		};
	}

	private HtmlEntityList example007ConfigureLinkFollowing() {
		//##CODE_START
		HtmlEntityList entityList = new HtmlEntityList();

		HtmlEntitySettings user = entityList.configureEntity("User");
		user.addField("name").match("a").getText();

		// The 'profileUrl' field has a link to the next page with user details. We want to follow that link.
		HtmlLinkFollower profileFollower = user.addField("profileUrl")
				.match("a")
				.getAttribute("href")
				.followLink();

		// We just add fields to the follower object. As the link follower comes from the "User" entity, the fields added
		// here end up in the "User" entity.
		getValueFromLabel(profileFollower, "username", "Username");
		getValueFromLabel(profileFollower, "age", "Age");
		getValueFromLabel(profileFollower, "location", "Location");
		getValueFromLabel(profileFollower, "created", "Profile created on");
		//##CODE_END

		// The Nesting setting controls how results from link followers are handled when putting together the final row
		profileFollower.setNesting(Nesting.JOIN);

		return entityList;
	}

	private void getValueFromLabel(HtmlLinkFollower follower, String fieldName, String key) {
		follower.addField(fieldName)
				.match("td").classes("value") //gets the text of a table cell with class "value"
				.precededImmediatelyBy("td").classes("label").withText(key) // if it is preceded by a cell with class "label" and a given text
				.getOwnText();
	}

	@Test(dataProvider = "nestingProvider")
	public void example007LinkFollowing(Nesting nesting) {

		HtmlEntityList entityList = example007ConfigureLinkFollowing();
		HtmlLinkFollower profileFollower = entityList.getEntity("User").getRemoteFollowers().get("profileUrl");

		// The Nesting setting controls how results from link followers are handled when putting together the final row  //##REWRITE_AS
		profileFollower.setNesting(nesting);

		FileProvider input = new FileProvider("documentation/tutorial/html/example_007/list.html", "UTF-8");
		HtmlParserResult users = new HtmlParser(entityList).parse(input).get("User");

		if (nesting.links()) {
			printResultAndLinkedResults(users);
		} else {
			printResult(users);
		}
		printAndValidate(nesting);
	}

	private HtmlEntityList configureLinkFollowingWithNewEntities() {
		HtmlEntityList entityList = example007ConfigureLinkFollowing();

		//##CODE_START
		// let's get the link follower created earlier back
		HtmlLinkFollower profileFollower = entityList.getEntity("User").getRemoteFollowers().get("profileUrl");

		// now we want to follow the link that points to a page with user addresses
		HtmlLinkFollower addressFollower = profileFollower.addField("addressUrl").match("a")
				.withExactText("Choose another")
				.getAttribute("href")
				.followLink();

		// Create a new "Address" entity. The results will linked to the parent `profileFollower`.
		HtmlEntitySettings address = addressFollower.getEntityList().configureEntity("Address");

		// Gets the content of all cells under the "Address" column
		address.addField("address")
				.match("td")
				.underHeader("td").withExactText("Address")
				.getText();

		// Finds a checked radio button and returns the text in the header of the corresponding column
		address.addField("type")
				.match("input").attribute("type", "radio") //matches radio buttons
				.attribute("checked") //matches only checked radio buttons
				.getHeadingText(); //gets the text of the first row of the table, at the same column

		// Now we add a field to the follower itself, it will be added to main "User" entity and will
		// store the number of addresses associated with each user.
		addressFollower.addField("address_count")
				.match("table") //just match a <table> and give the node to you so you can work with the DOM
				.getElement(new HtmlElementTransformation() { //you can use a lambda instead
					@Override
					public String transform(HtmlElement table) {
						//you must work with the matched element to return a String.
						//To get the number of addresses, we can query all <tr> elements of the current <table>.
						List<HtmlElement> rows = table.query("tr");

						//Subtract the first row from the total as it's a heading row. The result must be a String.
						return String.valueOf(rows.size() - 1);
					}
				});

		// we don't want to have a "addressUrl" field in the user records.
		// REPLACE_JOIN will replace the column of the link that was followed with the results obtained.
		addressFollower.setNesting(Nesting.REPLACE_JOIN);

		//##CODE_END

		return entityList;
	}

	@Test
	public void example008LinkFollowingWithNewEntities() {
		HtmlEntityList entityList = configureLinkFollowingWithNewEntities();

		//##CODE_START

		//parse and print the results.
		FileProvider input = new FileProvider("documentation/tutorial/html/example_008/list.html", "UTF-8");

		HtmlParserResult users = new HtmlParser(entityList).parse(input).get("User");
		for (HtmlRecord user : users.iterateRecords()) {
			println(Arrays.toString(user.getValues())); //the values collected by all followers are joined in a single row

			// As we configured the parser to join rows, the linked "Address" entity is available from the "User" record
			HtmlParserResult addressResults = user.getLinkedEntityData().get("Address");

			for (HtmlRecord addr : addressResults.iterateRecords()) {
				println("  * " + Arrays.toString(addr.getValues()));
			}
			println();
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void example008LinkFollowingResultTraversal() {
		HtmlEntityList entityList = configureLinkFollowingWithNewEntities();

		//##CODE_START

		//parse and print the results.
		FileProvider input = new FileProvider("documentation/tutorial/html/example_008/list.html", "UTF-8");

		HtmlParserResult users = new HtmlParser(entityList).parse(input).get("User");
		for (HtmlRecord user : users.iterateRecords()) {
			println(Arrays.toString(user.getValues())); //the values collected by all followers are joined in a single row

			//get the records collected by the first link follower
			HtmlParserResult profileResults = user.getLinkedFieldData();

			for (HtmlRecord profile : profileResults.iterateRecords()) {
				//the profile details were already joined with the parent row, so we ignore that data here.

				//we want the addresses collected by the second link follower. They are linked to each profile record.
				HtmlParserResult addressResults = profile.getLinkedEntityData().get("Address");

				for (HtmlRecord addr : addressResults.iterateRecords()) {
					println("  * " + Arrays.toString(addr.getValues()));
				}
			}
			println();
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void example008LinkFollowingResultToBeans() {
		HtmlEntityList entityList = configureLinkFollowingWithNewEntities();

		//##CODE_START

		//parse and print the results.
		FileProvider input = new FileProvider("documentation/tutorial/html/example_008/list.html", "UTF-8");

		HtmlParserResult users = new HtmlParser(entityList).parse(input).get("User");

		List<User> userList = users.getBeans(User.class);
		for (User user : userList) {
			println(user);
			println();
		}
		//##CODE_END
		printAndValidate();

	}

	@Test
	public void example009BasicPagination() {
		//##CODE_START
		HtmlEntityList entityList = new HtmlEntityList();

		//Configure the paginator
		HtmlPaginator paginator = entityList.getPaginator();
		paginator.setNextPage()
				.match("span").id("nextPage")
				.match("a").getAttribute("href"); // captures the link that goes to the next page

		//##CODE_END
		printResult(collectDataFromPaginatedResults(entityList));

		printAndValidate();
	}

	private HtmlParserResult collectDataFromPaginatedResults(HtmlEntityList entityList) {
		//##CODE_START

		// Configure the entity that collects search results:
		HtmlEntitySettings search = entityList.configureEntity("search");

		PartialPath resultPath = search.newPath().match("div").classes("result");

		resultPath.addField("title").match("a").getText();
		resultPath.addField("link").match("a").getAttribute("href");

		// Give the parser the first page to process.
		FileProvider firstPage = new FileProvider("documentation/tutorial/html/example_009/page1.html", "UTF-8");

		// It will visit all pages of the search results
		HtmlParserResult searchResults = new HtmlParser(entityList).parse(firstPage).get("search");

		//##CODE_END
		return searchResults;
	}

	@Test
	public void example009BasicPaginationWithLimit() {

		HtmlEntityList entityList = new HtmlEntityList();

		//Configure the paginator
		HtmlPaginator paginator = entityList.getPaginator();
		paginator.setNextPage()
				.match("span").id("nextPage")
				.match("a").getAttribute("href"); // captures the link that goes to the next page

		//##CODE_START
		paginator.setFollowCount(1);

		//##CODE_END
		printResult(collectDataFromPaginatedResults(entityList));

		printAndValidate();
	}

	@Test
	public void example010ParserListenerSimple() {
		HtmlEntityList entityList = example007ConfigureLinkFollowing();

		//##CODE_START

		// Let's get the "User" entity associated with the profile URL
		HtmlEntitySettings user = entityList
				.getEntity("User")
				.getRemoteFollower("profileUrl")
				.getEntity("User");

		// Assign our custom value detector to it.
		SimpleValueDetector valueDetector = new SimpleValueDetector();
		user.setListener(valueDetector);

		// Parse the users. Our custom listener will be called when the parser runs.
		FileProvider input = new FileProvider("documentation/tutorial/html/example_007/list.html", "UTF-8");
		HtmlParserResult users = new HtmlParser(entityList).parse(input).get("User");

		// Let's see if there is any value we missed from the profile pages
		List<String> missed = valueDetector.getUnmatchedElementReport();
		if (missed.isEmpty()) {
			println("All possible values have been captured by the parser");
		} else {
			println("Values *not* captured by the parser:");
			for (String entry : missed) {
				println(entry);
			}
		}
		//##CODE_END
		printAndValidate();
	}

	@Test
	public void example010ParserListener() {
		HtmlEntityList entityList = example007ConfigureLinkFollowing();

		// Let's get the "User" entity associated with the profile URL
		HtmlEntitySettings user = entityList
				.getEntity("User")
				.getRemoteFollower("profileUrl")
				.getEntity("User");

		// Assign our custom value detector to it.
		ValueDetector valueDetector = new ValueDetector();
		user.setListener(valueDetector);

		// Parse the users. Our custom listener will be called when the parser runs.
		FileProvider input = new FileProvider("documentation/tutorial/html/example_007/list.html", "UTF-8");
		HtmlParserResult users = new HtmlParser(entityList).parse(input).get("User");

		// Let's see if there is any value we missed from the profile pages
		List<String> missed = valueDetector.getUnmatchedElementReport();
		if (missed.isEmpty()) {
			println("All possible values have been captured by the parser");
		} else {
			println("Values *not* captured by the parser:");
			for (String entry : missed) {
				println(entry);
			}
		}

		//##CODE_START

		// We've also collected the elements matched, taking advantage of the information available from the
		// context object the parser sends to the listener.
		println("\nElements matched by the parser:");
		List<String> matched = valueDetector.getMatchedElementReport();
		for (String entry : matched) {
			println(entry);
		}

		//##CODE_END
		printAndValidate();
	}

	public static void main2(String... args) {
		UrlReaderProvider url = new UrlReaderProvider("http://ratings.fide.com/card.phtml?event={EVENT}");
		url.getRequest().setUrlParameter("EVENT", 2821109);

		HtmlElement doc = HtmlParser.parseTree(url);

		String rating = doc.query()
				.match("small").withText("std.")
				.match("br").getFollowingText()
				.getValue();

		System.out.println(rating);
	}

	public static void main(String... args) {
		UrlReaderProvider url = new UrlReaderProvider("http://www.chess.org.il/Players/Player.aspx?Id={PLAYER_ID}");
		url.getRequest().setUrlParameter("PLAYER_ID", 25022);

		HtmlEntityList entities = new HtmlEntityList();
		HtmlEntitySettings player = entities.configureEntity("player");
		player.addField("id").match("b").withExactText("מספר שחקן").getFollowingText().transform(s -> s.replaceAll(": ", ""));
		player.addField("name").match("h1").followedImmediatelyBy("b").withExactText("מספר שחקן").getText();
		player.addField("date_of_birth").match("b").withExactText("תאריך לידה:").getFollowingText();
		player.addField("fide_id").matchFirst("a").attribute("href", "http://ratings.fide.com/card.phtml?event=*").getText();

		HtmlLinkFollower playerCard = player.addField("fide_card_url").matchFirst("a").attribute("href", "http://ratings.fide.com/card.phtml?event=*").getAttribute("href").followLink();
		playerCard.addField("rating_std").match("small").withText("std.").match("br").getFollowingText();
		playerCard.addField("rating_rapid").match("small").withExactText("rapid").match("br").getFollowingText();
		playerCard.addField("rating_blitz").match("small").withExactText("blitz").match("br").getFollowingText();
		playerCard.setNesting(Nesting.REPLACE_JOIN);

		HtmlEntitySettings ratings = playerCard.addEntity("ratings");
		configureRatingsBetween(ratings, "World Rank", "National Rank ISR", "world");
		configureRatingsBetween(ratings, "National Rank ISR", "Continent Rank Europe", "country");
		configureRatingsBetween(ratings, "Continent Rank Europe", "Rating Chart", "continent");

		Results<HtmlParserResult> results = new HtmlParser(entities).parse(url);
		HtmlParserResult playerData = results.get("player");
		String[] playerFields = playerData.getHeaders();

		for(HtmlRecord playerRecord : playerData.iterateRecords()){
			for(int i = 0; i < playerFields.length; i++){
				System.out.print(playerFields[i] + ": " + playerRecord.getString(playerFields[i]) +"; ");
			}
			System.out.println();

			HtmlParserResult ratingData = playerRecord.getLinkedEntityData().get("ratings");
			for(HtmlRecord ratingRecord : ratingData.iterateRecords()){
				System.out.print(" * " + ratingRecord.getString("rank_type") + ": ");
				System.out.println(ratingRecord.fillFieldMap(new LinkedHashMap<>(), "all_players", "active_players", "female", "u16", "female_u16"));
			}
		}
	}

	private static void configureRatingsBetween(HtmlEntitySettings ratings, String startingHeader, String endingHeader, String rankType) {
		Group group = ratings.newGroup()
				.startAt("table").match("b").withExactText(startingHeader)
				.endAt("b").withExactText(endingHeader);

		group.addField("rank_type", rankType);

		group.addField("all_players").match("tr").withText("World (all", "National (all", "Rank (all").match("td", 2).getText();
		group.addField("active_players").match("tr").followedImmediatelyBy("tr").withText("Female (active players):").match("td", 2).getText();
		group.addField("female").match("tr").withText("Female (active players):").match("td", 2).getText();
		group.addField("u16").match("tr").withText("U-16 Rank (active players):").match("td", 2).getText();
		group.addField("female_u16").match("tr").withText("Female U-16 Rank (active players):").match("td", 2).getText();
	}
}
