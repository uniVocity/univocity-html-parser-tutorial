/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.html.tutorial.beans;

import com.univocity.parsers.annotations.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class PetrolStation implements Comparable<PetrolStation> {

	@Parsed(field = "petrol_station_name")
	public String name;

	@Linked
	public Reviewer reviewer;

	public PetrolStation(){
	}

	@Override
	public String toString() {
		return name + " - Reviewer: " + reviewer + "";
	}

	// we're going to use PetrolStation as the keys of a TreeMap, so we implemented
	// the `Comparable` interface
	@Override
	public int compareTo(PetrolStation o) {
		return this.toString().compareTo(o.toString());
	}
}
