package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"

	"github.com/go-redis/redis"
)

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
