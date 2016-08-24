/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.bugtracker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;

import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.control.CoreFunctionality;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.alert.AlertEventPublisher;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.ascan.ExtensionActiveScan;
import org.zaproxy.zap.extension.ascan.PolicyManager;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;
/**
 * A ZAP Extension to help user raise issues in bug trackers from within ZAP.
 */
public class ExtensionBugTracker extends ExtensionAdaptor implements ContextPanelFactory,
		ContextDataFactory, EventConsumer, SessionChangedListener  {

	public static final String NAME = "ExtensionBugTracker";
	public Set<Alert> alerts = null;
	
	protected static final String PREFIX = "bugtracker";

	private static final String RESOURCE = "/org/zaproxy/zap/extension/bugtracker/resources";

	private List<BugTracker> bugTrackers = new ArrayList<BugTracker>();
	private PopupSemiAutoIssue popupMsgRaiseSemiAuto;

	public static final String CONTEXT_CONFIG_BUG_TRACKER_RULES = Context.CONTEXT_CONFIG + ".bugTrackerRules";
	public static final String CONTEXT_CONFIG_BUG_TRACKER_RULE = CONTEXT_CONFIG_BUG_TRACKER_RULES + ".bugTrackerRule";
	
	private static final int TYPE_BUG_TRACKER_RULE = 500; // RecordContext.TYPE_USER
	
	/** The bugTrackerRule panels, mapped to each context. */
	private Map<Integer, ContextBugTrackerRulePanel> bugTrackerRulePanelsMap = new HashMap<>();

	/** The context managers, mapped to each context. */
	private Map<Integer, ContextBugTrackerRuleManager> contextManagers = new HashMap<>();
	
	private ExtensionAlert extAlert = null;
	private ExtensionHistory extHistory = null;
	private int lastAlert = -1;

	private static Map<String, Integer> nameToId = new HashMap<String, Integer>();
	private static Map<Integer, String> idToName = new HashMap<Integer, String>();
	private static List<String> allRuleNames;
	private static ExtensionActiveScan extAscan;

    private static final Logger log = Logger.getLogger(ExtensionBugTracker.class);

    public ExtensionBugTracker() {
        super(NAME);
        initialize();
    }
	
    public void addBugTracker(BugTracker bugTracker) {
    	bugTrackers.add(bugTracker);
    }

    public List<BugTracker> getBugTrackers() {
    	return bugTrackers;
    }

	private static ExtensionActiveScan getExtAscan() {
		if (extAscan == null) {
			extAscan = 
					(ExtensionActiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionActiveScan.NAME);
		}
		return extAscan;
	}

	public static List<String> getAllRuleNames() {
		if (allRuleNames == null) {
			allRuleNames = new ArrayList<String>();
			PolicyManager pm = getExtAscan().getPolicyManager();
			ScanPolicy sp = pm.getDefaultScanPolicy();
			for (Plugin plugin : sp.getPluginFactory().getAllPlugin()) {
				allRuleNames.add(plugin.getName());
				nameToId.put(plugin.getName(), Integer.valueOf(plugin.getId()));
				idToName.put(Integer.valueOf(plugin.getId()), plugin.getName());
			}
	    	List<PluginPassiveScanner> listTest = new ArrayList<>(CoreFunctionality.getBuiltInPassiveScanRules());
	    	listTest.addAll(ExtensionFactory.getAddOnLoader().getPassiveScanRules());
	        for (PluginPassiveScanner scanner : listTest) {
	        	if (scanner.getName() != null) {
	        		allRuleNames.add(scanner.getName());
					nameToId.put(scanner.getName(), Integer.valueOf(scanner.getPluginId()));
					idToName.put(Integer.valueOf(scanner.getPluginId()), scanner.getName());
	        	}
	        }
			Collections.sort(allRuleNames);
		}
		return allRuleNames;
	}

	public static int getIdForRuleName(String name) {
		if (allRuleNames == null) {
			// init
			getAllRuleNames();
		}
		return nameToId.get(name);
	}

	public static String getRuleNameForId(int ruleId) {
		if (allRuleNames == null) {
			// init
			getAllRuleNames();
		}
		return idToName.get(Integer.valueOf(ruleId));
	}

	private void initialize() {
        ZAP.getEventBus().registerConsumer(this, 
        		AlertEventPublisher.getPublisher().getPublisherName(), 
        		new String[] {AlertEventPublisher.ALERT_ADDED_EVENT});
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    
	    BugTrackerGithub githubTracker = new BugTrackerGithub();
        extensionHook.addOptionsParamSet(githubTracker.getOptions());
        BugTrackerBugzilla bugzillaTracker = new BugTrackerBugzilla();
        extensionHook.addOptionsParamSet(bugzillaTracker.getOptions());

	    extensionHook.addSessionListener(this);
	    
		// Register this as a context data factory
		Model.getSingleton().addContextDataFactory(this);
	    if (getView() != null) {
	    	addBugTracker(githubTracker);
			addBugTracker(bugzillaTracker);
			getView().addContextPanelFactory(this);
			View.getSingleton().getOptionsDialog("").addParamPanel(new String[]{Constant.messages.getString("bugtracker.name")}, githubTracker.getOptionsPanel(), true);
			View.getSingleton().getOptionsDialog("").addParamPanel(new String[]{Constant.messages.getString("bugtracker.name")}, bugzillaTracker.getOptionsPanel(), true);
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMsgRaiseSemiAuto());
	    }

	}

	/**
	 * Gets the context alert bugTrackerRule manager for a given context.
	 * 
	 * @param contextId the context id
	 * @return the context alert bugTrackerRule manager
	 */
	
	public ContextBugTrackerRuleManager getContextBugTrackerRuleManager(int contextId) {
		ContextBugTrackerRuleManager manager = contextManagers.get(contextId);
		if (manager == null) {
			manager = new ContextBugTrackerRuleManager(contextId);
			contextManagers.put(contextId, manager);
		}
		return manager;
	}

	@Override
	public void loadContextData(Session session, Context context) {
		try {
			List<String> encodedBugTrackerRules = session.getContextDataStrings(context.getIndex(),
					TYPE_BUG_TRACKER_RULE);
			ContextBugTrackerRuleManager afManager = getContextBugTrackerRuleManager(context.getIndex());
			for (String e : encodedBugTrackerRules) {
				BugTrackerRule af = BugTrackerRule.decode(context.getIndex(), e);
				afManager.addBugTrackerRule(af);
			}
		} catch (Exception ex) {
			log.error("Unable to load BugTrackerRules.", ex);
		}
	}

	@Override
	public void persistContextData(Session session, Context context) {
		try {
			List<String> encodedBugTrackerRules = new ArrayList<>();
			ContextBugTrackerRuleManager afManager = getContextBugTrackerRuleManager(context.getIndex());
			if (afManager != null) {
				for (BugTrackerRule af : afManager.getBugTrackerRules()) {
					encodedBugTrackerRules.add(BugTrackerRule.encode(af));
				}
				session.setContextData(context.getIndex(), TYPE_BUG_TRACKER_RULE, encodedBugTrackerRules);
			}
		} catch (Exception ex) {
			log.error("Unable to persist BugTrackerRules", ex);
		}
	}

	@Override
	public void exportContextData(Context ctx, Configuration config) {
		ContextBugTrackerRuleManager m = getContextBugTrackerRuleManager(ctx.getIndex());
		if (m != null) {
			for (BugTrackerRule af : m.getBugTrackerRules()) {
				config.addProperty(CONTEXT_CONFIG_BUG_TRACKER_RULE, 
						BugTrackerRule.encode(af));
			}
		}
	}

	@Override
	public void importContextData(Context ctx, Configuration config) {
		List<Object> list = config.getList(CONTEXT_CONFIG_BUG_TRACKER_RULE);
		ContextBugTrackerRuleManager m = getContextBugTrackerRuleManager(ctx.getIndex());
		for (Object o : list) {
			BugTrackerRule af = BugTrackerRule.decode(ctx.getIndex(), o.toString());
			m.addBugTrackerRule(af);
		}
	}

	@Override
	public AbstractContextPropertiesPanel getContextPanel(Context ctx) {
		return getContextPanel(ctx.getIndex());
	}

	/**
	 * Gets the context panel for a given context.
	 * 
	 * @param contextId the context id
	 * @return the context panel
	 */
	private ContextBugTrackerRulePanel getContextPanel(int contextId) {
		ContextBugTrackerRulePanel panel = this.bugTrackerRulePanelsMap.get(contextId);
		if (panel == null) {
			panel = new ContextBugTrackerRulePanel(this, contextId);
			this.bugTrackerRulePanelsMap.put(contextId, panel);
		}
		return panel;
	}

	@Override
	public void discardContexts() {
		this.contextManagers.clear();
		this.bugTrackerRulePanelsMap.clear();
	}

	@Override
	public void discardContext(Context ctx) {
		this.contextManagers.remove(ctx.getIndex());
		this.bugTrackerRulePanelsMap.remove(ctx.getIndex());
	}
	
	private ExtensionAlert getExtAlert() {
		if (extAlert == null) {
			extAlert = Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
		}
		return extAlert;
	}
	
	private ExtensionHistory getExtHistory() {
		if (extHistory == null) {
			extHistory = Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);
		}
		return extHistory;
	}
	
	@Override
	public void eventReceived(Event event) {
		TableAlert tableAlert = Model.getSingleton().getDb().getTableAlert();

		String alertId = event.getParameters().get(AlertEventPublisher.ALERT_ID);
		if (alertId != null) {
			// From 2.4.3 an alertId is included with these events, which makes life much simpler!
			try {
				handleAlert(tableAlert.read(Integer.parseInt(alertId)));
			} catch (Exception e) {
				log.error("Error handling alert", e);
			}
		} else {
			// Required for pre 2.4.3 versions
			RecordAlert recordAlert;
			while (true) {
				try {
					this.lastAlert ++;
					recordAlert = tableAlert.read(this.lastAlert);
					if (recordAlert == null) {
						break;
					}
					handleAlert(recordAlert);
					
				} catch (DatabaseException e) {
					break;
				}
			}
			// The loop will always go 1 further than necessary
			this.lastAlert--;
		}
	}
	
	private void handleAlert(final RecordAlert recordAlert) {
		final Alert alert = this.getAlert(recordAlert);
		if (alert == null || alert.getHistoryRef() == null) {
			log.error("No alert or href for " + recordAlert.getAlertId() + " " + recordAlert.getHistoryId());
		} else {
			if (alert.getHistoryRef().getSiteNode() != null) {
				this.handleAlert(alert);
			} else {
				// Have to add the SiteNode on the EDT
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						try {
							StructuralNode node = SessionStructure.addPath(Model.getSingleton().getSession(), alert.getHistoryRef(), 
									alert.getHistoryRef().getHttpMessage());
							
							if (node instanceof StructuralSiteNode) {
								StructuralSiteNode ssn = (StructuralSiteNode) node;
								alert.getHistoryRef().setSiteNode(ssn.getSiteNode());
							}
							handleAlert(alert);
						} catch (Exception e) {
							log.error("Error handling alert", e);
						}
					}});
			}
		}
	}


	private void handleAlert(Alert alert) {
		String uri = alert.getUri();
		log.debug("Alert: " + this.lastAlert + " URL: " + uri);
		// Loop through rules and apply as necessary..
		for (ContextBugTrackerRuleManager mgr : this.contextManagers.values()) {
			Context context = Model.getSingleton().getSession().getContext(mgr.getContextId());
			if (context.isInContext(uri)) {
				log.debug("Is in context " + context.getIndex() + 
						" got " + mgr.getBugTrackerRules().size() + " bugTrackerRules");
				// Its in this context
				for (BugTrackerRule bugTrackerRule : mgr.getBugTrackerRules()) {
					if (! bugTrackerRule.isEnabled()) {
						// rule ids dont match
						log.debug("Filter disabled");
						continue;
					}
					if (bugTrackerRule.getRuleId() != alert.getPluginId()) {
						// rule ids dont match
						log.debug("Filter didnt match plugin id: " + 
								bugTrackerRule.getRuleId() + " != " + alert.getPluginId());
						continue;
					}
					if (bugTrackerRule.getUrl() != null && bugTrackerRule.getUrl().length() > 0) {
						if (bugTrackerRule.isRegex()) {
							Pattern p = Pattern.compile(bugTrackerRule.getUrl());
							if (! p.matcher(uri).matches()) {
								// URL pattern doesnt match
								log.debug("Filter didnt match URL regex: " + bugTrackerRule.getUrl() + " url: " + uri);
								continue;
							}
						} else if (!bugTrackerRule.getUrl().equals(uri)) {
							// URL doesnt match
							log.debug("Filter didnt match URL: " + bugTrackerRule.getUrl());
							continue;
						}
					}
					if (bugTrackerRule.getParameter() != null && bugTrackerRule.getParameter().length() > 0) {
						if (! bugTrackerRule.getParameter().equals(alert.getParam())) {
							// Parameter doesnt match
							log.debug("Filter didnt match parameter: " + bugTrackerRule.getParameter() + 
									" != " + alert.getParam());
							continue;
						}
					}
					if (bugTrackerRule.getNewRisk() > -2) {
						if (bugTrackerRule.getNewRisk() != alert.getRisk()) {
							log.debug("Filter didnt match Risk: " + bugTrackerRule.getNewRisk() +
								    " != " + alert.getRisk());
							continue;
						}
					}
					this.alerts = new HashSet<>();
					this.alerts.add(alert);
					// BugTrackerGithub githubIssue = new BugTrackerGithub();
					// githubIssue.raise();
					break;
				}
			}
		}

	}
	
	private Alert getAlert(RecordAlert recordAlert) {
		int historyId = recordAlert.getHistoryId();
		if (historyId > 0) {
			HistoryReference href = this.getExtHistory().getHistoryReference(historyId);
			return new Alert(recordAlert, href);
		} else {
			// Not ideal :/
			return new Alert(recordAlert);
		}
	}

	@Override
	public void sessionChanged(Session session) {
		this.lastAlert = -1;
	}

	@Override
	public void sessionAboutToChange(Session session) {
		// Ignore
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// Ignore
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}

	@Override
	public boolean canUnload() {
		return true;
	}

	@Override
	public void unload() {
		super.unload();
	}

	private PopupSemiAutoIssue getPopupMsgRaiseSemiAuto() {
		if (popupMsgRaiseSemiAuto  == null) {
			popupMsgRaiseSemiAuto = new PopupSemiAutoIssue(this,
					Constant.messages.getString(PREFIX + ".popup.issue.semi"));
		}
		popupMsgRaiseSemiAuto.setExtension(Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class)); 
		return popupMsgRaiseSemiAuto;
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString(PREFIX + ".desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_EXTENSIONS_PAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}