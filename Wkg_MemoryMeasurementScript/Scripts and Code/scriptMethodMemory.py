#!/usr/bin/python

import os
import shutil
import re

# User needs to change the sourceFile.
# sourceFile requires absolute or relative path along with the file name to the applet, 
# whose constructor objects are required to be extracted and measured for memory
sourceFile = '/home/swatch/Desktop/Memory_Experiments/Memory/src/packageA/ServerApplet.java'

#==========================================================================================================================================#

# Initial processing. Applet is copied to the current directory to enable extraction of objects from constructor

# Variable to store the details of objects in the constructor of applet
# Not required to be changed 
constructorFileName = "constructorDetails.txt"

# Extract the name of the file without the extension
destinationRoot = os.path.basename(os.path.splitext(sourceFile)[0])
destinationDirectory = os.path.join(os.getcwd(), destinationRoot)

# Specify the current working directory
currentWorkingDirectory = os.getcwd()

# Copy the applet fron the source directory to current working directory
shutil.copy2(sourceFile, currentWorkingDirectory)

# Extract the file name. originalFileName is passed to all the methods for processing. 
originalFileName = os.path.basename(sourceFile)

print "\nCopied file:"
print "From: "+ sourceFile 
print "To  : "+ currentWorkingDirectory

# Create a directory having the applet file name in current working directory
if not os.path.exists(destinationDirectory):
	os.makedirs(destinationDirectory) 

# Create a directory named Original to store the original applet without modifications
destinationDirectory2 = os.path.join(destinationDirectory, "Original")

# Create the directory if does not exist
if not os.path.exists(destinationDirectory2):
	os.makedirs(destinationDirectory2) 

# Copy the original applet from current working directory to the newly created directory
shutil.copy2(originalFileName, destinationDirectory2)

print "\nCopied file: "+ originalFileName 
print "To  : "+ destinationDirectory

# Rename the original applet file
os.rename(originalFileName,originalFileName+"txt2")
originalFileName = originalFileName+"txt2"
print "\nRenamed file to "+ originalFileName

#=========================================================================================================================================#

# All the necessary methods required for processing of the applet file are declared

# Function determines if the inputLine is a comment
# 
# @Param inputLine is any string passed. Generally a line of string
#
# @Return "True" if true else returns "False"
def isComment(inputLine):
	iL = inputLine.strip()
	if (iL.startswith('/*')) or (iL.startswith('//')) or (iL.startswith('/**')) or (iL.startswith('*')) or (iL.startswith('*/')):
		return True
	else:
		return False

# Function determines if the inputLine is a Measurable statement. 
#
# @Param inputLine is any string passed. Generally a line of string
#
# @Return "True" if the statement is likely to consume memory else returns "False"
def isMeasurableStatement(inputLine):
	iL = inputLine.strip()
	if (iL.startswith('{')) or (iL.startswith('}')) or (iL.startswith('try')) or (iL.startswith('catch')) or (iL.startswith('if')) or ("register(" in iL) or (iL.startswith('for')):
		return False
	else:
		return True

# Function determines if the inputLine is a Measurable method. 
#
# @Param inputLine is any string passed. inputLine is string containing method declaration
#
# @Return "True" if the statement is likely to consume memory else returns "False"
def isMeasurableMethod(inputLine):
	iL = inputLine.strip()
	if (("install(" in iL) or ("select(" in iL) or ("deselect(" in iL) or ("process(" in iL)):
		return False
	else:
		return True

# Function removes file extension
#
# @Param fileName is string with file extension
#
# @Return fileName without the extension 
def removeFileExtension(fileName):
	return os.path.splitext(fileName)[0]

# Function to determine the line number and name of the package
#
# @Param fileName is string type with file extension
#
# @Return lineNumber returns the line number of package name
# after returns the package name
def findPackageLine(fileName):
	lineNumber = 0
	writeLineNumer = 0
	with open(fileName, 'r') as inputFile:
		for line in inputFile:
			line = " ".join(line.split())
			lineNumber += 1
			if "package " in line:
				before, at, after  = line.partition("package")
				break
		inputFile.close()
		return lineNumber, after

# Function determines the line number of the class name
#
# @Param fileName is string type with file extension
#
# @Return classNameLineNumber returns the line number of class name
def findClassName(fileName):
	className = "class " + 	removeFileExtension(fileName)
	print className
	lineNumber = 0
	classNameLineNumber = 0
	openBracket = 0
	with open(fileName, 'r') as inputFile:
		for line in inputFile:
			line = " ".join(line.split())
			lineNumber += 1
			if className in line:
				if "{" in line:
					openBracket += 1
					classNameLineNumber = lineNumber
				else:
					classNameLineNumber = lineNumber+1
				classEndNumber = lineNumber
				for classLine in inputFile:
					classEndNumber += 1
					openBracket = openBracket + classLine.count("{") - classLine.count("}")
					if (openBracket == 0):
						break
	return classNameLineNumber, classEndNumber

