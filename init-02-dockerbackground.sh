#!/bin/bash

# get specific tag name, for the latest tagged version
VERSION_TAG=$( curl -s GET https://api.github.com/repos/hashgraph/hedera-json-rpc-relay/tags |
    jq -r '.[].name' |
    grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' |
    cut -c2- |
    head -n1
)

docker pull \
    ghcr.io/hashgraph/hedera-json-rpc-relay:${VERSION_TAG}
