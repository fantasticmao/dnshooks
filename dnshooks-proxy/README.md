
DNS query/response flow in Netty component:

```text

+--------+
|        |          DatagramDnsQueryDecoder
|  DNS   | --------------------------------> DnsProxyServer
| client |
|        | <--------------------------------
|        |   DatagramDnsResponseHookEncoder
+--------+

```