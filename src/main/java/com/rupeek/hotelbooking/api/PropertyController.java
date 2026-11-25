package com.rupeek.hotelbooking.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rupeek.hotelbooking.api.dto.PropertyResponse;
import com.rupeek.hotelbooking.api.dto.SearchResultResponse;
import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.property.PropertyService;
import com.rupeek.hotelbooking.search.SearchCriteria;
import com.rupeek.hotelbooking.search.SearchService;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final SearchService searchService;
    private final PropertyService propertyService;

    public PropertyController(SearchService searchService, PropertyService propertyService) {
        this.searchService = searchService;
        this.propertyService = propertyService;
    }

    @GetMapping("/search")
    public List<SearchResultResponse> search(
            @RequestParam String city,
            @RequestParam(required = false) String locality,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) Set<Amenity> amenities) {

        SearchCriteria criteria = new SearchCriteria(city, locality, new DateRange(checkIn, checkOut), guests,
                minPrice == null ? null : Money.of(minPrice), maxPrice == null ? null : Money.of(maxPrice),
                minStars, amenities);
        return searchService.search(criteria).stream().map(SearchResultResponse::from).toList();
    }

    @GetMapping("/{propertyId}")
    public PropertyResponse get(@PathVariable String propertyId) {
        return PropertyResponse.from(propertyService.getProperty(propertyId));
    }
}
