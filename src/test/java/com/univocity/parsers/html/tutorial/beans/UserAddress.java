/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;

public class UserAddress {

	public enum Type {
		BUSINESS,
		MAILING,
		PERSONAL
	}

	@Parsed
	@UpperCase
	private Type type;

	@Parsed
	private String address;

	@Override
	public String toString() {
		return type + ": " + address;
	}
}
