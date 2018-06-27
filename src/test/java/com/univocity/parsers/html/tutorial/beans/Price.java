/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;

import java.math.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Price {

	@Parsed(field = "fuel_type")
	public FuelType fuelType;

	@Replace(expression = "\\$", replacement = "")
	@Parsed
	public BigDecimal price;

	@Override
	public String toString() {
		return fuelType.name() + " = $" + price;
	}
}
