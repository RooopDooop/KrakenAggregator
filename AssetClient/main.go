package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"os"
	"os/signal"
	"time"

	"github.com/go-redis/redis"
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

var strPair string = ""
var strProxy string = ""

var redisClient *redis.Client
var redisErr error

var cronTicker *cron.Cron
var cronOHLC *cron.Cron
var cronTrades *cron.Cron
var cronOrders *cron.Cron

func main() {
	//connectToDB()
	connectToServer()
}

func connectToServer() {
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
	requestPair()

	// Our main loop for the client
	// We send our relevant packets here
	for {
		select {
		case <-interrupt:
			//TODO disconnect from DB
			// We received a SIGINT (Ctrl + C). Terminate gracefully...
			log.Println("Received SIGINT interrupt signal. Closing all pending connections")
			unbindPair()
			disconnectFromRedis()

			// Close our websocket connection
			err := connSocket.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, "Client shutting down..."))
			if err != nil {
				unbindPair()
				log.Println("Error during closing websocket:", err)
				return
			}

			select {
			case <-done:
				log.Println("Receiver Channel Closed! Exiting....")
				unbindPair()
			case <-time.After(time.Duration(1) * time.Second):
				log.Println("Timeout in closing receiving channel. Exiting....")
				unbindPair()
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

		var wsMessage websocketCall
		errWsMess := json.Unmarshal(msg, &wsMessage)
		if errWsMess != nil {
			panic(errWsMess)
		}

		switch wsMessage.Action {
		case "AssignPair":
			strPair = wsMessage.Message
			requestProxy()
			pairReceived(wsMessage)
			connectToRedis()
		case "ReceiveProxy":
			strProxy = wsMessage.Message
			fmt.Println("Proxy received: " + strProxy)
		}
	}
}

func requestPair() {
	var jsonMessage websocketCall = websocketCall{
		Action:   "RequestPair",
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

func pairReceived(objMessage websocketCall) {
	fmt.Println("Pair has been received: " + objMessage.Message)

	var jsonMessage websocketCall = websocketCall{
		MessageID: objMessage.MessageID,
		Action:    "PairReceived",
		TimeSent:  time.Now().Unix(),
		Message:   "",
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

func requestProxy() {
	var jsonMessage websocketCall = websocketCall{
		MessageID: rand.Intn(100000),
		Action:    "AssignProxy",
		TimeSent:  time.Now().Unix(),
		Message:   "",
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

	if strProxy != "" {
		os.Setenv("HTTP_PROXY", "http://"+strProxy)
	}
}

func unbindPair() {
	var jsonMessage websocketCall = websocketCall{
		MessageID: rand.Intn(100000),
		Action:    "unbindPair",
		TimeSent:  time.Now().Unix(),
		Message:   strPair,
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

func connectToRedis() {
	//TODO re-implement a timeout
	redisClient = redis.NewClient(&redis.Options{
		Addr:         "localhost:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	//TODO REDIS if it was on docker
	//172.1.1.20

	fmt.Println("Connected to redis DB!")

	startCronTicker()
	startCronOHLC()
	startCronTrades()
	startCronOrders()
}

func disconnectFromRedis() {
	redisClient.Close()

	stopCronTicker()
	stopCronOHLC()
	stopCronTrades()
	stopCronOrders()

	fmt.Println("Closed redis DB connection!")
}

func startCronTicker() {
	watchTicker(redisClient, strPair)

	cronTicker = cron.New()
	cronTicker.AddFunc("@every 1h", func() {
		fmt.Println("Executing CRON job for {TICKER DATA} at: " + time.Now().UTC().String())
		watchTicker(redisClient, strPair)
	})

	cronTicker.Start()
}

func startCronOHLC() {
	watchOHLC(redisClient, strPair)

	cronOHLC = cron.New()
	cronOHLC.AddFunc("@every 10m", func() {
		fmt.Println("Executing CRON job for {OHLC DATA} at: " + time.Now().UTC().String())
		watchOHLC(redisClient, strPair)
	})

	cronOHLC.Start()
}

func startCronTrades() {
	watchTrades(redisClient, strPair)

	cronTrades = cron.New()
	cronTrades.AddFunc("@every 5m", func() {
		fmt.Println("Executing Trade job at: " + time.Now().UTC().String())
		watchTrades(redisClient, strPair)
	})

	cronTrades.Start()
}

func startCronOrders() {
	watchOrderBook(redisClient, strPair)

	cronOrders = cron.New()
	cronOrders.AddFunc("@every 1m", func() {
		fmt.Println("Executing Order job at: " + time.Now().UTC().String())
		watchOrderBook(redisClient, strPair)
	})

	cronOrders.Start()
}

func stopCronTicker() {
	cronTicker.Stop()
}

func stopCronOHLC() {
	cronOHLC.Stop()
}

func stopCronTrades() {
	cronTrades.Stop()
}

func stopCronOrders() {
	cronOrders.Stop()
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

	//TODO REDIS if it was on docker
	//172.1.1.20

	fmt.Println("Connected to redis DB!")

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
