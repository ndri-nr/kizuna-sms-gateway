package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"

	"github.com/gorilla/websocket"
)

// TunnelRequest represents the request format sent to the mobile app
type TunnelRequest struct {
	ID      string            `json:"id"`
	Method  string            `json:"method"`
	Path    string            `json:"path"`
	Headers map[string]string `json:"headers"`
	Body    *string           `json:"body"`
}

// TunnelResponse represents the response format received from the mobile app
type TunnelResponse struct {
	ID      string            `json:"id"`
	Status  int               `json:"status"`
	Headers map[string]string `json:"headers"`
	Body    *string           `json:"body"`
}

var (
	upgrader = websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool { return true },
	}
	// gatewayId -> *websocket.Conn
	sessions = make(map[string]*websocket.Conn)
	// requestId -> channel for response
	pendingRequests = make(map[string]chan *TunnelResponse)
	mutex           sync.RWMutex
)

func main() {
	http.HandleFunc("/ws/", handleWebSocket)
	http.HandleFunc("/", handleProxy)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	fmt.Println("Kizuna Tunnel Server starting...")
	printIPAddresses()
	fmt.Printf("Listening on 0.0.0.0:%s...\n", port)
	log.Fatal(http.ListenAndServe("0.0.0.0:"+port, nil))
}

func printIPAddresses() {
	// 1. Get Local IPs
	addrs, err := net.InterfaceAddrs()
	if err == nil {
		fmt.Println("\n--- Local Network Addresses (Use these if on the same Wi-Fi) ---")
		for _, address := range addrs {
			if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
				if ipnet.IP.To4() != nil {
					fmt.Printf("- http://%s:8081\n", ipnet.IP.String())
				}
			}
		}
	}

	// 2. Get Public IP
	fmt.Println("\n--- Public Address (Use this if using Mobile Data/External) ---")
	client := http.Client{Timeout: 5 * time.Second}
	resp, err := client.Get("https://api.ipify.org")
	if err == nil {
		defer resp.Body.Close()
		ip, _ := io.ReadAll(resp.Body)
		fmt.Printf("- http://%s:8081\n", string(ip))
	} else {
		fmt.Println("- Could not detect public IP")
	}
	fmt.Println("")
}

func handleWebSocket(w http.ResponseWriter, r *http.Request) {
	gatewayID := strings.TrimPrefix(r.URL.Path, "/ws/")
	if gatewayID == "" {
		http.Error(w, "Gateway ID required", http.StatusBadRequest)
		return
	}

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Upgrade error:", err)
		return
	}

	mutex.Lock()
	sessions[gatewayID] = conn
	mutex.Unlock()

	log.Printf("Device connected: %s", gatewayID)

	defer func() {
		mutex.Lock()
		delete(sessions, gatewayID)
		mutex.Unlock()
		conn.Close()
		log.Printf("Device disconnected: %s", gatewayID)
	}()

	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			log.Println("Read error:", err)
			break
		}

		var resp TunnelResponse
		if err := json.Unmarshal(message, &resp); err != nil {
			log.Println("Unmarshal error:", err)
			continue
		}

		mutex.RLock()
		ch, ok := pendingRequests[resp.ID]
		mutex.RUnlock()

		if ok {
			ch <- &resp
		}
	}
}

func handleProxy(w http.ResponseWriter, r *http.Request) {
	// Expected path: /{gatewayId}/api/v1/...
	parts := strings.SplitN(strings.TrimPrefix(r.URL.Path, "/"), "/", 2)
	if len(parts) < 2 {
		http.Error(w, "Invalid path format. Use /{gatewayId}/{path}", http.StatusBadRequest)
		return
	}

	gatewayID := parts[0]
	targetPath := "/" + parts[1]

	mutex.RLock()
	conn, ok := sessions[gatewayID]
	mutex.RUnlock()

	if !ok {
		http.Error(w, "Gateway not connected", http.StatusNotFound)
		return
	}

	requestID := fmt.Sprintf("%d", time.Now().UnixNano())

	var bodyStr *string
	if r.Body != nil {
		bodyBytes, _ := io.ReadAll(r.Body)
		s := string(bodyBytes)
		bodyStr = &s
	}

	headers := make(map[string]string)
	for k, v := range r.Header {
		headers[k] = strings.Join(v, ",")
	}

	tunnelReq := TunnelRequest{
		ID:      requestID,
		Method:  r.Method,
		Path:    targetPath,
		Headers: headers,
		Body:    bodyStr,
	}

	respChan := make(chan *TunnelResponse)
	mutex.Lock()
	pendingRequests[requestID] = respChan
	mutex.Unlock()

	defer func() {
		mutex.Lock()
		delete(pendingRequests, requestID)
		mutex.Unlock()
	}()

	err := conn.WriteJSON(tunnelReq)
	if err != nil {
		http.Error(w, "Failed to send request to device", http.StatusInternalServerError)
		return
	}

	select {
	case resp := <-respChan:
		for k, v := range resp.Headers {
			w.Header().Set(k, v)
		}
		w.WriteHeader(resp.Status)
		if resp.Body != nil {
			io.WriteString(w, *resp.Body)
		}
	case <-time.After(10 * time.Second):
		http.Error(w, "Gateway timeout (10s)", http.StatusGatewayTimeout)
	}
}
