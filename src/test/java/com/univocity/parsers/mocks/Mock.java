package com.univocity.parsers.mocks;

import com.github.tomakehurst.wiremock.client.*;

import java.io.*;
import java.net.*;

/**
 * Base wiremock mock configuration for local testing. All pages to be returned by the
 * mock server must be configured on the {@link #setup()} method.
 */
public abstract class Mock {

	private final String baseUrl;
	private WireMock mock;

	/**
	 * Associates this mock with the given port
	 * @param port the local port where this mock will be accessible from
	 */
	public Mock(int port) {
		baseUrl = "http://localhost:" + port;
		mock = new WireMock(port);
	}

	/**
	 * Adds a mock mapping to the mock server.
	 * @param mappingBuilder the wiremock mapping.
	 */
	protected void stubFor(MappingBuilder mappingBuilder) {
		mock.register(mappingBuilder);
	}

	/**
	 * Returns the base URL pointing to the localhost at the port number given in the constructor of this class.
	 *
	 * @return the "server" URL, i.e {@code "http://localhost:" + port}
	 */
	public final String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Configures the mock endpoints. Subclasses will typically map URLs to files stored locally.
	 */
	protected abstract void setup();
}
