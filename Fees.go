package main

import (
	"fmt"
	"reflect"
	"strconv"
)

func processFees(orderFees []interface{}, makerFee bool, strPair string) {
	var strMaker string

	if makerFee {
		strMaker = "Maker"
	} else {
		strMaker = "Standard"
	}

	for _, arrFees := range orderFees {
		intVolume := int(reflect.ValueOf(arrFees).Index(0).Interface().(float64))
		strPercentage := fmt.Sprintf("%v", (reflect.ValueOf(arrFees).Index(1).Interface().(float64)))

		afterInsertExists, PairID := determineIfPairExists(strPair)

		if !afterInsertExists {
			panic("Pair not found")
		}

		feeExists, FeeID := determineIfFeeExists(*PairID, strMaker, strconv.Itoa(intVolume))

		if feeExists {
			sqlUpdateFee := "UPDATE [KrakenDB].[dbo].[PairFeeInstances] " +
				"SET FeePercentCost = " + strPercentage + " " +
				"WHERE FeeID = " + strconv.Itoa(*FeeID)

			_, errUpdateFee := deb.Exec(sqlUpdateFee)
			if errUpdateFee != nil {
				panic(errUpdateFee)
			}
		} else {
			sqlInsertFee := "INSERT INTO [KrakenDB].[dbo].[PairFeeInstances] (PairID, FeeType, FeeVolume, FeePercentCost) " +
				"VALUES (" + strconv.Itoa(*PairID) + ", '" + strMaker + "', " + strconv.Itoa(intVolume) + ", " + strPercentage + ")"

			_, errInsertFee := deb.Exec(sqlInsertFee)
			if errInsertFee != nil {
				panic(errInsertFee)
			}
		}
	}
}
