#!/bin/bash
REMOTE_COMMAND='cd /srv/canvas-video; git pull; lein ring uberjar; export LEIN_NO_DEV=true'

ssh -n -l ryan humble $REMOTE_COMMAND;