# Function finds the constructor block in a given file
#
# @Param fileName is string type with file extension
#
# @Return constructorLineNumber returns the line number of constructor name
# maxConstructorLines returns the number of non empty lines in constructor
# constructorArray returns array containing line numbers of statements which can be measured in constructor
def findConstructor(fileName):
	constructorName1 = "protected " + removeFileExtension(fileName) +"("
	constructorName2 = "public " + removeFileExtension(fileName) +"("
	constructorName3 = "private " + removeFileExtension(fileName) +"("
	lineNumber = 0
	maxConstructorLines = 0
	constructorLineNumber = 0
	cline = 0
	constructorArray = []
	openBracket = 0
	closeBracket = 0
	with open(fileName, 'r') as inputFile:
		for line in inputFile:
			line = " ".join(line.split())
			lineNumber += 1
			if ((constructorName1 in line) or (constructorName2 in line) or (constructorName3 in line)):
				if "{" in line:
					openBracket += 1
					constructorLineNumber = lineNumber
				else:
					constructorLineNumber = lineNumber + 1
				cline = lineNumber
				for constructorLine in inputFile:
					cline += 1
					if (len(constructorLine.strip())!=0) and (not isComment(constructorLine)):
						openBracket = openBracket + constructorLine.count("{") - constructorLine.count("}")
						if (openBracket == 0):
							break
						if (isMeasurableStatement(constructorLine)):
							maxConstructorLines += 1
							constructorArray.append(cline)
						
	return constructorLineNumber, maxConstructorLines, constructorArray

# Function determines the line number of the method declaration
#
# @Param fileName is string type with file extension
#
# @Return classNameLineNumber returns the line number of class name
def findMethods(fileName):
	pattern = '\s*(public|private|protected)?\s*(static)?\s*(void|short|short[]|byte|byte[]|boolean)\s+\w+\(.*?\)'
	lineNumber = 0
	methodsArray = []
	with open(fileName, 'r') as inputFile:
		for line in inputFile:
			line = " ".join(line.split())
			lineNumber += 1
			z = re.match(pattern, line)
			if z:
				if(isMeasurableMethod(line)):
					methodsArray.append(lineNumber)
	return methodsArray

# Function to find the scope of a method
#
# @Param fileName is string type with file extension
# methodName is string type. Method name is passed
#
# @Return methodNameNumber returns the line number of method name declaration
# methodEndNumber returns end line number of the method
def findStartEndMethodLine(fileName, inputLineNumber):
	lineNumber = 0
	methodNameNumber = 0
	openBracket = 0
	maxLines = 0
	with open(fileName, 'r') as inputFile:
		for line in inputFile:
			line = " ".join(line.split())
			lineNumber += 1
			if (lineNumber == inputLineNumber):
				if "{" in line:
					openBracket += 1
					methodNameNumber = lineNumber
				else:
					methodNameNumber = lineNumber
				methodEndNumber = lineNumber
				for methodLine in inputFile:
					methodEndNumber += 1
					openBracket = openBracket + methodLine.count("{") - methodLine.count("}")
					if (openBracket == 0):
						break
		return methodNameNumber, methodEndNumber


# Function to find the location of a method
#
# @Param fileName is string type with file extension
# methodName is string type. Method name is passed
#
# @Return methodNameNumber returns the line number of method name declaration
# methodEndNumber returns end line number of the method
def findStartEndMethod(fileName, methodName):
	lineNumber = 0
	methodNameNumber = 0
	openBracket = 0
	maxLines = 0
	methodArray = []
	with open(fileName, 'r') as inputFile:
		for line in inputFile:
			line = " ".join(line.split())
			lineNumber += 1
			if methodName in line:
				if "{" in line:
					openBracket += 1
					methodNameNumber = lineNumber
				else:
					methodNameNumber = lineNumber
				methodEndNumber = lineNumber
				for methodLine in inputFile:
					methodArray.append(methodLine)
					methodEndNumber += 1
					openBracket = openBracket + methodLine.count("{") - methodLine.count("}")
					if (openBracket == 0):
						break
		return methodNameNumber, methodEndNumber, methodArray


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

