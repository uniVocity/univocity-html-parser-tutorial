/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;
import com.univocity.parsers.html.tutorial.beans.*;

import java.util.*;

public class Company {

	@Parsed(field = "company_id")
	public Long id;

	@Parsed
	public String name;

	@Linked(entity = "address", type = Address.class)
	public List<Address> addresses;

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("Company ").append(id).append(": ").append(name);

		if (addresses != null && addresses.size() > 0) {
			for (Address address : addresses) {
				out.append("\n * ").append(address);
			}
		}

		return out.toString();
	}
}
