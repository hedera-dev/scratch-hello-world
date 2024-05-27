package hello1;

import com.hedera.hashgraph.sdk.AccountId;

public class Hello1 {
    public static void main(String[] args) {
        System.out.println("Hello World! (1)");
        AccountId accId = AccountId.fromString("0.0.12345");
        System.out.println(accId);
    }
}
