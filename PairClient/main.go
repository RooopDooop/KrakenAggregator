package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/go-redis/redis"
	"github.com/robfig/cron"
)

func main() {
	connectToDB()
}

func connectToDB() {
	//TODO fix this
	//I don't like this, Timeouts exists for a reason
	//Had to do this for now, had IO timeout errors as the database grows
	//Will come back and fix it next commit
	client := redis.NewClient(&redis.Options{
		Addr:         "172.1.1.20:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	fmt.Println("Connected to redis DB!")

	GetAssetInfo(client)
	GetAssetPairData(client)

	fmt.Println("Arming CRON jobs...")

	cronPair := cron.New()
	cronPair.AddFunc("@every 30m", func() {
		fmt.Println("Executing CRON job for {TICKER DATA} at: " + time.Now().UTC().String())
		GetAssetInfo(client)
		GetAssetPairData(client)
	})

	cronPair.Start()

	fmt.Println("CRONs armed and ready")
	time.Sleep(time.Duration(1<<63 - 1))
}

func GetAssetInfo(client *redis.Client) {
	resp, err := http.Get("https://api.kraken.com/0/public/Assets")
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

	for _, objResult := range response["result"].(map[string]interface{}) {
		var aClass string = objResult.(map[string]interface{})["aclass"].(string)
		var altName string = objResult.(map[string]interface{})["altname"].(string)
		var decimal string = fmt.Sprintf("%v", objResult.(map[string]interface{})["decimals"].(float64))
		var displayDecimal string = fmt.Sprintf("%v", objResult.(map[string]interface{})["display_decimals"].(float64))

		if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
			rdb.HSet("Asset:"+altName, "Class", aClass)
			rdb.HSet("Asset:"+altName, "Decimal", decimal)
			rdb.HSet("Asset:"+altName, "DisplayDecimals", displayDecimal)
			rdb.HSet("Asset:"+altName, "CollateralValue", determineCollateral(objResult.(map[string]interface{})))
			rdb.HSet("Asset:"+altName, "Fiat", determineFiat(altName))
			return nil
		}); err != nil {
			panic(err)
		}
	}

	fmt.Println("Completed Asset Pull!")
}

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

			fmt.Println("Updating: " + pName)
			wg.Done()
		}(objProtoResult)
	}

	wg.Wait()
}

func determineFiat(strAlternativeName string) string {
	switch strAlternativeName {
	case "GBP", "GBP.HOLD", "AUD", "AUD.HOLD", "EUR", "EUR.HOLD", "CAD", "CAD.HOLD", "USD", "USD.HOLD", "CHF", "CHF.HOLD", "JPY":
		return "true"
	}

	return "false"
}

func determineCollateral(collateralValue map[string]interface{}) string {
	if collateralValue["collateral_value"] != nil {
		collateralValue := fmt.Sprintf("%v", collateralValue["collateral_value"].(float64))

		return collateralValue
	}

	return "N/A"
}