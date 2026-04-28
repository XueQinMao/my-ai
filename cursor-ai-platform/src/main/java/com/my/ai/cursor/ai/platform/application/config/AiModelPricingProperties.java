package com.my.ai.cursor.ai.platform.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.observability.pricing")
public class AiModelPricingProperties {

    private Map<String, ModelPricing> models = new LinkedHashMap<>();

    public Map<String, ModelPricing> getModels() {
        return models;
    }

    public void setModels(Map<String, ModelPricing> models) {
        this.models = models;
    }

    public static class ModelPricing {

        private BigDecimal promptCostPer1kTokens = BigDecimal.ZERO;

        private BigDecimal completionCostPer1kTokens = BigDecimal.ZERO;

        public BigDecimal getPromptCostPer1kTokens() {
            return promptCostPer1kTokens;
        }

        public void setPromptCostPer1kTokens(BigDecimal promptCostPer1kTokens) {
            this.promptCostPer1kTokens = promptCostPer1kTokens;
        }

        public BigDecimal getCompletionCostPer1kTokens() {
            return completionCostPer1kTokens;
        }

        public void setCompletionCostPer1kTokens(BigDecimal completionCostPer1kTokens) {
            this.completionCostPer1kTokens = completionCostPer1kTokens;
        }
    }
}
