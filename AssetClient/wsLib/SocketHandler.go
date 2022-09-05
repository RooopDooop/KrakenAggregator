package wsLib

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"time"

	krakenLib "J.Morin/KrakenScraper/krakenLib"
	"github.com/gorilla/websocket"
	"github.com/robfig/cron"
)

type websocketCall struct {
	MessageID int    `json:"MessageID"`
	Action    string `json:"Action"`
	TimeSent  int64  `json:"TimeSent"`
	Message   string `json:"Message"`
}

func receiveHandler(strPair string, done chan interface{}, connSocket *websocket.Conn) {
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
			BeginPairWork(strPair, connSocket)
		case "ClientConnected":
			fmt.Println("Client connected to server: " + wsMessage.Message)
		case "ClientDisconnected":
			fmt.Println("Client disconnected from server: " + wsMessage.Message)
		case "ClientError":
			fmt.Println("Received Error from server: " + wsMessage.Message)
		case "tickVerification":
			PairVerificationTick(wsMessage, strPair, connSocket)
		case "TerminateClient":
			//TODO terminate here
			fmt.Println("Server has indicated a termination of the client")

			//TODO somehow close the runtime
		}
	}
}

func ConnectToServer(strPair string, done chan interface{}, interrupt chan os.Signal) {
	//done = make(chan interface{})    // Channel to indicate that the receiverHandler is done
	//interrupt = make(chan os.Signal) // Channel to listen for interrupt signal to terminate gracefully

	var cronTicker *cron.Cron = krakenLib.GenerateCronOHLC(strPair)
	var cronOHLC *cron.Cron = krakenLib.GenerateCronTicker(strPair)
	var cronTrades *cron.Cron = krakenLib.GenerateCronTrades(strPair)
	var cronOrders *cron.Cron = krakenLib.GenerateCronOrders(strPair)

	cronOHLC.Start()
	cronTicker.Start()
	cronTrades.Start()
	cronOrders.Start()

	signal.Notify(interrupt, os.Interrupt) // Notify the interrupt channel for SIGINT

	socketUrl := "ws://192.168.0.13:8081" + "/"
	connSocket, _, errDial := websocket.DefaultDialer.Dial(socketUrl, nil)
	if errDial != nil {
		log.Fatal("Error connecting to Websocket Server:", errDial)
	}

	defer connSocket.Close()

	go receiveHandler(strPair, done, connSocket)

	for {
		select {
		case <-interrupt:
			//TODO disconnect from DB
			// We received a SIGINT (Ctrl + C). Terminate gracefully...
			log.Println("Received SIGINT interrupt signal. Closing all pending connections")
			//UnbindPair()
			//disconnectFromRedis()
			cronTicker.Stop()
			cronOHLC.Stop()
			cronTrades.Stop()
			cronOrders.Stop()

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
