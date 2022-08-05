package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
)

func GetAssetInfo() {
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

		boolAlreadyExists, AssetID := determineIfAssetExists(altName)

		if boolAlreadyExists {
			sqlUpdateExisting := "UPDATE [KrakenDB].[dbo].[Assets] " +
				"SET Class = '" + aClass + "', " +
				"   Decimals = " + decimal + ", " +
				"	DisplayDecimals = " + displayDecimal + ", " +
				"	CollateralValue = " + determineCollateral(objResult.(map[string]interface{})) + ", " +
				"	FiatAsset = " + determineFiat(altName) + " " +
				"WHERE AssetID = " + strconv.Itoa(*AssetID) + ""

			returnVal, errUpdate := deb.Exec(sqlUpdateExisting)

			if errUpdate != nil {
				panic(errUpdate)
			}

			_, errAffected := returnVal.RowsAffected()

			if errAffected != nil {
				panic(errAffected)
			}

			//fmt.Println(strconv.Itoa(*AssetID) + " exists - " + strconv.FormatInt(affectedRows, 2) + " rows updated")
		} else {
			_, errInsert := deb.Exec("USE [KrakenDB] INSERT INTO Assets(Class, AlternativeName, Decimals, DisplayDecimals, CollateralValue, FiatAsset) VALUES('" + aClass + "', '" + altName + "', " + decimal + ", " + displayDecimal + ", " + determineCollateral(objResult.(map[string]interface{})) + ", " + determineFiat(altName) + ");")

			if errInsert != nil {
				panic(errInsert)
			}

			fmt.Println(altName + " created")
		}
	}

	fmt.Println("Completed Asset Pull!")
}
