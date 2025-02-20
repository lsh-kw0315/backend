package team.klover.server.global.initData;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import team.klover.server.domain.tour.api.scheduler.ApisScheduler;

import java.util.List;

@Configuration
@Profile("!prod")
public class NotProd {
    @Bean
    public ApplicationRunner applicationRunner(
            ApisScheduler apisScheduler
    ) {
        return new ApplicationRunner() {
            @Transactional
            @Override
            public void run(ApplicationArguments args) throws Exception {
                // Apis, Kopis 스케쥴러 실행
                //
                //
                //
                apisScheduler.getApisApiData();
            }
        };
    }
}
