/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.constants;

/**
 * Global class for all Searchprovidercoveosearchservices constants. You can add global constants for your extension into this class.
 */
public final class SearchprovidercoveosearchservicesConstants extends GeneratedSearchprovidercoveosearchservicesConstants
{
	public static final String EXTENSIONNAME = "searchprovidercoveosearchservices";

	private SearchprovidercoveosearchservicesConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension

	public static final String COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY = "CoveoProductRebuildStreamService";
	public static final String COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY = "CoveoProductUpdateStreamsService";
	public static final String COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY = "CoveoAvailabilityRebuildStreamService";
	public static final String COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY = "CoveoAvailabilityUpdateStreamsService";

	public static final String SUPPORTED_AVAILABILITY_TYPES_CODE = "coveo.availability.typecodes";
	public static final String SUPPORTED_PRODUCT_TYPES_CODE = "coveo.product.typecodes";
	public static final String SUPPORTED_VARIANT_TYPES_CODE = "coveo.variant.typecodes";

	public static final String COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE = "coveoDocumentId";
	public static final String COVEO_URI_TYPE_INDEX_ATTRIBUTE = "coveoClickableUri";

	public static final String COSAP_CONNECTOR_USER_AGENT = "CoSAPConnector/v3";
	public static final String COSAP_CONNECTOR_USER_AGENT_PROPERTY = "coveo.header.useragent";

	public static final String COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE = "coveo.document.stream.log.interval.percentage";
	public static final int COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT = 20;
}
