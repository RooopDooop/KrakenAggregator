package main

import (
	"fmt"
	"strconv"
)

func processLeverages(arrLeverage []interface{}, strType string, pairID int) {
	if len(arrLeverage) > 0 {
		if strType == "buy" {
			for _, intLeverage := range arrLeverage {
				exists, _ := determineIfLeverageExists(pairID, "Buy", fmt.Sprintf("%v", (intLeverage.(float64))))

				if !exists {
					sqlInsert := "INSERT INTO [KrakenDB].[dbo].[PairLeverageInstances](PairID, LeverageType, LeverageValue) VALUES (" + strconv.Itoa(pairID) + ", 'Buy', " + fmt.Sprintf("%v", (intLeverage.(float64))) + ")"
					_, errInsertFee := deb.Exec(sqlInsert)
					if errInsertFee != nil {
						panic(errInsertFee)
					}
				}
			}

		} else if strType == "sell" {
			for _, intLeverage := range arrLeverage {
				exists, _ := determineIfLeverageExists(pairID, "Sell", fmt.Sprintf("%v", (intLeverage.(float64))))

				if !exists {
					sqlInsert := "INSERT INTO [KrakenDB].[dbo].[PairLeverageInstances](PairID, LeverageType, LeverageValue) VALUES (" + strconv.Itoa(pairID) + ", 'Sell', " + fmt.Sprintf("%v", (intLeverage.(float64))) + ")"
					_, errInsertFee := deb.Exec(sqlInsert)
					if errInsertFee != nil {
						panic(errInsertFee)
					}
				}
			}
		} else {
			panic("Error, invalid leverage type")
		}
	}
}
