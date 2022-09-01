package redisLib

import (
	"github.com/go-redis/redis"
)

func ConnectToRedis() *redis.Client {
	//TODO re-implement a timeout, they exist for a reason
	redisClient := redis.NewClient(&redis.Options{
		Addr:         "192.168.0.20:6379",
		Password:     "",
		DB:           0,
		ReadTimeout:  -1,
		WriteTimeout: -1,
	})

	return redisClient
}

func DisconnectFromRedis(redisClient *redis.Client) {
	redisClient.Close()
}
