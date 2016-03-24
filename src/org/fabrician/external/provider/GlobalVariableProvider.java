package org.fabrician.provider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.datasynapse.fabric.admin.info.ComponentInfo;
import com.datasynapse.fabric.admin.info.FabricEngineInfo;
import com.datasynapse.fabric.admin.info.RuntimeContextVariableInfo;
import com.datasynapse.fabric.admin.info.StackInfo;
import com.datasynapse.fabric.broker.archives.VersionedArchive;
import com.datasynapse.fabric.broker.userartifact.variable.AbstractDynamicVariableProvider;
import com.datasynapse.fabric.broker.userartifact.variable.DynamicVariableContext;
import com.datasynapse.fabric.broker.userartifact.variable.DynamicVariableContextInfo;
import com.datasynapse.gridserver.admin.Property;

public class GlobalVariableProvider extends AbstractDynamicVariableProvider {

	private static final long serialVersionUID = -3219622937289083282L;

	private String jdbcUrl;
	private String jdbcDriver;

	private String dbUser;
	private String dbPwd;
	private String projCol;
	private String logicalMachineCol;
	private String envCol;
	private String locCol;
	private String nameCol;
	private String valueCol;

	private String archiveNameCol;
	private String artifactGroupIdCol;
	private String artifactIdCol;
	private String artifactVersionCol;

	private String projRuntimeVariable;
	private String envRuntimeVariable;
	private String logicalMachineRuntimeVariable;
	private String locEngineProperty;

	private String compVarsTable;
	private String archiveVarsTable;
	private String compQuery;
	private String archiveQuery;
	private String loglevel;

	private transient PreparedStatement compStmt;
	private transient PreparedStatement archiveStmt;
	private transient Connection connection;
	private transient Logger logger;

	public GlobalVariableProvider() {
		super();
	}

	@Override
	public void destroy() {
		logger.fine("Enter destroy");
		cleanup();
		logger.fine("Exit destroy");
	}

