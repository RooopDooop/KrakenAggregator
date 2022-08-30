package wsLib

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
	MessageID int    `json:"MessageID"`
	Action    string `json:"Action"`
	TimeSent  int64  `json:"TimeSent"`
	Message   string `json:"Message"`
}

var connSocket *websocket.Conn
var errDial error

var done chan interface{}
var interrupt chan os.Signal

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
			RequestPair()
		case "ClientConnected":
			fmt.Println("Client connected to server: " + wsMessage.Message)
		case "ClientDisconnected":
			fmt.Println("Client disconnected from server: " + wsMessage.Message)
		case "ClientError":
			fmt.Println("Received Error from server: " + wsMessage.Message)
		case "PairAssignment":
			ReceivedPair(wsMessage)
		case "tickVerification":
			fmt.Println("Server is verifying: " + wsMessage.Message + " - against: " + strPair)
			PairVerificationTick(wsMessage)

			/*case "AssignPair":
			strPair = wsMessage.Message
			//requestProxy()
			pairReceived(wsMessage)
			connectToRedis()*/
		}
	}
}

func ConnectToServer() {
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
			disconnectFromRedis()

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
