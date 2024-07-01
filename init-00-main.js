#!/usr/bin/env node

const fs = require('node:fs/promises');
const path = require('node:path');
const readline = require('node:readline/promises');
const { stdin, stdout } = require('node:process');
const {
    PrivateKey,
    Mnemonic,
} = require ('@hashgraph/sdk');
const dotenv = require('dotenv');

async function init01Main() {

    let {
        use1stAccountAsOperator,
        operatorId,
        operatorKey,
        seedPhrase,
        numAccounts,
        accounts,
        rpcUrl,
        allowOverwrite1stChar,
    } = await promptForInputs();
    let operatorEvmAddress = '';

    if (use1stAccountAsOperator) {
        operatorEvmAddress = operatorId;
        operatorId = '';
    }

    // construct env vars file contents
    const dotEnvAccounts = accounts.map((account, accountIndex) => {
        const {
            privateKey,
            evmAddress,
        } = account;
        const text =
`## ${accountIndex} ECDSA secp256k1 account generated from seed phrase
ACCOUNT_${accountIndex}_PRIVATE_KEY="${privateKey}"
ACCOUNT_${accountIndex}_EVM_ADDRESS="${evmAddress}"
ACCOUNT_${accountIndex}_ID=""`;
        return text;
    }).join('\n\n');

    const dotEnvFileContentsProposed = `
# ECDSA secp256k1 account obtained from https://portal.hedera.com/dashboard
OPERATOR_ACCOUNT_PRIVATE_KEY="${operatorKey}"
OPERATOR_ACCOUNT_EVM_ADDRESS="${operatorEvmAddress}"
OPERATOR_ACCOUNT_ID="${operatorId}"

# BIP-39 seed phrase used to generate multiple accounts
SEED_PHRASE="${seedPhrase}"
NUM_ACCOUNTS="${numAccounts}"

${dotEnvAccounts}

# JSON-RPC endpoint
RPC_URL="${rpcUrl}"
`;

    console.log('####');
    console.log(dotEnvFileContentsProposed);
    console.log('####');

    if (allowOverwrite1stChar === 'y') {
        console.log('OK, overwriting .env file');
        const fileName = path.resolve('.env');
        await fs.writeFile(fileName, dotEnvFileContentsProposed);
    } else {
        console.log('OK, leaving current .env file as it was');
    }
}

