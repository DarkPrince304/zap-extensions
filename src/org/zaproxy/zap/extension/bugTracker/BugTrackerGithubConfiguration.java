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

public class BugTrackerGithubConfiguration {

	JPanel credentialTable = null;
	JPanel configTable = null;

	public BugTrackerGithubConfiguration(){
		initializeCredentialTable();       
		initializeConfigTable();        
	}

	public void initializeCredentialTable() {
		String[] columnNames = {"Username/Email","Password"};
        Object[][] data = {
        {"Kathy", "Smith",
         "Snowboarding", new Integer(5), new Boolean(false)},
        {"John", "Doe",
         "Rowing", new Integer(3), new Boolean(true)},
        {"Sue", "Black",
         "Knitting", new Integer(2), new Boolean(false)},
        {"Jane", "White",
         "Speed reading", new Integer(20), new Boolean(true)},
        {"Joe", "Brown",
         "Pool", new Integer(10), new Boolean(false)}
        };


        credentialTable = new JPanel(new BorderLayout());
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
        credentialTable.add(new JLabel("User Credentials"), BorderLayout.PAGE_START);
        credentialTable.add(scrollPane, BorderLayout.CENTER);
        credentialTable.add(buttonLayout, BorderLayout.SOUTH);
	}

	public void initializeConfigTable() {
		String[] columnNames = {"Repository Path"};
        Object[][] data = {
        {"Kathy", "Smith",
         "Snowboarding", new Integer(5), new Boolean(false)},
        {"John", "Doe",
         "Rowing", new Integer(3), new Boolean(true)},
        {"Sue", "Black",
         "Knitting", new Integer(2), new Boolean(false)},
        {"Jane", "White",
         "Speed reading", new Integer(20), new Boolean(true)},
        {"Joe", "Brown",
         "Pool", new Integer(10), new Boolean(false)}
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
        configTable.add(new JLabel("User Credentials"), BorderLayout.PAGE_START);
        configTable.add(scrollPane, BorderLayout.CENTER);
        configTable.add(buttonLayout, BorderLayout.SOUTH);
	}

	public JPanel getCredentialsTable() {
		return credentialTable;
	}

	public JPanel getConfigTable() {
		return configTable;
	}
}