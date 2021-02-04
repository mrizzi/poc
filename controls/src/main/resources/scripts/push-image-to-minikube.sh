IMAGE_ID=$(podman images --filter=reference='localhost/'$USER'/'$1':'$2'-'$3 -n --format {{.ID}})
TAR_NAME=$1'_'$IMAGE_ID'_'$(date +%Y%m%d%H%M%S)'.tar'
TAR_PATH=/tmp/$TAR_NAME

if command -v minikube &> /dev/null
then
  if minikube status &> /dev/null
  then
    eval $(minikube -p minikube podman-env)
    echo 'Evaluated minikube environment variables'
    podman save -o $TAR_PATH $IMAGE_ID
    echo 'Saved temporary image '$IMAGE_ID' to file '$TAR_PATH
    podman-remote load -i $TAR_PATH $USER'/'$1':'$2'-'$3
    echo 'Loaded into minikube the image '$USER'/'$1':'$2'-'$3
    rm $TAR_PATH
    echo 'Removed file '$TAR_PATH
    kubectl rollout restart deployment controls
  else
    echo 'Minikube not running: skip deployment'
  fi
else
  echo 'Minikube not installed'
fi