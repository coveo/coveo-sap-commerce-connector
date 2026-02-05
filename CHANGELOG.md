The version number format is `vMajor.Minor.Patch` where:

- `Major` is incremented for the major version when the connector is adjusted to support a new version of SAP Commerce Cloud.

- `Minor` is incremented when a breaking change is introduced.

- `Patch` is incremented for each non-breaking change.

Next to each version number, you can see if the version is required or recommended.

* <span>$${\color{red}required}$$</span> urges upgrade to avoid potential issues with catalog data in Coveo.

* <span>$${\color{green}recommended}$$</span> suggests upgrading as soon as possible to benefit from new features.

# Change log

## v2.1.10 (2026-1-22) <sub>$${\color{red}required}$$</sub>

- Resolved an issue where missing mandatory data in an item can abort the entire index job. Now the item will be skipped and the remaining items will be indexed correctly

## v2.1.9 (2025-07-15) <sub>$${\color{green}recommended}$$</sub>

- Resolves an issue where pushing a large amount of data during an incremental update could result in the loss of some of thoes updates
- Increased the document queue size to 50MB to improve index performance

## v2.1.8 (2025-3-06) <sub>$${\color{green}recommended}$$</sub>

- Add new conditions to the retry logic that handles the following:
   - HTTP error codes of 408, 503, and 504
   - a scenario when the server responds with the `HTTP/1.1 header parser received no bytes` message

## 2.1.7 (2025-2-19) <sub>$${\color{green}recommended}$$</sub>

- Resolve an issue where the index count was wrong when processing country-specific products.

## v2.1.6 (2025-1-20) <sub>$${\color{green}recommended}$$</sub>

- Add the [Coveo Push API client](https://github.com/coveo/push-api-client.java) 2.6.1 directly to the connector. The original client is being sunsetted, and this change protects against any future removal of the library.


## v2.1.5 (2024-12-20) <sub>$${\color{green}recommended}$$</sub>

- Add support for regional language ISO codes. Previously, only ISO 3166-1 alpha-2 (XX) was supported; now, ISO 3166-2 is also supported for defining regions within the country (XX_yy).

## v2.1.4 (2024-08-28) <sub>$${\color{green}recommended}$$</sub>

- Corrected failing test code

## v2.1.3 (2024-08-9) <sub>$${\color{green}recommended}$$</sub>

- Added data to extension update to make initial configration less manual.

## v2.1.2 (2024-05-01) <sub>$${\color{red}required}$$</sub>

- Upgraded the Coveo SDK version

## v2.1.1 (2024-04-25) <sub>$${\color{green}recommended}$$</sub>

- Add the `User-Agent` header to requests to provide information on version usage.


## v2.1.2 (2024-05-01) <sub>$${\color{green}recommended}$$</sub>

- Upgrade the used version of the [Coveo Push API client](https://github.com/coveo/push-api-client.java) to 2.6.1.

## v2.1.1 (2024-04-25) <sub>$${\color{green}recommended}$$</sub>

- Add the `User-Agent` header to requests to provide information on version usage.

- Add dictionary field support. Dictionary fields are added as a Java Map using Gson to serialize to a json object for the request.


## 2.1.0 (2024-03-14) <sub>$${\color{green}recommended}$$</sub>

- Add multi-source support, allowing you to push data to:

   - a product and availability source.

   - different sources based on locales (language, country, and currency).

## 2.0.0 (2024-03-08) <sub>$${\color{red}required}$$</sub>

- Add support for SAP Commerce Cloud 2105 and 2205.

- Add a single source push capability.
