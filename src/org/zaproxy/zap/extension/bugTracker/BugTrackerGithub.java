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
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.HttpException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

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
import org.parosproxy.paros.view.AbstractParamPanel;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

public class BugTrackerGithub extends BugTracker {

    private String NAME = "Github";
    private String FIELD_REPO = "bugTracker.trackers.github.issue.repo";
	private String FIELD_TITLE = "bugTracker.trackers.github.issue.title";
	private String FIELD_BODY = "bugTracker.trackers.github.issue.body";
	private String FIELD_LABELS = "bugTracker.trackers.github.issue.labels";
    private String FIELD_ASSIGNEE_MANUAL = "bugTracker.trackers.github.issue.assignee.manual";
    private String FIELD_ASSIGNEE_LIST = "bugTracker.trackers.github.issue.assignee.list";
	private String FIELD_USERNAME = "bugTracker.trackers.github.issue.username";
	private String FIELD_PASSWORD = "bugTracker.trackers.github.issue.password";
    private String FIELD_GITHUB_CONFIG = "bugTracker.trackers.github.issue.config";
    private String titleIssue = null;
    private String bodyIssue = null;
    private String labelsIssue = null;
    private JPanel configTable = null;
    private JPanel githubPanel = null;
    private BugTrackerGithubTableModel githubModel = null;
    private RaiseSemiAutoIssueDialog dialog = null;

    private static final Logger log = Logger.getLogger(BugTrackerGithub.class);   

    public BugTrackerGithub() {
        initializeConfigTable();
    }

    public void setDetails(Set<Alert> alerts) {
        setTitle(alerts);
        setBody(alerts);
        setLabels(alerts);
    }

    public void setDialog(RaiseSemiAutoIssueDialog dialog) {
        this.dialog = dialog;
    }

    public void initializeConfigTable() {
        githubPanel = new BugTrackerGithubMultipleOptionsPanel(getGithubModel());
    }

    public JPanel getConfigPanel() {
        return githubPanel;
    }

