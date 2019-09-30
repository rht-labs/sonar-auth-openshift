package com.rhc.sonarqube.auth.openshift;

import org.junit.Assert;
import org.junit.Test;

public class OpenShiftUserTest {


	@Test
	public void testUser() {
		String user = "{\"kind\":\"User\",\"apiVersion\":\"user.openshift.io/v1\",\"metadata\":{\"name\":\"developer\",\"selfLink\":\"/apis/user.openshift.io/v1/users/developer\",\"uid\":\"c7e4c4da-e150-11e9-abd6-0a580a80009a\",\"resourceVersion\":\"230428\",\"creationTimestamp\":\"2019-09-27T18:01:07Z\"},\"identities\":[\"htpasswd_provider:developer\"],\"groups\":[\"group1\",\"sonarqube_admin\",\"system:authenticated\",\"system:authenticated:oauth\"]}";
		OpenShiftUserResponse openshiftUser = OpenShiftUserResponse.create(user);
		
		Assert.assertEquals("developer", openshiftUser.getUserName());
		Assert.assertTrue(openshiftUser.isMemberOf("sonarqube_admin"));
		
	}
}
