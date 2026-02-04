# Coveo SAP Commerce Connector
Integrate your SAP Commerce Cloud project with the Coveo Platform using the SAP Commerce Connector. 
The connector is a set of extensions tailored specifically for SAP Commerce 2211.
This repository houses 4 versions. v4 of the connector can be found on the main branch. Support for earlier SAP Commerce versions can be found in the branches v1, v2 and v3. The CHANGELOG for each version can be found in it's respective branch.

| Branch | SAP Commerce Cloud support | Status |
|--------|-----------------------------|--------|
| [`v4`](https://github.com/coveo/coveo-sap-commerce-connector/tree/v4) | 2211-jdk21 | Active development - SAP Commerce Cloud version [2211-jdk21.1](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/75d4c3895cb346008545900bffe851ce/bcb7ba6cf2f84ab89b136d496b188f17.html?locale=en-US)|
| [`v3`](https://github.com/coveo/coveo-sap-commerce-connector/tree/v3) | 2211 | Active development - SAP Commerce Cloud version [2211](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2211&locale=en-US)|
| [`v2`](https://github.com/coveo/coveo-sap-commerce-connector/tree/v2) | 2105, 2205 | Maintained, with no new features.<br><br>This is because the SAP Commerce Cloud versions [2105](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2105&locale=en-US) and [2205](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2205&locale=en-US) are out of maintenance. |
| [`v1`](https://github.com/coveo/coveo-sap-commerce-connector/tree/v1) | 2011 | Deprecated.<br><br>This is because the SAP Commerce Cloud version [2011](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD?version=v2011) is out of maintenance. |

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
