package krakenLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
)

type Order struct {
	AlternativePairName string
	Type                string
	Price               string
	Volume              string
	Timestamp           int64
}

func ProcessOrder(chanWSResponse chan []byte, URL string) {
	var PairName string = strings.Split(URL, "?pair=")[1]

	resp, err := http.Get(URL)
	if err != nil {
		log.Fatalln(err)
	}

	defer resp.Body.Close()

	bodyBytes, bodyErr := ioutil.ReadAll(resp.Body)
	if bodyErr != nil {
		log.Fatalln(bodyErr)
	}

	var response map[string]interface{}

	if errResponse := json.Unmarshal(bodyBytes, &response); errResponse != nil {
		log.Fatal(errResponse)
	}

	var arrOrders []Order = []Order{}

	if response["result"] != nil {
		for _, objResult := range response["result"].(map[string]interface{}) {
			for interfaceHeader, objBook := range objResult.(map[string]interface{}) {
				if interfaceHeader == "bids" {
					for _, objBid := range objBook.([]interface{}) {
						var epoch int64 = int64(objBid.([]interface{})[2].(float64))

						var objOrders Order = Order{
							PairName,
							"Bid",
							fmt.Sprint(objBid.([]interface{})[0]),
							fmt.Sprint(objBid.([]interface{})[1]),
							epoch,
						}

						//var epoch string = fmt.Sprintf("%v", int(objBid.([]interface{})[2].(float64)))
						/*client.HSet("OrderBid:"+PairName+"#"+epoch+"#"+fmt.Sprint(objBid.([]interface{})[0])+"#"+fmt.Sprint(objBid.([]interface{})[1]), "Price", objBid.([]interface{})[0])
						client.HSet("OrderBid:"+PairName+"#"+epoch+"#"+fmt.Sprint(objBid.([]interface{})[0])+"#"+fmt.Sprint(objBid.([]interface{})[1]), "Volume", objBid.([]interface{})[1])*/

						arrOrders = append(arrOrders, objOrders)
					}
				} else if interfaceHeader == "asks" {
					for _, objAsk := range objBook.([]interface{}) {
						var epoch int64 = int64(objAsk.([]interface{})[2].(float64))

						var objOrders Order = Order{
							PairName,
							"Ask",
							fmt.Sprint(objAsk.([]interface{})[0]),
							fmt.Sprint(objAsk.([]interface{})[1]),
							epoch,
						}

						//

						/*client.HSet("OrderAsk:"+PairName+"#"+epoch+"#"+fmt.Sprint(objAsk.([]interface{})[0])+"#"+fmt.Sprint(objAsk.([]interface{})[1]), "Price", objAsk.([]interface{})[0])
						client.HSet("OrderAsk:"+PairName+"#"+epoch+"#"+fmt.Sprint(objAsk.([]interface{})[0])+"#"+fmt.Sprint(objAsk.([]interface{})[1]), "Volume", objAsk.([]interface{})[1])*/

						arrOrders = append(arrOrders, objOrders)
					}
				}
			}
		}
	}

	jsonOrders, errJson := json.Marshal(arrOrders)
	if errJson != nil {
		panic(errJson.Error())
	}

	ScheduleJob(chanWSResponse, "SubmitOrders", string(jsonOrders))
	fmt.Println("Order processed: " + PairName)
}

/*
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

	socketSync.Lock()
	err := connSocket.WriteMessage(websocket.TextMessage, strJson)
	if err != nil {
		panic(err.Error())
	}
	socketSync.Unlock()
*/
