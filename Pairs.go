package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"strings"
	"sync"
)

func GetAssetPairData() {
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

			baseExists, idBase := determineIfAssetExists(strings.Split(wsName, "/")[0])
			quoteExists, idQuote := determineIfAssetExists(strings.Split(wsName, "/")[1])

			if baseExists && quoteExists {
				pairExists, pairID := determineIfPairExists(pName)

				feeAssetExists, feeAssetID := determineIfAssetExists(feeCurrency[1:])

				if !feeAssetExists {
					panic("Fee Asset does not exist")
				}

				if pairExists {
					sqlUpdatePair := "UPDATE [KrakenDB].[dbo].[AssetPairs] " +
						"SET PairDecimals = " + fmt.Sprintf("%v", pDecimal) +
						", LotDecimals = " + fmt.Sprintf("%v", lDecimal) +
						", LotMultiplier = " + fmt.Sprintf("%v", lMultiplier) +
						", FeeCurrency = " + strconv.Itoa(*feeAssetID) +
						", MarginCall = " + fmt.Sprintf("%v", marginCall) +
						", MarginStop = " + fmt.Sprintf("%v", marginStop) +
						", OrderMinimum = " + orderMin +
						" WHERE PairID = " + strconv.Itoa(*pairID)

					_, errUpdate := deb.Exec(sqlUpdatePair)
					if errUpdate != nil {
						panic(errUpdate)
					}

					errPair, pairID := determineIfPairExists(pName)

					if !errPair {
						panic("Error, pair: " + pName + " - Not found")
					}

					orderFees := objResult.(map[string]interface{})["fees"].([]interface{})
					orderFeesMaker := objResult.(map[string]interface{})["fees_maker"].([]interface{})
					orderLeverageBuy := objResult.(map[string]interface{})["leverage_buy"].([]interface{})
					orderLeverageSell := objResult.(map[string]interface{})["leverage_sell"].([]interface{})

					//fmt.Println(pName + " updated - Processing fees and leverage now...")
					processFees(orderFees, false, pName)
					processFees(orderFeesMaker, true, pName)
					processLeverages(orderLeverageBuy, "buy", *pairID)
					processLeverages(orderLeverageSell, "sell", *pairID)

				} else {
					sqlInsertPair := "INSERT INTO [KrakenDB].[dbo].[AssetPairs] (AlternativePairName, WebsocketPairName, BaseID, QuoteID, PairDecimals, LotDecimals, LotMultiplier, FeeCurrency, MarginCall, MarginStop, OrderMinimum) " +
						"VALUES ('" + pName + "',  " +
						"'" + wsName + "', " +
						strconv.Itoa(*idBase) + ", " +
						strconv.Itoa(*idQuote) + ", " +
						fmt.Sprintf("%v", pDecimal) + ", " +
						fmt.Sprintf("%v", lDecimal) + ", " +
						fmt.Sprintf("%v", lMultiplier) + ", " +
						strconv.Itoa(*feeAssetID) + ", " +
						fmt.Sprintf("%v", marginCall) + ", " +
						fmt.Sprintf("%v", marginStop) + ", " +
						orderMin + ");"

					_, errInsert := deb.Exec(sqlInsertPair)
					if errInsert != nil {
						panic(errInsert)
					}

					errPair, pairID := determineIfPairExists(pName)

					if !errPair {
						panic("Error, pair: " + pName + " - Not found")
					}

					orderFees := objResult.(map[string]interface{})["fees"].([]interface{})
					orderFeesMaker := objResult.(map[string]interface{})["fees_maker"].([]interface{})
					orderLeverageBuy := objResult.(map[string]interface{})["leverage_buy"].([]interface{})
					orderLeverageSell := objResult.(map[string]interface{})["leverage_sell"].([]interface{})

					fmt.Println(pName + " created - Processing fees and leverage now...")
					processFees(orderFees, false, pName)
					processFees(orderFeesMaker, true, pName)
					processLeverages(orderLeverageBuy, "buy", *pairID)
					processLeverages(orderLeverageSell, "sell", *pairID)
				}
			} else {
				panic(wsName + " - NOT FOUND")
			}

			//fmt.Println(pName + " Processed")
			wg.Done()
		}(objProtoResult)
	}

	wg.Wait()
}
