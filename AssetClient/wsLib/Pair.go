package wsLib

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"strconv"
	"time"

	"github.com/gorilla/websocket"
)

var strPair string = ""

func RequestPair() {
	rand.Seed(time.Now().UnixNano())
	var jsonMessage websocketCall = websocketCall{
		MessageID: rand.Intn(9999999),
		Action:    "RequestPair",
		TimeSent:  time.Now().Unix(),
		Message:   "",
	}

	strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	err := connSocket.WriteMessage(websocket.TextMessage, []byte(strJson))
	if err != nil {
		panic(err)
	}
}

func ReceivedPair(objMessage websocketCall) {
	//Somehow begin CRON jobs from here
	fmt.Println("Server has Assigned: " + objMessage.Message)
	strPair = objMessage.Message

	var jsonMessage websocketCall = websocketCall{
		MessageID: objMessage.MessageID,
		Action:    "PairReceived",
		TimeSent:  time.Now().Unix(),
		Message:   objMessage.Message,
	}

	strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	err := connSocket.WriteMessage(websocket.TextMessage, []byte(strJson))
	if err != nil {
		panic(err)
	}
}

func PairVerificationTick(objMessage websocketCall) {
	var responseAnswer bool = false

	if objMessage.Message == strPair {
		responseAnswer = true
	}

	var jsonMessage websocketCall = websocketCall{
		MessageID: objMessage.MessageID,
		Action:    "tickVerifyResponse",
		TimeSent:  time.Now().Unix(),
		Message:   strconv.FormatBool(responseAnswer),
	}

	strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	err := connSocket.WriteMessage(websocket.TextMessage, []byte(strJson))
	if err != nil {
		panic(err)
	}
}
