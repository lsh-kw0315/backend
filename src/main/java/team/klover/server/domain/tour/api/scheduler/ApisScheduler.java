package team.klover.server.domain.tour.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.klover.server.domain.tour.api.service.ApiService;
import team.klover.server.domain.tour.post.entity.Post;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@Configuration
public class ApisScheduler {
    @Value("${schedule.use}")
    private boolean useSchedule;

    //축제 OpenApi. 한국관광공사_국문 관광정보 서비스_GW 사용.
    @Value("${apis.api.service-key}")
    private String serviceKeyForApis;

    private final ApiService apiService;

    @Scheduled(cron = "${schedule.cron_for_apis}")
    public void getApisApiData() {

        if(useSchedule == true) {
            log.info("Apis스케쥴러 실행");

            int numOfRows = 100;
            int maxPage = 15;
            String mobileOS = "ETC";   //OS구분
            String mobileApp = "LOCAL";//서비스명
            String arrange = "O";     // (A=제목순, C=수정일순, D=생성일순), 대표이미지가 반드시 있음(O=제목순, Q=수정일순, R=생성일순)
            int contentTypeId = 12;   // 관광지(12) 문화시설(14) 축제/공연/행사(15)
            List<String> cat1List = Arrays.asList("A01", "A02");  // 대분류(A01, A02): 관광지(12) 문화시설(14) 축제/공연/행사(15) 모두 포함

            for (int pageNo = 1; pageNo <= maxPage; pageNo++) {
                for (String cat1 : cat1List) {
                    try {
                        String apiUrl = String.format(
                                "https://apis.data.go.kr/B551011/KorService1/areaBasedList1?pageNo=%d&numOfRows=%d&MobileOS=%s&MobileApp=%s&arrange=%s&contentTypeId=%d&cat1=%s&serviceKey=%s&_type=json",
                                pageNo, numOfRows, mobileOS, mobileApp, arrange, contentTypeId, cat1, serviceKeyForApis
                        );

                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Accept", "application/json");

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                        StringBuilder responseBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                        reader.close();
                        String response = responseBuilder.toString();

                        if (response.trim().startsWith("<?xml")) {
                            response = response.replaceAll("<\\?xml[^>]*>", "").trim();
                        }

                        if (response.startsWith("{")) {
                            try {
                                apiService.saveForApis(response);
                            } catch (Exception e) {
                                log.error("Apis Scheduler 저장 중 에러 발생", e);
                            }
                        } else {
                            log.error("예상치 못한 응답 형식: " + response);
                        }
                    } catch (Exception e) {
                        log.error("Apis Scheduler API 호출 중 에러 발생", e);
                    }
                }
            }

            log.info("Apis스케쥴러 종료");
        }
    }
}