    public void createDialogs() {
        List<BugTrackerGithubConfigParams> configs = getOptions().getConfigs();
        Set<String> collaborators = new HashSet<String>();
        List<String> configNames = new ArrayList<String>();
        for(BugTrackerGithubConfigParams config: configs) {
            configNames.add(config.getName());
        }
        dialog.setXWeights(0.1D, 0.9D);
        dialog.addComboField(FIELD_GITHUB_CONFIG, configNames, "");
        dialog.addTextField(FIELD_REPO, "");
        dialog.addTextField(FIELD_TITLE, getTitle());
        dialog.addMultilineField(FIELD_BODY, getBody());
        dialog.addTextField(FIELD_LABELS, getLabels());
        dialog.addTextField(FIELD_ASSIGNEE_MANUAL, "");
        dialog.addComboField(FIELD_ASSIGNEE_LIST, new ArrayList<String>(), "");
        dialog.addTextField(FIELD_USERNAME, "");
        dialog.addTextField(FIELD_PASSWORD, "");
        updateAssigneeList();        
        dialog.addFieldListener(FIELD_GITHUB_CONFIG, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAssigneeList();
            }
        });
    }

    public void updateAssigneeList() {
        try {
            String currentItem = dialog.getStringValue(FIELD_GITHUB_CONFIG);
            Set<String> collaborators = new HashSet<String>();
            List<BugTrackerGithubConfigParams> configs = getOptions().getConfigs();
            for(BugTrackerGithubConfigParams config: configs) {
                if(config.getName().equals(currentItem)) {
                    collaborators = getCollaborators(config.getName(), config.getPassword(), config.getRepoUrl());
                    if(collaborators.size() > 0) {
                        List<String> assignees = new ArrayList<String>(collaborators);
                        dialog.setComboFields(FIELD_ASSIGNEE_LIST, assignees, ""); 
                    } else {
                        List<String> assignees = new ArrayList<String>();
                        dialog.setComboFields(FIELD_ASSIGNEE_LIST, assignees, ""); 
                    }
                }
            }
        } catch(IOException e) {
            log.debug(Constant.messages.getString("bugTracker.trackers.github.issue.msg.param"));
        }
    }

    public void setTitle(Set<Alert> alerts) {
        StringBuilder title = new StringBuilder("");
        for(Alert alert: alerts) {
            if(alert.getName().length() > 0 ) {
                title.append(alert.getName().toString() + ", ");
            }
        }
        title.replace(title.length()-2, title.length()-1, "");
        titleIssue = title.toString();
    }
    
    public void setBody(Set<Alert> alerts) {
        StringBuilder body = new StringBuilder("");
        for(Alert alert: alerts) {

            if(alert.getName().length() > 0 ) {
                body.append(" *ALERT IN QUESTION* \n" + alert.getName().toString() + "\n\n");
            }
            if(alert.getUri().length() > 0 ) {
                body.append(" *URL* \n" + alert.getUri().toString() + "\n\n");
            }
            if(alert.getDescription().length() > 0 ) {
                body.append(" *DESCRIPTION* \n" + alert.getDescription().toString() + "\n\n");
            }
            if(alert.getOtherInfo().length() > 0 ) {
                body.append(" *OTHER INFO* \n" + alert.getOtherInfo().toString() + "\n\n");
            }
            if(alert.getSolution().length() > 0 ) {
                body.append(" *SOLUTION* \n" + alert.getSolution().toString() + "\n\n");
            }
            if(alert.getReference().length() > 0 ) {
                body.append(" *REFERENCE* \n" + alert.getReference().toString() + "\n\n");
            }
            if(alert.getParam().length() > 0 ) {
                body.append(" *PARAMETER* \n" + alert.getParam().toString() + "\n\n");
            }
            if(alert.getAttack().length() > 0 ) {
                body.append(" *ATTACK* \n" + alert.getAttack().toString() + "\n\n");
            }
            if(alert.getEvidence().length() > 0 ) {
                body.append(" *EVIDENCE* \n" + alert.getEvidence().toString() + "\n\n\n\n");
            }

        }
        bodyIssue = body.toString();
    }
    
    public void setLabels(Set<Alert> alerts) {
        StringBuilder labels = new StringBuilder("");
        for(Alert alert: alerts) {

            if(alert.getRisk() >= 0 ) {
                labels.append("Risk: " + alert.MSG_RISK[alert.getRisk()] + ", ");
            }
            if(alert.getConfidence() >= 0 ) {
                labels.append("Conf: " + alert.MSG_CONFIDENCE[alert.getConfidence()] + ", ");
            }
            if(alert.getCweId() >= 0 ) {
                labels.append("CWE: " + alert.getCweId() + ", ");
            }
            if(alert.getWascId() >= 0 ) {
                labels.append("WASC: " + alert.getWascId() + ", ");
            }

        }
        labelsIssue = labels.toString();
    }

    public String getTitle() {
        return this.titleIssue;
    }
    
    public String getBody() {
        return this.bodyIssue;
    }
    
    public String getLabels() {
        return this.labelsIssue;
    }

    public Set<String> getCollaborators(String username, String password, String repo) throws IOException{
        GitHub github = GitHub.connectUsingPassword(username, password);
        Set<String> collaborators = null;
        try {
            GHRepository repository = github.getRepository(repo);
            collaborators = repository.getCollaboratorNames();
        } catch(ArrayIndexOutOfBoundsException e) {
            log.debug(Constant.messages.getString("bugTracker.trackers.github.issue.msg.repo"));
        } catch(HttpException e) {
            log.debug(e.toString());
        }
        return collaborators;
    }

    public void raiseOnTracker(String repo, String title, String body, String labels, String assignee, String username, String password) throws IOException {
        GitHub github = GitHub.connectUsingPassword(username, password);
        try {
            GHRepository repository = github.getRepository(repo);
            GHIssueBuilder issue = repository.createIssue(title);
            issue.body(body);
            issue.assignee(assignee);
            String[] labelArray = labels.split(",\\s");
            for( int i = 0; i < labelArray.length; i++ ) {
                issue.label(labelArray[i]);
            }
            String response = issue.create().toString();
            System.out.println(response);
            if(response.contains("401")) {
                log.debug(Constant.messages.getString("bugTracker.trackers.github.issue.msg.auth"));
            }
            log.debug(response);
        } catch(ArrayIndexOutOfBoundsException e) {
            log.debug(Constant.messages.getString("bugTracker.trackers.github.issue.msg.repo"));
        } catch(HttpException e) {
            log.debug(e.toString());
        }
    }

    public void raise() {
        String repo, title, body, labels, assignee, username, password;
        repo = "darkprince304/structjs";
        title = getTitle();
        body = getBody();
        labels = getLabels();
        assignee = "darkprince304";
        username = "darkprince304";
        password = "";
        System.out.println(repo+ " "+ title + " "+ body + " " + labels + " " + assignee + " " + username + " " + password + " ");
        try {
            raiseOnTracker(repo, title, body, labels, assignee, username, password);
            System.out.println("Raised");
        } catch(IOException e) {
            log.debug(Constant.messages.getString("bugTracker.trackers.github.issue.msg.param"));
        }
    }

    public void raise(RaiseSemiAutoIssueDialog dialog) {
        String repo, title, body, labels, assignee, username, password, configGithub;
        repo = dialog.getStringValue(FIELD_REPO);
        title = dialog.getStringValue(FIELD_TITLE);
        body = dialog.getStringValue(FIELD_BODY);
        labels =dialog.getStringValue(FIELD_LABELS);
        assignee = dialog.getStringValue(FIELD_ASSIGNEE_MANUAL);
        username = dialog.getStringValue(FIELD_USERNAME);
        password = dialog.getStringValue(FIELD_PASSWORD);
        configGithub = dialog.getStringValue(FIELD_GITHUB_CONFIG);
        if(repo.equals("") || username.equals("") || password.equals("")) {
            List<BugTrackerGithubConfigParams> configs = getOptions().getConfigs();
            for(BugTrackerGithubConfigParams config: configs) {
                if(config.getName().equals(configGithub)) {
                    repo = config.getRepoUrl();
                    username = config.getName();
                    password = config.getPassword();
                }
            }
        }
        if(assignee.equals("")) {
            assignee = dialog.getStringValue(FIELD_ASSIGNEE_LIST);
        }
        System.out.println(repo+ " "+ title + " "+ body + " " + labels + " " + assignee + " " + username + " " + password + " ");
        try {
            raiseOnTracker(repo, title, body, labels, assignee, username, password);
            System.out.println("Raised");
        } catch(IOException e) {
            log.debug(Constant.messages.getString("bugTracker.trackers.github.issue.msg.param"));
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
    private BugTrackerGithubTableModel getGithubModel() {
        if (githubModel == null) {
            githubModel = new BugTrackerGithubTableModel();
        }
        return githubModel;
    }

    private BugTrackerGithubParam options;

    public BugTrackerGithubParam getOptions() {
        if (options == null) {
            options = new BugTrackerGithubParam();
        }
        return options;
    }

    public static class BugTrackerGithubMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<BugTrackerGithubConfigParams> {
        
        private static final long serialVersionUID = -115340627058929308L;
        
        private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("bugTracker.trackers.github.dialog.config.remove.title");
        private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("bugTracker.trackers.github.dialog.config.remove.text");
        
        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("bugTracker.trackers.github.dialog.config.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("bugTracker.trackers.github.dialog.config.remove.button.cancel");
        
        private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("bugTracker.trackers.github.dialog.config.remove.checkbox.label");
        
        private DialogAddGithubConfig addDialog = null;
        private DialogModifyGithubConfig modifyDialog = null;
        
        private BugTrackerGithubTableModel model;
        
        public BugTrackerGithubMultipleOptionsPanel(BugTrackerGithubTableModel model) {
            super(model);
            
            this.model = model;
            
            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public BugTrackerGithubConfigParams showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddGithubConfig(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setConfigs(model.getElements());
            addDialog.setVisible(true);
            
            BugTrackerGithubConfigParams config = addDialog.getConfig();
            addDialog.clear();
            
            return config;
        }
        
        @Override
        public BugTrackerGithubConfigParams showModifyDialogue(BugTrackerGithubConfigParams e) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyGithubConfig(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setConfigs(model.getElements());
            modifyDialog.setConfig(e);
            modifyDialog.setVisible(true);
            
            BugTrackerGithubConfigParams config = modifyDialog.getConfig();
            modifyDialog.clear();
            
            if (!config.equals(e)) {
                return config;
            }
            
            return null;
        }
        
        @Override
        public boolean showRemoveDialogue(BugTrackerGithubConfigParams e) {
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

    private BugTrackerGithubOptionsPanel optionsPanel;

    public BugTrackerGithubOptionsPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new BugTrackerGithubOptionsPanel();
        }
        return optionsPanel;
    }


}