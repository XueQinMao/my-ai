package com.my.ai.cursor.memory.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能体长期记忆实体
 * 用于存储从对话中提取的用户偏好、事实信息和会话摘要等长期记忆数据
 */
@TableName("agent_memory")
@Data
public class AgentMemory {

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     * 标识该记忆所属的用户，用于隔离不同用户的记忆数据
     */
    @TableField("user_id")
    private String userId;

    /**
     * 会话ID
     * 标识该记忆来源的具体会话，用于追溯记忆的上下文来源
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 记忆类型
     * 枚举值：SESSION_SUMMARY（会话摘要）、PREFERENCE（用户偏好）、FACT（事实信息）、GOAL（用户目标）等
     * 用于分类检索和管理不同类型的记忆
     */
    @TableField("memory_type")
    private String memoryType;

    /**
     * 标准化键
     * 用于记忆去重和合并的唯一标识符
     * 例如：preference:language、session-summary:{sessionId}:{messageId}
     * 相同 normalizedKey 的记忆会被视为同一类，可进行更新或合并操作
     */
    @TableField("normalized_key")
    private String normalizedKey;

    /**
     * 记忆内容
     * 记忆的完整文本描述，包含详细信息
     * 例如："用户偏好使用中文进行医学咨询回答"
     */
    @TableField("content")
    private String content;

    /**
     * 记忆摘要
     * 记忆的简短概括，用于快速展示和预览
     * 例如："偏好中文"
     */
    @TableField("summary")
    private String summary;

    /**
     * 重要性评分（0.0 - 1.0）
     * 表示该记忆对用户的重要程度，越高越重要
     * 用于排序和筛选关键记忆，例如偏好类记忆通常重要性较高（0.8+）
     */
    @TableField("importance")
    private BigDecimal importance;

    /**
     * 置信度评分（0.0 - 1.0）
     * 表示提取系统对该记忆准确性的确信程度
     * 低置信度的记忆可能需要人工审核或在多次确认后再生效
     */
    @TableField("confidence")
    private BigDecimal confidence;

    /**
     * 向量数据库表id
     */
    @TableField("vector_store_id")
    private String vectorStoreId;

    /**
     * 记忆状态
     * 枚举值：ACTIVE（活跃）、ARCHIVED（归档）、EXPIRED（过期）、DELETED（已删除）
     * 用于管理记忆的生命周期
     */
    @TableField("status")
    private String status;

    /**
     * 来源消息ID
     * 关联到 chat_message 表的主键，指向生成该记忆的原始用户消息
     * 用于追溯记忆的来源和上下文
     */
    @TableField("source_message_id")
    private Long sourceMessageId;

    /**
     * 元数据JSON
     * 存储额外的结构化信息，例如：
     * - 偏好类型的具体参数（语言代码、风格标签等）
     * - 提取模型的版本信息
     * - 记忆的标签或分类
     * 示例：{"language": "zh-CN", "style": "concise"}
     */
    @TableField("metadata_json")
    private String metadataJson;

    /**
     * 过期时间
     * 记忆的有效截止时间，超过此时间后记忆将被标记为过期或自动清理
     * 用于实现记忆的时效性管理，例如临时偏好或短期目标
     * null 表示永久有效
     */
    @TableField("ttl_at")
    private LocalDateTime ttlAt;

    /**
     * 最后访问时间
     * 记录该记忆最后一次被检索或使用的时间
     * 用于实现基于访问频率的记忆热度排序和冷数据归档
     */
    @TableField("last_accessed_at")
    private LocalDateTime lastAccessedAt;

    /**
     * 创建时间
     * 记忆首次被提取并存储的时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 记忆最后一次被修改或更新的时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
