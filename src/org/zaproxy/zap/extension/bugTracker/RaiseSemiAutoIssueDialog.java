/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 sanchitlucknow@gmail.com
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

import java.awt.Frame;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.utils.ZapTextField;
 
import org.zaproxy.zap.view.StandardFieldsDialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class RaiseSemiAutoIssueDialog extends StandardFieldsDialog {

	private static final long serialVersionUID = -3223449799557586758L;

	private ExtensionBugTracker extension = null;
	private ZapTextField txtFind = null;
    
	protected static final String PREFIX = "bugTracker";
    private String[] bugTrackers = {"github","bugzilla"/*,"jira","atlassan"*/};
    private BugTrackerGithub githubIssue;
    private BugTrackerBugzilla bugzillaIssue;
    private Set<Alert> alerts = null;
    private String FIELD_TRACKER_LIST = "bugTracker.trackers.list";

    public RaiseSemiAutoIssueDialog(ExtensionBugTracker ext, Frame owner, Dimension dim){
        super(owner, "bugTracker.dialog.semi.title", dim);
        this.extension = ext;
        this.alerts = ext.alerts;
        // System.out.println(this.alerts[0].getName());
 		initialize();
    }

    public void setAlert(Set<Alert> alerts) {
    	this.alerts = alerts;
    	initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.removeAllFields();
        this.setTitle(Constant.messages.getString("bugTracker.popup.issue.semi"));
        addTrackerList(extension.getBugTrackers().get(0).getName());
        updateTrackerFields();
        
	}

    public void addTrackerList(String value) {
        List<BugTracker> bugTrackers = extension.getBugTrackers();
        List<String> trackerNames = new ArrayList<String>();
        for(BugTracker bugTracker: bugTrackers) {
            trackerNames.add(bugTracker.getName());
        }
        this.addComboField(FIELD_TRACKER_LIST, trackerNames, value);
        for(BugTracker bugTracker: bugTrackers) {
            bugTracker.setDialog(this);
        }
        this.addFieldListener(FIELD_TRACKER_LIST, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTrackerFields();
            }
        });
    }

    public void updateTrackerFields() {
        String currentItem = this.getStringValue(FIELD_TRACKER_LIST);
        List<BugTracker> bugTrackers = extension.getBugTrackers();
        for(BugTracker bugTracker: bugTrackers) {
            if(bugTracker.getName().equals(currentItem)) {
                bugTracker.setDetails(alerts);
                this.removeAllFields();
                System.out.println(bugTracker.getName());
                addTrackerList(bugTracker.getName());
                bugTracker.createDialogs();
                validate();
            }
        }
    }

	@Override
    public String validateFields() {
    	return null;
    }

    public void save() {
    	int bugTrackerCount = bugTrackers.length;
        List<BugTracker> bugTrackers = extension.getBugTrackers();
        for(BugTracker bugTracker: bugTrackers) {
            bugTracker.raise(this);
        }
    }

}
