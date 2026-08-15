package com.phegondev.InventoryManagementSystem.unit;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UnitSpecification {

    private UnitSpecification() {}

    public static Specification<Unit> buildCriteria(UnitFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String like = "%" + filter.getSearch().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("shortName"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("symbol"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("pluralName"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("baseUnit"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("category"), "")), like)
                ));
            }

            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                String status = filter.getStatus().toUpperCase(Locale.ROOT);
                switch (status) {
                    case "ARCHIVED" ->
                            predicates.add(cb.isTrue(root.get("archived")));
                    case "ACTIVE" -> {
                        predicates.add(cb.isFalse(root.get("archived")));
                        predicates.add(cb.isTrue(root.get("status")));
                    }
                    case "INACTIVE" -> {
                        predicates.add(cb.isFalse(root.get("archived")));
                        predicates.add(cb.isFalse(root.get("status")));
                    }
                    default -> {
                        predicates.add(cb.or(
                                cb.isFalse(root.get("archived")),
                                cb.isNull(root.get("archived"))
                        ));
                    }
                }
            }

            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")),
                        filter.getCategory().toLowerCase(Locale.ROOT)));
            }

            if (filter.getMeasurementSystem() != null && !filter.getMeasurementSystem().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("measurementSystem")),
                        filter.getMeasurementSystem().toLowerCase(Locale.ROOT)));
            }

            if (filter.getBaseUnit() != null && !filter.getBaseUnit().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("baseUnit")),
                        filter.getBaseUnit().toLowerCase(Locale.ROOT)));
            }

            if (filter.getUsages() != null && !filter.getUsages().isEmpty()) {
                List<Predicate> usagePredicates = new ArrayList<>();
                for (String usage : filter.getUsages()) {
                    String u = usage.toUpperCase(Locale.ROOT);
                    org.springframework.data.jpa.domain.Specification<Unit> usageSpec = switch (u) {
                        case "PURCHASE" -> (r, q, c) -> c.isTrue(r.get("usesPurchases"));
                        case "SALES" -> (r, q, c) -> c.isTrue(r.get("usesSales"));
                        case "INVENTORY" -> (r, q, c) -> c.isTrue(r.get("usesInventory"));
                        case "MANUFACTURING" -> (r, q, c) -> c.isTrue(r.get("usesManufacturing"));
                        case "PRESCRIPTIONS" -> (r, q, c) -> c.isTrue(r.get("usesPrescriptions"));
                        case "REPORTS" -> (r, q, c) -> c.isTrue(r.get("usesReports"));
                        case "BARCODE" -> (r, q, c) -> c.isTrue(r.get("usesBarcodePrinting"));
                        default -> null;
                    };
                    if (usageSpec != null) {
                        usagePredicates.add(usageSpec.toPredicate(root, query, cb));
                    }
                }
                if (!usagePredicates.isEmpty()) {
                    predicates.add(cb.or(usagePredicates.toArray(new Predicate[0])));
                }
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        filter.getFromDate().atStartOfDay()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        filter.getToDate().atTime(23, 59, 59)));
            }

            if (filter.getUpdatedFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"),
                        filter.getUpdatedFromDate().atStartOfDay()));
            }
            if (filter.getUpdatedToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"),
                        filter.getUpdatedToDate().atTime(23, 59, 59)));
            }

            if (filter.getMinConversionRate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("conversionRate"), filter.getMinConversionRate()));
            }
            if (filter.getMaxConversionRate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("conversionRate"), filter.getMaxConversionRate()));
            }

            if (filter.getCreatedBy() != null && !filter.getCreatedBy().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("createdBy")),
                        filter.getCreatedBy().toLowerCase(Locale.ROOT)));
            }

            if (Boolean.FALSE.equals(filter.getArchived())) {
                predicates.add(cb.or(cb.isFalse(root.get("archived")), cb.isNull(root.get("archived"))));
            } else if (Boolean.TRUE.equals(filter.getArchived())) {
                predicates.add(cb.isTrue(root.get("archived")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort buildSort(String sort, String direction) {
        org.springframework.data.domain.Sort.Direction dir =
                (direction != null && direction.equalsIgnoreCase("asc"))
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;

        if (sort == null || sort.isBlank()) {
            return org.springframework.data.domain.Sort.by(dir, "createdAt");
        }

        String s = sort.toUpperCase(Locale.ROOT);
        return switch (s) {
            case "OLDEST" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "createdAt");
            case "NEWEST" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
            case "NAME_AZ" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "name");
            case "NAME_ZA" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "name");
            case "RECENTLY_UPDATED" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "updatedAt");
            case "MOST_USED" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "usageCount");
            case "LEAST_USED" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "usageCount");
            case "CATEGORY" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "category");
            case "STATUS" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "status");
            default -> {
                java.util.Set<String> sortable = java.util.Set.of(
                        "name", "category", "status", "createdat", "updatedat", "usagescale", "usagecount");
                String field = sortable.contains(s)
                        ? (s.equals("CREATEDAT") ? "createdAt" : s.equals("UPDATEDAT") ? "updatedAt" : s.equals("USAGECOUNT") ? "usageCount" : s.toLowerCase(Locale.ROOT))
                        : "name";
                yield org.springframework.data.domain.Sort.by(dir, field);
            }
        };
    }
}