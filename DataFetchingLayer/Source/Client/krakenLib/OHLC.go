package krakenLib

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

type OHLC struct {
	Epoch                 float64
	Open                  string
	High                  string
	Low                   string
	Close                 string
	VolumeWeightedAverage string
	Volume                string
	Count                 float64
}

func ProcessOHLC(mongoClient *mongo.Client, URL string) {
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

	OHLCCollections := mongoClient.Database("KrakenDB").Collection("OHLCs")
	if response["result"] != nil {
		for headerStr, objResult := range response["result"].(map[string]interface{}) {
			if headerStr != "last" {
				for _, OHLCData := range objResult.([]interface{}) {
					shaEncoded := sha256.Sum256([]byte(fmt.Sprintf("%v", OHLCData)))
					objOHLC := bson.M{
						"_id":                   shaEncoded,
						"AlternativePairName":   PairName,
						"Epoch":                 OHLCData.([]interface{})[0].(float64),
						"Open":                  OHLCData.([]interface{})[1].(string),
						"High":                  OHLCData.([]interface{})[2].(string),
						"Low":                   OHLCData.([]interface{})[3].(string),
						"Close":                 OHLCData.([]interface{})[4].(string),
						"VolumeWeightedAverage": OHLCData.([]interface{})[5].(string),
						"Volume":                OHLCData.([]interface{})[6].(string),
						"Count":                 OHLCData.([]interface{})[7].(float64),
					}

					_, errInsert := OHLCCollections.InsertOne(context.Background(), objOHLC)
					if errInsert != nil {
						if strings.Split(errInsert.Error(), ":")[0] != "write exception" {
							panic(errInsert)
						}
					}
				}
			}
		}
	}

	fmt.Println("OHLC processed: " + PairName + " - " + URL)
}
