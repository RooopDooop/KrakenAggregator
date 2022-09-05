package main

import (
	"flag"
	"fmt"
	"os"
	"strconv"
	"strings"
	"sync"

	"J.Morin/KrakenScraper/wsLib"
)

var strRawPairs string

var Done = make(chan interface{})
var Interrupt = make(chan os.Signal)

func main() {
	flag.StringVar(&strRawPairs, "pair", "Unknown", "This is used to assign the pair the client should be watching")
	flag.Parse()

	var wg sync.WaitGroup
	var threadNumber int = 0

	for _, strPair := range strings.Split(strRawPairs, ",") {
		wg.Add(1)

		fmt.Println("Starting Thread: " + strconv.Itoa(threadNumber) + " - " + strPair)
		go wsLib.ConnectToServer(strPair, Done, Interrupt)

		threadNumber += 1
	}

	wg.Wait()
}
