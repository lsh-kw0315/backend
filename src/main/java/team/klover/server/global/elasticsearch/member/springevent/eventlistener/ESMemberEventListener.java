package team.klover.server.global.elasticsearch.member.springevent.eventlistener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.klover.server.global.elasticsearch.member.springevent.event.MemberDeleteEvent;
import team.klover.server.global.elasticsearch.member.springevent.event.MemberUpdateEvent;
import team.klover.server.global.elasticsearch.member.springevent.message.MemberDeletionMessage;
import team.klover.server.global.elasticsearch.member.springevent.message.MemberModificationMessage;
import team.klover.server.global.redis.RedisService;

@Component
@RequiredArgsConstructor
public class ESMemberEventListener {
    private final RedisService redisService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberModification(MemberUpdateEvent event){
        redisService.saveMemberModification(new MemberModificationMessage(event));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberDeletion(MemberDeleteEvent event){
        redisService.saveMemberDeletion(new MemberDeletionMessage(event));
    }
}
