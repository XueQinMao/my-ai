package com.my.ai.cursor.interfaces;

import com.my.ai.cursor.knowledge.application.cleaning.TextCorrectionService;
import com.my.ai.cursor.knowledge.application.pojo.dto.ChatCleanDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController
 *
 * @author ��ǿ
 * @version 2026/04/09 19:45
 **/
@RequestMapping("/test")
@RestController
public class TestController {

    private final TextCorrectionService textCorrectionService;

    public TestController(TextCorrectionService textCorrectionService) {
        this.textCorrectionService = textCorrectionService;
    }

    @GetMapping()
    public ChatCleanDto test(@RequestParam String message,
                             @RequestParam(defaultValue = "通用文档") String domainName) {
        return textCorrectionService.correct(message, domainName);
    }
}
