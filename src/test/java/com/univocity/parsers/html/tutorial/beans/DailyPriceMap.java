/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;

import java.util.*;

/**
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class DailyPriceMap {

	@Parsed(field = "day")
	public String dayOfWeek;

	@Group(key = PetrolStation.class, container = TreeMap.class)
	@Linked(entity = "fuel", type = Price.class, container = ArrayList.class)
	public Map<PetrolStation, List<Price>> pricesPerStation;

}
