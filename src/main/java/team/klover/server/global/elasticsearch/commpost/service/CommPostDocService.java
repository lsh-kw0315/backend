package team.klover.server.global.elasticsearch.commpost.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import team.klover.server.domain.community.commPost.dto.res.CommPostDto;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.global.elasticsearch.commpost.doc.CommPostDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommPostDocService {

    private static final int RADIUS = 5;
    private static final String PERCENTAGE = "50%";
    private final ElasticsearchClient client;

    //mapX: longitude(경도, lon), mapY: latitude(위도, lat)
    @SneakyThrows
    public Page<CommPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, boolean searchByContent, boolean searchByNickname, CommPostSort sort){

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        List<SortOptions> sortOptions = new ArrayList<>();

        if(mapX!=null && mapY!=null){
            boolQueryBuilder.filter(f->f.geoDistance(gd -> gd.distance(RADIUS+"km")
                    .field("location")
                    .distanceType(GeoDistanceType.Arc)
                    .location(loc -> loc.latlon(l->l.lat(mapY).lon(mapX)))));
        }

        if(sort!=null) {
            switch (sort){
                case COMMENT_COUNT ->             sortOptions.add(SortOptions.of(so->so.field(f->f.field("comment_count").order(SortOrder.Desc))));
                case LIKE_COUNT ->       sortOptions.add(SortOptions.of(so->so.field(f->f.field("like_count").order(SortOrder.Desc))));
                case DISTANCE -> {
                    GeoDistanceSort geoDistanceSort =
                            GeoDistanceSort.of(ds -> ds
                                    .field("location")
                                    .location(loc -> loc.latlon(l->l.lat(mapY).lon(mapX)))
                                    .unit(DistanceUnit.Kilometers)
                                    .order(SortOrder.Asc)

                            );
                    SortOptions result = SortOptions.of(so->so.geoDistance(geoDistanceSort));
                    sortOptions.add(result);
                }
            }
        }

        sortOptions.add(SortOptions.of(so->so.field(f->f.field("_score").order(SortOrder.Desc))));

        if(searchByContent) {
            boolQueryBuilder.should(s -> s.match(m -> m.field("content").query(keyword).boost(5f)));
        }

        if(searchByNickname){
            boolQueryBuilder.should(s -> s.matchPhrase(mp -> mp.field("nickname").query(keyword).boost(30f)));
            boolQueryBuilder.should(s -> s.matchPhrase(mp -> mp.field("nickname.normalized").query(keyword).boost(15f)));
            boolQueryBuilder.should(s -> s.match(m -> m.field("nickname.ngram").query(keyword).boost(1f)));
        }

        if(searchByContent || searchByNickname){
            boolQueryBuilder.minimumShouldMatch("1");
        }

        BoolQuery boolQuery = boolQueryBuilder.build();

        sortOptions.add(SortOptions.of(so -> so.field(f->f.field("create_date").order(SortOrder.Desc))));
        sortOptions.add(SortOptions.of(so -> so.field(f->f.field("id").order(SortOrder.Desc))));

        String index;
        double minScore;
        switch (language) {
            case KO -> {
                index="commpostkor";
                minScore=!keyword.isBlank()?4.0:0;
            }
            case JA -> {
                index = "commpostjpn";
                minScore=!keyword.isBlank()?6.0:0;
            }
            case ZH -> {
                index="commpostchs";
                minScore=!keyword.isBlank()?3.0:0;
            }
            default -> {
                index="commposteng";
                minScore=!keyword.isBlank()?6.5:0;
            }
        }


        SearchRequest searchRequest = SearchRequest.of(
            sq->sq.index(index)
                    .from((int)pageable.getOffset())
                    .size(pageable.getPageSize())
                    .sort(sortOptions)
                    .query(q -> q.bool(boolQuery))
                    .minScore(minScore)
        );

        System.out.println("My Query:"+searchRequest.toString());

        SearchResponse<CommPostDoc> response = client.search(searchRequest,CommPostDoc.class);

        long totalElements = Objects.requireNonNull(response.hits().total()).value();

        List<CommPostDto> list = response.hits().hits().stream()
                .map(hit -> new CommPostDto(Objects.requireNonNull(hit.source())))
                .toList();

        return new PageImpl<>(
                list,
                pageable,
                totalElements
        );
    }
}

