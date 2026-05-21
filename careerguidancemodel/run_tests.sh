#!/bin/bash
# Script to run unit tests with correct environment and PYTHONPATH

# Check if venv exists, if not create and install
if [ ! -d "venv" ]; then
    echo "Tạo virtual environment mới..."
    python3 -m venv venv
    source venv/bin/activate
    export PYO3_USE_ABI3_FORWARD_COMPATIBILITY=1
    pip install -r requirements.txt
    pip install pytest pytest-asyncio pytest-cov pytest-mock httpx respx mongomock fakeredis
else
    source venv/bin/activate
fi

# Run pytest with PYTHONPATH set
PYTHONPATH=. pytest --cov=app tests/
