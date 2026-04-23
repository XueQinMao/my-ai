package com.my.ai.cursor.ai.platform.application.agent;

import com.my.ai.cursor.common.annotation.AgentToolGroup;
import jakarta.annotation.PostConstruct;
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

    private List<Object> tools;


    @PostConstruct
    public void resolveTools() {
        Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(AgentToolGroup.class);
        tools =  toolBeans.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
            .map(Map.Entry::getValue)
            .toList();
    }

    public List<Object> getTools() {
        if (tools == null) {
            resolveTools();
            return List.of();
        }
        return tools;
    }
}
