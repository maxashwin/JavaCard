#!/bin/bash

ant server

java -jar gp.jar -install Shareable/output/server.cap 

java -jar gp.jar --package 0102030401 --applet 010203040101 --create 010203040101

java -jar gp.jar --package 0102030401 --applet 010203040102 --create 010203040102

