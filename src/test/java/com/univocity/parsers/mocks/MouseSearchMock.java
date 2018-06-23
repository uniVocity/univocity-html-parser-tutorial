/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.mocks;

import com.github.tomakehurst.wiremock.client.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MouseSearchMock extends Mock{

	static final String MOUSE_SEARCH = "/s/field-keywords=mouse";

	public MouseSearchMock(int port) {
		super(port);
	}

	public void setup() {
		for (int i = 1; i <= 5; i++) {
			UrlMatchingStrategy url;
			if (i == 1) {
				url = urlEqualTo(MOUSE_SEARCH);
			} else {
				url = urlPathMatching("/s/ref=sr_pg_" + i + "/*");
			}

			stubFor(any(url)
					.willReturn(aResponse()
							.withStatus(200)
							.withBodyFile("mouse_search/mouse_" + i + ".html")));
		}
	}

	public String urlForMouseSearch() {
		return getBaseUrl() + MOUSE_SEARCH;
	}
}
