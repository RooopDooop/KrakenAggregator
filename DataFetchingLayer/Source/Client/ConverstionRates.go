package main

import (
	"fmt"

	"github.com/go-redis/redis"
)

type FiatData struct {
	FiatID          int
	AlternativeName string
}

func GetFiatExchange(client *redis.Client) {
	//TODO reimplement this in the Java server

	/*for _, strFiat := range fetchFiatAssets(client) {
		var strFormatted string = strings.Split(strFiat, ":")[1]

		if strFormatted != "USD" {
			httpClient := &http.Client{}

			requestData, err := http.NewRequest("GET", "https://api.apilayer.com/exchangerates_data/convert?to=USD&from="+strFormatted+"&amount=1", nil)
			if err != nil {
				log.Fatalln(err)
			}

			requestData.Header.Set("apikey", "REMOVED")

			conversionData, sfg := httpClient.Do(requestData)

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

			if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
				rdb.HSet("FiatExchange:"+strFormatted+"#"+strconv.Itoa(conversionEpoch), "Rate", conversionRate)
				rdb.HSet("FiatExchange:"+strFormatted+"#"+strconv.Itoa(conversionEpoch), "ExchangedFiat", "USD")
				return nil
			}); err != nil {
				panic(err)
			}
		}
	}*/

	fmt.Println("Completed Conversion Fetch!")
}
