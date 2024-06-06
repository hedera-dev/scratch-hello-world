package hscssmartcontract;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractFunctionResult;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ScriptHscsSmartContract {
    public static void main(String[] args) throws Exception {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();
        String accountIdStr = dotenv.get("ACCOUNT_ID");
        String privateKeyStr = dotenv.get("ACCOUNT_PRIVATE_KEY");
        if (accountIdStr == null || privateKeyStr == null) {
            throw new RuntimeException("Please set required keys in .env file.");
        }
        if (privateKeyStr.startsWith("0x")) {
            privateKeyStr = privateKeyStr.substring(2);
        }

        // Configure client using environment variables
        AccountId accountId = AccountId.fromString(accountIdStr);
        PrivateKey accountKey = PrivateKey.fromStringECDSA(privateKeyStr);
        Client client = Client.forTestnet().setOperator(accountId, accountKey);

        String accountExplorerUrl = "https://hashscan.io/testnet/address/" + accountId;

	    // Read EVM bytecode file for deployment
        byte[] evmBytecode = Files.readAllBytes(Paths.get("./my_contract_sol_MyContract.bin"));

	    // Upload bytecode to HFS, in preparation for deployment to HSCS
        TransactionResponse fileCreateTransaction = new FileCreateTransaction()
            .setKeys(accountKey)
            .setContents(evmBytecode)
            .execute(client);
        TransactionReceipt fileReceipt = fileCreateTransaction.getReceipt(client);
        FileId bytecodeFileId = Objects.requireNonNull(fileReceipt.fileId);

	    // Deploy smart contract
        TransactionResponse contractCreateTransaction = new ContractCreateTransaction()
            .setBytecodeFileId(bytecodeFileId)
            .setGas(100_000)
            .execute(client);
        TransactionReceipt contractReceipt = contractCreateTransaction.getReceipt(client);
        ContractId myContractId = Objects.requireNonNull(contractReceipt.contractId);
        String myContractExplorerUrl = "https://hashscan.io/testnet/address/" + myContractId;

        // Write data to smart contract
        // NOTE: Invoke a smart contract transaction
        // Step (3) in the accompanying tutorial
        // .setFunction("introduce", new ContractFunctionParameters().addString(/* ... */))
        TransactionResponse contractExecuteTransaction = new ContractExecuteTransaction()
            .setContractId(myContractId)
            .setGas(100_000)
            .setFunction("introduce", new ContractFunctionParameters().addString("bguiz"))
            .execute(client);
        TransactionReceipt executeReceipt = contractExecuteTransaction.getReceipt(client);
        TransactionId myContractWriteTxId = executeReceipt.transactionId;
        String myContractWriteTxExplorerUrl = "https://hashscan.io/testnet/transaction/" + myContractWriteTxId;

        // Read data from smart contract
        // NOTE: Invoke a smart contract query
        // Step (4) in the accompanying tutorial
        //  .setContractId(/* ... */)
        //  .setGas(100000)
        //  .setFunction(/* ... */);
        ContractCallQuery contractCallQuery = new ContractCallQuery()
            .setContractId(myContractId)
            .setGas(100_000)
            .setFunction("greet", new ContractFunctionParameters());
        ContractFunctionResult greetResult = contractCallQuery.execute(client);
        String myContractQueryResult = greetResult.getString(0);

        client.close();

        // Output results
        System.out.println("accountId: " + accountId);
        System.out.println("accountExplorerUrl: " + accountExplorerUrl);
        System.out.println("myContractId: " + myContractId);
        System.out.println("myContractExplorerUrl: " + myContractExplorerUrl);
        System.out.println("myContractWriteTxId: " + myContractWriteTxId);
        System.out.println("myContractWriteTxExplorerUrl: " + myContractWriteTxExplorerUrl);
        System.out.println("myContractQueryResult: " + myContractQueryResult);
    }
}
