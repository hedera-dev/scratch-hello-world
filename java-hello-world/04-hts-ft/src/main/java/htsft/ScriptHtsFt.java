package htsft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ScriptHtsFt {
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

        // Create the token
        TokenCreateTransaction tokenCreateTx = new TokenCreateTransaction()
            // NOTE: Configure HTS token to be created
            // Step (1) in the accompanying tutorial
            // .setTokenType(/* ... */)
            // .setTokenName(/* ... */)
            // .setTokenSymbol(/* ... */)
            // .setDecimals(/* ... */)
            // .setInitialSupply(/* ... */)
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setTokenName("bguiz coin")
            .setTokenSymbol("BGZ")
            .setDecimals(2)
            .setInitialSupply(1_000_000)
            .setTreasuryAccountId(accountId)
            .setAdminKey(accountKey)
            .setFreezeDefault(false)
            .freezeWith(client);
        TokenCreateTransaction tokenCreateTxSigned = tokenCreateTx.sign(accountKey);
        TransactionResponse tokenCreateTxSubmitted = tokenCreateTxSigned.execute(client);
        TransactionReceipt tokenCreateTxReceipt = tokenCreateTxSubmitted.getReceipt(client);
        TokenId tokenId = tokenCreateTxReceipt.tokenId;
        String tokenExplorerUrl = "https://hashscan.io/testnet/token/" + tokenId;

        client.close();

        // Wait for 5 seconds for the record files to be ingested by the mirror nodes
        Thread.sleep(5000);

        // Query token balance of account (mirror node)
        // NOTE: Mirror Node API to query specified token balance
        // Step (2) in the accompanying tutorial
        // const accountBalanceFetchApiUrl =
        //     /* ... */;
        String accountBalanceFetchApiUrl =
            "https://testnet.mirrornode.hedera.com/api/v1/accounts/" +
            accountId.toString() +
            "/tokens?token.id=" +
            tokenId.toString() +
            "&limit=1&order=desc";

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(accountBalanceFetchApiUrl))
                .build();

        final HttpClient httpClient = HttpClient.newHttpClient();

        final var mirrorNodeResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();

        JsonObject jsonResponse = JsonParser.parseString(mirrorNodeResponse).getAsJsonObject();
        JsonArray tokensArray = jsonResponse.getAsJsonArray("tokens");
        JsonObject balanceObject = tokensArray.get(0).getAsJsonObject();
        long accountBalanceToken = balanceObject.get("balance").getAsLong();

        // Output results
        System.out.println("accountId: " + accountId);
        System.out.println("tokenId: " + tokenId);
        System.out.println("tokenExplorerUrl: " + tokenExplorerUrl);
        System.out.println("accountTokenBalance: " + accountBalanceToken);
        System.out.println("accountBalanceFetchApiUrl: " + accountBalanceFetchApiUrl);
    }
}
