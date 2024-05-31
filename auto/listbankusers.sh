#!/bin/bash

# Debugging: Print current directory and list files
echo "Current directory: $(pwd)"
echo "Listing files:"
ls -la ./auto

clear
mysql -h 10.128.0.118 -u root -pCAdemo123 -P 3309 < .auto/digitalbank.sql