package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
)

type OHCLObject struct {
	PairID     int    `json:"PairID"`
	EpochTime  int    `json:"epochTime"`
	PriceOpen  string `json:"priceOpen"`
	PriceHigh  string `json:"priceHigh"`
	PriceLow   string `json:"priceLow"`
	PriceClose string `json:"priceClose"`
	VWAP       string `json:"VWAP"`
	Volume     string `json:"volume"`
	Count      int    `json:"count"`
}

type detectedEpoch struct {
	PairID int `json:"PairID"`
	Epoch  int `json:"epochTime"`
}

func watchOHLC() {
	var threadOne []OHCLObject = []OHCLObject{}
	var threadTwo []OHCLObject = []OHCLObject{}
	var threadThree []OHCLObject = []OHCLObject{}
	var threadFour []OHCLObject = []OHCLObject{}

	var threadCycle int = 0

	var arrEpochsAndID []interface{}

	for _, objPairData := range getPairs() {
		fmt.Println("Pulling OHLC data for: " + objPairData.AlternativeName)
		resp, err := http.Get("https://api.kraken.com/0/public/OHLC?pair=" + objPairData.AlternativeName + "&interval=1")
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
			for headerStr, objResult := range response["result"].(map[string]interface{}) {
				if headerStr != "last" {
					for _, OHLCData := range objResult.([]interface{}) {
						var OHCLInstance OHCLObject = OHCLObject{
							PairID:     objPairData.PairID,
							EpochTime:  int(OHLCData.([]interface{})[0].(float64)),
							PriceOpen:  OHLCData.([]interface{})[1].(string),
							PriceHigh:  OHLCData.([]interface{})[2].(string),
							PriceLow:   OHLCData.([]interface{})[3].(string),
							PriceClose: OHLCData.([]interface{})[4].(string),
							VWAP:       OHLCData.([]interface{})[5].(string),
							Volume:     OHLCData.([]interface{})[6].(string),
							Count:      int(OHLCData.([]interface{})[7].(float64)),
						}

						switch threadCycle {
						case 0:
							threadOne = append(threadOne, OHCLInstance)
							threadCycle += 1
						case 1:
							threadTwo = append(threadTwo, OHCLInstance)
							threadCycle += 1
						case 2:
							threadThree = append(threadThree, OHCLInstance)
							threadCycle += 1
						case 3:
							threadFour = append(threadFour, OHCLInstance)
							threadCycle = 0
						}

						arrEpochsAndID = append(arrEpochsAndID, map[string]interface{}{"PairID": OHCLInstance.PairID, "Epoch": OHCLInstance.EpochTime})
					}
				}
			}
		} else {
			fmt.Println("INVALID OHLC, skipping: " + objPairData.AlternativeName)
		}
	}

	jsonData, errJSON := json.Marshal(arrEpochsAndID)
	if errJSON != nil {
		panic(errJSON)
	}
	sqlGetEpochs := "EXEC [KrakenDB].[dbo].[getPairEpochs] @jsonEpochsAndID = '" + string(jsonData) + "'"

	rowEpoch, errEpoch := deb.Query(sqlGetEpochs)
	if errEpoch != nil {
		panic(errEpoch)
	}

	var arrFoundEpoch []detectedEpoch = []detectedEpoch{}

	for rowEpoch.Next() {
		var objFoundEpoch detectedEpoch

		scanErr := rowEpoch.Scan(&objFoundEpoch.PairID, &objFoundEpoch.Epoch)

		if scanErr != nil {
			panic(scanErr)
		}

		arrFoundEpoch = append(arrFoundEpoch, objFoundEpoch)
	}

	for w := 0; w <= 3; w++ {
		switch w {
		case 0:
			go processOHLCThread(0, arrFoundEpoch, threadOne)
		case 1:
			go processOHLCThread(1, arrFoundEpoch, threadTwo)
		case 2:
			go processOHLCThread(2, arrFoundEpoch, threadThree)
		case 3:
			go processOHLCThread(3, arrFoundEpoch, threadFour)
		}
	}

	fmt.Println("Threads started - Completed CRON job")
}

func processOHLCThread(threadCount int, arrEpochFound []detectedEpoch, arrThreadOHLCData []OHCLObject) {
	var threadInsert []OHCLObject = []OHCLObject{}

	for _, objOHCLInstance := range arrThreadOHLCData {
		var alreadyExists bool = false

		for _, objFoundEpoch := range arrEpochFound {
			if objFoundEpoch.Epoch == objOHCLInstance.EpochTime && objFoundEpoch.PairID == objOHCLInstance.PairID {
				alreadyExists = true
				break
			}
		}

		if !alreadyExists {
			threadInsert = append(threadInsert, objOHCLInstance)
		}

	}

	fmt.Println("Thread: " + strconv.Itoa(threadCount) + " Inserting count: " + strconv.Itoa(len(threadInsert)))

	b, err := json.Marshal(threadInsert)
	if err != nil {
		fmt.Printf("Error: %s", err)
		return
	}

	sqlInsertOHLC := "EXEC [KrakenDB].[dbo].[insertOHLC] @JSONData = '" + string(b) + "'"

	_, errInsertOHLC := deb.Exec(sqlInsertOHLC)

	if errInsertOHLC != nil {
		panic(errInsertOHLC)
	}
}
