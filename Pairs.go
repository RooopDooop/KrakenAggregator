package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
	"sync"

	"github.com/go-redis/redis"
)

func GetAssetPairData(client *redis.Client) {
	resp, err := http.Get("https://api.kraken.com/0/public/AssetPairs")
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

	var wg sync.WaitGroup

	for _, objProtoResult := range response["result"].(map[string]interface{}) {

		wg.Add(1)
		go func(objResult interface{}) {

			var pName string = objResult.(map[string]interface{})["altname"].(string)
			var wsName string = objResult.(map[string]interface{})["wsname"].(string)

			var pDecimal float64 = objResult.(map[string]interface{})["pair_decimals"].(float64)
			var lDecimal float64 = objResult.(map[string]interface{})["lot_decimals"].(float64)
			var lMultiplier float64 = objResult.(map[string]interface{})["lot_multiplier"].(float64)

			var feeCurrency string = objResult.(map[string]interface{})["fee_volume_currency"].(string)

			var marginCall float64 = objResult.(map[string]interface{})["margin_call"].(float64)
			var marginStop float64 = objResult.(map[string]interface{})["margin_stop"].(float64)

			var orderMin string = objResult.(map[string]interface{})["ordermin"].(string)

			if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
				rdb.HSet(pName, "HashType", "AssetPairs")
				rdb.HSet(pName, "Base", strings.Split(wsName, "/")[0])
				rdb.HSet(pName, "Quote", strings.Split(wsName, "/")[1])
				rdb.HSet(pName, "WebsocketName", wsName)
				rdb.HSet(pName, "PairDecimals", pDecimal)
				rdb.HSet(pName, "LotDecimals", lDecimal)
				rdb.HSet(pName, "LotMultiplier", lMultiplier)
				rdb.HSet(pName, "FeeCurrency", feeCurrency)
				rdb.HSet(pName, "MarginCall", marginCall)
				rdb.HSet(pName, "MarginStop", marginStop)
				rdb.HSet(pName, "OrderMinimum", orderMin)
				rdb.HSet(pName, "LeverageBuy", fmt.Sprintf("%v", objResult.(map[string]interface{})["leverage_buy"].([]interface{})))
				rdb.HSet(pName, "LeverageSell", fmt.Sprintf("%v", objResult.(map[string]interface{})["leverage_sell"].([]interface{})))
				rdb.HSet(pName, "Fees", fmt.Sprintf("%v", objResult.(map[string]interface{})["fees"].([]interface{})))
				rdb.HSet(pName, "FeesMaker", fmt.Sprintf("%v", objResult.(map[string]interface{})["fees_maker"].([]interface{})))
				return nil
			}); err != nil {
				panic(err)
			}
			wg.Done()
		}(objProtoResult)
	}

	wg.Wait()
}
