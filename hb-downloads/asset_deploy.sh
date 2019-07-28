#!/bin/bash

aws --profile humble s3 --recursive cp resources/ s3://hb-download-app/ --acl public-read
aws --profile sl s3 --recursive cp resources/ s3://sl-download-app/ --acl public-read

