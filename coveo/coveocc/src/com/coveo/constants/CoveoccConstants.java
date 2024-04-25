/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.constants;

/**
 * Global class for all coveocc constants. You can add global constants for your extension into this class.
 */
@SuppressWarnings({ "deprecation", "squid:CallToDeprecatedMethod" })
public final class CoveoccConstants extends GeneratedCoveoccConstants
{
	public static final String EXTENSIONNAME = "coveocc"; //NOSONAR

	private CoveoccConstants()
	{
		//empty to avoid instantiating this constant class
	}

	public static final String ORG_URL ="coveocc.org.url";
	public static final String ORG_API_KEY ="coveocc.org.api.key";

	public static final int SEARCH_TOKEN_MAX_AGE_SECONDS=86400;

	public static final String COVEOCC_USER_AGENT = "CoSAPTokenConnector/v3";

	public static final String COVEOCC_USER_AGENT_PROPERTY = "coveocc.header.useragent";
}
