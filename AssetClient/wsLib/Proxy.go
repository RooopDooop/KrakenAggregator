package wsLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
)

type proxyData struct {
	Speed       int      `json:"Speed"`
	IP          string   `json:"IP"`
	Port        string   `json:"Port"`
	LastCheckup int      `json:"LastCheckup"`
	Descriptors []string `json:"Descriptors"`
	Location    string   `json:"Location"`
}

func requestProxy() {
	response, err := http.Get("http://localhost:8080/randomProxy")

	if err != nil {
		fmt.Print(err.Error())
		os.Exit(1)
	}

	responseData, err := ioutil.ReadAll(response.Body)
	if err != nil {
		log.Fatal(err)
	}

	var proxyInfo proxyData

	errUnMashal := json.Unmarshal(responseData, &proxyInfo)

	if errUnMashal != nil {

		// if error is not nil
		// print error
		fmt.Println(errUnMashal)
	}

	fmt.Println(proxyInfo)
}
