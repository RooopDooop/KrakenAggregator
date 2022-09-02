package main

import (
	"flag"

	wsLib "J.Morin/KrakenScraper/wsLib"
)

var strPair string

func main() {
	flag.StringVar(&strPair, "pair", "Unknown", "This is used to assign the pair the client should be watching")
	flag.Parse()

	wsLib.ConnectToServer(strPair)
}
