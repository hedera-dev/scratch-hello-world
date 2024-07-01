#!/usr/bin/env node

const path = require('node:path');
const fs = require('node:fs/promises');
const readline = require('node:readline/promises');
const { stdin, stdout } = require('node:process');
const dotenv = require('dotenv');

async function init01RpcRelay() {
    // read the .env file.
    // this has either been created manually,
    // or generated using the init-00-main script
    dotenv.config();
    const {
        OPERATOR_ACCOUNT_PRIVATE_KEY,
        OPERATOR_ACCOUNT_EVM_ADDRESS,
        OPERATOR_ACCOUNT_ID,
    } = process.env;

    let operatorKey = OPERATOR_ACCOUNT_PRIVATE_KEY;
    let operatorEvmAddress = OPERATOR_ACCOUNT_EVM_ADDRESS;
    let operatorId = OPERATOR_ACCOUNT_ID;

    const rlPrompt = readline.createInterface({
        input: stdin,
        output: stdout,
    });

    if (!operatorId) {
        if (operatorEvmAddress) {
            console.log(`Please enter the the operator account ID which corresponds to ${OPERATOR_ACCOUNT_EVM_ADDRESS}`);
            console.log('If this account has not yet been created or funded, you may do so via https://faucet.hedera.com/ ');
            const inputOperatorId = await rlPrompt.question('> ');
            operatorId = inputOperatorId;
        } else {
            console.error('Neither OPERATOR_ACCOUNT_ID nor OPERATOR_ACCOUNT_EVM_ADDRESS defined.');
            return;
        }
    }

    // TODO validation

    // construct a .env file for the RPC relay
    const dotEnvFileContentsProposed =
`
HEDERA_NETWORK=testnet
OPERATOR_ID_MAIN=${operatorId}
OPERATOR_KEY_MAIN=${operatorKey}
CHAIN_ID=0x128
MIRROR_NODE_URL=https://testnet.mirrornode.hedera.com/
`;

    // write the .env file in the RPC relay directory
const fileName = path.resolve('.rpcrelay.env');
await fs.writeFile(fileName, dotEnvFileContentsProposed);

    // Next, need to:
    // 1. Install the RPC relay
    // 2. Copy the .rpcrelay.env file into its directory
    // 3. Run the RPC relay server
    // 4. Notify the user
    // These will be taken care of in a shell script (better suited than NodeJs)

    console.log('.rpcrelay.env written');

    rlPrompt.close();
}

init01RpcRelay();
