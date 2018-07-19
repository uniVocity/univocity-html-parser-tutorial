/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

/**
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum FuelType {

	UNLEADED("UL"),
	DIESEL("D");

	public final String code;

	FuelType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}
}
