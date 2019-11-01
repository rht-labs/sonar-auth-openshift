[![Build Status](https://travis-ci.org/rht-labs/sonar-auth-openshift.svg?branch=master)](https://travis-ci.org/rht-labs/sonar-auth-openshift)

# Openshift Authentication Plugin for SonarQube 

## Description

This plugin enables user authentication and Single Sign-On via OpenShift. It is based on the code by Julien Lancelot. Tested on version 7 of Sonarqube and OCP 3.11. It is intended to run deployed in a pod on OpenShift.

This plugin is designed to work out of the box without configuration. During plugin deployment, it looks up oauth information from OpenShift's well-known information and takes advantage of information already on the running pod. 

During deployment the plugin will:

- Look up well-known oauth information at https://openshift.default.svc
- Pull the service account client_id, secret and cert from the file system 
- Pull the OpenShift API location from the env variables of the pod
- Get the ServiceAccount name from the API
- Get the Route of the service that is coordinating sonarqube for callback. This relies on the service name being available in the configuration (via sonar.properties). As a fallback it will default to `sonarqube`

## Installation

This plugin is currently hosted at [rht-labs](https://github.com/rht-labs/sonar-auth-openshift/releases/latest). The latest jar is [here](https://github.com/rht-labs/sonar-auth-openshift/releases/latest/download/sonar-auth-openshift-plugin.jar). You can build it locally and place this plugin on to the volume where Sonarqube reads plugins at startup if modifying it. Typically, this might be `/opt/sonarqube/data/plugins`.

The service account can be used as the oauth client in OpenShift. The service account that runs Sonarqube should have a redirect uri that references the route that Sonarqube is using. You must specify this service account in the DeploymentConfig.

```
- apiVersion: v1
  kind: ServiceAccount
  metadata:
    annotations:
      serviceaccounts.openshift.io/oauth-redirectreference.sonarqube: '{"kind":"OAuthRedirectReference","apiVersion":"v1","reference":{"kind":"Route","name":"sonarqube"}}'
    name: sonarqube
```

The service account must have the ability to view routes in the project. 

```
oc policy add-role-to-user view system:serviceaccount:sonarqube-project:sonarqube
```

The environment variable sonar.auth.openshift.isEnabled must be set to true. The preferred way is to place that value in the sonar.properties file during your container build:

```
sonar.auth.openshift.isEnabled=true
```

You may also enable it in the Administrative console
 
## Configuration

This plugin will map OpenShift groups to Sonarqube roles. These values are set with the property 

```
sonar.auth.openshift.sar.groups=ocp-admin=sonar-administrators,ocp-users=sonar-users
```

This shows that Sonarqube will allow OpenShift users who are in the group ocp-admin users to be administrators with the role of sonar-administrators. Ordinary users will be added as sonar-users if they are OpenShift users in the group ocp-users. These OpenShift groups do not exist by default.

The default mapping value is: 

```
sonar.auth.openshift.sar.groups=sonar-administrators=sonar-administrators,sonar-users=sonar-users
```
To disable certificate validation (not recommended for production) configure the `ignore.certs` property

```
ignore.certs=true
```

The pod that sonarqube runs in should have a valid certificate to access the OpenShift/Kubernetes API. The Oauth server may have a different certificate. That certificate needs to be loaded into the keystore. To do so, place the certificate on the container's file system (via configmap, dockerfile, etc...). Then configure the sonar property to point the location on the file system

```
oauth.cert=/opt/sonarqube/conf/oauth.crt
```


You may choose the background color of the log in button with the property

```
sonar.auth.openshift.button.color=#666666
```

Set the kubernetes API. In the example the API is set automatically with environment variables

```
kubernetes.service=https://${env:KUBERNETES_SERVICE_HOST}:${env:KUBERNETES_SERVICE_PORT}/
```

See the example set up using the [OpenShift Applier](https://github.com/redhat-cop/openshift-applier) [here](example/README.md)

### Configuration Table

| Config        | Purpose           |
| ------------- |-------------|
| sonar.auth.openshift.sar.groups      | A map converting OpenShift groups to Sonarqube roles |
| sonar.auth.openshift.route.name      | The name of the route. Must also be defined in the service account (See example template)     |
| oauth.cert | File system location of the certificate      |
| ignore.certs | Option to ignore certificates. Not recommended for production      |
| kubernetes.service | The url of the api server with port if necessary   |
| sonar.auth.openshift.isEnabled | Ability to control whether to user this plugin   |
| sonar.auth.openshift.button.color | The hex color of the login button (#666666)   |

### License

Licensed under the [Apache License](http://www.apache.org/licenses/LICENSE-2.0.txt)
