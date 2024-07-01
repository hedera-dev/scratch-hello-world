## Prep tasks

- [x] Hello world with Golang
    - Print "hello world"
    - Import Hedera Go SDK
    - Demo multiple subdirectories are possible
- [x] Hello world with Java
    - Print "hello world"
    - Import Hedera Java SDK
    - Demo multiple subdirectories are possible

## Actual tasks

- Create 1 repo for java, 1 repo for golang
- Each repo will have multiple subdirectories
    - 00-create-fund-account
    - 01-hfs-files-sdk
    - 02-hscs-smart-contract-sdk --> Note: **Not* using EthersJs
    - 04-hts-ft-sdk
    - 06-hcs-topic
- Each of these will have script translated from the Javascript original
- Intent is to have as close as possible match, so that the tutorial can be followed in choice of any programming language.

- [x] java
    - [x] 00-create-fund-account
    - [x] 01-hfs-files-sdk
    - [x] 02-hscs-smart-contract-sdk --> Note: **Not** using EthersJs
    - [x] 04-hts-ft-sdk
    - [x] 06-hcs-topic
- [x] golang
    - [x] 00-create-fund-account
    - [x] 01-hfs-files-sdk
    - [x] 02-hscs-smart-contract-sdk --> Note: **Not** using EthersJs
    - [x] 04-hts-ft-sdk
    - [x] 06-hcs-topic

## Feedback

- [x] You don't need to freeze and then sign with the accountKey because you've already set it as operatorKey and this key will be the payer and signer of the transaction by default
  - NOTE This suggestion works against the Golang SDK, however, it results in an error against the Java SDK:
    ```text
    Exception in thread "main" java.lang.IllegalStateException: Signing requires transaction to be frozen
        at com.hedera.hashgraph.sdk.Transaction.signWith(Transaction.java:891)
        at com.hedera.hashgraph.sdk.Transaction.sign(Transaction.java:879)
        at hfsfiles.ScriptHfsFiles.main(ScriptHfsFiles.java:37)
    ```
  - WONTFIX Based on above feedback, SDK engineering team has identified a bug, and is fixing it. Summary is that the behaviour seen in Java was correct, and there should be an error, but the Go SDK was not doing this, and should do so too.. See: https://github.com/hashgraph/hedera-sdk-go/issues/946
- [x] When getting the receipt it's good to set the validation status to true resp.SetValidateStatus(true).GetReceipt(env.Client)
  - FIXED in Golang + Java - validate status performed on all occurrences of get receipt
  - TODO check that this works in JS as well
- [x] Here instead of nesting the if else statements, I'd just log.Fatalf if there is an error
  - WONTFIX - this is intentional, as I explicitly want to print nil/null values when the account has yet to be created or funded
- [x] Here I don't think you need to wait 5 seconds. You can check out this example and take some ideas to enhance your topic example. - https://github.com/hashgraph/hedera-sdk-go/blob/main/examples/consensus_pub_sub/main.go
  - FIXED in Golang + Java - sleep removed
  - TODO check that this works in JS as well

## Clean up

- [ ] Recreate a copy of the existing JS hello world sequences in this repo, so that it is easier to work with them side by side
- [ ] Add/ update README files for all 3 languages, for each subdirectory
- [ ] Ensure subfolder name consistency between repos of the 3 different languages
- [ ] Ensure consistency between comments across repos for all 3 languages
- [ ] For Java imports, each SDK class explicitly rather than use the catch-call import
- [ ] Language translations of the redacted lines
- [ ] Update to include links from repo to docs.hedera.com
- [ ] Split this repo into 2x repos, 1 each for java and golang, and start with `completed` branch
- [ ] In the `main` branches, redact specific lines to create initial version from the completed version as comments
