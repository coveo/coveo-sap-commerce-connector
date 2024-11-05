The version number format is `vMajor.Minor.Patch` where:

- `Major` is incremented for the major version when the connector is adjusted to support a new version of SAP Commerce Cloud.

- `Minor` is incremented when a breaking change is introduced.

- `Patch` is incremented for each non-breaking change.

Next to each version number, you can see if the version is required or recommended.

* <span>$${\color{red}required}$$</span> urges upgrade to avoid potential issues with catalog data in Coveo.

* <span>$${\color{green}recommended}$$</span> suggests upgrading as soon as possible to benefit from new features.

# Change log

## v3.2.0 (2024-11-5) <sub>$${\color{green}recommended}$$</sub>

- Added improved logging for monitoring and indexation progress

- Changed category hierarchy index value provider to format the data in line with Coveo best practice

- For attributes of a document that have no value, no value is pushed to the index rather than a an empty string

## v3.1.4 (2024-08-27) <sub>$${\color{red}required}$$</sub>

- Fix a bug in a unit test that used data that wasn't available without an internal extension.


## v3.1.3 (2024-08-09) <sub>$${\color{green}recommended}$$</sub>

- Add the [Coveo Push API client](https://github.com/coveo/push-api-client.java) 2.6.1 directly to the connector.

- Add the ServicelayerJobs to both default indexing processes, full and incremental.


## v3.1.2 (2024-05-01) <sub>$${\color{green}recommended}$$</sub>

- Upgrade the used version of the [Coveo Push API client](https://github.com/coveo/push-api-client.java) to 2.6.1.

## v3.1.1 (2024-04-25) <sub>$${\color{green}recommended}$$</sub>

- Add the `User-Agent` header to requests.

- Add dictionary field support.


## 3.1.0 (2024-03-14) <sub>$${\color{green}recommended}$$</sub>

- Add multi-source support, allowing you to push data to:

   - a product and availability source.
   
   - different sources based on locales (language, country, and currency).

## 3.0.0 (2024-03-07) <sub>$${\color{red}required}$$</sub>

- Add support for SAP Commerce Cloud 2211 and later.

- Add a single source push capability.