package com.reconciliation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProgressService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String sessionId) {
        SseEmitter emitter = new SseEmitter(0L); // sem timeout
        emitters.put(sessionId, emitter);
        
        // Remove automaticamente quando completar ou expirar
        emitter.onCompletion(() -> emitters.remove(sessionId));
        emitter.onTimeout(() -> emitters.remove(sessionId));
        emitter.onError((e) -> emitters.remove(sessionId));
        
        return emitter;
    }

    public void sendProgress(String sessionId, int percentage, String message) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(new ProgressEvent(percentage, message)));
            } catch (IOException e) {
                emitters.remove(sessionId);
            }
        }
    }

    public void sendComplete(String sessionId, Object result) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(result));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } finally {
                emitters.remove(sessionId);
            }
        }
    }

    public void sendError(String sessionId, String error) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(error));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } finally {
                emitters.remove(sessionId);
            }
        }
    }

    public static class ProgressEvent {
        private int percentage;
        private String message;

        public ProgressEvent(int percentage, String message) {
            this.percentage = percentage;
            this.message = message;
        }

        public int getPercentage() { return percentage; }
        public String getMessage() { return message; }
    }
}