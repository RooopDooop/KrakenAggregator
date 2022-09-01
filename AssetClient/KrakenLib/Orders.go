package krakenLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"time"

	proxyLib "J.Morin/KrakenScraper/proxyLib"
	redisLib "J.Morin/KrakenScraper/redisLib"

	"github.com/go-redis/redis"
	"github.com/robfig/cron"
)

func GenerateCronOrders(strPair string) *cron.Cron {
	watchOrderBook(redisLib.ConnectToRedis(), strPair)

	cronOrders := cron.New()
	cronOrders.AddFunc("@every 1m", func() {
		fmt.Println("Executing Order job at: " + time.Now().UTC().String())
		watchOrderBook(redisLib.ConnectToRedis(), strPair)
	})

	return cronOrders
}

func watchOrderBook(client *redis.Client, strPair string) {
	fmt.Println("Processing Order Book: " + strPair)
	proxyLib.RequestProxy()

	resp, err := http.Get("https://api.kraken.com/0/public/Depth?pair=" + strPair)
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
						client.HSet("OrderBid:"+strPair+"#"+epoch, "Price", objBid.([]interface{})[0])
						client.HSet("OrderBid:"+strPair+"#"+epoch, "Volume", objBid.([]interface{})[1])
					}
				} else if interfaceHeader == "asks" {
					for _, objAsk := range objBook.([]interface{}) {
						var epoch string = fmt.Sprintf("%v", int(objAsk.([]interface{})[2].(float64)))

						client.HSet("OrderAsk:"+strPair+"#"+epoch, "Price", objAsk.([]interface{})[0])
						client.HSet("OrderAsk:"+strPair+"#"+epoch, "Volume", objAsk.([]interface{})[1])
					}
				}
			}
		}

	}

	fmt.Println("Order Book Inserts Completed")
	redisLib.DisconnectFromRedis(client)
}