# Function to insert the global variables required for memory measurement
#
# @Param maxMemoryArray defines the length of memory measurment object
#
# @Return null
def insertClassVariableDeclarations(maxMemoryArray):
	inputFile.write('	final static byte INS_MEMORYMEASURE = (byte) 0x80;\n')
	inputFile.write('	final static byte CLA_SIMPLEAPPLET = (byte) 0xB0;\n')
	inputFile.write('\n	private short memoryMeasureLength = (short)'+maxMemoryArray+';\n')
    	inputFile.write('	private short j = (short)0;\n')
    
    	inputFile.write('	public MemoryMeasurement mm[] = new MemoryMeasurement[memoryMeasureLength];\n')
	inputFile.write('	short tempArrayLength1 = (short)0x7fff;\n')
    	inputFile.write('	short tempArrayLength2 = (short)0x3fff;\n\n')

# Function to insert the local variables in the constructor
#
# @Param null
#
# @Return null
def insertConstructorVariableDeclarations():
	inputFile.write('	byte[] tempByteArray1 = new byte[tempArrayLength1];\n')
        inputFile.write('	byte[] tempByteArray2 = new byte[tempArrayLength2];\n\n')
	inputFile.write('	for(short i=0; i<memoryMeasureLength; i++){\n')
	inputFile.write('		mm[i] = new MemoryMeasurement();\n')
        inputFile.write('	}\n\n')

# Function to insert the public void process method to process the APDU buffer
#
# @Param null
#
# @Return null
def insertProcessFunction():
	inputFile.write('	public void process(APDU apdu) throws ISOException {\n')
        inputFile.write('		byte[] apduBuffer = apdu.getBuffer();\n')

        inputFile.write('		if (selectingApplet())\n')
        inputFile.write('			return;\n')

        inputFile.write('		if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {\n')
        inputFile.write('    			switch (apduBuffer[ISO7816.OFFSET_INS] ){\n')
	inputFile.write('				case INS_MEMORYMEASURE: memoryMeasure(apdu); break;\n')
        inputFile.write('		        	default : ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ; break ;\n')
	inputFile.write('			}\n')
        inputFile.write('		}\n')
        inputFile.write('		else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);\n')
    	inputFile.write('	}\n')


# Function to insert the public void method to send the memory measurement values to the userprocess the APDU buffer
#
# @Param null
#
# @Return null
def insertMemoryMeasurementFunction(functionName):
	inputFile.write('\n	public void '+functionName+'(APDU apdu){\n')
	inputFile.write('		byte[]    apdubuf = apdu.getBuffer();\n')

        inputFile.write('		apdu.setOutgoing();\n')

        inputFile.write('		apdu.setOutgoingLength((short) (6 * memoryMeasureLength));\n')
        
        inputFile.write('		short index = (short)0;\n')
        inputFile.write('		for(short i=0; i<memoryMeasureLength; i++){\n')
        inputFile.write('			Util.setShort(apdubuf,(index), (mm[i].getPersistentConsumption()));\n')
        inputFile.write('			Util.setShort(apdubuf,(short)(index+2), (mm[i].getDeselectConsumption()));\n')
        inputFile.write('			Util.setShort(apdubuf,(short)(index+4), (mm[i].getResetConsumption()));\n')
        inputFile.write('			index = (short)(index + 6);\n')
        inputFile.write('		}\n')
        inputFile.write('		apdu.sendBytes((short)0 , (short) (6 * memoryMeasureLength));\n')
    	inputFile.write('	}\n')

# Function finds and replaces oldWord with newWord in the file
#
# @Param fileName is string type with file extension
# oldWord is the word to be found and replaced
# newWord replaces oldWord
# 
# @Return null
def replaceWord(fileName, oldWord, newWord):
	with open(fileName, 'r') as inputFile :
		fileData = inputFile.read()

	# Replace the oldWord
	fileData = fileData.replace(oldWord, newWord)

	# Write the file out again
	with open(fileName, 'w') as inputFile:
		inputFile.write(fileData)

	inputFile.close()

#=========================================================================================================================================#

# Start of the main function

# Find the line number of the Applet Name
appletNameLineNumber, appletEndNumber = findClassName(originalFileName)
if (appletNameLineNumber == 0):
	print "Problem: Applet Class Name not found"
	exit()

# Find the line number, maximum lines in constructor and content of constructor 
constructorStartLine, constructorLineMax, constructorLineArray = findConstructor(originalFileName)

if (constructorStartLine == 0):
	print "Problem: Constructor not found "
	exit()

