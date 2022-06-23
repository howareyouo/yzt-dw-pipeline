#!/usr/bin/env bash


. ./docker/build.sh --job-artifacts ./yzt-dw-pipeline/build/libs/*.jar \
     --from-release \
     --flink-version 1.10.1 
     --scala-version 2.11  \
     --image-name 
