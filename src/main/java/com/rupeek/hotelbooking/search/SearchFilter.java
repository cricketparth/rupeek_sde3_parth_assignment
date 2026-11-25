package com.rupeek.hotelbooking.search;

/**
 * Extension point for search filters. To add a filter, write one @Component implementing this
 * interface - {@link SearchService} discovers all filters automatically and never changes.
 */
public interface SearchFilter {

    /** Whether the criteria ask for this filter at all. */
    boolean appliesTo(SearchCriteria criteria);

    /** Narrow the candidate's offers, or return {@code candidate.excluded()} to drop the property. */
    PropertyCandidate apply(SearchCriteria criteria, PropertyCandidate candidate);
}
