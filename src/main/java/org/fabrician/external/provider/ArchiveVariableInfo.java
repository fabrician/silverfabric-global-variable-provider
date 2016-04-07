package org.fabrician.external.provider;

import java.io.Serializable;

public class ArchiveVariableInfo implements Serializable {

	private static final long serialVersionUID = -6452487776645413300L;

	private static final int ARCHIVE_NAME_SCORE = 359;
	private static final int ARTIFACT_ID_SCORE = 179;
	private static final int ARTIFACT_GROUPID_SCORE = 89;
	private static final int ARTIFACT_VERSION_SCORE = 47;

	private static final int PROJ_SCORE = 23;
	private static final int ENV_SCORE = 11;
	private static final int LOC_SCORE = 7;
	private static final int LOGICAL_MACHINE_SCORE = 2;

	private String proj;
	private String env;
	private String loc;
	private String logicalMachine;

	private String archiveName;
	private String artifactId;
	private String artifactVersion;
	private String artifactGroupId;

	private String name;
	private String value;

	public ArchiveVariableInfo(String archiveName, String artifactId,
			String artifactGroupId, String artifactVersion, String proj,
			String env, String loc, String logicalMachine, String name,
			String value) {

		this.archiveName = archiveName;
		this.artifactId = artifactId;
		this.artifactGroupId = artifactGroupId;
		this.artifactVersion = artifactVersion;

		this.proj = proj;
		this.env = env;
		this.loc = loc;
		this.logicalMachine = logicalMachine;

		this.name = name;
		this.value = value;
	}

	public int getScore() {
		int score = 0;

		score += (isEmpty(archiveName) ? 0 : ARCHIVE_NAME_SCORE);
		score += (isEmpty(artifactId) ? 0 : ARTIFACT_ID_SCORE);
		score += (isEmpty(artifactGroupId) ? 0 : ARTIFACT_GROUPID_SCORE);
		score += (isEmpty(artifactVersion) ? 0 : ARTIFACT_VERSION_SCORE);

		score += (isEmpty(env) ? 0 : PROJ_SCORE);
		score += (isEmpty(loc) ? 0 : ENV_SCORE);
		score += (isEmpty(env) ? 0 : LOC_SCORE);
		score += (isEmpty(loc) ? 0 : LOGICAL_MACHINE_SCORE);

		return score;
	}

	private boolean isEmpty(String s) {
		return (s == null || s.trim().length() == 0);
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getArtifactGroupId() {
		return artifactGroupId;
	}

	public void setArtifactGroupId(String artifactGroupId) {
		this.artifactGroupId = artifactGroupId;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Archive Name:");
		sb.append(archiveName);
		sb.append(";Artifact Id:").append(artifactId);
		sb.append(";Artifact Group Id:").append(artifactGroupId);
		sb.append(";Artifact Version:").append(artifactVersion);

		sb.append(";Project:").append(proj);
		sb.append(";Environment:").append(env).append(";Location:").append(loc);
		sb.append(";Logical Machine:").append(logicalMachine);
		sb.append(";Name:").append(name).append(";Value:").append(value);

		return sb.toString();
	}

	public String getProj() {
		return proj;
	}

	public void setProj(String proj) {
		this.proj = proj;
	}

	public String getLogicalMachine() {
		return logicalMachine;
	}

	public void setLogicalMachine(String logicalMachine) {
		this.logicalMachine = logicalMachine;
	}
}
