#!/bin/bash
push_flag='false'
registry=''     # e.g. 'descartesresearch/'

print_usage() {
  printf "Usage: docker_build.sh [-p] [-r REGISTRY_NAME]\n"
}

while getopts 'pr:' flag; do
  case "${flag}" in
    p) push_flag='true' ;;
    r) registry="${OPTARG}" ;;
    *) print_usage
       exit 1 ;;
  esac
done

docker build -t "${registry}tomact" ../utilities/tools.tuberlin.jvm/
docker build -t "${registry}teastore-db" ../utilities/tools.descartes.teastore.database/
perl -i -pe's|.*FROM descartesresearch/|FROM '"${registry}"'|g' ../utilities/tools.descartes.teastore.dockerbase/Dockerfile
docker build -t "${registry}teastore-base" ../utilities/tools.descartes.teastore.dockerbase/
perl -i -pe's|.*FROM descartesresearch/|FROM '"${registry}"'|g' ../utilities/tools.descartes.teastore.docker.all/Dockerfile

docker build -t "${registry}teastore-all" ../utilities/tools.descartes.teastore.docker.all/

perl -i -pe's|.*FROM '"${registry}"'|FROM descartesresearch/|g' ../utilities/tools.descartes.teastore.docker.all/Dockerfile
perl -i -pe's|.*FROM '"${registry}"'|FROM descartesresearch/|g' ../utilities/tools.descartes.teastore.dockerbase/Dockerfile

if [ "$push_flag" = 'true' ]; then
  docker push "${registry}teastore-db"
  docker push "${registry}teastore-base"
  docker push "${registry}teastore-all"
fi