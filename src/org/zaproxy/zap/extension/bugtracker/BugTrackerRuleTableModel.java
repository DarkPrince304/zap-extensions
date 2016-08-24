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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

/**
 * A table model for holding a set of BugTrackerRule, for a {@link Context}.
 */
public class BugTrackerRuleTableModel extends AbstractMultipleOptionsTableModel<BugTrackerRule> {

	/** The Constant defining the table column names. */
	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("bugtracker.table.header.enabled"),
			Constant.messages.getString("bugtracker.table.header.alertid"),
			Constant.messages.getString("bugtracker.table.header.url"),
			Constant.messages.getString("bugtracker.table.header.newalert") };

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4463944219657112162L;

	/** The alert filters. */
	private List<BugTrackerRule> bugTrackerRules = new ArrayList<>();

	/**
	 * Instantiates a new alert filters table model. An internal copy of the provided list is stored.
	 * 
	 * @param bugTrackerRules the alert filters
	 */
	public BugTrackerRuleTableModel(List<BugTrackerRule> bugTrackerRules) {
		this.bugTrackerRules = new ArrayList<>(bugTrackerRules);
	}

	/**
	 * Instantiates a new user table model.
	 */
	public BugTrackerRuleTableModel() {
		this.bugTrackerRules = new ArrayList<>();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return bugTrackerRules.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		BugTrackerRule af = bugTrackerRules.get(rowIndex);
		if (af == null) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			return af.isEnabled();
		case 1:
			return ExtensionBugTracker.getRuleNameForId(af.getRuleId());
		case 2:
			return af.getUrl();
		case 3:
			return af.getNewRiskName();
		default:
			return null;
		}
	}

	@Override
	public List<BugTrackerRule> getElements() {
		return bugTrackerRules;
	}

	/**
	 * Gets the internal list of bugTrackerRules managed by this model. 
	 * 
	 * @return the bugTrackerRules
	 */
	public List<BugTrackerRule> getBugTrackerRules() {
		return bugTrackerRules;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Just the enable/disable
		return (columnIndex == 0);
	}

	/**
	 * Sets a new list of bugTrackerRules for this model. An internal copy of the provided list is stored.
	 * 
	 * @param bugTrackerRules the new bugTrackerRules
	 */
	public void setBugTrackerRules(List<BugTrackerRule> bugTrackerRules) {
		this.bugTrackerRules = new ArrayList<>(bugTrackerRules);
		this.fireTableDataChanged();
	}
	
	/**
	 * Removes all the bugTrackerRules for this model.
	 */
	public void removeAllBugTrackerRules(){
		this.bugTrackerRules=new ArrayList<>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Adds a new user to this model
	 *
	 * @param af the user
	 */
	public void addBugTrackerRule(BugTrackerRule af){
		this.bugTrackerRules.add(af);
		this.fireTableRowsInserted(this.bugTrackerRules.size()-1, this.bugTrackerRules.size()-1);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return String.class;
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			if (aValue instanceof Boolean) {
				bugTrackerRules.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

}
