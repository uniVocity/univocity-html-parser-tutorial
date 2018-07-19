/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;

public class Address {

	public enum Type {
		BUSINESS('B'),
		MAILING('M');

		public final char code;

		Type(char code) {
			this.code = code;
		}
	}

	@Parsed
	@EnumOptions(customElement = "code")
	private Type type;

	@Parsed
	@Trim
	@LowerCase
	private String street1;

	@Parsed
	@LowerCase
	private String street2;

	@Parsed
	private String city;

	@Parsed
	private String state;

	@Parsed
	@UpperCase
	private String country;

	@Parsed(field = "zip")
	private long postCode;

	@Override
	public String toString() {
		return type + ": " + street1 + (street2 == null ? "" : " " + street2) + ", " + city + " - " + state + ", " + country + " " + postCode;
	}
}
