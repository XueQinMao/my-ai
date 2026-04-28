package com.my.ai.cursor.ai.platform.application.observability;

import java.math.BigDecimal;

public record AiCostBreakdown(BigDecimal promptCost, BigDecimal completionCost, BigDecimal totalCost) {

    public static AiCostBreakdown zero() {
        return new AiCostBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
