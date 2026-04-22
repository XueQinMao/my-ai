package com.my.ai.cursor.interfaces;

import com.my.ai.cursor.application.dto.memory.MemoryDeleteRequest;
import com.my.ai.cursor.application.dto.memory.MemoryQueryRequest;
import com.my.ai.cursor.interfaces.pojo.vo.Response;
import com.my.ai.cursor.memory.application.LongTermMemoryService;
import com.my.ai.cursor.memory.pojo.dto.MemoryItemDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final LongTermMemoryService longTermMemoryService;

    public MemoryController(LongTermMemoryService longTermMemoryService) {
        this.longTermMemoryService = longTermMemoryService;
    }

    @GetMapping
    public Response<List<MemoryItemDto>> query(@RequestParam String userId,
                                               @RequestParam(required = false) String sessionId,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String status) {
        return Response.success(longTermMemoryService.query(new MemoryQueryRequest(userId, sessionId, type, status)));
    }

    @DeleteMapping("/{id}")
    public Response<Boolean> delete(@PathVariable Long id, @RequestParam String userId) {
        longTermMemoryService.forget(new MemoryDeleteRequest(userId, id));
        return Response.success(Boolean.TRUE);
    }
}
