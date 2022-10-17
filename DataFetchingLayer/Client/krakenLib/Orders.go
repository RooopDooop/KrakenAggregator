package krakenLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	redisLib "J.Morin/KrakenScraper/redisLib"

	"github.com/go-redis/redis"
)

func ProcessOrder(client *redis.Client, URL string) {
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

	if response["result"] != nil {
		for _, objResult := range response["result"].(map[string]interface{}) {
			for interfaceHeader, objBook := range objResult.(map[string]interface{}) {
				if interfaceHeader == "bids" {
					for _, objBid := range objBook.([]interface{}) {
						var epoch string = fmt.Sprintf("%v", int(objBid.([]interface{})[2].(float64)))
						client.HSet("OrderBid:"+PairName+"#"+epoch+"#"+fmt.Sprint(objBid.([]interface{})[0])+"#"+fmt.Sprint(objBid.([]interface{})[1]), "Price", objBid.([]interface{})[0])
						client.HSet("OrderBid:"+PairName+"#"+epoch+"#"+fmt.Sprint(objBid.([]interface{})[0])+"#"+fmt.Sprint(objBid.([]interface{})[1]), "Volume", objBid.([]interface{})[1])
					}
				} else if interfaceHeader == "asks" {
					for _, objAsk := range objBook.([]interface{}) {
						var epoch string = fmt.Sprintf("%v", int(objAsk.([]interface{})[2].(float64)))

						client.HSet("OrderAsk:"+PairName+"#"+epoch+"#"+fmt.Sprint(objAsk.([]interface{})[0])+"#"+fmt.Sprint(objAsk.([]interface{})[1]), "Price", objAsk.([]interface{})[0])
						client.HSet("OrderAsk:"+PairName+"#"+epoch+"#"+fmt.Sprint(objAsk.([]interface{})[0])+"#"+fmt.Sprint(objAsk.([]interface{})[1]), "Volume", objAsk.([]interface{})[1])
					}
				}
			}
		}

	}

	fmt.Println("Order processed: " + PairName)
	redisLib.DisconnectFromRedis(client)
}
