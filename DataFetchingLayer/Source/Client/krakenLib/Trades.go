package krakenLib

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"strings"
)

type Trade struct {
	AlternativePairName string `json:"AlternativePairName"`
	Price               string `json:"Price"`
	Volume              string `json:"Volume"`
	Time                string `json:"tradeTime"`
	BuyOrSell           string `json:"BuyOrSell"`
	MarketOrLimit       string `json:"MarketOrLimit"`
}

func ProcessTrades(sqlConn *sql.DB, URL string) {
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

	var arrTrades []Trade = []Trade{}

	if response["result"] != nil {
		for key, arrRawTrades := range response["result"].(map[string]interface{}) {
			if key != "last" {
				for _, objTrade := range arrRawTrades.([]interface{}) {
					var BuyOrSell string
					if objTrade.([]interface{})[3].(string) == "b" {
						BuyOrSell = "Buy"
					} else if objTrade.([]interface{})[3].(string) == "s" {
						BuyOrSell = "Sell"
					}

					var MarketOrLimit string
					if objTrade.([]interface{})[4].(string) == "l" {
						MarketOrLimit = "Limit"
					} else if objTrade.([]interface{})[4].(string) == "m" {
						MarketOrLimit = "Market"
					}

					var objTrade Trade = Trade{
						PairName,
						objTrade.([]interface{})[0].(string),
						objTrade.([]interface{})[1].(string),
						fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)),
						BuyOrSell,
						MarketOrLimit,
					}

					arrTrades = append(arrTrades, objTrade)
				}
			}
		}
	}

	jsonTrades, errJson := json.Marshal(arrTrades)
	if errJson != nil {
		panic(errJson.Error())
	}

	var rowsAffected string
	execErr := sqlConn.QueryRow("EXEC PUT_InsertTrades @JSONData='" + string(jsonTrades) + "', @AlternativeName='" + PairName + "'").Scan(&rowsAffected)
	if execErr != nil {
		panic(execErr)
	}

	fmt.Println("Trade processed: " + PairName + " - Affected: " + rowsAffected + " Array Size: " + strconv.Itoa(len(arrTrades)) + " - " + URL)
}
