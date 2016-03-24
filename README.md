### Silver Fabric Global Variable Provider

### Introduction
-----------------------
TIBCO Silver Fabric *Variable Provider* concepts are covered in detail in 
[TIBCO Silver Fabric Developer's Guide].

This project defines a TIBCO Silver Fabric global variable provider. This global variable provider
extends `com.datasynapse.fabric.broker.userartifact.variable.AbstractDynamicVariableProvider` class from
TIBCO Silver Fabric SDK for Java  and provides external variables to Silver Fabric 
Manager in two separate contexts:

	* At the time of a Silver Fabric Component activation 
	* At the time of an archive deployment using Silver Fabric Continuous Deployment API

The global variables provided by this global variable provider are assumed to be stored in a relational database. 
This global variable provider was developed and tested with MySQL database using a MySQL JDBC driver. it may need to be adapted to work with other 
relational databases. This global  variable provider only *reads* variables from the global  variable database. You will need a different 
application (for example a web application) to add, update or delete relevant variables in the global variable  database.

### Building Global Variable Provider
------------------------------------------------
This project builds a Zip file under `target` directory. The Zip file contains the content
you need to install the global  variable provider on the Silver Fabric Manager. 

1. Download `SilverFabricSDK.jar` from Silver Fabric Manager UI under `Admin/Downloads`
2. Copy `SilverFabricSDK.jar` under project `lib` folder
3. Build *Maven* install target to create a Zip file under the target directory.

### Download MySQL JDBC Driver
--------------------------------------------
[Download MySQL Connector/J] latest MySQL JDBC driver and copy it to Silver Fabric Manager host under some staging folder.

### Create Database Tables
--------------------------------------------
Create MySQL database tables using the SQL statements shown below:

```
CREATE TABLE  COMP_VARS (project VARCHAR(1024), environment VARCHAR(1024), location VARCHAR(1024), name VARCHAR(1024) NOT NULL, value VARCHAR(1024) NOT NULL );
CREATE TABLE  ARCHIVE_VARS (archive_name VARCHAR(1024), artifact_id VARCHAR(1024), artifact_group_id VARCHAR(1024), artifact_version VARCHAR(1024), environment VARCHAR(1024), location VARCHAR(1024), name VARCHAR(1024) NOT NULL, value VARCHAR(1024) NOT NULL );
```

### Installing Global Variable Provider
-------------------------------------------------
To install the global variable provider to a Silver Fabric Manager:

1. Create 
`SF_HOME/fabric/webapps/livecluster/deploy/config/variableProviders/GlobalVariableProvider` directory on the Silver Fabric Manager host.
The name of this directory is arbitrary, but it helps to give it a meaningful name.
2. Copy the zip file from project `target` directory to Silver Fabric Manager host.
3. Extract the Zip file to a staging folder on the Manager host and  copy following files to `SF_HOME/fabric/webapps/livecluster/deploy/config/variableProviders/GlobalVariableProvider` directory:
	* `GlobalVariableProvider-version.jar`
	* `GlobalVariableProvider.xml`
4. Copy MySQL JDBC driver jar file from staging directory on the Manager host to `SF_HOME/fabric/webapps/livecluster/deploy/config/variableProviders/GlobalVariableProvider` directory

### Detecting of Variable Provider by Manager
----------------------------------------------------------

The global variable provider will be automatically detected and loaded by Silver Fabric Manager.  You can verify
the global variable provider has been correctly loaded in Silver Fabric Manager UI under `Admin/Varaibles`

### Configuring Global Variable Provider
------------------------------------------------------

The `SF_HOME/fabric/webapps/livecluster/deploy/config/variableProviders/GlobalVariableProvider.xml`  contains database  related parameters, which must be configured to
to connect the global variable provider with the back end global  variable relational database. To increase `loglevel` for
torubleshooting problems, change log level to `FINE` or `FINER`.

```
<globalvariableprovider class="org.fabrician.external.provider.GlobalVariableProvider">
	<property name="name" value="GlobalVariableProvider" />
	<property name="description"
		value="Global variable provider using relational database" />
	<property name="enabled" value="false" />

	<property name="jdbcDriver" value="JDBC Driver Class Name" />
	<property name="jdbcUrl" value="JDBC URL" />
	<property name="dbUser" value="Database user name" />
	<property name="dbPwd" value="Database password" />

	<property name="projCol" value="PROJECT" />
	<property name="envCol" value="ENVIRONMENT" />
	<property name="locCol" value="LOCATION" />
	<property name="logicalMachineCol" value="LOGICAL_MACHINE" />

	<property name="archiveNameCol" value="ARCHIVE_NAME" />
	<property name="artifactIdCol" value="ARCHIVE_ID" />
	<property name="artifactGroupIdCol" value="ARCHIVE_GROUP_ID" />
	<property name="artifactVersionCol" value="ARCHIVE_VERSION" />

	<property name="nameCol" value="GV_NAME" />
	<property name="valueCol" value="GV_VALUE" />

	<property name="compVarsTable" value="COMP_VARS" />
	<property name="archiveVarsTable" value="ARCHIVE_VARS" />

	<property name="projRuntimeVariable" value="PROJECT_NAME" />
	<property name="envRuntimeVariable" value="ENVIRONMENT_NAME" />
	<property name="logicalMachineRuntimeVariable" value="LOGICAL_MACHINE_NAME" />
	<property name="locEngineProperty" value="ec2Zone" />
	
	<property name="loglevel" value="INFO" />

</globalvariableprovider>

```

### Enabling Global Variable Provider in Manager
---------------------------------------------------------------

Once the  `SF_HOME/fabric/webapps/livecluster/deploy/config/variableProviders/GlobalVariableProvider.xml` is correctly configured, you
must Enable the global variable provider in Silver Fabric Manager UI under `Admin/Variables`. 


[Download MySQL Connector/J]:<https://dev.mysql.com/downloads/connector/j/>
[TIBCO Silver Fabric Developer's Guide]:<https://docs.tibco.com/pub/silver_fabric/5.7.1/doc/pdf/TIB_silver_fabric_5.7.1_developers_guide.pdf>