package krakenLib

import (
	"encoding/json"
	"sync"

	"github.com/gorilla/websocket"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type WebsocketCall struct {
	MessageID primitive.ObjectID `json:"MessageID"`
	Action    string             `json:"Action"`
	Message   string             `json:"Message"`
}

func ScheduleJob(connSocket *websocket.Conn, websocketMutex *sync.Mutex, strAction string, URL string) {
	var jsonMessage WebsocketCall = WebsocketCall{
		Action:  strAction,
		Message: URL,
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
