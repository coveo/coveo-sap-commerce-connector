The version number format is `vMajor.Minor.Patch` where:

- `Major` is incremented for the major version when the connector is adjusted to support a new version of SAP Commerce Cloud.

- `Minor` is incremented when a breaking change is introduced.

- `Patch` is incremented for each non-breaking change.

Next to each version number, you can see if the version is required or recommended.

* <span>$${\color{red}required}$$</span> urges upgrade to avoid potential issues with catalog data in Coveo.

* <span>$${\color{green}recommended}$$</span> suggests upgrading as soon as possible to benefit from new features.

# Change log

## v4.0.0 (2026-01-19) <sub>$${\color{red}required only for upgrade to SAP Commerce 2211-jdk21}$$</sub>

- Implemented changes required for the upgrade to Java 21 and Spring 6

## v3.4.8 (2025-12-16) <sub>$${\color{green}recommended}$$</sub>

- Removed a number of unused classes and code from the `coveopushclient` extension
- Increased test covarage for the `coveopushclient` and `searchprovidercoveosearchservices` extensions
- Removed redundent test from the coveocc `extension`

## v3.4.7 (2025-7-16) <sub>$${\color{green}recommended}$$</sub>

- Resolves an issue where pushing a large amount of data during an incremental update could result in the loss of some of thoes updates

## v3.4.6 (2025-6-09) <sub>$${\color{green}recommended}$$</sub>

- Increased the document queue size to 50MB to improve index performance

## v3.4.5 (2025-6-09) <sub>$${\color{green}recommended}$$</sub>

- Add example of OOTB indexer retry configuration in the electronics index impex example file. This example if for optimising the SAP Commerce retry logic and is seperate from the Coveo Client retry configuration.

- Add new index value provider to allow the configuration of a stock threshold to decide if a sku is available. By configuring the property `coveo.availability.lowstock.threshold`, the value provider will only mark this sku as available in that location if the stock level is greater that this configuration. This new value provider is in addition to the pre-existing availability provider which marks a sku as available even if it's not in stock.

- Push `ec_product_id` by default. If a code attribute is configured on the index as per the example provided in the electronics index example impex file, the connector will set this value in the standard `ec_product_id` field. If a mapping for `ec_product_id` already exists for the source in Coveo, this mapping will take precedence.

## v3.4.4 (2025-3-10) <sub>$${\color{green}recommended}$$</sub>

- Upgrade of the `resilience4j` library to v2.2.0

## v3.4.3 (2025-3-05) <sub>$${\color{green}recommended}$$</sub>

- Add new conditions to the retry logic that handles the following: 

   - HTTP error codes of 408, 503, and 504

   - a scenario when the server responds with the `HTTP/1.1 header parser received no bytes` message

## v3.4.2 (2025-1-20) <sub>$${\color{green}recommended}$$</sub>

- Resolve an issue where the index count was wrong when processing country-specific products.

## v3.4.1 (2025-1-20) <sub>$${\color{green}recommended}$$</sub>

- Add the ability to configure the Coveo Stream API client retry count and interval duration.

- Fix the issue where manual cancellation of the full index job would corrupt the index. Previously if an indexing job was aborted, or failed, the index would be pushed with all the documents that had been processed to that point. This could result in some of the index being removed. Now if there is a failure, or the job is stoped, nothing is pushed so that the index remains in the original state.

## v3.4.0 (2025-1-03) <sub>$${\color{red}required}$$</sub>

- Add example on how to configure index batch size and concurrency.

- Implement changes required to upgraded connector for use with SAP Commerce 2211.32.

## v3.3.1 (2024-11-12) <sub>$${\color{green}recommended}$$</sub>

- Add support for regional language ISO codes. Previously, only ISO 3166-1 alpha-2 (XX) was supported; now, ISO 3166-2 is also supported for defining regions within the country (XX_yy).

- Add support for pushing products to country-specific sources. If a country isn't specified for a product, the product will be pushed to all sources.

## v3.3.0 (2024-11-12) <sub>$${\color{green}recommended}$$</sub>

- Remove the `searchhub` parameter from the Coveo search token endpoint.

## v3.2.0 (2024-11-5) <sub>$${\color{green}recommended}$$</sub>

- Add improved logging for indexing process.

- Change the `coveoProductCategoryHierarchy` value provider to format the data in line with Coveo best practice. This removed spaces between the demimiter and the values.

- For attributes of a document that have no value, no value is pushed to the index instead of an empty string.

## v3.1.4 (2024-08-27) <sub>$${\color{red}required}$$</sub>

- Fix a bug in a unit test that used data that wasn't available without an internal extension.

## v3.1.3 (2024-08-09) <sub>$${\color{green}recommended}$$</sub>

- Add the [Coveo Push API client](https://github.com/coveo/push-api-client.java) 2.6.1 directly to the connector. The original client is being sunsetted, and this change protects against any future removal of the library.

- Add the ServicelayerJobs to both default indexing processes, full and incremental. This removed the need to explixitly import this data when configuring a new index configuration


## v3.1.2 (2024-05-01) <sub>$${\color{green}recommended}$$</sub>

- Upgrade the used version of the [Coveo Push API client](https://github.com/coveo/push-api-client.java) to 2.6.1.

## v3.1.1 (2024-04-25) <sub>$${\color{green}recommended}$$</sub>

- Add the `User-Agent` header to requests to provide information on version usage.

- Add dictionary field support. Dictionary fields are added as a Java Map using Gson to serialize to a json object for the request.


## 3.1.0 (2024-03-14) <sub>$${\color{green}recommended}$$</sub>

- Add multi-source support, allowing you to push data to:

   - a product and availability source.
   
   - different sources based on locales (language, country, and currency).

## 3.0.0 (2024-03-07) <sub>$${\color{red}required}$$</sub>

- Add support for SAP Commerce Cloud 2211 and later.

- Add a single source push capability.