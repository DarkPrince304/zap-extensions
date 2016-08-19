/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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

package org.zaproxy.zap.extension.bugTracker;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

public class BugTrackerGithubTableModel extends AbstractMultipleOptionsTableModel<BugTrackerGithubParams> {

	private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("bugTracker.trackers.github.table.header.username"),
            Constant.messages.getString("bugTracker.trackers.github.table.header.password"),
            Constant.messages.getString("bugTracker.trackers.github.table.header.repoUrl")};
    
	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
	
    private List<BugTrackerGithubParams> configs = new ArrayList<>(0);
    
    public BugTrackerGithubTableModel() {
        super();
    }
    
    @Override
    public List<BugTrackerGithubParams> getElements() {
        return configs;
    }

    /**
     * @param configs The configs to set.
     */
    public void setConfigs(List<BugTrackerGithubParams> configs) {
		this.configs = new ArrayList<>(configs.size());
		
		for (BugTrackerGithubParams config : configs) {
			this.configs.add(new BugTrackerGithubParams(config));
		}
    	
  	  	fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }
    
    @Override
	public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public int getRowCount() {
        return configs.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0);
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            // return Boolean.valueOf(getElement(rowIndex).isEnabled());
        case 1:
            return getElement(rowIndex).getName();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            
        }
    }
    
}
