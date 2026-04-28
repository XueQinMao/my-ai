package com.my.ai.cursor.ai.platform.application.observability;

import com.my.ai.cursor.ai.platform.application.config.AiModelPricingProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AiCostCalculator {

    private static final BigDecimal TOKENS_PER_THOUSAND = new BigDecimal("1000");

    private final AiModelPricingProperties pricingProperties;

    public AiCostCalculator(AiModelPricingProperties pricingProperties) {
        this.pricingProperties = pricingProperties;
    }

    public AiCostBreakdown calculate(String modelName, Integer promptTokens, Integer completionTokens) {
        if (!StringUtils.hasText(modelName)) {
            return AiCostBreakdown.zero();
        }
        AiModelPricingProperties.ModelPricing pricing = pricingProperties.getModels().get(modelName);
        if (pricing == null) {
            return AiCostBreakdown.zero();
        }
        BigDecimal promptCost = scaledCost(promptTokens, pricing.getPromptCostPer1kTokens());
        BigDecimal completionCost = scaledCost(completionTokens, pricing.getCompletionCostPer1kTokens());
        return new AiCostBreakdown(promptCost, completionCost, promptCost.add(completionCost));
    }

    private BigDecimal scaledCost(Integer tokens, BigDecimal unitCost) {
        if (tokens == null || tokens <= 0 || unitCost == null || unitCost.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(BigDecimal.valueOf(tokens))
            .divide(TOKENS_PER_THOUSAND, 8, RoundingMode.HALF_UP);
    }
}
