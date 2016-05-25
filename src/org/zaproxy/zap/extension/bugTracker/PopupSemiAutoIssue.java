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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Set;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.parosproxy.paros.view.View;
import java.awt.Dimension;
import org.zaproxy.zap.extension.alert.AlertPanel;
import org.zaproxy.zap.extension.alert.PopupMenuItemAlert;
import org.zaproxy.zap.view.StandardFieldsDialog;

public class PopupSemiAutoIssue extends PopupMenuItemAlert {

	private static final long serialVersionUID = 1L;

    private ExtensionBugTracker extBT = null;

    private ExtensionAlert extension = null;
	private ExtensionHistory extHist = null; 
    private RaiseSemiAutoIssueDialog raiseSemiAutoIssueDialog = null;

    public PopupSemiAutoIssue(ExtensionBugTracker ext, String label) {
        super(label, true);
        this.extBT = ext;
    }

    private void showRaiseSemiAutoIssueDialog(Set<Alert> alerts) {
        if (raiseSemiAutoIssueDialog == null) {
            this.extBT.alerts = alerts;
            raiseSemiAutoIssueDialog = new RaiseSemiAutoIssueDialog(this.extBT, View.getSingleton()
                    .getMainFrame(), new Dimension(500, 500));
        } else if (raiseSemiAutoIssueDialog.isVisible()) {
            bringToFront(raiseSemiAutoIssueDialog);
            return;
        }

        raiseSemiAutoIssueDialog.setAlert(alerts);
        raiseSemiAutoIssueDialog.setVisible(true);
    }

    @Override
    public void performActions(Set<Alert> alertNodes) {
        System.out.println(alertNodes);
        showRaiseSemiAutoIssueDialog(alertNodes);
    }

    @Override
    public void performAction(Alert alert) {
        // Empty because alerts are retrieved, stored and sent to the dialogs in a list
    }
    
    void setExtension(ExtensionAlert extension) {
        this.extension = extension;
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    private void bringToFront(StandardFieldsDialog dialog) {
        dialog.toFront();
        dialog.requestFocus();
    }
    
}
