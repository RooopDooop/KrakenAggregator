package wsLib

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"strings"
	"sync"
	"time"

	"github.com/gorilla/websocket"
	"github.com/robfig/cron/v3"
)

type WebsocketCall struct {
	MessageID int    `json:"MessageID"`
	Action    string `json:"Action"`
	TimeSent  int64  `json:"TimeSent"`
	Message   string `json:"Message"`
}

func StartWebsocket() {
	done := make(chan interface{})    // Channel to indicate that the receiverHandler is done
	interrupt := make(chan os.Signal) // Channel to listen for interrupt signal to terminate gracefully

	signal.Notify(interrupt, os.Interrupt) // Notify the interrupt channel for SIGINT

	socketUrl := "ws://localhost:8080" + "/"
	connSocket, _, errDial := websocket.DefaultDialer.Dial(socketUrl, nil)
	if errDial != nil {
		log.Fatal("Error connecting to Websocket Server:", errDial)
	}

	defer connSocket.Close()

	go receiveHandler(done, connSocket)

	for {
		select {
		case <-interrupt:
			//TODO disconnect from DB
			// We received a SIGINT (Ctrl + C). Terminate gracefully...
			log.Println("Received SIGINT interrupt signal. Closing all pending connections")
			//UnbindPair()
			//disconnectFromRedis()
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

	for {
		_, msg, err := connSocket.ReadMessage()
		if err != nil {
			log.Println("Error in receive:", err)
			return
		}

		var wsMessage WebsocketCall
		errWsMess := json.Unmarshal(msg, &wsMessage)
		if errWsMess != nil {
			panic(errWsMess)
		}

		switch wsMessage.Action {
		case "ClientWelcoming":
			fmt.Println("Successfully connected to server")
			//RequestPairs(connSocket)
			//BeginPairWork(strPair, connSocket)
		case "AssignPairs":
			//TODO re-assign pairs, just stop all CRON jobs and restart messages

			for _, job := range CRONScheduler.Entries() {
				CRONScheduler.Remove(job.ID)
			}

			var socketSync sync.Mutex
			for _, strPair := range strings.Split(wsMessage.Message, ", ") {
				CRONPair := strPair
				/*CRONScheduler.AddFunc("@every 15s", func() {
					fmt.Println(strconv.Itoa(len(CRONScheduler.Entries())) + " - CRON JOB OHLC: " + CRONPair)
				})

				CRONScheduler.AddFunc("@every 10s", func() {
					fmt.Println(strconv.Itoa(len(CRONScheduler.Entries())) + " - CRON JOB Ticker: " + CRONPair)
				})*/

				CRONScheduler.AddFunc("@every 10s", func() {
					ScheduleTradeJob(connSocket, &socketSync, CRONPair)
				})

				/*CRONScheduler.AddFunc("@every 1s", func() {
					fmt.Println(strconv.Itoa(len(CRONScheduler.Entries())) + " - CRON JOB Orders: " + CRONPair)
				})*/
			}

			CRONScheduler.Start()

			/*case "ClientConnected":
				fmt.Println("Client connected to server: " + wsMessage.Message)
			case "ClientDisconnected":
				fmt.Println("Client disconnected from server: " + wsMessage.Message)
			case "ClientError":
				fmt.Println("Received Error from server: " + wsMessage.Message)
			case "tickVerification":
				//PairVerificationTick(wsMessage, strPair, connSocket)
			case "TerminateClient":
				//TODO terminate here
				fmt.Println("Server has indicated a termination of the client")*/

			//TODO somehow close the runtime
		}
	}
}

func ScheduleTradeJob(connSocket *websocket.Conn, socketSync *sync.Mutex, strPair string) {
	var jsonMessage WebsocketCall = WebsocketCall{
		MessageID: 0,
		Action:    "ScheduleTrade",
		TimeSent:  time.Now().Unix(),
		Message:   "https://api.kraken.com/0/public/Trades?pair=" + strPair,
	}

	strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	socketSync.Lock()
	err := connSocket.WriteMessage(websocket.TextMessage, strJson)
	if err != nil {
		panic(err)
	}
	socketSync.Unlock()
}
