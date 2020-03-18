DNS query/response flow in Netty component:

```text

+--------+                                                     +--------------------+
|        | --> DatagramDnsQueryDecoder                         | +----------------+ |
| DNS    |                     DnsProxyServerClientHandler --> | | DNSHooks Proxy | |
| client |                                                     | | Server         | |
|        |                  DnsProxyServerDisruptorHandler <-- | +----------------+ |
|        | <--  DatagramDnsResponseEncoder     |               |      A     |       |
+--------+                                     |               |      |     V       |                                       +--------+
                                               |               | +----------------+ |                                       |        |
                                               |               | | DNSHooks Proxy | | --> ProxyQueryEncoder --------------> | DNS    |
                                               V               | | Client         | |              ProxyResponseDecoder <-- | Server |
                                        +-------------+        | +----------------+ | <-- ObtainMessageChannelHandler       |        |
                                       /             /|        +--------------------+                                       +--------+
                                      +-------------+ |
                                      | Ring Buffer | |
                                      |             |/
                                      +-------------+
                                         |   |   |
                                         V   V   V
                                       Hook Hook Hook

```
