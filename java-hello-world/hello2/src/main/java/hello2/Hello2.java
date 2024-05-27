package hello2;

import com.hedera.hashgraph.sdk.AccountId;

public class Hello2 {
    public static void main(String[] args) {
        System.out.println("Hello World! (2)");
        AccountId accId = AccountId.fromString("0.0.12345");
        System.out.println(accId);
    }
}
