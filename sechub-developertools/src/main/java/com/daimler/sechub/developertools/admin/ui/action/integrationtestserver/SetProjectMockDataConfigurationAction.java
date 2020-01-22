// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class SetProjectMockDataConfigurationAction extends IntegrationTestAction {
	private static final long serialVersionUID = 1L;

	public SetProjectMockDataConfigurationAction(UIContext context) {
		super("Set project mock config", context);
	}


	@Override
	protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
		Optional<String> projectId = getUserInput("Please enter projectId to setup mock configuration",InputCacheIdentifier.PROJECT_ID);
		if (!projectId.isPresent()) {
			return;
		}
		Optional<String> projectMockConfig = getUserInputFromTextArea("Please enter mock configuration for project:"+projectId,InputCacheIdentifier.PROJECT_MOCK_CONFIG_JSON);
		if (!projectMockConfig.isPresent()) {
			return;
		}
		DeveloperAdministration administration = getContext().getAdministration();
		String url = administration.getUrlBuilder().buildSetProjectMockConfiguration(projectId.get());
		administration.getRestHelper().putJSon(url, projectMockConfig.get());

	}

}