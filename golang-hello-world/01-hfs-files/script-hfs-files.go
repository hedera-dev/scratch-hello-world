package main

import (
	"fmt"
	"log"
	"os"
	"strings"

	"github.com/hashgraph/hedera-sdk-go/v2"
	"github.com/joho/godotenv"
)

func main() {
	// Load environment variables from .env file
	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env")
	}

	accountIdStr := os.Getenv("ACCOUNT_ID")
	accountKeyStr := os.Getenv("ACCOUNT_PRIVATE_KEY")
	accountKeyStr = strings.TrimPrefix(accountKeyStr, "0x")

	accountId, err := hedera.AccountIDFromString(accountIdStr)
	if err != nil {
		log.Fatalf("Error parsing account ID: %v", err)
	}
	accountKey, err := hedera.PrivateKeyFromStringECDSA(accountKeyStr)
	if err != nil {
		log.Fatalf("Error parsing account private key: %v", err)
	}
	client := hedera.ClientForTestnet()
	client.SetOperator(accountId, accountKey)

	// Read file from disk
	localFileContents, err := os.ReadFile("my-file.txt")
	if err != nil {
		log.Fatalf("Error reading input file: %v", err)
	}

	// Write file onto Hedera Testnet, using HFS FileCreateTransaction
	fileCreateTx, err := hedera.NewFileCreateTransaction().
		// NOTE: File create transaction
		// Step (1) in the accompanying tutorial
		// /* ... */
		SetContents(localFileContents).
		FreezeWith(client)
	if err != nil {
		log.Fatalf("Error creating FileCreateTransaction: %v", err)
	}
	fileCreateTxSigned := fileCreateTx.Sign(accountKey)
	fileCreateTxSubmitted, err := fileCreateTxSigned.Execute(client)
	if err != nil {
		log.Fatalf("Error executing FileCreateTransaction: %v", err)
	}
	fileCreateTxId := fileCreateTxSubmitted.TransactionID
	fileCreateTxReceipt, err := fileCreateTxSubmitted.
		SetValidateStatus(true).
		GetReceipt(client)
	if err != nil {
		log.Fatalf("Error getting receipt for FileCreateTransaction: %v", err)
	}
	fileId := fileCreateTxReceipt.FileID

	// Read file from Hedera Testnet, using HFS FileContentsQuery
	fileReadQuery := hedera.NewFileContentsQuery().
		// NOTE: File contents query
		// Step (2) in the accompanying tutorial
		// /* ... */;
		SetFileID(*fileId)
	networkFileContents, err := fileReadQuery.Execute(client)
	if err != nil {
		log.Fatalf("Error executing for FileContentsQuery: %v", err)
	}

	txExplorerUrl := fmt.Sprintf("https://hashscan.io/testnet/transaction/%v", fileCreateTxId)
	fmt.Printf("fileId: %v\n", fileId)
	fmt.Printf("fileCreateTxId: %v\n", fileCreateTxId)
	fmt.Printf("txExplorerUrl: %v\n", txExplorerUrl)
	fmt.Printf("localFileContents: %s\n", localFileContents)
	fmt.Printf("networkFileContents: %s\n", networkFileContents)

	client.Close()
}
