package org.zaproxy.zap.extension.bugTracker;

import java.awt.BorderLayout;

import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.bugTracker.BugTrackerGithub.BugTrackerGithubMultipleOptionsPanel;

public class BugTrackerGithubOptionsPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private BugTrackerGithubMultipleOptionsPanel githubPanel;

    public BugTrackerGithubOptionsPanel() {
        super();
        setLayout(new BorderLayout());
        this.setName("Github");
        githubPanel = new BugTrackerGithubMultipleOptionsPanel(getGithubModel());

        add(githubPanel, BorderLayout.CENTER);
    }

    private BugTrackerGithubTableModel githubModel;

    private BugTrackerGithubTableModel getGithubModel() {
        if (githubModel == null) {
            githubModel = new BugTrackerGithubTableModel();
        }
        return githubModel;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        BugTrackerGithubParam options = optionsParam.getParamSet(BugTrackerGithubParam.class);
        githubModel.setConfigs(options.getConfigs());
    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        BugTrackerGithubParam options = optionsParam.getParamSet(BugTrackerGithubParam.class);
        options.setConfigs(githubModel.getElements());
    }

    @Override
    public String getHelpIndex() {
        return "addon.alertFilter";
    }
}