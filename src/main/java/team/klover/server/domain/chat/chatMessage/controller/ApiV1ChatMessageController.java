package team.klover.server.domain.chat.chatMessage.controller;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.klover.server.domain.chat.chatMessage.dto.req.ChatMessageForm;
import team.klover.server.domain.chat.chatMessage.dto.res.ChatMessageDto;
import team.klover.server.domain.chat.chatMessage.entity.ChatMessagePage;
import team.klover.server.domain.chat.chatMessage.service.ChatMessageService;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.common.response.KloverPage;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.util.AuthUtil;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-room/message")
@RequiredArgsConstructor
public class ApiV1ChatMessageController {
    private final ChatMessageService chatMessageService;

    // 해당 채팅방의 메시지 실시간 조회 시작
    // http://localhost:8080/api/v1/chat-room/message/1
    @GetMapping("/{chatRoomId}")
    public ApiResponse<ChatMessageDto> findByChatRoomId(@ModelAttribute ChatMessagePage request, @PathVariable("chatRoomId") Long chatRoomId) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(chatMessageService.findByChatRoomId(currentMemberId, chatRoomId, pageable)));
    }

    // 해당 채팅방의 메시지 실시간 조회 중단
    // http://localhost:8080/api/v1/chat-room/message/1
    @PutMapping("/{chatRoomId}")
    public ApiResponse<String> updateLastReadMessage(@PathVariable("chatRoomId") Long chatRoomId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatMessageService.updateLastReadMessage(currentMemberId, chatRoomId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 채팅방에서 메시지 검색(닉네임/내용)
    // http://localhost:8080/api/v1/chat-room/message/1/keyword?keyword=테스트
    @GetMapping("/{chatRoomId}/keyword")
    public ApiResponse<ChatMessageDto> searchChatMessage(@ModelAttribute ChatMessagePage request, @PathVariable("chatRoomId") Long chatRoomId,
                                                         @RequestParam("keyword") String keyword) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(chatMessageService.searchByKeyword(currentMemberId, chatRoomId, keyword, pageable)));
    }

    // 해당 채팅방에서 메시지 생성
    // http://localhost:8080/api/v1/chat-room/message/1
//    @MessageMapping("/{chatRoomId}") // 웹소켓 사용
    @PostMapping("/{chatRoomId}")
    public ApiResponse<String> writeChatMessage(@PathVariable("chatRoomId") Long chatRoomId,
                                                @RequestPart(value ="chatMessageForm" ) ChatMessageForm chatMessageForm,
                                                @RequestPart(value = "imageFile", required = false) List<MultipartFile> imageFiles) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatMessageService.writeChatMessage(currentMemberId, chatRoomId, chatMessageForm, imageFiles);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 메시지 삭제
    // http://localhost:8080/api/v1/chat-room/message/1
    @DeleteMapping("/{messageId}")
    public ApiResponse<String> deleteChatMessage(@PathVariable("messageId") Long messageId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatMessageService.deleteChatMessage(currentMemberId, messageId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
