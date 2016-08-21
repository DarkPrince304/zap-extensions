package org.zaproxy.zap.extension.bugTracker;

import java.awt.BorderLayout;

import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.bugTracker.BugTrackerBugzilla.BugTrackerBugzillaMultipleOptionsPanel;

public class BugTrackerBugzillaOptionsPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private BugTrackerBugzillaMultipleOptionsPanel bugzillaPanel;

    public BugTrackerBugzillaOptionsPanel() {
        super();
        setLayout(new BorderLayout());
        this.setName("Bugzilla");
        bugzillaPanel = new BugTrackerBugzillaMultipleOptionsPanel(getBugzillaModel());

        add(bugzillaPanel, BorderLayout.CENTER);
    }

    private BugTrackerBugzillaTableModel bugzillaModel;

    private BugTrackerBugzillaTableModel getBugzillaModel() {
        if (bugzillaModel == null) {
            bugzillaModel = new BugTrackerBugzillaTableModel();
        }
        return bugzillaModel;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        BugTrackerBugzillaParam options = optionsParam.getParamSet(BugTrackerBugzillaParam.class);
        bugzillaModel.setConfigs(options.getConfigs());
    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        BugTrackerBugzillaParam options = optionsParam.getParamSet(BugTrackerBugzillaParam.class);
        options.setConfigs(bugzillaModel.getElements());
    }

    @Override
    public String getHelpIndex() {
        return "addon.alertFilter";
    }
}