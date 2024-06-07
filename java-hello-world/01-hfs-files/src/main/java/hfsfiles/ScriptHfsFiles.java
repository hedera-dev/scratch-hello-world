package hfsfiles;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileContentsQuery;
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

public class ScriptHfsFiles {
    public static void main(String[] args) throws Exception {
        // Ensure required environment variables are available
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

        // Read file from disk
        String localFileContents = Files.readString(Paths.get("my-file.txt"));

        // Write file onto Hedera Testnet, using HFS FileCreateTransaction
        FileCreateTransaction fileCreateTx = new FileCreateTransaction()
            // NOTE: File create transaction
            // Step (1) in the accompanying tutorial
            // /* ... */
            .setContents(localFileContents)
            .freezeWith(client);
        FileCreateTransaction fileCreateTxSigned = fileCreateTx.sign(accountKey);
        TransactionResponse fileCreateTxSubmitted = fileCreateTxSigned.execute(client);
        TransactionId fileCreateTxId = fileCreateTxSubmitted.transactionId;
        TransactionReceipt fileCreateTxReceipt = fileCreateTxSubmitted.getReceipt(client);
        FileId fileId = Objects.requireNonNull(fileCreateTxReceipt.fileId);

        // Read file from Hedera Testnet, using HFS FileContentsQuery
        FileContentsQuery fileReadQuery = new FileContentsQuery()
            // NOTE: File contents query
            // Step (2) in the accompanying tutorial
            // /* ... */;
            .setFileId(fileId);
        String networkFileContents = fileReadQuery.execute(client).toStringUtf8();

        // Output results
        String txExplorerUrl = "https://hashscan.io/testnet/transaction/" + fileCreateTxId;
        System.out.println("fileId: " + fileId);
        System.out.println("fileCreateTxId: " + fileCreateTxId);
        System.out.println("txExplorerUrl: " + txExplorerUrl);
        System.out.println("localFileContents: " + localFileContents);
        System.out.println("networkFileContents: " + networkFileContents);

        client.close();
    }
}
