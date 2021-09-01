#!/bin/bash
echo "Setting up Client, Ensure ServerTest has been run..."
cd ..
javac client/*.java
echo "Server Compiled..."
java client/ClientRunner
echo "Client Running, Ctrl + C"