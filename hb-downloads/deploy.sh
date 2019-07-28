#!/bin/bash

aws --profile humble s3 --recursive cp resources/public/ s3://hb-download-app/public/ --acl public-read

REMOTE_COMMAND='cd /srv/hb-downloads; git pull; lein ring uberjar; export LEIN_NO_DEV=true'

ssh -n -l ryan humble $REMOTE_COMMAND;
