#!/bin/bash

java -jar gp.jar -uninstall Shareable/output/client.cap -d

ant server

ant client

java -jar gp.jar -install Shareable/output/client.cap -d
