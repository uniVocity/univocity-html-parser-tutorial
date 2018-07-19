/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.parsers.mocks;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MemoryStickSearchMock extends Mock {

	public MemoryStickSearchMock(int port) {
		super(port);
	}

	public void setup() {

		for (int i = 0; i < 10; i++) {
			stubFor(any(urlEqualTo("/sch/i.html?_from=R40&_sacat=0&_nkw=memory+stick&_pgn=" + i + "&_skc=50&rt=nc"))
					.willReturn(aResponse()
							.withStatus(200)
							.withBody("page " + i)));
		}
	}

}
