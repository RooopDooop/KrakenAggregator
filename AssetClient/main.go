package main

import (
	wsLib "J.Morin/KrakenScraper/wsLib"
)

var strProxy string = ""

func main() {
	//connectToDB()
	wsLib.ConnectToServer()
}

/*func connectToRedis() {
	//TODO re-implement a timeout
	redisClient = redis.NewClient(&redis.Options{
		Addr:         "localhost:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	//TODO REDIS if it was on docker
	//172.1.1.20

	fmt.Println("Connected to redis DB!")

	startCronTicker()
	startCronOHLC()
	startCronTrades()
	startCronOrders()
}

func disconnectFromRedis() {
	redisClient.Close()

	stopCronTicker()
	stopCronOHLC()
	stopCronTrades()
	stopCronOrders()

	fmt.Println("Closed redis DB connection!")
}*/

//func connectToDB() {
//TODO fix this
//I don't like this, Timeouts exists for a reason
//Had to do this for now, had IO timeout errors as the database grows
//Will come back and fix it next commit
/*client := redis.NewClient(&redis.Options{
		Addr:         "localhost:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	//TODO REDIS if it was on docker
	//172.1.1.20

	fmt.Println("Connected to redis DB!")

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
}*/