# Find the line number of the Applet Name
appletMethodsArray = findMethods(originalFileName)
if (appletMethodsArray == 0):
	print "Problem: Methods not found in Applet"
	exit()

# Write the content of constructorLineArray into a text file passed
#with open(constructorFileName, 'w') as inputFile:
#	for i in range(0, len(constructorLineArray)):
#		inputFile.write(str(constructorLineArray[i]))
#		inputFile.write('\n')		
#	inputFile.close()

#print "\nDetails of Constructor written in file : ", constructorFileName

# Find the start line and the end line of the method process
processMethodStart, processMethodEnd, processMethodArray = findStartEndMethod(originalFileName, "public void process(")

# Create Array of Lines of the Original File to process
appletLineArray = createFileArray(originalFileName)

# Add the memory measurement statments before and after the object in constructor
#for i in range(0, len(constructorLineArray)):
#	strippedLine = appletLineArray[constructorLineArray[i]-1].strip('\n')
#	strippedLine2 = strippedLine.strip()
#	codeToInsert = "mm[j].startMeasurement(); "+strippedLine2+" mm[j].endMeasurement(); j++;"+'\n'
#	appletLineArray[constructorLineArray[i]-1] = codeToInsert

# Create the Modified Filename from Original Filename
modifiedFileName = removeFileExtension(originalFileName) + ".java"

# Open a file with same name as original applet name
# Write the modified statements into the file 
with open(modifiedFileName, 'w') as inputFile:

	# Copy statements from original applet till Class Name Definition line
	for i in range(0, appletNameLineNumber):
		inputFile.write(appletLineArray[i])

	# Insert the Class Variable Declarations required for Memory Measurements
	insertClassVariableDeclarations(str(len(appletMethodsArray)))

	# Copy further statements from original applet till Constructor Declarations
	for i in range(appletNameLineNumber, constructorStartLine):
		inputFile.write(appletLineArray[i])

	# Insert the Constructor Variable Declarations
	insertConstructorVariableDeclarations()

	# Copy all statements from constructor till process method
	for i in range(constructorStartLine, processMethodStart):
		inputFile.write(appletLineArray[i])

	caseNum = 0
	# Insert the process function with memory measurements for each case statment
	for i in range(0, len(processMethodArray)):
		if ('case' in processMethodArray[i]) or ('default' in processMethodArray[i]):
			inputFile.write(processMethodArray[i])
			inputFile.write("		mm[j].startMeasurement();\n")
			caseNum += 1
		else:			
			if 'break' in processMethodArray[i]:
				inputFile.write("		mm[j].endMeasurement(); j++;\n")
				inputFile.write(processMethodArray[i])
				if (caseNum == 1):
					inputFile.write('		case INS_MEMORYMEASURE: memoryMeasure(apdu); break;\n')
			else:
				inputFile.write(processMethodArray[i])

	for i in range(processMethodEnd, appletEndNumber-1):
			inputFile.write(appletLineArray[i])

	# Insert function to return memory measurement values 
	insertMemoryMeasurementFunction('memoryMeasure')
	inputFile.write('}\n')

	inputFile.close()

# Replace the name of the Original Applet with Modified Applet
replaceWord(modifiedFileName, removeFileExtension(originalFileName), removeFileExtension(modifiedFileName))

print "\nMemory Measurement statements and methods added to : ", modifiedFileName

# Delete the original applet class file from source path
os.remove(sourceFile)
print "\nDeleted file : " + sourceFile
sourceFilePath = os.path.dirname(sourceFile)

# Copy the newly created applet with measurements to source path
shutil.copy2(modifiedFileName, sourceFilePath)
print "Copied file " + modifiedFileName + " to " + sourceFilePath

destinationDirectory2 = os.path.join(destinationDirectory, "Modified 1")

# Create directory to store the newly created applet
if not os.path.exists(destinationDirectory2):
	os.makedirs(destinationDirectory2) 

# Copy newly created applet file into directory
shutil.copy2(modifiedFileName, destinationDirectory2)
print "Copied file " + modifiedFileName + " to " + destinationDirectory2

memoryFileName = "MemoryMeasurement.java"

packageLine1, packageName1 = findPackageLine(modifiedFileName)
packageLine2, packageName2 = findPackageLine(memoryFileName)

# Replace the package name in MemoryMeasurement class file
replaceWord(memoryFileName, packageName2, packageName1)

# Copy the MemoryMeasurement class file into the source path
shutil.copy2(memoryFileName, sourceFilePath)
print "\nCopied file "+ memoryFileName + " to " + sourceFilePath

