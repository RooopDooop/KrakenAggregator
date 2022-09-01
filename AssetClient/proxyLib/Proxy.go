package proxyLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
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

func RequestProxy() {
	response, err := http.Get("http://192.168.0.20:8080/randomProxy")

	if err != nil {
		fmt.Print(err.Error())
		os.Exit(1)
	}

	responseData, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}

	var proxyInfo proxyData
	errUnMashal := json.Unmarshal(responseData, &proxyInfo)

	if errUnMashal != nil {
		panic(errUnMashal)
	}

	os.Setenv("HTTP_PROXY", "http://"+proxyInfo.IP+":"+proxyInfo.Port)
	fmt.Println("Proxy delivered: " + proxyInfo.IP + ":" + proxyInfo.Port)
}
