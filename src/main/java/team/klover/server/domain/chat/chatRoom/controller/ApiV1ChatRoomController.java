package team.klover.server.domain.chat.chatRoom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomForm;
import team.klover.server.domain.chat.chatRoom.dto.res.ChatRoomDto;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoomPage;
import team.klover.server.domain.chat.chatRoom.service.ChatRoomService;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.common.response.KloverPage;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.util.AuthUtil;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/chat-room",produces = APPLICATION_JSON_VALUE)
@Tag(name="ApiV1ChatRoomController",description = "ChatRoom API")
@RequiredArgsConstructor
public class ApiV1ChatRoomController {
    private final ChatRoomService chatRoomService;

    // 채팅방 목록 조회(DM/그룹) / 참여자권한
    // http://localhost:8080/api/v1/chat-room
    @GetMapping
    @Operation(summary = "채팅방 목록 조회(DM/그룹)")
    public ApiResponse<ChatRoomDto> findByMemberId(@ModelAttribute ChatRoomPage request){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(chatRoomService.findByMemberId(currentMemberId, pageable)));
    }

    // 채팅방 생성(DM/그룹) / 로그인 멤버 권한
    // http://localhost:8080/api/v1/chat-room
    @PostMapping
    @Operation(summary = "채팅방 생성(DM/그룹)")
    public ChatRoomDto addChatRoom(@RequestBody @Valid ChatRoomForm chatRoomForm){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        return chatRoomService.addChatRoom(currentMemberId, chatRoomForm);
    }

    // 채팅방 이름 수정(그룹) / 참여자권한
    // http://localhost:8080/api/v1/chat-room/title/1
    @PutMapping("/title/{chatRoomId}")
    public ApiResponse<String> updateChatRoomTitle(@PathVariable("chatRoomId") Long chatRoomId, @RequestBody @Valid ChatRoomForm chatRoomForm){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatRoomService.updateChatRoomTitle(chatRoomId, currentMemberId, chatRoomForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 채팅방에 초대(그룹) / 참여자권한
    // http://localhost:8080/api/v1/chat-room/group/1
    @PutMapping("/group/{chatRoomId}")
    public ApiResponse<String> inviteChatRoomMember(@PathVariable("chatRoomId") Long chatRoomId, @RequestBody @Valid ChatRoomForm chatRoomForm){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatRoomService.inviteChatRoomMember(chatRoomId, currentMemberId, chatRoomForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 채팅방에서 강퇴(그룹) / 방장권한
    // http://localhost:8080/api/v1/chat-room/group/1
    @DeleteMapping("/group/{chatRoomId}")
    public ApiResponse<String> kickOutChatRoomMember(@PathVariable("chatRoomId") Long chatRoomId, @RequestBody @Valid ChatRoomForm chatRoomForm){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatRoomService.kickOutChatRoomMember(chatRoomId, currentMemberId, chatRoomForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 채팅방 나가기(DM/그룹) / 참여자권한
    // http://localhost:8080/api/v1/chat-room/1
    @DeleteMapping("/{chatRoomId}")
    public ApiResponse<String> leaveChatRoomMember(@PathVariable("chatRoomId") Long chatRoomId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        chatRoomService.leaveChatRoomMember(chatRoomId, currentMemberId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
