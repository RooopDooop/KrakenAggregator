package krakenLib

import (
	"encoding/json"
	"time"
)

type WebsocketCall struct {
	MessageID int    `json:"MessageID"`
	Action    string `json:"Action"`
	TimeSent  int64  `json:"TimeSent"`
	Message   string `json:"Message"`
}

func ScheduleJob(chanWSResponse chan []byte, strAction string, URL string) {
	var jsonMessage WebsocketCall = WebsocketCall{
		MessageID: 0,
		Action:    strAction,
		TimeSent:  time.Now().Unix(),
		Message:   URL,
	}

	strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	chanWSResponse <- strJson

	/*strJson, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	err := connSocket.WriteMessage(websocket.TextMessage, strJson)
	if err != nil {
		panic(err.Error())
	}*/
}
