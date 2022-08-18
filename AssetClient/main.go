package main

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"time"

	"github.com/gorilla/websocket"
)

type websocketCall struct {
	Action   string `json:"Action"`
	TimeSent int64  `json:"TimeSent"`
	Message  string `json:"Message"`
}

var connSocket *websocket.Conn
var errDial error

var done chan interface{}
var interrupt chan os.Signal

func main() {
	//connectToDB()
	connectToServer()
}

func connectToServer() {
	done = make(chan interface{})    // Channel to indicate that the receiverHandler is done
	interrupt = make(chan os.Signal) // Channel to listen for interrupt signal to terminate gracefully

	signal.Notify(interrupt, os.Interrupt) // Notify the interrupt channel for SIGINT

	socketUrl := "ws://localhost:8080" + "/"
	connSocket, _, errDial = websocket.DefaultDialer.Dial(socketUrl, nil)
	if errDial != nil {
		log.Fatal("Error connecting to Websocket Server:", errDial)
	}

	defer connSocket.Close()

	go receiveHandler()
	requestPairs()

	// Our main loop for the client
	// We send our relevant packets here
	for {
		select {
		case <-interrupt:
			// We received a SIGINT (Ctrl + C). Terminate gracefully...
			log.Println("Received SIGINT interrupt signal. Closing all pending connections")

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

func receiveHandler() {
	defer close(done)
	for {
		_, msg, err := connSocket.ReadMessage()
		if err != nil {
			log.Println("Error in receive:", err)
			return
		}

		log.Println(string(msg))

		var wsMessage websocketCall
		errWsMess := json.Unmarshal(msg, &wsMessage)
		if errWsMess != nil {
			panic(errWsMess)
		}

		fmt.Println(wsMessage)

		switch wsMessage.Action {
		case "AssigningPairs":
			fmt.Println(wsMessage.Message)
		}
	}
}

func requestPairs() {
	var jsonMessage websocketCall = websocketCall{
		Action:   "RequestPairs",
		TimeSent: time.Now().Unix(),
		Message:  "",
	}

	strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	err := connSocket.WriteMessage(websocket.TextMessage, []byte(strJson))
	if err != nil {
		log.Println("Error during writing to websocket:", err)
		return
	}
}

//func connectToDB() {
//TODO fix this
//I don't like this, Timeouts exists for a reason
//Had to do this for now, had IO timeout errors as the database grows
//Will come back and fix it next commit
/*client := redis.NewClient(&redis.Options{
		Addr:         "localhost:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	fmt.Println("Connected to redis DB!")

	GetAssetInfo(client)
	GetAssetPairData(client)
	GetFiatExchange(client)
	watchTicker(client)
	watchOHLC(client)
	watchTrades(client)
	watchOrderBook(client)

	fmt.Println("Arming CRON jobs...")

	cronTicker := cron.New()
	cronTicker.AddFunc("@every 1h", func() {
		fmt.Println("Executing CRON job for {TICKER DATA} at: " + time.Now().UTC().String())
		watchTicker(client)
	})

	cronOHCL := cron.New()
	cronOHCL.AddFunc("@every 10m", func() {
		fmt.Println("Executing OHLC job at: " + time.Now().UTC().String())
		watchOHLC(client)
	})

	cronConversion := cron.New()
	cronOHCL.AddFunc("@every 24h", func() {
		fmt.Println("Executing Conversion job at: " + time.Now().UTC().String())
		GetFiatExchange(client)
	})

	cronTrades := cron.New()
	cronTrades.AddFunc("@every 5m", func() {
		fmt.Println("Executing Trade job at: " + time.Now().UTC().String())
		watchTrades(client)
	})

	cronOrders := cron.New()
	cronOrders.AddFunc("@every 1m", func() {
		fmt.Println("Executing Trade job at: " + time.Now().UTC().String())
		watchOrderBook(client)
	})

	cronOHCL.Start()
	cronTicker.Start()
	cronConversion.Start()
	cronTrades.Start()
	cronOrders.Start()

	fmt.Println("CRONs armed and ready")
	time.Sleep(time.Duration(1<<63 - 1))
}*/
