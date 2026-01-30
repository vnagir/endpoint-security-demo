package com.endpoint.security.api.service;

import com.endpoint.security.api.document.AlertDocument;
import com.endpoint.security.api.dto.PageResponse;
import com.endpoint.security.api.repository.SecurityEventRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
public class EndpointService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SecurityEventRepository securityEventRepository;
    private final MongoTemplate mongoTemplate;

    public EndpointService(SecurityEventRepository securityEventRepository, MongoTemplate mongoTemplate) {
        this.securityEventRepository = securityEventRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public PageResponse<String> getUniqueEndpointIds(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        List<String> fromPostgres = securityEventRepository.findDistinctEndpointIds();
        List<String> fromMongo = mongoTemplate.findDistinct("endpointId", AlertDocument.class, String.class);
        TreeSet<String> unique = new TreeSet<>();
        if (fromPostgres != null) unique.addAll(fromPostgres);
        if (fromMongo != null) unique.addAll(fromMongo);
        List<String> all = new ArrayList<>(unique);
        long total = all.size();
        int totalPages = total == 0 ? 0 : (int) ((total + safeSize - 1) / safeSize);
        int fromIndex = (int) Math.min((long) safePage * safeSize, total);
        int toIndex = (int) Math.min((long) (safePage + 1) * safeSize, total);
        List<String> content = fromIndex < toIndex ? all.subList(fromIndex, toIndex) : List.of();
        int currentPage1Based = total == 0 ? 0 : safePage + 1;
        return PageResponse.of(total, totalPages, currentPage1Based, safeSize, content);
    }
}
