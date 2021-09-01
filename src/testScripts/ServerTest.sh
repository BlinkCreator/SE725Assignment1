#!/bin/bash
echo "Setting up Server..."
cd ..
javac server/*.java
echo "Server Compiled..."
java server/TCPServer
echo "Server Running Ctrl + C"