package com.my.ai.cursor.ai.platform.application.agent;

import com.my.ai.cursor.common.annotation.AgentToolGroup;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class AgentToolRegistry {

    private final ApplicationContext applicationContext;

    public AgentToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<Object> resolveTools() {
        Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(AgentToolGroup.class);
        return toolBeans.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
            .map(Map.Entry::getValue)
            .toList();
    }
}
