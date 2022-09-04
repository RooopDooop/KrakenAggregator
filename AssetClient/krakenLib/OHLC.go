package krakenLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"time"

	"github.com/go-redis/redis"
	"github.com/robfig/cron"

	proxyLib "J.Morin/KrakenScraper/proxyLib"
	redisLib "J.Morin/KrakenScraper/redisLib"
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

func GenerateCronOHLC(strPair string) *cron.Cron {
	watchOHLC(redisLib.ConnectToRedis(), strPair)

	cronOHLC := cron.New()
	cronOHLC.AddFunc("@every 10m", func() {
		fmt.Println("Executing CRON job for {OHLC DATA} at: " + time.Now().UTC().String())
		watchOHLC(redisLib.ConnectToRedis(), strPair)
	})

	return cronOHLC
}

func watchOHLC(client *redis.Client, strPair string) {
	proxyLib.RequestProxy()

	if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
		fmt.Println("Processing OHLC: " + strPair)
		resp, errCall := http.Get("https://api.kraken.com/0/public/OHLC?pair=" + strPair + "&interval=1")
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
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceOpen", OHLCData.([]interface{})[1].(string))
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceHigh", OHLCData.([]interface{})[2].(string))
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceLow", OHLCData.([]interface{})[3].(string))
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceClose", OHLCData.([]interface{})[4].(string))
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "PriceVolumeWeightedAverage", OHLCData.([]interface{})[5].(string))
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "Volume", OHLCData.([]interface{})[6].(string))
						rdb.HSet("OHCL:"+strPair+"#"+strconv.Itoa(int(OHLCData.([]interface{})[0].(float64))), "Count", OHLCData.([]interface{})[7].(float64))
					}
				}
			}
		}
		return nil
	}); err != nil {
		panic(err)
	}
	fmt.Println("OHLC Inserts Completed")
	redisLib.DisconnectFromRedis(client)
}
