NAMESPACE=pathfinder
oc new-project ${NAMESPACE}
oc policy add-role-to-user -z default view
oc apply -f mongodb-persistent.yml
oc new-app --template=${NAMESPACE}/mongodb-persistent --param=MONGODB_DATABASE=pathfinder
oc process -f pathfinder-full-template.yaml|oc create -f-