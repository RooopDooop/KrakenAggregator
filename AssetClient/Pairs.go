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
				rdb.HSet("AssetPair:"+pName, "Base", strings.Split(wsName, "/")[0])
				rdb.HSet("AssetPair:"+pName, "Quote", strings.Split(wsName, "/")[1])
				rdb.HSet("AssetPair:"+pName, "WebsocketName", wsName)
				rdb.HSet("AssetPair:"+pName, "PairDecimals", pDecimal)
				rdb.HSet("AssetPair:"+pName, "LotDecimals", lDecimal)
				rdb.HSet("AssetPair:"+pName, "LotMultiplier", lMultiplier)
				rdb.HSet("AssetPair:"+pName, "FeeCurrency", feeCurrency)
				rdb.HSet("AssetPair:"+pName, "MarginCall", marginCall)
				rdb.HSet("AssetPair:"+pName, "MarginStop", marginStop)
				rdb.HSet("AssetPair:"+pName, "OrderMinimum", orderMin)
				rdb.HSet("AssetPair:"+pName, "LeverageBuy", fmt.Sprintf("%v", objResult.(map[string]interface{})["leverage_buy"].([]interface{})))
				rdb.HSet("AssetPair:"+pName, "LeverageSell", fmt.Sprintf("%v", objResult.(map[string]interface{})["leverage_sell"].([]interface{})))
				rdb.HSet("AssetPair:"+pName, "Fees", fmt.Sprintf("%v", objResult.(map[string]interface{})["fees"].([]interface{})))
				rdb.HSet("AssetPair:"+pName, "FeesMaker", fmt.Sprintf("%v", objResult.(map[string]interface{})["fees_maker"].([]interface{})))
				return nil
			}); err != nil {
				panic(err)
			}
			wg.Done()
		}(objProtoResult)
	}

	wg.Wait()
}
