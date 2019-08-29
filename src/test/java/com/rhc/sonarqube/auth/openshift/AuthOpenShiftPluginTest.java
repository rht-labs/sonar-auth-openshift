package com.rhc.sonarqube.auth.openshift;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.PluginContextImpl;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

/**
 * AuthOpenShiftPluginTest
 */
public class AuthOpenShiftPluginTest {

    private Plugin.Context context = new PluginContextImpl.Builder()
        .setSonarRuntime(SonarRuntimeImpl.forSonarQube(Version.create(7, 3), SonarQubeSide.SERVER))
        .build();
    
    private AuthOpenShiftPlugin testPlugin = new AuthOpenShiftPlugin();

    @Test
    public void testExtensions() {
        testPlugin.define(context);
        assertEquals(7, context.getExtensions().size());
    }
}