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
public class PriceDetails {

	// Maps records with headers [fuel_type, price, petrol_station_name]
	// each having 0 or 1 linked records with headers [reviewer_id, reviewer_name]
	// These headers are defined in the @Parsed annotations of classes `Reviewer` and `Price`

	/**
	 * For each record with data for a `PriceDetails` object, we expect to obtain 0 or 1
	 * linked records from an entity named "reviewer". The linked record will be converted
	 * to an instance the `Reviewer` class
	 *
	 * As the attribute name matches the entity name, 'entity = "reviewer"' could have been omitted
	 */
	@Linked(entity = "reviewer")
	public Reviewer reviewer;

	/**
	 * The nested `Price` attribute has fields "fuel_type" and "price".
	 * Each record with data for a `PriceDetails` object is expected to have
	 * fields named "fuel_type" and "price", which will be used to populate the
	 * attributes of an instance of `Price`
	 */
	@Nested
	public Price price;

	/**
	 * Each record with data for a `PriceDetails` object is expected to also have
	 * a field named "petrol_station_name", whose value will be used to set this
	 * "name" attribute
	 */
	@Parsed(field = "petrol_station_name")
	public String name;

}
