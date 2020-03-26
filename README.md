# DNSHooks [![Actions Status](https://github.com/FantasticMao/dnshooks/workflows/action/badge.svg)](https://github.com/FantasticMao/dnshooks/actions) [![codecov](https://codecov.io/gh/FantasticMao/dnshooks/branch/master/graph/badge.svg)](https://codecov.io/gh/FantasticMao/dnshooks/branch/master) [![image](https://img.shields.io/badge/license-GPL3.0-green.svg)](https://github.com/FantasticMao/dnshooks/blob/master/LICENSE)

A simple DNS proxy, with support for some hooks (such as webhooks).

## DNSHooks-Proxy

DNSHooks-Proxy's inbound/outbound channel handler pipeline in Netty:

```text
+--------+                                                     +--------------------+
|        | --> DatagramDnsQueryDecoder                         | +----------------+ |
| DNS    |                     DnsProxyServerClientHandler --> | | DNSHooks Proxy | |
| Client |                                                     | | Server         | |
|        |                  DnsProxyServerDisruptorHandler <-- | +----------------+ |
|        | <-- DatagramDnsResponseEncoder     |                |      A     |       |                                       +--------+
+--------+                                    |                |      |     V       |                                       |        |
                                              |                | +----------------+ | --> ProxyQueryEncoder --------------> | DNS    |
                                              |                | | DNSHooks Proxy | |                                       | Server |
                                              V                | | Client         | |              ProxyResponseDecoder <-- |        |
                                       +-------------+         | +----------------+ | <-- ObtainMessageChannelHandler       |        |
                                      /             /|         +--------------------+                                       +--------+
                                     +-------------+ |
                                     | Disruptor   | |
                                     | Ring Buffer |/
                                     +-------------+
                                        |   |   |
                                        V   V   V
                                      Hook Hook Hook
```

## DNS Related RFC

-   RFC 1034 [Domain Names - Concepts And Facilities](https://tools.ietf.org/html/rfc1034)
-   RFC 1035 [Domain Names - Implementation And Specification](https://tools.ietf.org/html/rfc1035)
-   RFC 2136 [Dynamic Updates in the Domain Name System (DNS UPDATE)](https://tools.ietf.org/html/rfc2136)
-   RFC 2181 [Clarifications to the DNS Specification](https://tools.ietf.org/html/rfc2181)
-   RFC 2535 [Domain Name System Security Extensions](https://tools.ietf.org/html/rfc2535)
-   RFC 2929 [Domain Name System (DNS) IANA Considerations](https://tools.ietf.org/html/rfc2929)
-   RFC 7766 [DNS Transport over TCP - Implementation Requirements](https://tools.ietf.org/html/rfc7766)
