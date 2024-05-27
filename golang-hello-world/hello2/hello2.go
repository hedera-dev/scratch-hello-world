package main

import (
	"fmt"

	"github.com/hashgraph/hedera-sdk-go/v2"
)

func main() {
	fmt.Println("hello world (2)")
	accId, err := hedera.AccountIDFromString("0.0.12345")
	if err != nil {
		panic(err)
	}
	fmt.Println(accId)
}
