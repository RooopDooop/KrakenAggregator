package wsLib

import (
	"encoding/json"
	"math/rand"
	"time"

	"github.com/gorilla/websocket"
)

func BeginPairWork(strPair string, connSocket *websocket.Conn) {
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

func PairVerificationTick(objMessage websocketCall, strPair string, connSocket *websocket.Conn) {
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
