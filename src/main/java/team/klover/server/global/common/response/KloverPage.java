package team.klover.server.global.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

public class KloverPage<T> {
    private List<T> contents;

    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalCount;

    public static <T> KloverPage<T> of(Page<T> pagedContents) {
        KloverPage<T> converted = new KloverPage<>();
        converted.contents = pagedContents.getContent();
        converted.pageNumber = pagedContents.getNumber();
        converted.pageSize = pagedContents.getSize();
        converted.totalPages = pagedContents.getTotalPages();
        converted.totalCount = pagedContents.getTotalElements();
        return converted;
    }
}

