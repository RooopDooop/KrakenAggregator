package krakenLib

import (
	"encoding/json"
	"sync"
	"time"

	"github.com/gorilla/websocket"
)

type WebsocketCall struct {
	MessageID int    `json:"MessageID"`
	Action    string `json:"Action"`
	TimeSent  int64  `json:"TimeSent"`
	Message   string `json:"Message"`
}

func ScheduleJob(connSocket *websocket.Conn, websocketMutex *sync.Mutex, strAction string, URL string) {
	var jsonMessage WebsocketCall = WebsocketCall{
		MessageID: 0,
		Action:    strAction,
		TimeSent:  time.Now().Unix(),
		Message:   URL,
	}

	byteJSON, errMarsh := json.Marshal(jsonMessage)
	if errMarsh != nil {
		panic(errMarsh)
	}

	websocketMutex.Lock()
	err := connSocket.WriteMessage(websocket.TextMessage, byteJSON)
	if err != nil {
		panic(err.Error())
	}
	websocketMutex.Unlock()
}
