#!/bin/bash

# get specific tag name, for the latest tagged version
VERSION_TAG=$( curl -s GET https://api.github.com/repos/hashgraph/hedera-json-rpc-relay/tags |
    jq -r '.[].name' |
    grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' |
    cut -c2- |
    head -n1
)
echo "Running RPC relay: ${VERSION_TAG}"

docker run \
    --rm \
    --publish 7546:7546 \
    --env-file ./.rpcrelay.env \
    ghcr.io/hashgraph/hedera-json-rpc-relay:${VERSION_TAG}
