package hfsfiles;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        String localFileContents = new String(Files.readAllBytes(Paths.get("my-file.txt")), StandardCharsets.UTF_8);

        // Write file onto Hedera Testnet, using HFS FileCreateTransaction
        FileCreateTransaction fileCreateTx = new FileCreateTransaction()
            // NOTE: File create transaction
            // Step (1) in the accompanying tutorial
            // /* ... */
            .setContents(localFileContents)
            .freezeWith(client);
        FileCreateTransaction fileCreateTxSigned = fileCreateTx.sign(accountKey);
        TransactionResponse fileCreateTxSubmitted = fileCreateTxSigned.execute(client);
        TransactionId fileCreateTxId = fileCreateTxSubmitted.transactionId;
        TransactionReceipt fileCreateTxReceipt = fileCreateTxSubmitted
            .setValidateStatus(true)
            .getReceipt(client);
        FileId fileId = fileCreateTxReceipt.fileId;

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
