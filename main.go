package main

import (
	"database/sql"
	"fmt"
	"net/url"
	"time"

	_ "github.com/denisenkom/go-mssqldb"
	"github.com/robfig/cron/v3"
)

var deb *sql.DB

func main() {
	connectToDB()
}

func connectToDB() {
	query := url.Values{}
	query.Add("kraken", "KrakenScraper")

	u := &url.URL{
		Scheme:   "sqlserver",
		User:     url.UserPassword("sa" /*REMOVED*/),
		Host:     "192.168.0.12:1433",
		RawQuery: query.Encode(),
	}

	db, err := sql.Open("sqlserver", u.String())

	if err != nil {
		panic(err)
	}

	deb = db
	fmt.Println("Connected!")

	GetAssetInfo()
	GetAssetPairData()
	watchTrades()
	GetFiatExchange()
	watchTicker()
	watchOHLC()

	fmt.Println("Arming CRON jobs...")

	cronTicker := cron.New()
	cronTicker.AddFunc("@every 1h", func() {
		fmt.Println("Executing CRON job for {TICKER DATA} at: " + time.Now().UTC().String())
		watchTicker()
	})

	cronOHCL := cron.New()
	cronOHCL.AddFunc("@every 10m", func() {
		fmt.Println("Executing OHLC job at: " + time.Now().UTC().String())
		watchOHLC()
	})

	cronConversion := cron.New()
	cronOHCL.AddFunc("@every 24h", func() {
		fmt.Println("Executing Conversion job at: " + time.Now().UTC().String())
		GetFiatExchange()
	})

	cronTrades := cron.New()
	cronTrades.AddFunc("@every 5m", func() {
		fmt.Println("Executing Trade job at: " + time.Now().UTC().String())
		watchTrades()
	})

	cronOHCL.Start()
	cronTicker.Start()
	cronConversion.Start()
	cronTrades.Start()

	fmt.Println("CRONs armed and ready")
	time.Sleep(time.Duration(1<<63 - 1))
}
