package createfundaccount;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.Mnemonic;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ScriptCreateFundAccount {
    public static void main(String[] args) throws Exception {

        // Generate seed phrase
//        Mnemonic seedPhraseMnemonic = Mnemonic.generate12();
//        System.out.println("Seed phrase: " + seedPhraseMnemonic.toString());

        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();

        String seedPhrase = dotenv.get("SEED_PHRASE");
        if (seedPhrase == null || seedPhrase.isEmpty()) {
            throw new IllegalArgumentException("Please set required keys in .env file.");
        }

        // Derive private key
        PrivateKey privateKey = Mnemonic.fromString(seedPhrase).toStandardECDSAsecp256k1PrivateKey("", 0);
        PublicKey publicKey = privateKey.getPublicKey();

        // At this point the account technically does not yet exist,
        // and will need to be created when it receives its first transaction (later).
        // Convert the private key to string format as well as an EVM address.
        String privateKeyHex = "0x" + privateKey.toStringRaw();

        // Derive EVM address
        String evmAddress = "0x" + publicKey.toEvmAddress().toString();
        String accountExplorerUrl = "https://hashscan.io/testnet/account/" + evmAddress;
        String accountBalanceFetchApiUrl = "https://testnet.mirrornode.hedera.com/api/v1/balances?account.id=" + evmAddress + "&limit=1&order=asc";

        // Query account ID and balance using mirror node API
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(accountBalanceFetchApiUrl))
                .build();

        final HttpClient httpClient = HttpClient.newHttpClient();

        final var mirrorNodeResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();

        JsonObject jsonResponse = JsonParser.parseString(mirrorNodeResponse).getAsJsonObject();
        JsonArray balancesArray = jsonResponse.getAsJsonArray("balances");
        JsonObject balanceObject = balancesArray.get(0).getAsJsonObject();
        String accountId = balanceObject.get("account").getAsString();
        long accountBalanceTinybar = balanceObject.get("balance").getAsLong();

        Hbar accountBalanceHbar = Hbar.fromTinybars(accountBalanceTinybar);

        // Output results
        System.out.println("privateKeyHex: " + privateKeyHex);
        System.out.println("evmAddress: " + evmAddress);
        System.out.println("accountExplorerUrl: " + accountExplorerUrl);
        System.out.println("accountId: " + accountId);
        System.out.println("accountBalanceHbar: " + accountBalanceHbar);
    }
}
