# Coveo SAP Commerce Connector
Integrate your SAP Commerce Cloud project with the Coveo Platform using the SAP Commerce Connector. 
The connector is a set of extensions tailored specifically for SAP Commerce 2211.
This repository houses 3 versions. v3 of the connector can be found on the main branch. Support for earlier SAP Commerce versions can be found in the branches v1 and v2

| Branch | SAP Commerce Cloud support | Status |
|--------|-----------------------------|--------|
| `main` | 2211-jdk21 | Active development |
| `v3` | 2211 | Active development |
| `v2` | 2105, 2205 | Maintained, with no new features.<br><br>This is because the SAP Commerce Cloud versions [2105](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2105&locale=en-US) and [2205](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2205&locale=en-US) are out of maintenance. |
| `v1` | 2011 | Deprecated.<br><br>This is because the SAP Commerce Cloud version [2011](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2011) is out of maintenance. |

## Available extensions
Each extension offers unique functionalities to enhance your integration experience. This repository includes

* **coveocc**. Generate a JWT to enhance your users' search experience with dedicated search results based on the users profile.

* **coveopushclient**. A client to interact with the Coveo Stream API

* **searchprovidercoveosearchservices**. Seamlessly push your product and availability data to your Coveo sources.

## Getting started
Navigate to the README.md file within each extension's directory. 
There, you'll discover instructions on how to leverage the capabilities of each extension and integrate them into your SAP Commerce Cloud environment.

## Contributions
We welcome contributions from the community to improve and expand the capabilities of the Coveo SAP Commerce Connector. Whether you've found a bug or have a feature request, submit your feedback at https://connect.coveo.com/s/ideas.