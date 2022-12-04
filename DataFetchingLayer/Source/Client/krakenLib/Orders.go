package krakenLib

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

func ProcessOrder(mongoClient *mongo.Client, URL string) {
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

	var arrDocuments []interface{}

	now := time.Now()
	orderCollections := mongoClient.Database("KrakenDB").Collection("Orders")
	if response["result"] != nil {
		for _, objResult := range response["result"].(map[string]interface{}) {
			for interfaceHeader, objBook := range objResult.(map[string]interface{}) {
				if interfaceHeader == "bids" {
					for _, objBid := range objBook.([]interface{}) {
						shaEncoded := sha256.Sum256([]byte(fmt.Sprintf("%v", objBid)))
						objBid := bson.M{
							"_id":                 shaEncoded,
							"LocalInsertTime":     now.Unix(),
							"AlternativePairName": PairName,
							"Type":                "Bid",
							"Epoch":               int64(objBid.([]interface{})[2].(float64)),
							"Price":               fmt.Sprint(objBid.([]interface{})[0]),
							"Volume":              fmt.Sprint(objBid.([]interface{})[1]),
						}

						arrDocuments = append(arrDocuments, objBid)
					}
				} else if interfaceHeader == "asks" {
					for _, objAsk := range objBook.([]interface{}) {
						shaEncoded := sha256.Sum256([]byte(fmt.Sprintf("%v", objAsk)))
						objAsk := bson.M{
							"_id":                 shaEncoded,
							"LocalInsertTime":     now.Unix(),
							"AlternativePairName": PairName,
							"Type":                "Ask",
							"Epoch":               int64(objAsk.([]interface{})[2].(float64)),
							"Price":               fmt.Sprint(objAsk.([]interface{})[0]),
							"Volume":              fmt.Sprint(objAsk.([]interface{})[1]),
						}

						arrDocuments = append(arrDocuments, objAsk)
					}
				}
			}
		}

		resultsInsert, errInsert := orderCollections.InsertMany(context.Background(), arrDocuments)
		if errInsert != nil {
			if strings.Split(errInsert.Error(), ":")[0] != "bulk write exception" {
				panic(errInsert)
			}
		}

		fmt.Println(strconv.FormatInt(now.Unix(), 10) + " - Order processed: " + PairName + " - " + URL + " - Inserted Quantity: " + strconv.Itoa(len(resultsInsert.InsertedIDs)))
	}
}
