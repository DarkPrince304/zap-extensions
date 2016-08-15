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

public class BugTrackerGithubIssue {

    private String FIELD_REPO = "bugTracker.trackers.github.issue.repo";
	private String FIELD_TITLE = "bugTracker.trackers.github.issue.title";
	private String FIELD_BODY = "bugTracker.trackers.github.issue.body";
	private String FIELD_LABELS = "bugTracker.trackers.github.issue.labels";
	private String FIELD_ASSIGNEE = "bugTracker.trackers.github.issue.assignee";
	private String FIELD_USERNAME = "bugTracker.trackers.github.issue.username";
	private String FIELD_PASSWORD = "bugTracker.trackers.github.issue.password";
    private String titleIssue = null;
    private String bodyIssue = null;
    private String labelsIssue = null;

    private static final Logger log = Logger.getLogger(BugTrackerGithubIssue.class);   

    public BugTrackerGithubIssue (Set<Alert> alerts) {
        setTitle(alerts);
        setBody(alerts);
        setLabels(alerts);
    }

    public void createDialogs(RaiseSemiAutoIssueDialog dialog, int index) {
        dialog.addTextField(index, FIELD_REPO, "");
        dialog.addTextField(index, FIELD_TITLE, getTitle());
        dialog.addMultilineField(index, FIELD_BODY, getBody());
        dialog.addTextField(index, FIELD_LABELS, getLabels());
        dialog.addTextField(index, FIELD_ASSIGNEE, "");
        dialog.addTextField(index, FIELD_USERNAME, "");
        dialog.addTextField(index, FIELD_PASSWORD, "");        
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
                log.debug(Constant.messages.getString("bugTracker.popup.issue.msg.auth"));
            }
            log.debug(response);
        } catch(ArrayIndexOutOfBoundsException e) {
            log.debug(Constant.messages.getString("bugTracker.popup.issue.msg.repo"));
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
        password = "mishlet1304";
        System.out.println(repo+ " "+ title + " "+ body + " " + labels + " " + assignee + " " + username + " " + password + " ");
        try {
            raiseOnTracker(repo, title, body, labels, assignee, username, password);
            System.out.println("Raised");
        } catch(IOException e) {
            log.debug(e.toString());
        }
    }

    public void raise(RaiseSemiAutoIssueDialog dialog) {
        String repo, title, body, labels, assignee, username, password;
        repo = dialog.getStringValue(FIELD_REPO);
        title = dialog.getStringValue(FIELD_TITLE);
        body = dialog.getStringValue(FIELD_BODY);
        labels =dialog.getStringValue(FIELD_LABELS);
        assignee = dialog.getStringValue(FIELD_ASSIGNEE);
        username = dialog.getStringValue(FIELD_USERNAME);
        password = dialog.getStringValue(FIELD_PASSWORD);
        System.out.println(repo+ " "+ title + " "+ body + " " + labels + " " + assignee + " " + username + " " + password + " ");
        try {
            raiseOnTracker(repo, title, body, labels, assignee, username, password);
            System.out.println("Raised");
        } catch(IOException e) {
            log.debug(e.toString());
        }
    }

}