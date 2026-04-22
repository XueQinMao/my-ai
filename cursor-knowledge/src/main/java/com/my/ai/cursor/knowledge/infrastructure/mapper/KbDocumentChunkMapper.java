package com.my.ai.cursor.knowledge.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.ai.cursor.knowledge.application.pojo.dto.DocumentInfoDto;
import com.my.ai.cursor.knowledge.infrastructure.entity.KbDocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KbDocumentChunkMapper extends BaseMapper<KbDocumentChunk> {

    @Select("""
        SELECT c.id AS chunkId,
               c.document_id AS documentId,
               c.chunk_no AS chunkNo,
               d.title AS title,
               d.source_url AS sourceUrl,
               d.source_org AS sourceOrg,
               d.doc_type AS docType,
               c.chunk_text AS content,
               c.vector_doc_id AS vectorDocId
        FROM kb_document_chunk c
        JOIN kb_source_document d ON d.id = c.document_id
        WHERE c.vector_doc_id = #{vectorDocId}
        LIMIT 1
        """)
    DocumentInfoDto selectHitByVectorDocId(@Param("vectorDocId") String vectorDocId);
}
