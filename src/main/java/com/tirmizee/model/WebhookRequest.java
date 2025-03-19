package com.tirmizee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

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
