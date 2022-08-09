package main

import (
	"fmt"
	"time"

	"github.com/go-redis/redis"
	"github.com/robfig/cron"
)

func main() {
	connectToDB()
}

func connectToDB() {
	client := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "",
		DB:       0,
	})

	fmt.Println("Connected to redis DB!")

	GetAssetInfo(client)
	GetAssetPairData(client)
	GetFiatExchange(client)
	watchTicker(client)
	watchOHLC(client)
	watchTrades(client)
	watchOrderBook(client)

	fmt.Println("Arming CRON jobs...")

	cronTicker := cron.New()
	cronTicker.AddFunc("@every 1h", func() {
		fmt.Println("Executing CRON job for {TICKER DATA} at: " + time.Now().UTC().String())
		watchTicker(client)
	})

	cronOHCL := cron.New()
	cronOHCL.AddFunc("@every 10m", func() {
		fmt.Println("Executing OHLC job at: " + time.Now().UTC().String())
		watchOHLC(client)
	})

	cronConversion := cron.New()
	cronOHCL.AddFunc("@every 24h", func() {
		fmt.Println("Executing Conversion job at: " + time.Now().UTC().String())
		GetFiatExchange(client)
	})

	cronTrades := cron.New()
	cronTrades.AddFunc("@every 5m", func() {
		fmt.Println("Executing Trade job at: " + time.Now().UTC().String())
		watchTrades(client)
	})

	cronOrders := cron.New()
	cronOrders.AddFunc("@every 1m", func() {
		fmt.Println("Executing Trade job at: " + time.Now().UTC().String())
		watchOrderBook(client)
	})

	cronOHCL.Start()
	cronTicker.Start()
	cronConversion.Start()
	cronTrades.Start()
	cronOrders.Start()

	fmt.Println("CRONs armed and ready")
	time.Sleep(time.Duration(1<<63 - 1))
}
