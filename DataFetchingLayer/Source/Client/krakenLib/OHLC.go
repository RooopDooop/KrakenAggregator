package krakenLib

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
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

func ProcessOHLC(sqlConn *sql.DB, URL string) {
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

	var arrOHLC []OHLC = []OHLC{}

	if response["result"] != nil {
		for headerStr, objResult := range response["result"].(map[string]interface{}) {
			if headerStr != "last" {
				for _, OHLCData := range objResult.([]interface{}) {
					var objOHLC OHLC = OHLC{
						OHLCData.([]interface{})[0].(float64),
						OHLCData.([]interface{})[1].(string),
						OHLCData.([]interface{})[2].(string),
						OHLCData.([]interface{})[3].(string),
						OHLCData.([]interface{})[4].(string),
						OHLCData.([]interface{})[5].(string),
						OHLCData.([]interface{})[6].(string),
						OHLCData.([]interface{})[7].(float64),
					}

					arrOHLC = append(arrOHLC, objOHLC)
				}
			}
		}
	}

	jsonOHLCs, errJson := json.Marshal(arrOHLC)
	if errJson != nil {
		panic(errJson.Error())
	}

	var rowsAffected int
	execErr := sqlConn.QueryRow("EXEC PUT_InsertOHLCs @JSONData='" + string(jsonOHLCs) + "', @AlternativeName='" + PairName + "'").Scan(&rowsAffected)
	if execErr != nil {
		panic(execErr)
	}

	fmt.Println("OHLC processed: " + PairName + " - Affected: " + strconv.Itoa(rowsAffected) + " - Array Size: " + strconv.Itoa(len(arrOHLC)) + " - " + URL)
}
