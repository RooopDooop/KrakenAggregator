package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	"github.com/go-redis/redis"
)

func watchOrderBook(client *redis.Client) {
	if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
		for _, strPair := range fetchAssetsPairs(client) {
			fmt.Println("Processing Trade Book: " + strings.Split(strPair, ":")[1])
			resp, err := http.Get("https://api.kraken.com/0/public/Depth?pair=" + strings.Split(strPair, ":")[1])
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
								rdb.HSet("OrderBid:"+strings.Split(strPair, ":")[1]+"#"+epoch, "Price", objBid.([]interface{})[0])
								rdb.HSet("OrderBid:"+strings.Split(strPair, ":")[1]+"#"+epoch, "Volume", objBid.([]interface{})[1])
							}
						} else if interfaceHeader == "asks" {
							for _, objAsk := range objBook.([]interface{}) {
								var epoch string = fmt.Sprintf("%v", int(objAsk.([]interface{})[2].(float64)))

								rdb.HSet("OrderAsk:"+strings.Split(strPair, ":")[1]+"#"+epoch, "Price", objAsk.([]interface{})[0])
								rdb.HSet("OrderAsk:"+strings.Split(strPair, ":")[1]+"#"+epoch, "Volume", objAsk.([]interface{})[1])
							}
						}
					}
				}

			}
		}
		return nil
	}); err != nil {
		panic(err)
	}

	fmt.Println("Order Book Inserts Completed")
}
