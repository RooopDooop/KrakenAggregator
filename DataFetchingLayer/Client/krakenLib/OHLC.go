package krakenLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"
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

func ProcessOHLC(chanWSResponse chan []byte, URL string) {
	var PairName string = strings.Split(URL, "?pair=")[1]

	resp, errCall := http.Get(URL + "&interval=1")
	if errCall != nil {
		panic(errCall)
	}

	defer resp.Body.Close()

	bodyBytes, bodyErr := ioutil.ReadAll(resp.Body)
	if bodyErr != nil {
		panic(bodyErr)
	}

	var response map[string]interface{}

	if errResponse := json.Unmarshal(bodyBytes, &response); errResponse != nil {
		panic(errResponse)
	}

	if response["result"] != nil {
		for headerStr, objResult := range response["result"].(map[string]interface{}) {
			if headerStr != "last" {
				for _, OHLCData := range objResult.([]interface{}) {
					/*rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceOpen", OHLCData.([]interface{})[1].(string))
					rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceHigh", OHLCData.([]interface{})[2].(string))
					rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceLow", OHLCData.([]interface{})[3].(string))
					rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceClose", OHLCData.([]interface{})[4].(string))
					rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceVolumeWeightedAverage", OHLCData.([]interface{})[5].(string))
					rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "Volume", OHLCData.([]interface{})[6].(string))
					rdb.HSet("OHCL:"+PairName+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "Count", OHLCData.([]interface{})[7].(float64))*/

					fmt.Println(OHLCData)
				}
			}
		}
	}
	fmt.Println("OHLC processed: " + PairName)
}
