/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;

/**
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Reviewer {

	@Parsed(field = "reviewer_id")
	public 	String id;

	@Parsed(field = "reviewer_name")
	public String name;

	@Override
	public String toString() {
		return name + '-' + id;
	}
}
