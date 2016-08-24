/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.bugtracker;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.utils.Enableable;

public class BugTrackerRule extends Enableable {

	/** The Constant FIELD_SEPARATOR used for separating BugTrackerRule's field during serialization. */
	private static final String FIELD_SEPARATOR = ";";

	private int contextId;
	private int ruleId;
	// Use -1 as false positive
	private int newRisk;
	private String parameter;
	private String url;
	private boolean regex;

	private static final Logger log = Logger.getLogger(BugTrackerRule.class);

	public BugTrackerRule() {
	}

	public BugTrackerRule(int contextId, int ruleId, int newRisk, String url,
			boolean regex, boolean enabled) {
		super();
		this.contextId = contextId;
		this.ruleId = ruleId;
		this.newRisk = newRisk;
		this.parameter = parameter;
		this.url = url;
		this.regex = regex;
		this.setEnabled(enabled);
	}

	public int getContextId() {
		return contextId;
	}

	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

	public int getRuleId() {
		return ruleId;
	}

	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	public static String getNameForRisk(int risk) {
		switch (risk) {
		case -1:
			return Constant.messages.getString("bugtracker.panel.newalert.fp");
		case Alert.RISK_INFO:	
			return Constant.messages.getString("bugtracker.panel.newalert.info");
		case Alert.RISK_LOW:
			return Constant.messages.getString("bugtracker.panel.newalert.low");
		case Alert.RISK_MEDIUM:
			return Constant.messages.getString("bugtracker.panel.newalert.medium");
		case Alert.RISK_HIGH:
			return Constant.messages.getString("bugtracker.panel.newalert.high");
		default:
			return "";	
		}
	}

	public String getNewRiskName() {
		return getNameForRisk(this.newRisk);
	}

	public int getNewRisk() {
		return newRisk;
	}

	public void setNewRisk(int newRisk) {
		this.newRisk = newRisk;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isRegex() {
		return regex;
	}

	public void setRegex(boolean regex) {
		this.regex = regex;
	}
	
	/**
	 * Encodes the BugTrackerRule in a String. Fields that contain strings are Base64 encoded.
	 * 
	 * @param bugTrackerRule the BugTrackerRule
	 * @return the encoded string
	 */
	public static String encode(BugTrackerRule bugTrackerRuleuser) {
		StringBuilder out = new StringBuilder();
		out.append(bugTrackerRuleuser.isEnabled()).append(FIELD_SEPARATOR);
		out.append(bugTrackerRuleuser.getRuleId()).append(FIELD_SEPARATOR);
		out.append(bugTrackerRuleuser.getNewRisk()).append(FIELD_SEPARATOR);
		if (bugTrackerRuleuser.url != null) {
			out.append(Base64.encodeBase64String(bugTrackerRuleuser.url.getBytes()));
		}
		out.append(FIELD_SEPARATOR);
		out.append(bugTrackerRuleuser.isRegex()).append(FIELD_SEPARATOR);
		if (bugTrackerRuleuser.parameter != null) {
			out.append(Base64.encodeBase64String(bugTrackerRuleuser.parameter.getBytes()));
		}
		out.append(FIELD_SEPARATOR);
		// log.debug("Encoded BugTrackerRule: " + out.toString());
		return out.toString();
	}

	/**
	 * Decodes an User from an encoded string. The string provided as input should have been
	 * obtained through calls to {@link #encode(BugTrackerRule)}.
	 * @param encodedString the encoded string
	 * @return the user
	 */
	protected static BugTrackerRule decode(int contextId, String encodedString) {
		String[] pieces = encodedString.split(FIELD_SEPARATOR, -1);
		BugTrackerRule bugTrackerRule = null;
		try {
			bugTrackerRule = new BugTrackerRule();
			bugTrackerRule.setContextId(contextId);
			bugTrackerRule.setEnabled(Boolean.parseBoolean(pieces[0]));
			bugTrackerRule.setRuleId(Integer.parseInt(pieces[1]));
			bugTrackerRule.setNewRisk(Integer.parseInt(pieces[2]));
			bugTrackerRule.setUrl(new String(Base64.decodeBase64(pieces[3])));
			bugTrackerRule.setRegex(Boolean.parseBoolean(pieces[4]));
			bugTrackerRule.setParameter(new String(Base64.decodeBase64(pieces[5])));
		} catch (Exception ex) {
			log.error("An error occured while decoding bugTrackerRule from: " + encodedString, ex);
			return null;
		}
		// log.debug("Decoded bugTrackerRule: " + bugTrackerRule);
		return bugTrackerRule;
	}

	
}
