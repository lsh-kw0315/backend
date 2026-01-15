package team.klover.server.global.elasticsearch.tourpost.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.enums.Area;
import team.klover.server.domain.tour.enums.ContentType;
import team.klover.server.domain.tour.enums.TourPostSort;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;
import team.klover.server.global.common.constant.SearchConstant;
import team.klover.server.global.elasticsearch.tourpost.doc.TourPostDoc;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TourPostDocService {
    private static final int RADIUS = 5;
    private static final String PERCENTAGE = "75%";
    private final ElasticsearchClient client;


    //mapX: longitude(경도, lon), mapY: latitude(위도, lat)
    @SneakyThrows
    public Page<TourPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, Area area, ContentType contentType, boolean hasExotic, boolean hasHealing, boolean hasTraditional, boolean hasActive, boolean searchByTitle, boolean searchByOverview, TourPostSort sort){

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        List<SortOptions> sortOptions = new ArrayList<>();

        if(mapX != null && mapY != null){
            boolQueryBuilder.filter(f->f.geoDistance(gd -> gd.distance(RADIUS+"km")
                    .field("location")
                    .distanceType(GeoDistanceType.Arc)
                    .location(loc -> loc.latlon(l->l.lat(mapY).lon(mapX)))));
            if(sort != null && sort==TourPostSort.DISTANCE){
                GeoDistanceSort geoDistanceSort =
                        GeoDistanceSort.of(ds -> ds
                                .field("location")
                                .location(loc -> loc.latlon(l -> l.lat(mapY).lon(mapX)))
                                .unit(DistanceUnit.Kilometers)
                                .order(SortOrder.Asc)

                        );

                SortOptions result = SortOptions.of(so -> so.geoDistance(geoDistanceSort));
                sortOptions.add(result);
            }
        }

        sortOptions.add(SortOptions.of(so->so.field(f->f.field("_score").order(SortOrder.Desc))));


        if(sort != null){
            switch (sort){
                case REVIEW_COUNT -> sortOptions.add(SortOptions.of(so->so.field(f->f.field("review_count").order(SortOrder.Desc))));
                case RATING_AVERAGE -> sortOptions.add(SortOptions.of(so->so.field(f->f.field("rating_average").order(SortOrder.Desc))));
            }
        }

        if(searchByTitle){
            boolQueryBuilder.should(s -> s.matchPhrase(mp -> mp.field("title").query(keyword).boost(30f)));
            boolQueryBuilder.should(s -> s.match(m -> m.field("title.ngram").query(keyword).boost(0.5f)));
        }

        if(searchByOverview){
            boolQueryBuilder.should(s -> s.match(m -> m.field("overview").query(keyword).boost(8f)));

        }

        if(searchByTitle || searchByOverview){
            boolQueryBuilder.minimumShouldMatch("1");
        }

        if(area!=null) {
            switch (area) {
                case SEOUL ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("area_code").value(SearchConstant.SEOUL)));
                case JEJU -> boolQueryBuilder.filter(f -> f.term(t -> t.field("area_code").value(SearchConstant.JEJU)));
                case BUSAN ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("area_code").value(SearchConstant.BUSAN)));
                case INCHEON ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("area_code").value(SearchConstant.INCHEON)));
            }
        }

        if(contentType!=null) {
            switch (contentType) {
                case ACCOMMODATION ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                                .value(language.equals(Country.KO)?SearchConstant.ACCOMMODATION_KOREAN:SearchConstant.ACCOMMODATION_FOREIGN)));
                case CULTURAL_FACILITY ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                                .value(language.equals(Country.KO)?SearchConstant.CULTURAL_FACILITY_KOREAN:SearchConstant.CULTURAL_FACILITY_FOREIGN)));
                case ACTIVITY ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                                .value(language.equals(Country.KO)?SearchConstant.ACTIVITY_KOREAN:SearchConstant.ACTIVITY_FOREIGN)));
                case DINING ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                                .value(language.equals(Country.KO)?SearchConstant.DINING_KOREAN:SearchConstant.DINING_FOREIGN)));
                case SHOPPING ->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                                .value(language.equals(Country.KO)?SearchConstant.SHOPPING_KOREAN:SearchConstant.SHOPPING_FOREIGN)));
                case  ATTRACTION->
                        boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                                .value(language.equals(Country.KO)?SearchConstant.ATTRACTION_KOREAN:SearchConstant.ATTRACTION_FOREIGN)));

                case  EVENT -> boolQueryBuilder.filter(f -> f.term(t -> t.field("content_type_id")
                        .value(language.equals(Country.KO)?SearchConstant.EVENT_KOREAN:SearchConstant.EVENT_FOREIGN)));
            }
        }

        Set<String> categories = new HashSet<>();
        if(hasExotic){
            categories.addAll(Arrays.asList(SearchConstant.EXOTIC));
        }

        if(hasActive){
            categories.addAll(Arrays.asList(SearchConstant.ACTIVE));
        }

        if(hasHealing){
            categories.addAll(Arrays.asList(SearchConstant.HEALING));
        }

        if(hasTraditional){
            categories.addAll(Arrays.asList(SearchConstant.TRADITIONAL));
        }

        List<FieldValue> values = categories.stream().map(FieldValue::of).toList();
        if(!categories.isEmpty()){
            boolQueryBuilder.filter(f->f.terms(t->t.field("cat3").terms(ts->ts.value(values))));
        }

        BoolQuery boolQuery = boolQueryBuilder.build();

        sortOptions.add(SortOptions.of(so -> so.field(f->f.field("create_date").order(SortOrder.Desc))));
        sortOptions.add(SortOptions.of(so -> so.field(f->f.field("content_id").order(SortOrder.Desc))));

        String index;
        double minScore;
        switch (language) {
            case KO -> {
                index="tourpostkor";
                minScore=!keyword.isBlank()?7.5:0;
            }
            case JA -> {
                index="tourpostjpn";
                minScore=!keyword.isBlank()?10:0;
            }
            case ZH -> {
                index="tourpostchs";
                minScore=!keyword.isBlank()?7.0:0;
            }
            default -> {
                index="tourposteng";
                minScore=!keyword.isBlank()?9:0;
            }
        }


        SearchRequest searchRequest = SearchRequest.of(
                sq->sq.index(index)
                        .from((int)pageable.getOffset())
                        .size(pageable.getPageSize())
                        .sort(sortOptions)
                        .query(q -> q
                                .bool(boolQuery))
                        .minScore(minScore)
        );

        //System.out.println("My Query:"+searchRequest.toString());

        SearchResponse<TourPostDoc> response = client.search(searchRequest,TourPostDoc.class);

        long totalElements = Objects.requireNonNull(response.hits().total()).value();

        List<TourPostDto> list = response.hits().hits().stream()
                .map(hit -> new TourPostDto(Objects.requireNonNull(hit.source())))
                .toList();

        return new PageImpl<>(
                list,
                pageable,
                totalElements
        );
    }
}
