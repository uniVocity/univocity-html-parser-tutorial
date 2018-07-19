/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;
import com.univocity.parsers.annotations.Format;

import java.text.*;
import java.util.*;

public class User {

	@Parsed
	private String name;

	@Parsed
	private String username;

	@Parsed
	private int age;

	@Parsed(field = "address_count")
	private int addressCount;

	@Parsed
	private String location;

	@Parsed
	@Format(formats = "dd/M/yyyy")
	private java.util.Date created;

	@Linked(entity = "Address", type = UserAddress.class)
	private List<UserAddress> addresses;

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(name)
				.append(" (").append(username).append(")")
				.append(", age ").append(age)
				.append(", location=").append(location)
				.append(" - Created on ").append(new SimpleDateFormat("yyyy-MMM-dd").format(created));

		out.append("\n").append(addressCount).append(" addresses");

		for (UserAddress address : addresses) {
			out.append("\n * ").append(address);
		}
		return out.toString();
	}
}
