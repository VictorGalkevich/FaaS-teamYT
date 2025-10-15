package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
)

func main() {
	port := getenv("PORT", "8081")
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		fmt.Fprintln(w, "Hello from Go")
	})
	log.Printf("Go app listening on port %s", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func getenv(k, def string) string {
	if v, ok := os.LookupEnv(k); ok {
		return v
	}
	return def
}