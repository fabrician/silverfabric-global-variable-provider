package org.fabrician.provider;

import java.io.Serializable;

public class ComponentVariableInfo implements Serializable {

	private static final long serialVersionUID = -877763767213664080L;

	private static final int PROJ_SCORE = 23;
	private static final int ENV_SCORE = 11;
	private static final int LOC_SCORE = 7;
	private static final int LOGICAL_MACHINE_SCORE = 2;

	private String env;
	private String proj;
	private String loc;
	private String logicalMachine;
	private String name;
	private String value;

	public ComponentVariableInfo(String proj, String env, String loc,
			String logicalMachine, String name, String value) {
		this.env = env;
		this.proj = proj;
		this.loc = loc;
		this.logicalMachine = logicalMachine;
		this.name = name;
		this.value = value;
	}

	public int getScore() {
		int score = 0;

		score += (isEmpty(proj) ? 0 : PROJ_SCORE);
		score += (isEmpty(env) ? 0 : ENV_SCORE);
		score += (isEmpty(loc) ? 0 : LOC_SCORE);
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

	public String getProj() {
		return proj;
	}

	public void setProj(String proj) {
		this.proj = proj;
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

	public String getLogicalMachine() {
		return logicalMachine;
	}

	public void setLogicalMachine(String logicalMachine) {
		this.logicalMachine = logicalMachine;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Project:");
		sb.append(proj);
		sb.append(";Environment:").append(env).append(";Location:").append(loc);
		sb.append(";Logical Machine:").append(logicalMachine);
		sb.append(";Name:").append(name).append(";Value:").append(value);

		return sb.toString();
	}
}
