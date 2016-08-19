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

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

public class ContextAlertFilterPanel extends AbstractContextPropertiesPanel implements ItemListener {

	private AlertFiltersMultipleOptionsPanel alertFilterOptionsPanel;
	private AlertFiltersMultipleOptionsPanel alertFilterOptionsPanel1;
	private ContextAlertFilterManager contextManager;
	private ExtensionBugTracker extension;
	private AlertFilterTableModel alertFilterTableModel;
    private JLabel jl = new JLabel("Choose a Bug Tracker to Configure");
    private List<String> item = new ArrayList<String>();
    private JComboBox jb = null;
    private GridBagConstraints c = null;
    private JPanel panelBugTrackers = null;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3920598166129639573L;
	private static final String PANEL_NAME = Constant.messages.getString("bugTracker.panel.title");

	public ContextAlertFilterPanel(ExtensionBugTracker extension, int contextId) {
		super(contextId);
		this.extension = extension;
		this.contextManager = extension.getContextAlertFilterManager(contextId);
		initialize();
	}

	public static String getPanelName(int contextId) {
		// Panel names have to be unique, so prefix with the context id
		return contextId + ": " + PANEL_NAME;
	}

    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.setName(getPanelName(getContextIndex()));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0D;
        c.weighty = 0.4D;
        c.fill = GridBagConstraints.BOTH;

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Rules"), BorderLayout.PAGE_START);
        alertFilterTableModel = new AlertFilterTableModel();
        alertFilterOptionsPanel = new AlertFiltersMultipleOptionsPanel(
                this.extension,
                alertFilterTableModel,
                getContextIndex());
        panel.add(alertFilterOptionsPanel, BorderLayout.CENTER);
        add(panel, c);

        List<BugTracker> bugTrackers = extension.getBugTrackers();
        for(BugTracker bugTracker: bugTrackers) {
        	item.add(bugTracker.getName());
        }
		jb = new JComboBox(item.toArray());
		c.weighty = 0.02D;
		c.gridx = 0;
		c.gridy = 1;
        add(jb, c);
        jb.addItemListener(this);
        
        BugTrackerGithub githubConfig = new BugTrackerGithub();

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 0.58D;

        panelBugTrackers = githubConfig.getConfigTable();

        add(panelBugTrackers, c);
    }

    public void itemStateChanged(ItemEvent ie)
    {
        String getItem = (String)jb.getSelectedItem();
        
        List<BugTracker> bugTrackers = extension.getBugTrackers();
        for(BugTracker bugTracker: bugTrackers) {
        	if(getItem.equals(bugTracker.getName())) {
        		remove(panelBugTrackers);
        		panelBugTrackers = bugTracker.getConfigTable();
        		add(panelBugTrackers, c);
        	}
        }
    }

	@Override
	public String getHelpIndex() {
		return "addon.alertFilter";
	}

	public static class AlertFiltersMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<AlertFilter> {

		private static final long serialVersionUID = -7216673905642941770L;

		private static final String REMOVE_DIALOG_TITLE = 
				Constant.messages.getString("bugTracker.dialog.remove.title");
		private static final String REMOVE_DIALOG_TEXT = 
				Constant.messages.getString("bugTracker.dialog.remove.text");

		private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = 
				Constant.messages.getString("bugTracker.dialog.remove.button.confirm");
		private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = 
				Constant.messages.getString("bugTracker.dialog.remove.button.cancel");

		private static final String REMOVE_DIALOG_CHECKBOX_LABEL = 
				Constant.messages.getString("bugTracker.dialog.remove.checkbox.label");

		private DialogAddAlertFilter addDialog = null;
		private DialogModifyAlertFilter modifyDialog = null;
		private ExtensionBugTracker extension;
		private Context uiSharedContext;

		public AlertFiltersMultipleOptionsPanel(ExtensionBugTracker extension, AlertFilterTableModel model,
				int contextId) {
			super(model);
			this.extension = extension;

			Component rendererComponent;
			if (getTable().getColumnExt(0).getHeaderRenderer()==null) {// If there isn't a header renderer then get the default renderer
				rendererComponent = getTable().getTableHeader().getDefaultRenderer().getTableCellRendererComponent(null, getTable().getColumnExt(0).getHeaderValue(), false, false, 0, 0);
			} else {// If there is a custom renderer then get it
				rendererComponent = getTable().getColumnExt(0).getHeaderRenderer().getTableCellRendererComponent(null, getTable().getColumnExt(0).getHeaderValue(), false, false, 0, 0);
			}
			
			getTable().getColumnExt(0).setMaxWidth(rendererComponent.getMaximumSize().width);
			getTable().setSortOrder(1, SortOrder.ASCENDING);
			getTable().packAll();
		}

		@Override
		public AlertFilter showAddDialogue() {
			if (addDialog == null) {
				addDialog = new DialogAddAlertFilter(View.getSingleton().getOptionsDialog(null), this.extension);
				addDialog.pack();
			}
			addDialog.setWorkingContext(this.uiSharedContext);
			addDialog.setVisible(true);

			AlertFilter alertFilter = addDialog.getAlertFilter();
			addDialog.clear();

			return alertFilter;
		}

		@Override
		public AlertFilter showModifyDialogue(AlertFilter alertFilter) {
			if (modifyDialog == null) {
				modifyDialog = new DialogModifyAlertFilter(View.getSingleton().getOptionsDialog(null),
						this.extension);
				modifyDialog.pack();
			}
			modifyDialog.setWorkingContext(this.uiSharedContext);
			modifyDialog.setAlertFilter(alertFilter);
			modifyDialog.setVisible(true);

			alertFilter = modifyDialog.getAlertFilter();
			modifyDialog.clear();

			return alertFilter;
		}

		@Override
		public boolean showRemoveDialogue(AlertFilter e) {
			JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
			Object[] messages = { REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox };
			int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(), messages,
					REMOVE_DIALOG_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new String[] { REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL },
					null);

			if (option == JOptionPane.OK_OPTION) {
				setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());
				return true;
			}

			return false;
		}

		protected void setWorkingContext(Context context) {
			this.uiSharedContext = context;
		}

	}

	@Override
	public void initContextData(Session session, Context uiCommonContext) {
		this.alertFilterOptionsPanel.setWorkingContext(uiCommonContext);
		this.alertFilterTableModel.setAlertFilters(this.contextManager.getAlertFilters());
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Nothing to validate
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		this.contextManager.setAlertFilters(alertFilterTableModel.getAlertFilters());

	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		// Data is already saved in the uiSharedContext
	}

	protected AlertFilterTableModel getAlertFiltersTableModel() {
		return alertFilterTableModel;
	}
}
