# chat-app

A distributed real-time messaging system built in Java.

## Architecture

```
Gateway (port 9090)
  └─ load-balances → Server nodes (port 8080)

Server node internals:
  ConnectionManager     — local client sessions (per-node)
  MessageRouter         — local → cluster relay → offline queue
  Cluster layer:
    ClusterManager      — server node health tracking
    ServerRegistry      — userId → nodeId mapping
    InterServerConnection — TCP/gRPC relay between nodes

Supporting services:
  PresenceService       — online/offline tracking
  NotificationService   — push & email for offline users
```

## Modules

| Module         | Description                              |
|----------------|------------------------------------------|
| `shared`       | Protocol models, enums, JSON codec       |
| `gateway`      | Entry point, load balancer               |
| `server`       | Core chat logic + cluster layer          |
| `presence`     | Online/offline presence tracking         |
| `notification` | Push (FCM/APNs) & email alerts           |
| `client`       | Java client library                      |
| `testing`      | End-to-end & load tests                  |

## Build

```bash
# Build all modules
mvn clean install

# Run server tests only
mvn test -pl server

# Run all tests
mvn test
```

## Key Message Flow

Alice (Server 1) → Bob (Server 2):

1. Alice's `ChatClient` sends JSON over WebSocket to Server 1
2. Server 1's `MessageRouter` checks `ConnectionManager` — Bob is not local
3. `MessageRouter` calls `ServerRegistry.findServer("bob")` → `"server-2"`
4. Message is forwarded via `InterServerConnection` to Server 2
5. Server 2's `ConnectionManager.deliverLocal()` pushes to Bob's `ClientSession`
6. Bob's `ChatClient` receives the message
