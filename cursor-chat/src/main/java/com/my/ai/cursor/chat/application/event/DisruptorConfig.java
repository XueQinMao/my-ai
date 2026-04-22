package com.my.ai.cursor.chat.application.event;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.my.ai.cursor.ai.platform.application.event.LlmTokenCostEvent;
import com.my.ai.cursor.ai.platform.application.event.handler.LlmTokenCostEventHandler;
import com.my.ai.cursor.chat.application.event.handler.ChatCompleteEventHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * DisruptorConfig
 *
 * @author 刘强
 * @version 2026/04/17 16:52
 **/
@Configuration
public class DisruptorConfig {

    private String waitStrategy = "blocking";

    private int ringBufferSize = 1024;

    private Disruptor<LlmTokenCostEvent> llmTokenCostDisruptor;

    private Disruptor<ChatCompleteEvent> chatCompleteDisruptor;

    @Resource
    private LlmTokenCostEventHandler llmTokenCostEventHandler;

    @Resource
    @Lazy
    private ChatCompleteEventHandler chatCompleteEventHandler;

    @PostConstruct
    public void initializeDisruptor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final LongAdder threadNumber = new LongAdder();

            @Override
            public Thread newThread(Runnable r) {
                threadNumber.add(1);
                var threadName = "disruptor-thread-" + threadNumber.intValue();
                var thread = new Thread(r, threadName);
                thread.setDaemon(false);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };

        // 根据配置创建等待策略
        llmTokenCostDisruptor =
            new Disruptor<>(LlmTokenCostEvent.FACTORY, ringBufferSize, threadFactory, ProducerType.MULTI, // 支持多生产者
                createWaitStrategy());
        // 设置事件处理器
        llmTokenCostDisruptor.handleEventsWith(llmTokenCostEventHandler);
        llmTokenCostDisruptor.start();

        chatCompleteDisruptor =
            new Disruptor<>(ChatCompleteEvent.FACTORY, ringBufferSize, threadFactory, ProducerType.MULTI, // 支持多生产者
                createWaitStrategy());
        // 设置事件处理器
        chatCompleteDisruptor.handleEventsWith(chatCompleteEventHandler);
        chatCompleteDisruptor.start();
    }

    @PreDestroy
    public void shutdownDisruptor() {
        Optional.ofNullable(llmTokenCostDisruptor).ifPresent(Disruptor::shutdown);
        Optional.ofNullable(chatCompleteDisruptor).ifPresent(Disruptor::shutdown);
    }

    @Bean("llmTokenCostEventDisruptor")
    public Disruptor<LlmTokenCostEvent> llmTokenCostEventDisruptor() {
        return llmTokenCostDisruptor;
    }

    @Bean("chatCompleteDisruptor")
    public Disruptor<ChatCompleteEvent> chatCompleteDisruptor() {
        return chatCompleteDisruptor;
    }

    private WaitStrategy createWaitStrategy() {
        switch (waitStrategy.toLowerCase()) {
            case "blocking":
                return new BlockingWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            case "yielding":
                return new YieldingWaitStrategy();
            case "busy-spinning":
                return new BusySpinWaitStrategy();
            case "timeout-blocking":
                return new TimeoutBlockingWaitStrategy(10, TimeUnit.SECONDS);
            default:
                return new BlockingWaitStrategy();
        }
    }
}
