package team.klover.server.global.elasticsearch.commpost.springevent.eventlistener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.klover.server.global.elasticsearch.commpost.springevent.event.*;
import team.klover.server.global.elasticsearch.commpost.springevent.message.CommPostCountMessage;
import team.klover.server.global.elasticsearch.commpost.springevent.message.CommPostDeletionMessage;
import team.klover.server.global.elasticsearch.commpost.springevent.message.CommPostModificationMessage;
import team.klover.server.global.elasticsearch.commpost.springevent.message.NicknameModificationMessage;
import team.klover.server.global.rabbitMQ.producer.RabbitMQProducer;
import team.klover.server.global.redis.RedisService;

@Component
@RequiredArgsConstructor
public class ESCommPostEventListener {
    private final RedisService redisService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) //커밋이 되었다 = 실제로 DB에 반영이 되었다. 이 때만 정합성을 반영해야함.
    public void handleCommPostDeletion(CommPostDeleteEvent event){
        redisService.saveCommPostDeletionMessage(new CommPostDeletionMessage(event.getCommPost()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommPostModification(CommPostUpdateEvent event){
        redisService.saveCommPostModificationMessage(new CommPostModificationMessage(event.getCommPost()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommPostCount(CommPostCountEvent event){
        redisService.saveCommPostCountMessage(new CommPostCountMessage(event.getCommPost()));
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNicknameModification(NicknameUpdateEvent event){
        redisService.saveNicknameModificationMessage(new NicknameModificationMessage(event.getMember()));
    }


}
