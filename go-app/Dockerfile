# build stage
FROM golang:1.24.9-alpine AS build
WORKDIR /src
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /out/go-app

# runtime stage
FROM alpine:3.20
WORKDIR /app
RUN adduser -D appuser
ENV PORT=8081
COPY --from=build /out/go-app /app/go-app
EXPOSE 8081
USER appuser
ENTRYPOINT ["/app/go-app"]