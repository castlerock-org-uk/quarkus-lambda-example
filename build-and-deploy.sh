#!/usr/bin/env bash

set -e
echo "Building Functions"
cd lambdas/test-lambda && mvn clean package
echo "Building CDK"
cd ../../infrastructure && mvn clean package
echo "Deploying.."
cdk deploy --profile "$1"