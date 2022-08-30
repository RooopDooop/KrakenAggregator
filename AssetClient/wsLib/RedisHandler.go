package wsLib

import (
	"fmt"

	"github.com/go-redis/redis"
)

//var redisClient *redis.Client
//var redisErr error

func connectToRedis() *redis.Client {
	//TODO re-implement a timeout, they exist for a reason
	redisClient := redis.NewClient(&redis.Options{
		Addr:         "localhost:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	//TODO REDIS if it was on docker
	//172.1.1.20

	fmt.Println("Connected to redis DB!")
	return redisClient

	/*startCronTicker()
	startCronOHLC()
	startCronTrades()
	startCronOrders()*/
}

func disconnectFromRedis(redisClient *redis.Client) {
	redisClient.Close()

	/*stopCronTicker()
	stopCronOHLC()
	stopCronTrades()
	stopCronOrders()*/

	fmt.Println("Closed redis DB connection!")
}
