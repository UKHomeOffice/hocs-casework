package uk.gov.digital.ho.hocs.casework.domain.repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.api.overview.ColumnFilter;
import uk.gov.digital.ho.hocs.casework.api.overview.ColumnSort;
import uk.gov.digital.ho.hocs.casework.api.overview.PageRequest;
import uk.gov.digital.ho.hocs.casework.api.overview.ColumnSort.SortOrder;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverviewRaw;

@Repository
@Slf4j
public class OverviewRepository {

    private final EntityManager entityManager;

    private final CriteriaBuilder criteriaBuilder;

    @Autowired
    public OverviewRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    public Page<CaseOverviewRaw> findByQuery(PageRequest pagedQuery) {

        Long count = getCountForQuery(pagedQuery);
        List<CaseOverviewRaw> cases = getResultsForQuery(pagedQuery);

        // Use a Spring Data Page to return the data
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(pagedQuery.getPageNumber(), pagedQuery.getResultsPerPage(), calculateSort(pagedQuery));
        return new PageImpl<>(cases, pageRequest, count);
    }

    private Long getCountForQuery(PageRequest pageRequest) {

        CriteriaQuery<Long> countCriteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CaseOverviewRaw> caseRoot = countCriteriaQuery.from(CaseOverviewRaw.class);

        countCriteriaQuery.select(criteriaBuilder.count(caseRoot))
                          .where(getPredicates(pageRequest.getColumnFilters(), caseRoot));

        return entityManager.createQuery(countCriteriaQuery)
                            .getSingleResult();
    }

    private List<CaseOverviewRaw> getResultsForQuery(PageRequest pageRequest) {

        CriteriaQuery<CaseOverviewRaw> resultsCriteriaQuery = criteriaBuilder.createQuery(CaseOverviewRaw.class);
        Root<CaseOverviewRaw> caseRoot = resultsCriteriaQuery.from(CaseOverviewRaw.class);

        resultsCriteriaQuery.select(caseRoot)
                            .where(getPredicates(pageRequest.getColumnFilters(), caseRoot))
                            .orderBy(getOrders(calculateSort(pageRequest), caseRoot));

        return entityManager.createQuery(resultsCriteriaQuery)
                            .setFirstResult(pageRequest.getOffset())
                            .setMaxResults(pageRequest.getResultsPerPage())
                            .getResultList();
    }

    private Predicate[] getPredicates(List<ColumnFilter> columnFilterCriteria, Root<CaseOverviewRaw> root) {
        return columnFilterCriteria.stream()
                             .map((entry) -> getPredicateForFilter(entry, root))
                             .toArray(Predicate[]::new);
    }

    private Predicate getPredicateForFilter(ColumnFilter columnFilter, Root<CaseOverviewRaw> caseRoot) {
        switch (columnFilter.getFilterType()) {
            case EQUALS:
                return criteriaBuilder.equal(caseRoot.get(columnFilter.getName()), columnFilter.getValue());
            case IN: {
                List<String> listString = Arrays.asList(columnFilter.getValue().split(",", -1));
                return caseRoot.get(columnFilter.getName()).in(listString);
            }
            case CASE_INSENSITIVE_LIKE:
                return criteriaBuilder.like(criteriaBuilder.lower(caseRoot.get(columnFilter.getName())),
                                            criteriaBuilder.lower(criteriaBuilder.literal("%" + columnFilter.getValue() + "%")));
            default:
                throw new IllegalArgumentException("Unsupported filter type for filter: " + columnFilter.getName());
        }
    }

    private Sort calculateSort(PageRequest pageRequest) {
        List<Sort.Order> orders = pageRequest.getColumnSorts().stream().map(this::getOrder).collect(Collectors.toList());
        return Sort.by(orders);
    }

    private List<Order> getOrders(Sort sort, Root<CaseOverviewRaw> from) {
        return sort.stream()
                   .map((order) -> translateOrder(order, from))
                   .collect(Collectors.toList());
    }

    private Order translateOrder(Sort.Order order, Root<CaseOverviewRaw> from) {
        Path<Object> field = from.get(order.getProperty());
        return order.isAscending() ? criteriaBuilder.asc(field) : criteriaBuilder.desc(field);
    }

    private Sort.Order getOrder(ColumnSort columnSort) {
        return new Sort.Order(SortOrder.DESCENDING.equals(columnSort.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC, columnSort.getName());
    }
}
