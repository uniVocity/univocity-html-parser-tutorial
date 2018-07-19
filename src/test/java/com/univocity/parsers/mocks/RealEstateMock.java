/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.mocks;

import java.io.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RealEstateMock extends Mock {

	public RealEstateMock(int port) {
		super(port);
	}

	public void setup() {
		for(int i = 1; i <= 2; i++){
			stubFor(any(urlEqualTo("/Property/Residential?search=&location=22008&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=" + i))
					.willReturn(aResponse()
							.withStatus(200)
							.withBodyFile("real_estate/results_000" + i + ".html")));
		}

		File dir = new File("src/test/resources/__files/real_estate");
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				continue;
			}
			if(!file.getName().startsWith("result")){
				String name = file.getName().replaceAll("\\.html", "");

				stubFor(any(urlMatching("/Property/([0-9]*)/"+name+"/.*"))
						.willReturn(aResponse()
								.withStatus(200)
								.withBodyFile("real_estate/" + file.getName())));
			}
		}

		dir = new File("src/test/resources/__files/real_estate/Images/Icons");
		for(File file : dir.listFiles()){
			String name = file.getName();
			stubFor(any(urlMatching(".*/Images/Icons/"+name))
					.willReturn(aResponse()
							.withStatus(200)
							.withBodyFile("real_estate/Images/Icons/" + name)));
		}
	}
}