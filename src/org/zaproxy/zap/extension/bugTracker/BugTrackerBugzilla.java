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

import org.apache.log4j.Logger;
import com.j2bugzilla.base.Bug; 
import com.j2bugzilla.base.BugFactory; 
import com.j2bugzilla.base.BugzillaConnector; 
import com.j2bugzilla.base.BugzillaException; 
import com.j2bugzilla.base.ConnectionException; 
import com.j2bugzilla.rpc.LogIn;
import com.j2bugzilla.rpc.ReportBug;

import org.parosproxy.paros.core.scanner.Alert;
import java.io.IOException;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class BugTrackerBugzilla extends BugTracker {

    private String NAME = "Bugzilla";
    private String FIELD_URL = "bugTracker.trackers.bugzilla.issue.url";
    private String FIELD_PRODUCT = "bugTracker.trackers.bugzilla.issue.product";
    private String FIELD_COMPONENT = "bugTracker.trackers.bugzilla.issue.component";
    private String FIELD_VERSION = "bugTracker.trackers.bugzilla.issue.version";
    private String FIELD_OS = "bugTracker.trackers.bugzilla.issue.os";
    private String FIELD_PLATFORM = "bugTracker.trackers.bugzilla.issue.platform";
    private String FIELD_DESCRIPTION = "bugTracker.trackers.bugzilla.issue.description";
    private String FIELD_SUMMARY = "bugTracker.trackers.bugzilla.issue.summary";
    private String FIELD_USERNAME = "bugTracker.trackers.bugzilla.issue.username";
    private String FIELD_PASSWORD = "bugTracker.trackers.bugzilla.issue.password";
    private String summaryIssue = null;
    private String descriptionIssue = null;
    private JPanel configTable = null;

    private static final Logger log = Logger.getLogger(BugTrackerBugzilla.class);

	public BugTrackerBugzilla(Set<Alert> alerts) {
        setSummary(alerts);
        setDesc(alerts);
    }

    public BugTrackerBugzilla() {
        initializeConfigTable();
    }

    public void initializeConfigTable() {
        String[] columnNames = {"Username/Email","Password","Bugzilla URL"};
        Object[][] data = {
        {"Kathy", "Smith", "https://landfill.bugzilla.org/bugzilla-5.0-branch/"},
        {"John", "Doe", "https://landfill.bugzilla.org/bugzilla-5.0-branch/"},
        {"Sue", "Black", "https://landfill.bugzilla.org/bugzilla-5.0-branch/"},
        {"Jane", "White", "https://landfill.bugzilla.org/bugzilla-5.0-branch/"},
        {"Joe", "Brown", "https://landfill.bugzilla.org/bugzilla-5.0-branch/"}
        };


        configTable = new JPanel(new BorderLayout());
        final DefaultTableModel model = new DefaultTableModel(data, columnNames);
        final JTable table = new JTable(model);

        JPanel buttonLayout = new JPanel();
        buttonLayout.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton add = new JButton("Add");
        JButton modify = new JButton("Modify");
        JButton remove = new JButton("Remove");
        add.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // check for selected row first
                if (table.getSelectedRow() != -1) {
                    // remove selected row from the model
                    model.removeRow(table.getSelectedRow());
                    model.addRow(new Object[]{"Joe", "Brown"});
                }
            }
        });
 
        buttonLayout.add(add);
        buttonLayout.add(modify);
        buttonLayout.add(remove);
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        configTable.add(new JLabel("Bugzilla Configuration"), BorderLayout.PAGE_START);
        configTable.add(scrollPane, BorderLayout.CENTER);
        configTable.add(buttonLayout, BorderLayout.SOUTH);
    }

    // public JPanel getCredentialsTable() {
    //     return credentialTable;
    // }

    public JPanel getConfigTable() {
        return configTable;
    }

    public void createDialogs(RaiseSemiAutoIssueDialog dialog, int index) {
		dialog.addTextField(index, FIELD_URL, "");
		dialog.addTextField(index, FIELD_PRODUCT, "");
		dialog.addTextField(index, FIELD_COMPONENT, "");
		dialog.addTextField(index, FIELD_VERSION, "");
		dialog.addTextField(index, FIELD_OS, "");
		dialog.addTextField(index, FIELD_PLATFORM, "");
		dialog.addTextField(index, FIELD_DESCRIPTION, getDesc());
		dialog.addTextField(index, FIELD_SUMMARY, getSummary());
		dialog.addTextField(index, FIELD_USERNAME, "");
		dialog.addTextField(index, FIELD_PASSWORD, "");
	}

	public void setSummary(Set<Alert> alerts) {
		StringBuilder summary = new StringBuilder("");
		for(Alert alert: alerts) {
            if(alert.getName().length() > 0 ) {
                summary.append(alert.getName().toString() + ", ");
            }
        }
        summary.replace(summary.length()-2, summary.length()-1, "");
        summaryIssue = summary.toString();
	}

	public void setDesc(Set<Alert> alerts) {
		StringBuilder description = new StringBuilder("");
		for(Alert alert: alerts) {

            if(alert.getName().length() > 0 ) {
                description.append(" *ALERT IN QUESTION* \n" + alert.getName().toString() + "\n\n");
            }
            if(alert.getUri().length() > 0 ) {
                description.append(" *URL* \n" + alert.getUri().toString() + "\n\n");
            }
            if(alert.getDescription().length() > 0 ) {
                description.append(" *DESCRIPTION* \n" + alert.getDescription().toString() + "\n\n");
            }
            if(alert.getOtherInfo().length() > 0 ) {
                description.append(" *OTHER INFO* \n" + alert.getOtherInfo().toString() + "\n\n");
            }
            if(alert.getSolution().length() > 0 ) {
                description.append(" *SOLUTION* \n" + alert.getSolution().toString() + "\n\n");
            }
            if(alert.getReference().length() > 0 ) {
                description.append(" *REFERENCE* \n" + alert.getReference().toString() + "\n\n");
            }
            if(alert.getParam().length() > 0 ) {
                description.append(" *PARAMETER* \n" + alert.getParam().toString() + "\n\n");
            }
            if(alert.getAttack().length() > 0 ) {
                description.append(" *ATTACK* \n" + alert.getAttack().toString() + "\n\n");
            }
            if(alert.getEvidence().length() > 0 ) {
                description.append(" *EVIDENCE* \n" + alert.getEvidence().toString() + "\n\n");
            }
            if(alert.getRisk() >= 0 ) {
                description.append("Risk: " + alert.MSG_RISK[alert.getRisk()] + ", ");
            }
            if(alert.getConfidence() >= 0 ) {
                description.append("Conf: " + alert.MSG_CONFIDENCE[alert.getConfidence()] + ", ");
            }
            if(alert.getCweId() >= 0 ) {
                description.append("CWE: " + alert.getCweId() + ", ");
            }
            if(alert.getWascId() >= 0 ) {
                description.append("WASC: " + alert.getWascId() + "\n\n\n\n ");
            }

        }
        descriptionIssue = description.toString();
	}

	public String getSummary() {
		return this.summaryIssue;
	}
	
	public String getDesc() {
		return this.descriptionIssue;
	}


	public void raiseOnTracker(String url, String summary, String description, String product, String component, String version, String os, String platform, String username, String password) throws IOException {
		try {
			BugzillaConnector conn = new BugzillaConnector(); 
			conn.connectTo(url); 

			LogIn logIn = new LogIn(username, password); 
			conn.executeMethod(logIn); 

			Bug bug = new BugFactory().newBug() 
				    .setProduct(product) 
				    .setComponent(component) 
				    .setVersion(version) 
				    .setPlatform(platform) 
				    .setOperatingSystem(os) 
				    .setDescription(description) 
				    .setSummary(summary) 
				    .createBug(); 

			ReportBug report = new ReportBug(bug); 
			conn.executeMethod(report); 

			System.out.println("Raised");
		} catch(ConnectionException e) {
		  
		} catch(BugzillaException e) {
		  
		}
	}

	public void raise(RaiseSemiAutoIssueDialog dialog) {
        String url, summary, description, product, component, version, os, platform, username, password;
        url = dialog.getStringValue(FIELD_URL);
        summary = dialog.getStringValue(FIELD_SUMMARY);
        description = dialog.getStringValue(FIELD_DESCRIPTION);
        product =dialog.getStringValue(FIELD_PRODUCT);
        component = dialog.getStringValue(FIELD_COMPONENT);
        version = dialog.getStringValue(FIELD_VERSION);
        os = dialog.getStringValue(FIELD_OS);
        platform = dialog.getStringValue(FIELD_PLATFORM);
        username = dialog.getStringValue(FIELD_USERNAME);
        password = dialog.getStringValue(FIELD_PASSWORD);
        try {
            raiseOnTracker(url, summary, description, product, component, version, os, platform, username, password);
            System.out.println("Raised");
        } catch(IOException e) {
            log.debug(e.toString());
        }
    } 

    public String getName() {
        return NAME;
    }
    
    public String getId() {
        return NAME.toLowerCase();
    }
}