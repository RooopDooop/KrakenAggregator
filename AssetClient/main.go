package main

import (
	"flag"
	"fmt"

	wsLib "J.Morin/KrakenScraper/wsLib"
)

var strPair string

func main() {
	flag.StringVar(&strPair, "pair", "Unknown", "This is used to assign the pair the client should be watching")
	flag.Parse()

	fmt.Println(strPair)

	wsLib.ConnectToServer(strPair)
}
