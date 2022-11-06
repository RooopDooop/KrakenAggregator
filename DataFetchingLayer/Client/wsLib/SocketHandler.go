package wsLib

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"strings"
	"time"

	"github.com/gorilla/websocket"
	"github.com/robfig/cron/v3"

	krakenLib "J.Morin/KrakenScraper/krakenLib"
)

func StartWebsocket() {
	done := make(chan interface{})    // Channel to indicate that the receiverHandler is done
	interrupt := make(chan os.Signal) // Channel to listen for interrupt signal to terminate gracefully

	signal.Notify(interrupt, os.Interrupt) // Notify the interrupt channel for SIGINT

	socketUrl := "ws://localhost:8081" + "/"
	connSocket, _, errDial := websocket.DefaultDialer.Dial(socketUrl, nil)
	if errDial != nil {
		log.Fatal("Error connecting to Websocket Server:", errDial)
	}

	defer connSocket.Close()

	go receiveHandler(done, connSocket)

	for {
		select {
		case <-interrupt:
			// We received a SIGINT (Ctrl + C). Terminate gracefully...
			log.Println("Received SIGINT interrupt signal. Closing all pending connections")
			os.Exit(1)

			// Close our websocket connection
			err := connSocket.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, "Client shutting down..."))
			if err != nil {
				log.Println("Error during closing websocket:", err)
				return
			}

			select {
			case <-done:
				log.Println("Receiver Channel Closed! Exiting....")
			case <-time.After(time.Duration(1) * time.Second):
				log.Println("Timeout in closing receiving channel. Exiting....")
			}
			return
		}
	}
}

func receiveHandler(done chan interface{}, connSocket *websocket.Conn) {
	defer close(done)
	CRONScheduler := cron.New()

	var chanWSResponse chan []byte = make(chan []byte)
	go WSResponseQueue(connSocket, chanWSResponse)

	for {
		_, msg, err := connSocket.ReadMessage()
		if err != nil {
			log.Println("Error in receive:", err)
			return
		}

		var wsMessage krakenLib.WebsocketCall
		errWsMess := json.Unmarshal(msg, &wsMessage)
		if errWsMess != nil {
			panic(errWsMess)
		}

		switch wsMessage.Action {
		case "ClientWelcoming":
			fmt.Println("Successfully connected to server!")
		case "AssignPairs":
			//TODO re-assign pairs, just stop all CRON jobs and restart messages

			for _, job := range CRONScheduler.Entries() {
				CRONScheduler.Remove(job.ID)
			}

			for _, strPair := range strings.Split(wsMessage.Message, ", ") {
				CRONPair := strPair
				//OHLC every 10m
				CRONScheduler.AddFunc("@every 10m", func() {
					go krakenLib.ScheduleJob(chanWSResponse, "ScheduleOHLC", "https://api.kraken.com/0/public/OHLC?pair="+CRONPair)
				})

				//Ticker every 1h
				CRONScheduler.AddFunc("@every 1h", func() {
					go krakenLib.ScheduleJob(chanWSResponse, "ScheduleTicker", "https://api.kraken.com/0/public/Ticker?pair="+CRONPair)
				})

				//Trades every 5m
				CRONScheduler.AddFunc("@every 5m", func() {
					go krakenLib.ScheduleJob(chanWSResponse, "ScheduleTrade", "https://api.kraken.com/0/public/Trades?pair="+CRONPair)
				})

				//Orders every 2m
				CRONScheduler.AddFunc("@every 3m", func() {
					go krakenLib.ScheduleJob(chanWSResponse, "ScheduleOrder", "https://api.kraken.com/0/public/Depth?pair="+CRONPair)
				})
			}

			CRONScheduler.Start()
		case "ProcessTrade":
			go krakenLib.ProcessTrades(chanWSResponse, wsMessage.Message)
		case "ProcessTicker":
			go krakenLib.ProcessTicker(chanWSResponse, wsMessage.Message)
		case "ProcessOrder":
			go krakenLib.ProcessOrder(chanWSResponse, wsMessage.Message)
		case "ProcessOHLC":
			go krakenLib.ProcessOHLC(chanWSResponse, wsMessage.Message)
		}
	}
}

func WSResponseQueue(connSocket *websocket.Conn, chanWSResponse chan []byte) {
	for true {
		byteJSON := <-chanWSResponse

		err := connSocket.WriteMessage(websocket.TextMessage, byteJSON)
		if err != nil {
			panic(err.Error())
		}
	}
}
