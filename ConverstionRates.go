package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
)

type FiatData struct {
	FiatID          int
	AlternativeName string
}

func GetFiatExchange() {
	var objStrFiat []FiatData = []FiatData{}

	sqlFiatAssets := "SELECT AssetID, AlternativeName FROM KrakenDB.dbo.Assets WHERE FiatAsset = 1 AND CollateralValue IS NOT NULL AND AlternativeName != 'USD'"
	rowFiat, errFiat := deb.Query(sqlFiatAssets)

	if errFiat != nil {
		panic(errFiat)
	}

	for rowFiat.Next() {
		var objFiat FiatData

		scanErr := rowFiat.Scan(&objFiat.FiatID, &objFiat.AlternativeName)

		if scanErr != nil {
			panic(scanErr)
		}

		objStrFiat = append(objStrFiat, objFiat)
	}

	for _, objFiat := range objStrFiat {
		fmt.Println("Fetching conversion data for: " + objFiat.AlternativeName)
		client := &http.Client{}

		requestData, err := http.NewRequest("GET", "https://api.apilayer.com/exchangerates_data/convert?to=USD&from="+objFiat.AlternativeName+"&amount=1", nil)
		if err != nil {
			log.Fatalln(err)
		}

		requestData.Header.Set("apikey", "PRPO39POzisheTTNCG8cqeLHYS6ZfPjJ")

		conversionData, sfg := client.Do(requestData)

		if sfg != nil {
			log.Fatalln(sfg)
		}

		bodyBytes, bodyErr := ioutil.ReadAll(conversionData.Body)
		if bodyErr != nil {
			log.Fatalln(bodyErr)
		}

		var response map[string]interface{}

		if errResponse := json.Unmarshal(bodyBytes, &response); errResponse != nil {
			log.Fatal(errResponse)
		}

		conversionEpoch := int(response["info"].(map[string]interface{})["timestamp"].(float64))
		conversionRate := fmt.Sprintf("%f", response["info"].(map[string]interface{})["rate"].(float64))

		sqlInsertConv := "EXEC [KrakenDB].[dbo].[insertFiatConversion] @convEpoch = " + strconv.Itoa(conversionEpoch) + ", @convFromID = " + strconv.Itoa(objFiat.FiatID) + ", @convToID = 5021, @convRate = " + conversionRate

		_, errInsertConv := deb.Exec(sqlInsertConv)

		if errInsertConv != nil {
			panic(errInsertConv)
		}
	}

	fmt.Println("Completed Conversion Fetch!")
}