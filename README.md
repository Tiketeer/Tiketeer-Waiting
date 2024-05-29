# Tiketeer-Waiting
티켓팅 시스템에서 트래픽 제어를 위한 대기 큐 레포지토리

- application.yml
```dtd
spring:
  profiles:
    active: dev
  application:
    name: TiketeerWaiting
  r2dbc:
    url: # JDBC Connection URL
    username: # DB User
    password: # DB Password
    host: # DB Host
    port: # DB Port
    database: # DB name
  data:
    redis:
      repositories:
        enabled: false
      host: # Redis Host
      port: # Redis Port
      password: # Redis Password

waiting:
  entry-size: # Waiting Queue Entry Size
  ttl: # Waiting Queue TTL
  entry-ttl: # Waiting Queue TTL after entered

logging:
  level:
    org:
      springframework:
        r2dbc: DEBUG
        cache: TRACE
```

- application-prod.yml
```dtd
spring:
  r2dbc:
    url: # JDBC Connection URL in production
    username: # DB User in production
    password: # DB Password in production
    host: # DB Host in production
    port: # DB Port in production
    database: # DB Name in production
  data:
    redis:
      host: # Redis Host in production
      port: # Redis Port in production
      password: # Redis Password in production
```