#!/bin/bash

java -jar gp.jar -uninstall Shareable/output/client.cap -d

java -jar gp.jar -uninstall Shareable/output/server.cap -d

ant server

java -jar gp.jar -install Shareable/output/server.cap -d
