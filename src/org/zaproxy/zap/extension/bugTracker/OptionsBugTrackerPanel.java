/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class OptionsBugTrackerPanel extends AbstractParamPanel implements ItemListener {

	private static final long serialVersionUID = 1L;
    private List<String> item = new ArrayList<String>();
    private JComboBox jb = null;
    private GridBagConstraints c = null;
    private JPanel panelBugTrackers = null;
    private ExtensionBugTracker extension;
    private List<BugTracker> bugTrackers = null;
	
    public OptionsBugTrackerPanel(ExtensionBugTracker extension) {
        super();
        this.extension = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        bugTrackers = extension.getBugTrackers();
        c = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        this.setName("BugTracker Configuration");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0D;
        c.weighty = 0.4D;
        c.fill = GridBagConstraints.BOTH;

        for(BugTracker bugTracker: bugTrackers) {
            item.add(bugTracker.getName());
        }
        jb = new JComboBox(item.toArray());
        c.weighty = 0.02D;
        c.gridx = 0;
        c.gridy = 1;
        add(jb, c);
        jb.addItemListener(this);
        
        String currentItem = (String)jb.getSelectedItem();

        for(BugTracker bugTracker: bugTrackers) {
            if(currentItem.equals(bugTracker.getName())) {
                panelBugTrackers = bugTracker.getConfigPanel();
                add(panelBugTrackers, c);
                validate();
                break;
            }
        }

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 0.58D;

        add(panelBugTrackers, c);
    }

    public void itemStateChanged(ItemEvent ie)
    {
        String getItem = (String)jb.getSelectedItem();
        
        for(BugTracker bugTracker: bugTrackers) {
            if(getItem.equals(bugTracker.getName())) {
                remove(panelBugTrackers);
                panelBugTrackers = bugTracker.getConfigPanel();
                add(panelBugTrackers, c);
                validate();
                break;
            }
        }
    }

	@Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    String currentItem = (String)jb.getSelectedItem();
        for(BugTracker bugTracker: bugTrackers) {
            if(currentItem.equals(bugTracker.getName())){
                bugTracker.init(optionsParam);
            }
        }
        // AntiCsrfParam param = optionsParam.getAntiCsrfParam();
	    // getAntiCsrfModel().setTokens(param.getTokens());
	    // tokensOptionsPanel.setRemoveWithoutConfirmation(!param.isConfirmRemoveToken());
    }


    @Override
    public void validateParam(Object obj) throws Exception {

    }


    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
        String currentItem = (String)jb.getSelectedItem();
        for(BugTracker bugTracker: bugTrackers) {
            if(currentItem.equals(bugTracker.getName())){
                bugTracker.save(optionsParam);
            }
        }
    }

    @Override
    public String getHelpIndex() {
        return "addon.alertFilter";
    }
}
