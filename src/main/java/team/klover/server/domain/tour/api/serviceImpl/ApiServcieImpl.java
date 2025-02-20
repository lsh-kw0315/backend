package team.klover.server.domain.tour.api.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.klover.server.domain.tour.api.service.ApiService;
import team.klover.server.domain.tour.post.entity.Post;
import team.klover.server.domain.tour.post.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiServcieImpl implements ApiService {
    private final PostRepository postRepository;

    // Apis의 데이터를 Post에 저장
    @Override
    @Transactional
    public void saveForApis(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            List<Post> festivalList = new ArrayList<>();

            for (JsonNode item : items) {
                Post entity = Post.builder()
                        .tourPlaceId(item.path("contentid").asText())
                        .tourPlaceName(item.path("addr1").asText())
                        .tourPlaceArea(item.path("areacode").asText())
                        .tourPhotoUrl(item.path("firstimage").asText())
                        .tourType(item.path("contenttypeid").asText())
                        .map_x(item.path("mapx").asText())
                        .map_y(item.path("mapy").asText())
                        .build();
                festivalList.add(entity);
            }
            postRepository.saveAll(festivalList);
            log.info(festivalList.toString());
        } catch (Exception e) {
            throw new RuntimeException("API 응답 처리 중 오류 발생", e);
        }
    }
}