	private void cleanup() {

		if (compStmt != null)
			try {
				compStmt.close();
			} catch (SQLException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.warning(sw.toString());
			} finally {
				compStmt = null;
			}

		if (archiveStmt != null)
			try {
				archiveStmt.close();
			} catch (SQLException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.warning(sw.toString());
			} finally {
				archiveStmt = null;
			}

		if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.warning(sw.toString());
			} finally {
				connection = null;
			}

	}

	@Override
	public void init() throws Exception {
		Logger.getLogger(getClass().getName()).info("Enter init");

		try {

			Logger.getLogger(getClass().getName()).info(
					"Environment Runtime Variable Name:" + envRuntimeVariable);

			Logger.getLogger(getClass().getName()).info(
					"Project Runtime Variable Name:" + projRuntimeVariable);

			Logger.getLogger(getClass().getName()).info(
					"Location Engine Property Name:" + locEngineProperty);

			Logger.getLogger(getClass().getName()).info(
					"Load JDBC driver:" + jdbcDriver);

			Class.forName(jdbcDriver);

			Logger.getLogger(getClass().getName()).info("JDBC URL:" + jdbcUrl);
			this.initCompQuery();
			this.initArchiveQuery();

			this.testDatabase();
			this.initLogger();

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.getLogger(getClass().getName()).severe(sw.toString());

			throw new RuntimeException(e);
		}

		Logger.getLogger(getClass().getName()).info("Exit init");
	}

	private void initLogger() {
		try {
			logger = Logger.getLogger(getClass().getName());
			logger.setLevel(Level.parse(loglevel.toUpperCase()));
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.getLogger(getClass().getName()).warning(sw.toString());

			throw new RuntimeException(e);
		}
	}

	private void testDatabase() {
		Connection conn = null;
		Statement stmt = null;

		try {
			Logger.getLogger(getClass().getName()).info(
					"Testing database connection...");
			conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPwd);

			stmt = conn.createStatement();
			stmt.setMaxRows(1);
			Logger.getLogger(getClass().getName()).info(
					"Select from component variable table...");
			stmt.executeQuery("SELECT * from " + compVarsTable);
			stmt.close();

			stmt = conn.createStatement();
			stmt.setMaxRows(1);
			Logger.getLogger(getClass().getName()).info(
					"Select from  archive variable table...");
			stmt.executeQuery("SELECT * from " + archiveVarsTable);
			stmt.close();
			Logger.getLogger(getClass().getName()).info(
					"Testing database ...successful");

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.getLogger(getClass().getName()).warning(sw.toString());

			throw new RuntimeException(e);
		} finally {

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private void initCompQuery() {

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ").append(projCol).append(",").append(envCol)
				.append(",").append(locCol).append(",");
		sb.append(logicalMachineCol).append(",");
		sb.append(nameCol).append(",").append(valueCol);
		sb.append(" FROM ").append(compVarsTable).append(" WHERE ( ");
		sb.append(projCol).append(" = ? OR ").append(projCol)
				.append(" is NULL ").append(" OR ").append(projCol)
				.append(" = '' )");
		sb.append(" AND (").append(envCol).append(" = ? OR ").append(envCol)
				.append(" is NULL ").append(" OR ").append(envCol)
				.append(" = '' )");
		sb.append(" AND (").append(locCol).append(" = ? OR ").append(locCol)
				.append(" is NULL ").append(" OR ").append(locCol)
				.append(" = '' )");
		sb.append(" AND (").append(logicalMachineCol).append(" = ? OR ")
				.append(logicalMachineCol).append(" is NULL ").append(" OR ")
				.append(logicalMachineCol).append(" = '' )");
		this.compQuery = sb.toString();
		Logger.getLogger(getClass().getName()).info(
				"Select component variable query:" + this.compQuery);

	}

	private void prepareCompStatement() {

		try {
			connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPwd);
			this.compStmt = this.connection.prepareStatement(this.compQuery);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.getLogger(getClass().getName()).warning(sw.toString());

			throw new RuntimeException(e);
		}

	}

	private void initArchiveQuery() {

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ").append(archiveNameCol).append(",");
		sb.append(artifactIdCol).append(",").append(artifactGroupIdCol)
				.append(",");
		sb.append(artifactVersionCol).append(",");
		sb.append(projCol).append(",");
		sb.append(envCol).append(",").append(locCol).append(",");
		sb.append(logicalMachineCol).append(",");
		sb.append(nameCol).append(",").append(valueCol);

		sb.append(" FROM ").append(archiveVarsTable).append(" WHERE ");
		sb.append(" (").append(archiveNameCol).append(" = ? OR ")
				.append(archiveNameCol).append(" is NULL ").append(" OR ")
				.append(archiveNameCol).append(" = '' )");
		sb.append(" AND (").append(artifactIdCol).append(" = ? OR ")
				.append(artifactIdCol).append(" is NULL ").append(" OR ")
				.append(artifactIdCol).append(" = '' )");
		sb.append(" AND (").append(artifactGroupIdCol).append(" = ? OR ")
				.append(artifactGroupIdCol).append(" is NULL ").append(" OR ")
				.append(artifactGroupIdCol).append(" = '' )");
		sb.append(" AND (").append(artifactVersionCol).append(" = ? OR ")
				.append(artifactVersionCol).append(" is NULL ").append(" OR ")
				.append(artifactVersionCol).append(" = '' )");

		sb.append(" AND (").append(projCol).append(" = ? OR ").append(projCol)
				.append(" is NULL ").append(" OR ").append(projCol)
				.append(" = '' )");
		sb.append(" AND (").append(envCol).append(" = ? OR ").append(envCol)
				.append(" is NULL ").append(" OR ").append(envCol)
				.append(" = '' )");
		sb.append(" AND (").append(locCol).append(" = ? OR ").append(locCol)
				.append(" is NULL ").append(" OR ").append(locCol)
				.append(" = '' )");
		sb.append(" AND (").append(logicalMachineCol).append(" = ? OR ")
				.append(logicalMachineCol).append(" is NULL ").append(" OR ")
				.append(logicalMachineCol).append(" = '' )");

		this.archiveQuery = sb.toString();

		Logger.getLogger(getClass().getName()).info(
				"Select archive variable query:" + this.archiveQuery);

	}

	private void prepareArchiveStatement() {

		try {
			connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPwd);
			this.archiveStmt = this.connection
					.prepareStatement(this.archiveQuery);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.severe(sw.toString());

			throw new RuntimeException(e);
		}

	}

	@Override
	public Properties getVariables(FabricEngineInfo engineInfo,
			StackInfo stackInfo, ComponentInfo componentInfo) {

		Properties properties = getActivationVariables(engineInfo, stackInfo,
				componentInfo);
		return properties;
	}

	@Override
	public Properties getVariables(FabricEngineInfo engineInfo,
			StackInfo stackInfo, ComponentInfo componentInfo,
			DynamicVariableContext context,
			Map<DynamicVariableContextInfo, Object> contextInfo) {
		Properties properties = null;

		if (context == null) {
			logger.fine("Get variables without context: component::"
					+ componentInfo.getName());
			properties = getActivationVariables(engineInfo, stackInfo,
					componentInfo);
		} else if (context.equals(DynamicVariableContext.ACTIVATION)) {
			logger.fine("Get variables for Activation context: component:"
					+ componentInfo.getName());
			properties = getActivationVariables(engineInfo, stackInfo,
					componentInfo);
		} else if (context.equals(DynamicVariableContext.ARCHIVE_DEPLOY)) {
			logger.fine("Get variables for archive deployment context");
			properties = getArchiveDeployVariables(engineInfo, stackInfo,
					componentInfo, contextInfo);
		}

		return properties;
	}

	private Properties getArchiveDeployVariables(FabricEngineInfo engineInfo,
			StackInfo stackInfo, ComponentInfo componentInfo,
			Map<DynamicVariableContextInfo, Object> contextInfo) {

		Properties properties = new Properties();

		try {
			// add variable for deployment archive
			String archiveName = ((String) contextInfo
					.get(DynamicVariableContextInfo.ARCHIVE_NAME));

			logger.fine("Archive deployment context: Archive Name:"
					+ archiveName);

			// add variables for deployment properties
			Properties props = (Properties) contextInfo
					.get(DynamicVariableContextInfo.ARCHIVE_DEPLOY_PROPERTIES);

			String artifactId = props
					.getProperty(VersionedArchive.ARTIFACT_ID_KEY);

			logger.fine("Archive deployment context: Artifact Id:" + artifactId);

			String artifactGroupId = props
					.getProperty(VersionedArchive.ARTIFACT_GROUP_ID_KEY);

			logger.fine("Archive deployment context: Artifact Group Id:"
					+ artifactGroupId);

			String artifactVersion = props
					.getProperty(VersionedArchive.ARTIFACT_VERSION_KEY);

			logger.fine("Archive deployment context: Archive Version:"
					+ artifactVersion);

			String env = null;
			String proj = null;
			String loc = null;
			String logicalMachine = null;

			if (locEngineProperty != null) {
				Property[] eps = engineInfo.getProperties();

				for (Property ep : eps) {
					if (ep.getName().equals(locEngineProperty)) {
						loc = ep.getValue();
						logger.fine("Activation context:location:" + loc);
						break;
					}
				}
			}

			if (envRuntimeVariable != null || projRuntimeVariable != null
					|| logicalMachineRuntimeVariable != null) {

				RuntimeContextVariableInfo[] rvs = componentInfo
						.getRuntimeContextVariables();
				for (RuntimeContextVariableInfo rv : rvs) {

					if (rv.getName().equals(envRuntimeVariable)) {
						env = (String) rv.getValue();
						logger.fine("Activation context:environment:" + env);
					} else if (rv.getName().equals(projRuntimeVariable)) {
						proj = (String) rv.getValue();
						logger.fine("Activation context:project:" + proj);
					} else if (rv.getName().equals(
							logicalMachineRuntimeVariable)) {
						logicalMachine = (String) rv.getValue();
						logger.fine("Activation context:logical machine:"
								+ logicalMachine);
					}

					if (env != null && proj != null && logicalMachine != null)
						break;
				}
			}

			this.prepareArchiveStatement();

			archiveStmt.setString(1, archiveName);
			archiveStmt.setString(2, artifactId);
			archiveStmt.setString(3, artifactGroupId);
			archiveStmt.setString(4, artifactVersion);
			archiveStmt.setString(5, proj);
			archiveStmt.setString(6, env);
			archiveStmt.setString(7, loc);
			archiveStmt.setString(8, logicalMachine);

			logger.fine("Archive deployment context: Sql Query:"
					+ this.archiveStmt.toString());

			HashMap<String, ArchiveVariableInfo> map = new HashMap<String, ArchiveVariableInfo>();

			ResultSet rs = archiveStmt.executeQuery();

			while (rs.next()) {

				archiveName = rs.getString(1);
				artifactId = rs.getString(2);
				artifactGroupId = rs.getString(3);
				artifactVersion = rs.getString(4);

				proj = rs.getString(5);
				env = rs.getString(6);
				loc = rs.getString(7);
				logicalMachine = rs.getString(8);

				String name = rs.getString(9);
				String value = rs.getString(10);

				ArchiveVariableInfo info = new ArchiveVariableInfo(archiveName,
						artifactId, artifactGroupId, artifactVersion, proj,
						env, loc, logicalMachine, name, value);

				logger.finer("Archive Variable Info:" + info);

				ArchiveVariableInfo cur = map.get(name);
				if (cur == null || (info.getScore() > cur.getScore())) {
					logger.finer("Adding to map:" + name + "-->" + info);
					map.put(name, info);
				}
			}

			Collection<ArchiveVariableInfo> varInfos = map.values();
			for (ArchiveVariableInfo varInfo : varInfos) {
				logger.finer("Setting property:" + varInfo.getName() + "="
						+ varInfo.getValue());
				properties.setProperty(varInfo.getName(), varInfo.getValue());
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			cleanup();
		}

		return properties;
	}

	private Properties getActivationVariables(FabricEngineInfo engineInfo,
			StackInfo stackInfo, ComponentInfo componentInfo) {

		Properties properties = new Properties();

		try {
			prepareCompStatement();

			String env = null;
			String proj = null;
			String loc = null;
			String logicalMachine = null;

			if (locEngineProperty != null) {
				Property[] eps = engineInfo.getProperties();

				for (Property ep : eps) {
					if (ep.getName().equals(locEngineProperty)) {
						loc = ep.getValue();
						logger.fine("Activation context:location:" + loc);
						break;
					}
				}
			}

			if (envRuntimeVariable != null || projRuntimeVariable != null
					|| logicalMachineRuntimeVariable != null) {

				RuntimeContextVariableInfo[] rvs = componentInfo
						.getRuntimeContextVariables();
				for (RuntimeContextVariableInfo rv : rvs) {

					if (rv.getName().equals(envRuntimeVariable)) {
						env = (String) rv.getValue();
						logger.fine("Activation context:environment:" + env);
					} else if (rv.getName().equals(projRuntimeVariable)) {
						proj = (String) rv.getValue();
						logger.fine("Activation context:project:" + proj);
					} else if (rv.getName().equals(
							logicalMachineRuntimeVariable)) {
						logicalMachine = (String) rv.getValue();
						logger.fine("Activation context:logical machine:"
								+ logicalMachine);
					}

					if (env != null && proj != null && logicalMachine != null)
						break;
				}
			}

			compStmt.setString(1, proj);
			compStmt.setString(2, env);
			compStmt.setString(3, loc);
			compStmt.setString(4, logicalMachine);

			HashMap<String, ComponentVariableInfo> map = new HashMap<String, ComponentVariableInfo>();

			ResultSet rs = compStmt.executeQuery();
			while (rs.next()) {
				proj = rs.getString(1);
				env = rs.getString(2);
				loc = rs.getString(3);
				logicalMachine = rs.getString(4);

				String name = rs.getString(5);
				String value = rs.getString(6);

				ComponentVariableInfo info = new ComponentVariableInfo(proj,
						env, loc, logicalMachine, name, value);

				logger.finer("Component Variable Info:" + info);

				ComponentVariableInfo cur = map.get(name);
				if (cur == null || (info.getScore() > cur.getScore())) {
					logger.finer("Activation context:put in map::" + name
							+ "-->" + info);
					map.put(name, info);
				}
			}

			Collection<ComponentVariableInfo> varInfos = map.values();
			for (ComponentVariableInfo varInfo : varInfos) {
				logger.finer("Activation context:set property:"
						+ varInfo.getName() + "=" + varInfo.getValue());
				properties.setProperty(varInfo.getName(), varInfo.getValue());
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.severe(sw.toString());

			throw new RuntimeException(e);
		} finally {
			cleanup();
		}

		return properties;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}

	public String getDbPwd() {
		return dbPwd;
	}

	public String getProjCol() {
		return projCol;
	}

	public void setProjCol(String projCol) {
		this.projCol = projCol;
	}

	public String getProjRuntimeVariable() {
		return projRuntimeVariable;
	}

	public void setProjRuntimeVariable(String projRuntimeVariable) {
		this.projRuntimeVariable = projRuntimeVariable;
	}

	public String getEnvCol() {
		return envCol;
	}

	public void setEnvCol(String envCol) {
		this.envCol = envCol;
	}

	public String getNameCol() {
		return nameCol;
	}

	public void setNameCol(String nameCol) {
		this.nameCol = nameCol;
	}

	public String getValueCol() {
		return valueCol;
	}

	public void setValueCol(String valueCol) {
		this.valueCol = valueCol;
	}

	public String getEnvRuntimeVariable() {
		return envRuntimeVariable;
	}

	public void setEnvRuntimeVariable(String envRuntimeVariable) {
		this.envRuntimeVariable = envRuntimeVariable;
	}

	public String getLocCol() {
		return locCol;
	}

	public void setLocCol(String locCol) {
		this.locCol = locCol;
	}

	public String getLocEngineProperty() {
		return locEngineProperty;
	}

	public void setLocEngineProperty(String locEngineProperty) {
		this.locEngineProperty = locEngineProperty;
	}

	public String getCompVarsTable() {
		return compVarsTable;
	}

	public void setCompVarsTable(String compVarsTable) {
		this.compVarsTable = compVarsTable;
	}

	public String getArtifactGroupIdCol() {
		return artifactGroupIdCol;
	}

	public void setArtifactGroupIdCol(String artifactGroupIdCol) {
		this.artifactGroupIdCol = artifactGroupIdCol;
	}

	public String getArtifactIdCol() {
		return artifactIdCol;
	}

	public void setArtifactIdCol(String artifactIdCol) {
		this.artifactIdCol = artifactIdCol;
	}

	public String getArtifactVersionCol() {
		return artifactVersionCol;
	}

	public void setArtifactVersionCol(String artifactVersionCol) {
		this.artifactVersionCol = artifactVersionCol;
	}

	public String getArtifactVarsTable() {
		return archiveVarsTable;
	}

	public void setArchiveNameCol(String archiveNameCol) {
		this.archiveNameCol = archiveNameCol;
	}

	public String getArchiveVarsTable() {
		return archiveVarsTable;
	}

	public void setArchiveVarsTable(String archiveVarsTable) {
		this.archiveVarsTable = archiveVarsTable;
	}

	public String getArchiveNameCol() {
		return archiveNameCol;
	}

	public String getLoglevel() {
		return loglevel;
	}

	public void setLoglevel(String loglevel) {
		this.loglevel = loglevel;
	}

	public String getLogicalMachineCol() {
		return logicalMachineCol;
	}

	public void setLogicalMachineCol(String logicalMachineCol) {
		this.logicalMachineCol = logicalMachineCol;
	}

	public String getLogicalMachineRuntimeVariable() {
		return logicalMachineRuntimeVariable;
	}

	public void setLogicalMachineRuntimeVariable(
			String logicalMachineRuntimeVariable) {
		this.logicalMachineRuntimeVariable = logicalMachineRuntimeVariable;
	}

}
