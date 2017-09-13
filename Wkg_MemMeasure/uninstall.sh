#!/bin/bash

java -jar gp.jar --package 0102030401 --applet 010203040102 --delete 010203040102
 
java -jar gp.jar --package 0102030401 --applet 010203040101 --delete 010203040101
 
java -jar gp.jar -uninstall Shareable/output/server.cap -d

