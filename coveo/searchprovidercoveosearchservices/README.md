# Coveo SAP Commerce indexer push extension

## Installation

1. Copy the `searchprovidercoveosearchservices` extension to the `hybris/bin/custom` directory of your project.

1. Add `<extension name='searchprovidercoveosearchservices'/>` in your `localextensions.xml` file. 
The Coveo Push API client library for Java is installed as part of the extension. 
See the `.jar` files in the `hybris/bin/custom/searchprovidercoveosearchservices/lib` directory.

1. Go to the `hybris/bin/platform/` folder.

1. Set up the Ant environment by running the script: `. ./setantenv.sh` (Linux) or `./setantenv.bat` (Windows).

1. Rebuild your installation with `ant clean all`.

1. Start your SAP Commerce installation by running the `./hybrisserver.sh` script.

1. Open the Hybris Administration console (HAC) at https://localhost:9002/platform/update.
   You can also navigate to the Update page by clicking **Platform** → **Update** in the top menu.

1. Find and select the checkboxes for:

   - `searchprovidercoveosearchservices` (required to create the new search provider)

   - `Update running system`

   - `Localize types`

1. At the top of the page, click the **Update** button.

> **_&#9432;_** &nbsp; The extension creates new data types in the database, but it does not import any data.

## Semi-automatic configuration

In the extension code base, if you navigate to `resources/examples`, you'll find the `electronics-search-configuration-example.impex` file.
This is an example impex of how you can perform the SAP Commerce Cloud configuration in a programmatic way.
This can be used as a template to fill with your data and configure your project faster.

You can also manually configure every source and your indexes via the Backoffice.
Perform the following steps to do so.


## Coveo setup

### Step 1: Create a source in your Coveo organization

