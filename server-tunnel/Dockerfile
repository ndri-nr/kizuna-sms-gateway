# Step 1: Build the binary
FROM golang:1.21-alpine AS builder

WORKDIR /app

# Copy dependency definition files first for better caching
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the source code
COPY . .

# Build a statically linked binary
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o server-tunnel main.go

# Step 2: Use a minimal runtime image
FROM alpine:3.18

RUN apk --no-cache add ca-certificates

WORKDIR /root/

# Copy the binary from the builder stage
COPY --from=builder /app/server-tunnel .

# Expose the port the server listens on
EXPOSE 8081

# Command to run the executable
CMD ["./server-tunnel"]
