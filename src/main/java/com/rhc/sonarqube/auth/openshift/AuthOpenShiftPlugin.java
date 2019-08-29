
package com.rhc.sonarqube.auth.openshift;

import org.sonar.api.Plugin;
import java.util.logging.Logger;

public class AuthOpenShiftPlugin implements Plugin {

	static final Logger LOGGER = Logger.getLogger(AuthOpenShiftPlugin.class.getName());

	@Override
	public void define(Context context) {
		context.addExtensions(OpenShiftScribeApi.class, OpenShiftConfiguration.class, OpenShiftIdentityProvider.class);
		context.addExtensions(OpenShiftConfiguration.definitions());
	}
}
