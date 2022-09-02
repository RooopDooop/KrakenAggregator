package wsLib

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"time"

	krakenLib "J.Morin/KrakenScraper/KrakenLib"
	"github.com/gorilla/websocket"
	"github.com/robfig/cron"
)

type websocketCall struct {
	MessageID int    `json:"MessageID"`
	Action    string `json:"Action"`
	TimeSent  int64  `json:"TimeSent"`
	Message   string `json:"Message"`
}

var connSocket *websocket.Conn
var errDial error

var done chan interface{}
var interrupt chan os.Signal

var cronTicker *cron.Cron
var cronOHLC *cron.Cron
var cronTrades *cron.Cron
var cronOrders *cron.Cron

var assignedPair string

func receiveHandler() {
	defer close(done)
	for {
		_, msg, err := connSocket.ReadMessage()
		if err != nil {
			log.Println("Error in receive:", err)
			return
		}

		var wsMessage websocketCall
		errWsMess := json.Unmarshal(msg, &wsMessage)
		if errWsMess != nil {
			panic(errWsMess)
		}

		switch wsMessage.Action {
		case "ClientWelcoming":
			fmt.Println("Server message: " + wsMessage.Message)
			BeginPairWork(assignedPair)
			startCronJobs(assignedPair)
		case "ClientConnected":
			fmt.Println("Client connected to server: " + wsMessage.Message)
		case "ClientDisconnected":
			fmt.Println("Client disconnected from server: " + wsMessage.Message)
		case "ClientError":
			fmt.Println("Received Error from server: " + wsMessage.Message)
		case "tickVerification":
			PairVerificationTick(wsMessage, assignedPair)
		case "TerminateClient":
			//TODO terminate here
			fmt.Println("Server has indicated a termination of the client")
			stopCronJobs()

			//TODO somehow close the runtime
		}
	}
}

func ConnectToServer(strPair string) {
	assignedPair = strPair

	done = make(chan interface{})    // Channel to indicate that the receiverHandler is done
	interrupt = make(chan os.Signal) // Channel to listen for interrupt signal to terminate gracefully

	signal.Notify(interrupt, os.Interrupt) // Notify the interrupt channel for SIGINT

	socketUrl := "ws://localhost:8081" + "/"
	connSocket, _, errDial = websocket.DefaultDialer.Dial(socketUrl, nil)
	if errDial != nil {
		log.Fatal("Error connecting to Websocket Server:", errDial)
	}

	defer connSocket.Close()

	go receiveHandler()

	for {
		select {
		case <-interrupt:
			//TODO disconnect from DB
			// We received a SIGINT (Ctrl + C). Terminate gracefully...
			log.Println("Received SIGINT interrupt signal. Closing all pending connections")
			//UnbindPair()
			//disconnectFromRedis()

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

func startCronJobs(pairName string) {
	cronOHLC = krakenLib.GenerateCronOHLC(pairName)
	cronTicker = krakenLib.GenerateCronTicker(pairName)
	cronTrades = krakenLib.GenerateCronTrades(pairName)
	cronOrders = krakenLib.GenerateCronOrders(pairName)

	cronOHLC.Start()
	cronTicker.Start()
	cronTrades.Start()
	cronOrders.Start()
}

func stopCronJobs() {
	cronTicker.Stop()
	cronOHLC.Stop()
	cronTrades.Stop()
	cronOrders.Stop()
}
