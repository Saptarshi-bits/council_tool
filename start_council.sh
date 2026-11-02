#!/bin/bash
cd "$(dirname "$0")/council_project"
sleep 1 && open http://localhost:5001 &
.venv/bin/python3 server.py
