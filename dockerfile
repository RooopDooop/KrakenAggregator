FROM debian
WORKDIR /KrakenPredictor/Software
COPY . .
RUN apt update -y
RUN apt install wget -y
RUN apt install golang-go -y
ENTRYPOINT go run *.go