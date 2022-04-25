#!/bin/bash

set -e

sbt -Dversion="$1" docker:publishLocal

git tag "$1"

docker save pricelist-generator:latest | pv -s 200000000 | ssh cloud 'docker load'
#ssh cloud -p 29 './docker/price-list-generator_recreate.sh'
