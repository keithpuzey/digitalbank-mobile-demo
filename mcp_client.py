#!/usr/bin/env python3
import json
from mcp2py import load

# MCP server endpoint
MCP_URL = "http://127.0.0.1:3333/mcp"  # change if different
API_KEY_FILE = "api-key.json"

def main():
    # Read API key
    with open(API_KEY_FILE, "r") as f:
        api_key = f.read().strip()

    # Load MCP server connection
    mcp = load(MCP_URL, headers={"X-API-Key": api_key})

    # Optional: inspect available tools
    # print("Available tools:", mcp.list_tools())

    # Send prompt / chat request
    prompt = "Summarize the last BlazeMeter test results"
    response = mcp.chat(prompt)  # may be 'ask' or 'prompt' depending on your server

    # Pretty-print JSON
    print(json.dumps(response, indent=2))

if __name__ == "__main__":
    main()