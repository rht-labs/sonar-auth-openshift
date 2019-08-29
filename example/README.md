# Example Build and Deploy


```
Instructions use ansible. Windows users use a vm or apply each command manually
```

This section contains an example for how this plugin can be used with Sonarqube

## Instructions

1. Build and copy your built or downloaded plugin into the example folder

```
mvn clean package && cp target/sonar-auth-openshift-plugin-1.0.0.jar example/
```

2. Create a project in OpenShift

```
oc new-project sonarqube
```

3. From the example folder run the prerequisites 

```
ansible-galaxy install -r requirements.yml --roles-path=roles
```

4. From the example folder run the ansible playbook which sets up the build and deploy for Sonarqube including a persistent volume and database

```
ansible-playbook -i inventory/ apply.yml
```

5. From the base folder build the Docker container on OpenShift

```
oc start-build sonarqube --from-dir=. -n sonarqube
```

## Explore

To explore the how this built, explore the files in the subfolders here, particularly `example/inventory/group_vars/all.yml`