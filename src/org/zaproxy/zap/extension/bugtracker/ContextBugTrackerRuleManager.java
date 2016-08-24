/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zaproxy.zap.model.Context;

/**
 * The Manager that handles all the information related to {@link BugTrackerRule BugTrackerRules}
 * corresponding to a particular {@link Context}.
 */
public class ContextBugTrackerRuleManager {

	/** The context id. */
	private int contextId;

	/** The model. */
	private List<BugTrackerRule> bugTrackerRules;

	public ContextBugTrackerRuleManager(int contextId) {
		this.contextId = contextId;
		this.bugTrackerRules = new ArrayList<>();
	}

	/**
	 * Builds a table model for the bugTrackerRules.
	 * 
	 * @return the model
	 */
	public BugTrackerRuleTableModel getBugTrackerRulesModel() {
		return new BugTrackerRuleTableModel(this.bugTrackerRules);
	}

	/**
	 * Gets the context id to which this object corresponds.
	 * 
	 * @return the context id
	 */
	public int getContextId() {
		return contextId;
	}

	/**
	 * Gets an unmodifiable view of the list of bugTrackerRules.
	 * 
	 * @return the bugTrackerRules
	 */
	public List<BugTrackerRule> getBugTrackerRules() {
		return Collections.unmodifiableList(bugTrackerRules);
	}

	/**
	 * Sets a new list of bugTrackerRules for this context. An internal copy of the provided list is stored.
	 * 
	 * @param bugTrackerRules the bugTrackerRules
	 * @return the list
	 */
	public void setBugTrackerRules(List<BugTrackerRule> bugTrackerRules) {
		this.bugTrackerRules = new ArrayList<>(bugTrackerRules);
	}

	/**
	 * Adds an bugTrackerRule.
	 * 
	 * @param bugTrackerRule the bugTrackerRule
	 */
	public void addBugTrackerRule(BugTrackerRule bugTrackerRule) {
		bugTrackerRules.add(bugTrackerRule);
	}

	/**
	 * Removes an bugTrackerRule.
	 * 
	 * @param bugTrackerRule the bugTrackerRule
	 */
	public boolean removeBugTrackerRule(BugTrackerRule bugTrackerRule) {
		return bugTrackerRules.remove(bugTrackerRule);
	}

	/**
	 * Removes all the bugTrackerRules.
	 */
	public void removeAllBugTrackerRules(){
		this.bugTrackerRules.clear();
	}
}
