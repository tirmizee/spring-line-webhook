# Spring LINE Webhook

Spring Boot application for handling LINE Messaging API webhooks.

## 🚀 Features
- Listens for webhook events from LINE Messaging API
- Validates incoming webhook signatures for security
- Handles text messages from LINE users
- Built with Spring WebFlux for reactive programming



## 🛠️ Prerequisites
Before running the application, ensure you have:
- Java 17 or later
- Maven
- Ngrok (for testing locally)
- A registered **LINE Messaging API** channel


## 🔧 Configuration
Set up the `application.yaml` with your **LINE Channel Secret**.

```yaml
spring.application.name: spring-line-webhook
line.bot.channel-secret: YOUR_LINE_CHANNEL_SECRET
```

## 📥 Webhook Request Format

```java

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookRequest {
    private List<Event> events;

    @Data
    public static class Event {
        private String type;
        private String replyToken;
        private Source source;
        private Message message;
    }

    @Data
    public static class Source {
        private String userId;
        private String type;
    }

    @Data
    public static class Message {
        private String type;
        private String text;
    }
}

```

Each event contains message details, reply token, and sender info.

## 🔒 Signature Validation

Incoming webhook requests are validated using X-Line-Signature to ensure authenticity.

```java
private boolean isValidSignature(String payload, String signature) {
    try {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(channelSecret.getBytes(UTF_8), "HmacSHA256");
        mac.init(secretKey);

        byte[] hash = mac.doFinal(payload.getBytes(UTF_8));
        String calculatedSignature = Base64.encodeBase64String(hash);

        return calculatedSignature.equals(signature);
    } catch (Exception e) {
        log.error("Signature validation failed", e);
        return false;
    }
}


```

Requests with invalid signatures return 403 Forbidden.

## 📡 Webhook Endpoint

The WebhookController listens for POST requests at /webhook.

```java
@PostMapping("/webhook")
public Mono<ResponseEntity<String>> handleWebhook(
    @RequestHeader("X-Line-Signature") String signature,
    @RequestBody WebhookRequest request) {
    
    log.info("Received LINE Signature: {}", signature);
    log.info("Received LINE Request: {}", request);

    return Mono.just(ResponseEntity.ok("Webhook received"));
}
```

## 🛠️ Running the Application

- 1️⃣ Clone the repository
- 2️⃣ Build and Run
- 3️⃣ Expose Local Server with Ngrok






