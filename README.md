# Tiketeer-Waiting
티켓팅 시스템에서 트래픽 제어를 위한 대기 큐 레포지토리

```dtd
spring:
  application:
    name: TiketeerWaiting
  data:
    redis:
      repositories:
        enabled: false
  redis:
    host: 
    port: 

waiting:
  entry-size: 
```