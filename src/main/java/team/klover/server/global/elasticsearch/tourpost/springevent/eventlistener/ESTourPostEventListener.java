package team.klover.server.global.elasticsearch.tourpost.springevent.eventlistener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.klover.server.global.elasticsearch.tourpost.springevent.event.TourPostCountEvent;
import team.klover.server.global.elasticsearch.tourpost.springevent.message.TourPostCountMessage;
import team.klover.server.global.rabbitMQ.producer.RabbitMQProducer;
import team.klover.server.global.redis.RedisService;

@Component
@RequiredArgsConstructor
public class ESTourPostEventListener {
    private final RedisService redisService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCount(TourPostCountEvent event){
        redisService.saveTourPostCountMessage(new TourPostCountMessage(event.getTourPost()));
    }

}
