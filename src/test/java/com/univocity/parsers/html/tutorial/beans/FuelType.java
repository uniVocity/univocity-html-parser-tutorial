/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum FuelType {

	UNLEADED("UL"),
	DIESEL("D");

	public final String acronym;

	FuelType(String acronym) {
		this.acronym = acronym;
	}

	@Override
	public String toString() {
		return acronym;
	}
}
