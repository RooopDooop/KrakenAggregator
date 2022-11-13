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

type Ticker struct {
	AskingPrice          string
	AskingWholeLotVolume string
	AskingLotVolume      string

	BuyPrice          string
	BuyWholeLotVolume string
	BuyLotVolume      string

	LastTradePrice  string
	LastTradeVolume string

	VolumeToday          string
	VolumeLastTwentyFour string

	VolumeWeightedToday          string
	VolumeWeightedLastTwentyFour string

	TradeQuantity               float64
	TradeQuantityLastTwentyFour float64

	LowToday          string
	LowLastTwentyFour string

	HighToday          string
	HighLastTwentyFour string

	OpeningPrice string
}

func ProcessTicker(sqlConn *sql.DB, URL string) {
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

	var arrTickers []Ticker = []Ticker{}
	for _, objResult := range response["result"].(map[string]interface{}) {
		var objTicker Ticker = Ticker{
			objResult.(map[string]interface{})["a"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["a"].([]interface{})[1].(string),
			objResult.(map[string]interface{})["a"].([]interface{})[2].(string),

			objResult.(map[string]interface{})["b"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["b"].([]interface{})[1].(string),
			objResult.(map[string]interface{})["b"].([]interface{})[2].(string),

			objResult.(map[string]interface{})["c"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["c"].([]interface{})[0].(string),

			objResult.(map[string]interface{})["v"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["v"].([]interface{})[1].(string),

			objResult.(map[string]interface{})["p"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["p"].([]interface{})[1].(string),

			objResult.(map[string]interface{})["t"].([]interface{})[0].(float64),
			objResult.(map[string]interface{})["t"].([]interface{})[1].(float64),

			objResult.(map[string]interface{})["l"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["l"].([]interface{})[1].(string),

			objResult.(map[string]interface{})["h"].([]interface{})[0].(string),
			objResult.(map[string]interface{})["h"].([]interface{})[1].(string),

			objResult.(map[string]interface{})["o"].(string),
		}

		arrTickers = append(arrTickers, objTicker)
	}

	jsonTickers, errJson := json.Marshal(arrTickers)
	if errJson != nil {
		panic(errJson.Error())
	}

	var rowsAffected int
	execErr := sqlConn.QueryRow("EXEC PUT_InsertTickers @JSONData='" + string(jsonTickers) + "', @AlternativeName='" + PairName + "'").Scan(&rowsAffected)
	if execErr != nil {
		panic(execErr)
	}

	fmt.Println("ticker processed: " + PairName + " - Affected: " + strconv.Itoa(rowsAffected) + " Array Size: " + strconv.Itoa(len(arrTickers)) + " - " + URL)

	//fmt.Println(PairName + " - " + string(jsonOrders))
}
