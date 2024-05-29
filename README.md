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

- [ ] java
    - [ ] 00-create-fund-account
    - [x] 01-hfs-files-sdk
    - [ ] 02-hscs-smart-contract-sdk --> Note: **Not** using EthersJs
    - [ ] 04-hts-ft-sdk
    - [ ] 06-hcs-topic
- [x] golang
    - [x] 00-create-fund-account
    - [x] 01-hfs-files-sdk
    - [x] 02-hscs-smart-contract-sdk --> Note: **Not** using EthersJs
    - [x] 04-hts-ft-sdk
    - [x] 06-hcs-topic

## Clean up

- Ensure subfolder name consistency between repos of the 3 different languages
- Redact specific lines to create initial version from the completed version as comments
- Include translation of the redacted lines
- Update to include links from repo to docs.hedera.com
- Ensure consistency between comments across repos for all 3 languages
- Split this repo into 2x repos, 1 each for java and golang, and start with `completed` branch
- Add README files for the 2x new repos
- Use the redaction comments to make the initial version in the `main` branch
