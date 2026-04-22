package com.my.ai.cursor.memory.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.memory")
public class AppMemoryProperties {

    private final ShortTerm shortTerm = new ShortTerm();

    private final LongTerm longTerm = new LongTerm();

    public ShortTerm getShortTerm() {
        return shortTerm;
    }

    public LongTerm getLongTerm() {
        return longTerm;
    }

    public static class ShortTerm {
        private boolean enabled = true;
        private int windowSize = 12;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }
    }

    public static class LongTerm {
        private boolean enabled = true;
        private int recallLimit = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRecallLimit() {
            return recallLimit;
        }

        public void setRecallLimit(int recallLimit) {
            this.recallLimit = recallLimit;
        }
    }
}
