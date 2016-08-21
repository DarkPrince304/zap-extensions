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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.SortOrder;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import org.parosproxy.paros.model.OptionsParam;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

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
    private String FIELD_BUGZILLA_CONFIG = "bugTracker.trackers.bugzilla.issue.config";
    private String summaryIssue = null;
    private String descriptionIssue = null;
    private JPanel configTable = null;
    private JPanel bugzillaPanel = null;
    private BugTrackerBugzillaTableModel bugzillaModel = null;
    private RaiseSemiAutoIssueDialog dialog = null;

    private static final Logger log = Logger.getLogger(BugTrackerBugzilla.class);

    public void setDetails(Set<Alert> alerts) {
        setSummary(alerts);
        setDesc(alerts);
    } 

    public void setDialog(RaiseSemiAutoIssueDialog dialog) {
        this.dialog = dialog;
    }

    public BugTrackerBugzilla() {
        initializeConfigTable();
    }

    public void initializeConfigTable() {
        bugzillaPanel = new BugTrackerBugzillaMultipleOptionsPanel(getBugzillaModel());
    }

    public JPanel getConfigPanel() {
        return bugzillaPanel;
    }

    public void createDialogs() {
        dialog.setXWeights(0.1D, 0.9D);
        List<BugTrackerBugzillaConfigParams> configs = getOptions().getConfigs();
        List<String> configNames = new ArrayList<String>();
        for(BugTrackerBugzillaConfigParams config: configs) {
            System.out.println(config.getName()+", "+config.getPassword()+", "+config.getBugzillaUrl());
            configNames.add(config.getName());
        }
        dialog.addComboField(FIELD_BUGZILLA_CONFIG, configNames, "");
        dialog.addTextField(FIELD_URL, "");
		dialog.addTextField(FIELD_PRODUCT, "");
		dialog.addTextField(FIELD_COMPONENT, "");
		dialog.addTextField(FIELD_VERSION, "");
		dialog.addTextField(FIELD_OS, "");
		dialog.addTextField(FIELD_PLATFORM, "");
		dialog.addTextField(FIELD_DESCRIPTION, getDesc());
		dialog.addTextField(FIELD_SUMMARY, getSummary());
		dialog.addTextField(FIELD_USERNAME, "");
		dialog.addTextField(FIELD_PASSWORD, "");
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

    public void raise() {
        String url, summary, description, product, component, version, os, platform, username, password;
        url = "https://landfill.bugzilla.org/bugzilla-5.0-branch/";
        summary = getSummary();
        description = getDesc();
        product = "WorldControl";
        component = "WeatherControl";
        version = "1.0";
        os = "Windows XP";
        platform = "PC";
        username = "sanchitlucknow@gmail.com";
        password = "";
        try {
            raiseOnTracker(url, summary, description, product, component, version, os, platform, username, password);
            System.out.println("Raised");
        } catch(IOException e) {
            log.debug(e.toString());
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

      /**
     * This method initializes authModel    
     *  
     * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel    
     */    
    private BugTrackerBugzillaTableModel getBugzillaModel() {
        if (bugzillaModel == null) {
            bugzillaModel = new BugTrackerBugzillaTableModel();
        }
        return bugzillaModel;
    }

    private BugTrackerBugzillaParam options;

    public BugTrackerBugzillaParam getOptions() {
        if (options == null) {
            options = new BugTrackerBugzillaParam();
        }
        return options;
    }

    public static class BugTrackerBugzillaMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<BugTrackerBugzillaConfigParams> {
        
        private static final long serialVersionUID = -115340627058929308L;
        
        private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("bugTracker.trackers.bugzilla.dialog.config.remove.title");
        private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("bugTracker.trackers.bugzilla.dialog.config.remove.text");
        
        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("bugTracker.trackers.bugzilla.dialog.config.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("bugTracker.trackers.bugzilla.dialog.config.remove.button.cancel");
        
        private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("bugTracker.trackers.bugzilla.dialog.config.remove.checkbox.label");
        
        private DialogAddBugzillaConfig addDialog = null;
        private DialogModifyBugzillaConfig modifyDialog = null;
        
        private BugTrackerBugzillaTableModel model;
        
        public BugTrackerBugzillaMultipleOptionsPanel(BugTrackerBugzillaTableModel model) {
            super(model);
            
            this.model = model;
            
            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public BugTrackerBugzillaConfigParams showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddBugzillaConfig(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setConfigs(model.getElements());
            addDialog.setVisible(true);
            
            BugTrackerBugzillaConfigParams config = addDialog.getConfig();
            addDialog.clear();
            
            return config;
        }
        
        @Override
        public BugTrackerBugzillaConfigParams showModifyDialogue(BugTrackerBugzillaConfigParams e) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyBugzillaConfig(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setConfigs(model.getElements());
            modifyDialog.setConfig(e);
            modifyDialog.setVisible(true);
            
            BugTrackerBugzillaConfigParams config = modifyDialog.getConfig();
            modifyDialog.clear();
            
            if (!config.equals(e)) {
                return config;
            }
            
            return null;
        }
        
        @Override
        public boolean showRemoveDialogue(BugTrackerBugzillaConfigParams e) {
            JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
            int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(), messages, REMOVE_DIALOG_TITLE,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[] { REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL }, null);

            if (option == JOptionPane.OK_OPTION) {
                setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());
                
                return true;
            }
            
            return false;
        }
    }

    private BugTrackerBugzillaOptionsPanel optionsPanel;

    public BugTrackerBugzillaOptionsPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new BugTrackerBugzillaOptionsPanel();
        }
        return optionsPanel;
    }
}