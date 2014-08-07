# Makefile for DBMS Project 1
# CS 04-530, Spring 2014, Dr. John Robinson, Rowan University

SRCPATH = src
BINPATH = bin

JAVAC = javac -d $(BINPATH) -sourcepath $(SRCPATH)#\;$(SOLJARS)
JAVA  = java -classpath $(BINPATH)


all: global diskmgr bufmgr tests

global:
	$(JAVAC) $(SRCPATH)/global/*.java

diskmgr:
	$(JAVAC) $(SRCPATH)/diskmgr/*.java

bufmgr:
	$(JAVAC) $(SRCPATH)/bufmgr/*.java

tests:
	$(JAVAC) $(SRCPATH)/tests/*.java

clean:
	\find . -name \*.class -exec rm -f {} \;