async function promptForInputs() {
    dotenv.config();
    const {
        OPERATOR_ACCOUNT_ID,
        OPERATOR_ACCOUNT_PRIVATE_KEY,
        SEED_PHRASE,
        RPC_URL,
        NUM_ACCOUNTS,
    } = process.env;

    let use1stAccountAsOperator = false;
    let operatorId = OPERATOR_ACCOUNT_ID;
    let operatorKey = OPERATOR_ACCOUNT_PRIVATE_KEY;
    let seedPhrase = SEED_PHRASE;
    let numAccounts = NUM_ACCOUNTS;
    let accounts = [];
    let rpcUrl = RPC_URL;
    let allowOverwrite1stChar;

    const rlPrompt = readline.createInterface({
        input: stdin,
        output: stdout,
    });

    let restart;
    do {
        restart = false;
        // prompt user to input their operator account details
        console.log('What is your operator account ID?');
        if (operatorId) {
            console.log(`Current: "${operatorId}"`);
            console.log('(leave blank to use the above)');
        } else {
            console.log('e.g. "0.0.12345"');
        }
        console.log('(enter "none" to use first account from BIP-39 seedphrase)');
        const inputOperatorId = await rlPrompt.question('> ');
        if (inputOperatorId === 'none') {
            use1stAccountAsOperator = true;
        } else {
            operatorId = inputOperatorId || operatorId;
            if (!operatorId) {
                console.error('Operator account ID must be specified.');
                restart = true;
                continue;
            }
        }

        if (!use1stAccountAsOperator) {
            console.log('What is your operator account private key?');
            if (operatorKey) {
                console.log(`Current: "${operatorKey}"`);
                console.log('(leave blank to use the above)');
            } else {
                console.log('e.g. "0x1234abcdef5678abcdef90d7edc0242ce802d1c3d5a2bccf7a9aa0cae63632d"');
            }
            const inputOperatorKey = await rlPrompt.question('> ');
            operatorKey = inputOperatorKey || operatorKey;
            if (!operatorKey) {
                console.error('Operator account private key must be specified.');
                restart = true;
                continue;
            }

            // validate operator account details
            const operatorPrivateKey = PrivateKey.fromStringECDSA(operatorKey);
            const operatorPublicKeyHex = `0x${operatorPrivateKey.publicKey.toStringRaw()}`;

            const accountFetchApiUrl =
                `https://testnet.mirrornode.hedera.com/api/v1/accounts?account.publickey=${operatorPublicKeyHex}&balance=true&limit=1&order=desc`;
            let accountBalanceTinybar;
            let accountId;
            try {
                const accountFetch = await fetch(accountFetchApiUrl);
                const accountJson = await accountFetch.json();
                const fetchedAccount = accountJson?.accounts[0];
                accountId = fetchedAccount?.account;
                accountBalanceTinybar = fetchedAccount?.balance?.balance;
                const accountKey = fetchedAccount?.key;
                const accountAlias = fetchedAccount?.alias;
                console.log('Fetched account:', {
                    accountId,
                    accountBalanceTinybar,
                    accountKey,
                    accountAlias,
                });
            } catch (ex) {
                // do nothing
            }
            if (accountId !== operatorId || !accountBalanceTinybar) {
                console.error('Specified operator account ID does not exist, its private key is a mismatch, or is currently unfunded.');
                restart = true;
                continue;
            }
        }

        // prompt user to input their seed phrase or leave blank to generate new one
        console.log('What is your BIP-39 seed phrase?');
        if (seedPhrase) {
            console.log(`Current: "${seedPhrase}"`);
            console.log('(leave blank to use the above)');
        } else {
            console.log('(leave blank to use randomly generate a new one)');
        }
        const inputSeedPhrase = await rlPrompt.question('> ');
        seedPhrase = inputSeedPhrase || seedPhrase;

        // validate seed phrase OR generate new one
        let mnemonic;
        if (!seedPhrase) {
            // generate a new seed phrase
            mnemonic = await Mnemonic.generate12();
            seedPhrase = mnemonic.toString();
        } else {
            // validate specified seed phrase
            let isValidSeedPhrase = true;
            try {
                mnemonic = await Mnemonic.fromString(seedPhrase);
            } catch (ex) {
                isValidSeedPhrase = false;
            }
            if (!isValidSeedPhrase) {
                console.error('Specified seed phrase is invalid.');
                restart = true;
                continue;
            }
        }

        // prompt user to input the number of accounts that they would like
        console.log('How many accounts would you like to generate from your BIP-39 seed phrase?');
        if (numAccounts) {
            console.log(`Current: "${numAccounts}"`);
            console.log('(leave blank to use the above)');
        } else {
            console.log('(leave blank to use default (1))');
        }
        const inputNumAccounts = await rlPrompt.question('> ');
        numAccounts = inputNumAccounts || numAccounts || 1;
        numAccounts = parseInt(numAccounts, 10);
        numAccounts = Math.max(1, numAccounts);

        accounts = new Array(numAccounts);
        for (let accountIndex = 0; accountIndex < numAccounts; ++accountIndex) {
            // generate account ID and key from seed phrase and derivation path
            // NOTE can now do this directly in the SDKs,
            // no longer need to import ethers.js or viem to accomplish this
            // See: https://github.com/hashgraph/hedera-sdk-js/pull/2341
            // See: https://github.com/hashgraph/hedera-sdk-java/pull/1842
            // See: https://github.com/hashgraph/hedera-sdk-go/pull/958
            const accountPrivateKeyObj =
                await mnemonic.toStandardECDSAsecp256k1PrivateKeyCustomDerivationPath(
                    '',
                    `m/44'/60'/0'/0/${accountIndex}`,
                );
            const accountPrivateKey = `0x${ accountPrivateKeyObj.toStringRaw() }`;
            const accountEvmAddress = `0x${ accountPrivateKeyObj.publicKey.toEvmAddress() }`;
            accounts[accountIndex] = {
                privateKey: accountPrivateKey,
                evmAddress: accountEvmAddress,
            };
        }

        if (use1stAccountAsOperator) {
            operatorId = accounts[0].evmAddress;
            operatorKey = accounts[0].privateKey;
        }

        // work out what the RPC URL should be
        // Note that when run in gitpod, the task is expected to set RPC_RELAY_URL.
        // Otherwise it default to a localhost instance of the RPC relay
        console.log('What is your preferred JSON-RPC endpoint URL?');
        if (rpcUrl) {
            console.log(`Current: "${rpcUrl}"`);
            console.log('(leave blank to use the above)');
        } else {
            console.log('(leave blank to use a default value)');
        }
        const inputRpcUrl = await rlPrompt.question('> ');
        if (!inputRpcUrl) {
            rpcUrl = rpcUrl || 'http://localhost:7546/';
        } else {
            rpcUrl = inputRpcUrl;
        }

        // prompt user y/n to overwrite .env file
        console.log('Are you OK to overwrite the .env file in this directory with the above? (restart/yes/NO)');
        const inputAllowOverwrite = await rlPrompt.question('> ');

        allowOverwrite1stChar = inputAllowOverwrite.toLowerCase().charAt(0);
        if (allowOverwrite1stChar === 'r') {
            console.log('OK, restarting');
            restart = true;
        }
    } while (restart);

    rlPrompt.close();

    return {
        use1stAccountAsOperator,
        operatorId,
        operatorKey,
        seedPhrase,
        numAccounts,
        accounts,
        rpcUrl,
        allowOverwrite1stChar,
    };
}

init01Main();