1. Log in to the [Coveo Administration Console](https://platform.cloud.coveo.com/login).

1. Create the Catalog sources that you'll be pushing your data to. See [Create a Catalog source](https://docs.coveo.com/en/n8of0593).

   The guidelines for creating sources are:

   - Each combination of language, currency, and country existing in your data should have a separate Coveo Catalog source to push to.

   - Any availability data you have should also have it's own dedicated Catalog source.

1. Note down the [**Stream API URL**](https://docs.coveo.com/en/n8of0593#stream-api-url) value for your source.

### Step 2: Create an API key

1. Create one API key that you'll use for all your sources. See [Manage API Keys](https://docs.coveo.com/en/1718/manage-an-organization/manage-api-keys). The key should have the privileges detailed here [Catalog Source Privileges](https://docs.coveo.com/en/n8of0593/coveo-for-commerce/create-a-catalog-source#required-privileges).

   > **_&#9432;_** &nbsp; Remember to note down the secret in a safe place.

## SAP Commerce setup

### Step 1: Create Coveo sources

For each source you created in the Coveo Administration Console, you should perform the following steps in the SAP Backoffice.

1. In the [SAP Backoffice Administration Cockpit](https://help.sap.com/docs/SAP_COMMERCE/5c9ea0c629214e42b727bf08800d8dfa/8c16979286691014abe6f41434c7614a.html), go to **System** → **API** → **Destinations** → **Coveo Source**

1. Click the plus sign (**+**) above the search results.

1. In the modal window that appears, enter a unique ID that represents your Catalog source.
   It's recommended to use a human-readable value to assist with identifying a specific source.

1. Click the **Finish** button.

1. Select the newly created item from the search listing.

1. On the **General** tab:

   1. Fill in the **Name** field.

   1. In the **Object type** menu, specify the type that matches your Coveo source:

      - **AVAILABILITY**. This type is for the availability source. See [Source configuration approaches for availability channel](https://docs.coveo.com/en/nbga0384) and [Catalog availability data](https://docs.coveo.com/en/m53g0124).

      - **PRODUCTANDVARIANT**. This type is for the the products and variations of the catalog. See [Catalog product data](https://docs.coveo.com/en/m53g7119) and [Catalog variant data and product groupings](https://docs.coveo.com/en/m53g0506).

      > **_&#9432;_** &nbsp; When selecting an object type of **PRODUCTANDVARIANT**, Language, Currency, and Country are mandatory for the system to determine the correct source for your product data.

   1. If the object type is **PRODUCTANDVARIANT**, specify the combination of the **Language**, **Currency**, and **Country** which this source represents.

1. Click the **Save** button.

### Step 2: Create a consumed destination

Create a consumed destination:

1. Go to **System** → **API** → **Destinations** → **Consumed Destinations**

1. Click the plus sign (**+**) above the search results.

1. In the modal window that appears:

   - **ID**. Enter a unique ID that represents your Catalog source.

   - **URL**. Paste the **Stream API URL** that you copied from the Coveo Catalog source.

   - **Active**. Verify that the **Active** checkbox is selected.

   - **Destination Target**. Click the field and select **Default_Template**.

   > **_&#9432;_** &nbsp; The **Destination Target** value is ignored by the Coveo extension, but is required to create the destination

1. Click the **Finish** button.

### Step 3: Create a credential for the consumed destination

> **_&#9432;_** &nbsp; If you have a number of sources, you only need to create one OAuth Credential and attach it to many Consumed Destinations.

1. Click the destination you created and switch to the **Destination Configuration** tab.

1. Double-click in the **Credential** field to open a dropdown menu and select **Create Credential** → **Consumed OAuth Credential**.

1. Fill in the following fields:

   - **ID**. Enter a unique ID for a new credential. For example, `CoveoCredential`.

   - **Client ID**. Enter a client ID for a new credential. For example, `CoveoClientID`.

1. Click the **Finish** button. You see the **Destination Configuration** tab again.

1. Double-click in the **Credential** field to open the credential settings.

1. In the **Client Secret** section, there are **Password** and **Verify password** fields.
   In both fields, paste the Coveo API key that you created earlier.

1. Click the **Save** button to save and close the credential settings.

1. Click the **Save** button to save the destination settings.

### Step 4: Assign the consumed destination to the Coveo source

1. Return to **System** → **API** → **Destinations** → **Coveo Source**.

1. Click your Coveo source.

1. On the **General** tab, find the **Consumed Destination** field.

1. Specify the consumed destination that you created earlier.

1. Click the **Save** button.

### Step 5: Create a search provider

Now that you've mapped the Coveo sources to the SAP Commerce API destinations, you need to configure the search index to push the data to Coveo.

1. Go to the **System** → **Search and Navigation** → **Search Provider Configurations** page.

1. Next to the plus sign, click the dropdown menu and select **Coveo Search Provider Configuration**.

1. Fill in the following fields:

   - **Identifier**. Enter a unique identifier for a new search provider, for example, `CoveoSearchProviderID`.

   - **Name**. Enter a name for a new search provider, for example, `Coveo Search Provider Configuration`.

   - **Coveo Source**. Select all the Coveo sources that you created.

1. Click the **Finish** button.

### Step 6: Create the index configuration

1. Go to **System** → **Search and Navigation** → **Index Configurations**.

1. Next to the plus sign, click the dropdown menu and select **CoveoSnIndexConfiguration**.

1. Fill in the following fields:

   - **Identifier**. Enter a unique identifier for a new configuration, for example, `CoveoIndexID`.

   - **Name**. Enter a name for a new search provider, for example, `Coveo Index Configuration`.

   - **Search Provider Configuration**. Select the Coveo Search Provider Configuration created in the previous step.

1. Click the **Next** button to proceed to the next step, **Index Types**.

   Skip this step for now and click **Next** again.

1. In the **Session** step, select all the **Languages**, **Currencies**, and **Countries** that are relevant for your data.

1. Click the **Finish** button.

### Step 7: Create the ServiceLayerJobs

Create the ServiceLayerJobs for the future cron jobs, one for full indexing and one for incremental indexing.

1. Go to **System** → **Types**.

1. In the search bar, type in `ServicelayerJob` and search.

1. In the search results, click the **ServicelayerJob**.

   The properties of the ServicelayerJob are displayed on the page.

1. In the upper part of the properties, click the search icon that's titled **Search by type**.

1. Click the plus sign (**+**) above the search listing.

1. In the modal window that appears:

   - **Spring ID**. Paste `fullCoveoSnIndexerJob`.

   - **Code**. Paste `fullCoveoSnIndexerJob`.

1. Click the **Finish** button.

1. Perform the same steps to create another ServicelayerJob for which specify `incrementalCoveoSnIndexerJob` for the **Code** and the **Spring ID** attributes.

### Step 8: Create the Index types

Create one Index type for full indexing and one for incremental indexing.

1. Go to **System** → **Search and Navigation** → **Index Types**.

1. Click the plus sign (**+**) above the search results.

1. In the modal window that appears, in the **Essential** step, fill in the following fields:

   - **Identifier**. Enter a unique identifier for a new type, for example, `CoveoFullIndexTypeID`.

   - **Name**. Enter a name for a new type, for example, `Coveo Full Index Type Configuration`.

   - **Composed Type**. Specify the type that matches a Coveo Object type for the source you'll use this to push data to. For example, if this index type will be used to push `availability` objects, then you may want to select the `Warehouse` Composed type. This will be dependent on how your data is structured.

   - **Index Configuration**. Select the Coveo Index Configuration created in the previous steps.

1. The next steps can be configured later, so you may skip them for now. Click **Next** to proceed to the **Cron Jobs** step.

1. In the **Cron Jobs** step, click the plus sign (**+**) at the end of the dropdown menu.

1. Select **Full Indexer Cronjob**.

1. Fill in the following fields:

   - **Code**. Enter a unique code for the cron job, for example, `coveoFullIndexType`.

   - **Job Definition**. Select the `fullCoveoSnIndexerJob` system type.
     The chosen type should correspond to the type of cron job you're creating.

   - **Indexer Item Source**. Click this field to create a **Flexible Search Indexer Item Source**.

     In the modal window that appears, fill in the **Query** field. It must be the [FlexibleSearch](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8bc399c186691014b8fce25e96614547.html) query that will be used to retrieve the Composed Type. For example, if you selected Warehouse for your `availability` object, the following query would retrieve all warehouses for the electronics site:

     ```sql
     `SELECT {w:PK} FROM {Warehouse as w JOIN BaseStore2WarehouseRel as rel ON {w:PK} = {rel:target} JOIN BaseStore AS bs ON {rel:source} = {bs:PK} } WHERE {bs:uid}='electronics'`
     ```

     For product indexing, the following query would retrieve all approved products in the catalog.

     ```sql
     `SELECT {p:pk} FROM {Product AS p LEFT JOIN ArticleApprovalStatus AS a ON {p:approvalStatus} = {a:pk}} WHERE {a:code} = 'approved'`
     ```

1. Click the **Finish** button for every modal window that has been opened.

1. Select the newly created Index type.

1. On the **General** tab, configure the **Identity Provider** as `snIdentityProvider` and link the catalog you want to index in the **Catalogs** field.

1. Perform the same steps to create an Index type for incremental indexing, but adjust them as follows:

   - in the **Essential** step, update the **Identifier** and **Name** fields to reflect the incremental indexing type.

   - in the **Create Cron Jobs** step, select **Incremental Indexer Cronjob**. Use `coveoIncrementalIndexType` for the **Code** attribute and `incrementalCoveoSnIndexerJob` for the **Job Definition** attribute. Update the FlexibleSearch query to match the incremental indexing requirements.

### Step 9: Create the objectType field

In both created Index types, create a field that will be used to specify the Coveo object type.

1. Click the required Index type, either `coveoFullIndexType` or `coveoIncrementalIndexType`.

1. Switch to the **Fields** tab.

1. For Coveo Push solution, the **Code**, **Name**, and **DocumentId** fields are mandatory.

1. Click in the **Fields** menu, to create a field specific for Coveo.

   > **_&#9432;_** &nbsp; You can create as many additional fields as you need to push to Coveo. In this example, you'll create one field, `objectType`.

1. In the modal window that appears, fill in the following fields:

   - **Identifier**. Enter `objectType`.

   - **Name**. Enter `Object Type`.

   - **Field Type**. Select `TEXT`.

1. Click the **Next** button.

1. In the **Indexer** step, paste `coveoObjectTypeSnIndexerValueProvider` in the **Value Provider** field.

1. Click the **Finish** button.

1. In your SAP Commerce Cloud project, open the `hybris/bin/custom/searchprovidercoveosearchservices/project.properties` file.

1. There will be three properties, each maps one Coveo Source object with one or more Composed Index types.

   For example, if you created a `Warehouse` Composed index type for the `availability` object, add it to the `coveo.availability.typecodes` property.

   ```java
   coveo.availability.typecodes=Warehouse
   coveo.product.typecodes=Product
   coveo.variant.typecodes=VariantProduct
   ```

### Step 10: Map the catalog fields

For both created Index types, map catalog fields with the Coveo fields.
Do the mapping only for the fields that you want to push to Coveo.

1. Navigate to the **System** → **Search and Navigation** → **Index Types** page.

1. In the list of index types, click the required index type.

1. Switch to the **Fields** tab.

1. Examine the **Identifier** column. The values in it should match the field name that was set within the source in the [Coveo Administration Console](https://platform.cloud.coveo.com/login).
     For example, for the SAP field `name`, you need to create the mapping `%[name]` in the Coveo Administration Console.

     See [Manage source mappings](https://docs.coveo.com/en/1640/) and [Mapping rule syntax reference](https://docs.coveo.com/en/1839/).

1. If you need to create a new field, do so the same way you created the `objectType` field in the previous step.

### Step 11: Run the indexers

Once you've configured your system correctly, you can run your indexers.

1. Navigate to the **System** → **Search and Navigation** → **Index Types page**.

1. In the list of index types, click the required index type.

1. Switch to the **Cron Jobs** tab.

1. Click the required cronjob.

1. Make sure that the cronjob is enabled, see the **True** checkbox in the **Enabled** section.

1. Click the **Save** button to save and close the cronjob settings.

1. While having the index type open, switch to the **Administration** tab.

1. Check that the **Stores** field is set to the store you want to index.

1. Above the index tabs, click the **Run indexer** button.

1. In the modal window that appears, click the dropdown menu and select the cronjob that you’ve just enabled.

1. Click the **Run** button.

You can track the indexing progress by clicking the **Processes** button at the top of the Administration Cockpit.
