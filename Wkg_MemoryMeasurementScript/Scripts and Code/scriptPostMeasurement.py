#!/usr/bin/python

import os
import shutil

# User needs to change the sourceFile.
# sourceFile requires absolute or relative path along with the file name to the applet, 
# whose constructor objects are required to be extracted and measured for memory
sourceFile = '/home/swatch/Desktop/Memory_Experiments/Memory/src/review/QTSesamApplet.java'

#==========================================================================================================================================#

# All the necessary methods required for processing of the applet file are declared

# Construct the name of applet with txt as suffix to keep a copy
originalFileName = os.path.basename(sourceFile)+"txt"

# Extract the name of the file without the extension
destinationRoot = os.path.basename(os.path.splitext(sourceFile)[0])
destinationDirectory = os.path.join(os.getcwd(), destinationRoot)

# Specify the current working directory
currentWorkingDirectory = os.getcwd()

# Create the directory if does not exist
if not os.path.exists(destinationDirectory):
	os.makedirs(destinationDirectory) 

# Create a directory Modified 2 in the directory created above 
destinationDirectory2 = os.path.join(destinationDirectory, "Modified 2")

if not os.path.exists(destinationDirectory2):
	os.makedirs(destinationDirectory2) 

# Function determines if the inputLine is a comment
# 
# @Param inputLine is any string passed. Generally a line of string
#
# @Return "True" if true else returns "False"
def isComment(inputLine):
	iL = inputLine.strip()
	if (iL.startswith('//')):
		return True
	else:
		return False

# Function removes file extension
#
# @Param fileName is string with file extension
#
# @Return fileName without the extension 
def removeFileExtension(fileName):
	return os.path.splitext(fileName)[0]

# Function creates an array of the input file. Each array row contains the line of file stored as string
#
# @Param fileName is string type with file extension
#
# @Return fileLineArray returns an array where each index contains a line of input file 
def createFileArray(fileName):
	fileLineArray = []
	with open(fileName, 'r') as inputFile:
		fileLineArray = inputFile.readlines()
		inputFile.close()
	return(fileLineArray)

# Function creates a word array of the input file. Each array row contains the words in a line of file stored as string
#
# @Param fileName is string type with file extension
#
# @Return wordList returns an array where each index contains sequence of words of a line 
def createWordArray(fileName):
	wordList = []
	with open("constructorDetails.txt",'r') as inputFile:
		wordList = inputFile.readlines()
		inputFile.close()			
	return wordList

#===========================================================================================================================================#

# Start of Main Function

# Create Array of Lines of the Original File to process
originalFileArray = createFileArray(originalFileName)

# Create the Modified Filename from Original Filename
modifiedFileName = removeFileExtension(originalFileName) + ".java"

# Create the word array of the measurements of objects stored in input file
constructorWordArray = createWordArray("constructorDetails.txt")

# Open a file with same name as original applet name
# Write the memory measurements statements in the file as comments 
with open(modifiedFileName,'w') as inputFile:
	count = 0
	for i in range(0, len(originalFileArray)):
		if (i != int(constructorWordArray[count].split()[0])-1):
			if (not isComment(originalFileArray[i])):
				inputFile.write(originalFileArray[i])
			else:
				pass		
		else:
			memPersistent = int(constructorWordArray[count].split()[1],16)
			memDeselect = int(constructorWordArray[count].split()[2],16)
			memReset = int(constructorWordArray[count].split()[3],16)
			if (memPersistent == 0) and (memDeselect == 0) and (memReset == 0):
				inputFile.write(originalFileArray[i])
			else:
				inputFile.write("\n /* Persistent : "+str(memPersistent)+" bytes, ")
				inputFile.write(" Transient(Deselect) : "+ str(memDeselect)+" bytes, ")
				inputFile.write(" Transient(Reset) : "+ str(memReset)+" bytes */\n")
				inputFile.write(originalFileArray[i])
			if (count < (len(constructorWordArray)-1)):
					count += 1
	inputFile.close()

print "Memory Measurements written as comments in ", modifiedFileName

# Delete the applet file from source path
os.remove(sourceFile)
print "\nDeleted file : " + sourceFile
sourceFilePath = os.path.dirname(sourceFile)

# Copy the applet with memory measurements to the source path
shutil.copy2(modifiedFileName, sourceFilePath)
print "Copied file " + modifiedFileName + " to " + sourceFilePath

# Copy the applet with memory measurments to the destinationDirectory2 for keeping copy
shutil.copy2(modifiedFileName, destinationDirectory2)		
print "Copied file " + modifiedFileName + " to " + destinationDirectory2
		

