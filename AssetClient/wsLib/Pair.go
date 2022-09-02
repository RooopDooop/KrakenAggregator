package wsLib

import (
	"encoding/json"
	"math/rand"
	"time"

	"github.com/gorilla/websocket"
)

func BeginPairWork(strPair string) {
	rand.Seed(time.Now().UnixNano())
	var jsonMessage websocketCall = websocketCall{
		MessageID: rand.Intn(9999999),
		Action:    "BeginPairWork",
		TimeSent:  time.Now().Unix(),
		Message:   strPair,
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

/*func RequestPair() {
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
}*/

/*func ReceivedPair(objMessage websocketCall) {
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
}*/

func PairVerificationTick(objMessage websocketCall, strPair string) {
	var jsonMessage websocketCall = websocketCall{
		MessageID: objMessage.MessageID,
		Action:    "tickVerifyResponse",
		TimeSent:  time.Now().Unix(),
		Message:   strPair,
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
