# Example Build and Deploy

```
This example uses ansible. Windows users use a vm or apply each command manually
```

This section contains an example for how this plugin can be used with Sonarqube

## Instructions

1. Build and copy your built or downloaded plugin into the example folder

```
mvn clean package && cp target/sonar-auth-openshift-plugin-1.0.0.jar example/
```

2. Inspect the [all.yml](example/inventory/group_vars/all.yml) file. In the following step it will run the [OpenShift Applier](https://github.com/redhat-cop/openshift-applier) to create resources in OpenShift. Most importantly
   1. A project / namespace called `sonarqube` that the other resources will belong too.
   2. A deployment config to deploy sonarqube with the plugin.
   3. A build config to build the sonarqube-auth-openshift project.
   4. A route to navigate to the application.
   5. Two groups that will define the users and administrators of Sonarqube.
      1. Inspect the group allocation. Add/Remove appropriate users for your scenario. Users are listed in the two yaml files located in the files directory. Group creation and editing usually require elvated privileges. Group names are also defined here. If changed, the sonar.properties must also be changed to match.

3. From the example folder run the prerequisites 

```
ansible-galaxy install -r requirements.yml --roles-path=roles
```

1. From the example folder, run the ansible playbook which sets up the build and deploy for Sonarqube including a persistent volume and database. 

```
ansible-playbook -i inventory/ apply.yml
```
If the group are already setup and you do not want to add them with this playbook run this command instead

```
ansible-playbook -i inventory/ apply.yml -e exclude_tags=users_and_groups
```


1. From the base folder, build the Docker container on OpenShift. This will replace the original build with the build with local files.

```
oc start-build sonarqube --from-dir=. -n sonarqube
```

## Explore

To explore the how this built, explore the files in the subfolders here, particularly `example/inventory/group_vars/all.yml`
