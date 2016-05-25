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
package org.zaproxy.zap.extension.bugTracker;

import java.awt.Dialog;

import org.parosproxy.paros.Constant;

public class DialogModifyAlertFilter extends DialogAddAlertFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7828871270310672334L;
	private static final String DIALOG_TITLE = 
			Constant.messages.getString("bugTracker.dialog.modify.title");

	public DialogModifyAlertFilter(Dialog owner, ExtensionBugTracker extension) {
		super(owner, extension, DIALOG_TITLE);
	}

	public void setAlertFilter(AlertFilter alertFilter) {
		this.alertFilter = alertFilter;
	}

	@Override
	protected String getConfirmButtonLabel() {
		return Constant.messages.getString("bugTracker.dialog.modify.button.confirm");
	}

	@Override
	protected void init() {
		if (this.workingContext == null) {
			throw new IllegalStateException(
					"A working Context should be set before setting the 'Add Dialog' visible.");
		}
		log.debug("Initializing modify alertFilter dialog for: " + alertFilter);
		getAlertCombo().setSelectedItem(
				ExtensionBugTracker.getRuleNameForId(alertFilter.getRuleId()));
		getNewLevelCombo().setSelectedItem(
				AlertFilter.getNameForRisk(alertFilter.getNewRisk()));
		getUrlTextField().setText(alertFilter.getUrl());
		getRegexCheckBox().setSelected(alertFilter.isRegex());
		getParamTextField().setText(alertFilter.getParameter());

		getEnabledCheckBox().setSelected(alertFilter.isEnabled());

		this.setConfirmButtonEnabled(true);

	}
}
