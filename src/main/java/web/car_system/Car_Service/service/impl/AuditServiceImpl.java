package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.domain.dto.audit.AuditDiffDTO;
import web.car_system.Car_Service.domain.dto.audit.AuditLogDTO;
import web.car_system.Car_Service.domain.dto.audit.EntityHistoryEntryDTO;
import web.car_system.Car_Service.domain.dto.audit.EntitySnapshotDTO;
import web.car_system.Car_Service.domain.dto.audit.RevisionDTO;
import web.car_system.Car_Service.domain.dto.audit.RevisionSummaryDTO;
import web.car_system.Car_Service.domain.entity.AuditedEntityType;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CustomRevisionEntity;
import web.car_system.Car_Service.service.AuditService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private static final List<String> LABEL_FIELD_CANDIDATES =
            List.of("name", "model", "displayName", "code", "title", "provinceCity", "description");

    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, Boolean.class, Character.class,
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigDecimal.class,
            Date.class, Instant.class
    );

    private final EntityManager entityManager;

    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(entityManager);
    }

    // =========================================================
    // Legacy
    // =========================================================

    @Override
    public List<AuditLogDTO<Car>> getCarHistory(Integer carId) {
        AuditReader reader = getAuditReader();
        @SuppressWarnings("unchecked")
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(Car.class, true, true)
                .add(AuditEntity.id().eq(carId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return results.stream().map(row -> {
            Car car = (Car) row[0];
            CustomRevisionEntity rev = (CustomRevisionEntity) row[1];
            RevisionType type = (RevisionType) row[2];
            return new AuditLogDTO<>(car, new RevisionDTO(rev.getId(), new Date(rev.getTimestamp()), rev.getUsername()), type);
        }).collect(Collectors.toList());
    }

    @Override
    public Car getCarAtRevision(Integer carId, Integer revisionNumber) {
        return getAuditReader().find(Car.class, carId, revisionNumber);
    }

    // =========================================================
    // Generic
    // =========================================================

    @Override
    public Page<EntityHistoryEntryDTO> getEntityHistory(String entityType, Integer entityId, Pageable pageable) {
        AuditedEntityType type = AuditedEntityType.resolve(entityType);
        AuditReader reader = getAuditReader();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = reader.createQuery()
                .forRevisionsOfEntity(type.getEntityClass(), false, true)
                .add(AuditEntity.id().eq(entityId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        List<EntityHistoryEntryDTO> all = rows.stream().map(row -> {
            CustomRevisionEntity rev = (CustomRevisionEntity) row[1];
            RevisionType revType = (RevisionType) row[2];
            return new EntityHistoryEntryDTO(rev.getId(), Instant.ofEpochMilli(rev.getTimestamp()),
                    rev.getUsername(), revType);
        }).toList();

        return paginate(all, pageable);
    }

    @Override
    public EntitySnapshotDTO getEntitySnapshot(String entityType, Integer entityId, int revisionId) {
        AuditedEntityType type = AuditedEntityType.resolve(entityType);
        AuditReader reader = getAuditReader();

        Object entity = reader.find(type.getEntityClass(), entityId, revisionId);
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không tìm thấy snapshot cho " + entityType + "#" + entityId + " tại revision " + revisionId);
        }

        CustomRevisionEntity rev = reader.findRevision(CustomRevisionEntity.class, revisionId);
        RevisionType revType = resolveRevisionType(reader, type.getEntityClass(), entityId, revisionId);

        return new EntitySnapshotDTO(
                type.getCode(), entityId, revisionId,
                Instant.ofEpochMilli(rev.getTimestamp()), rev.getUsername(),
                revType, toFlatMap(entity));
    }

    @Override
    public Page<RevisionSummaryDTO> getRecentRevisions(
            String entityTypeFilter, String usernameFilter,
            Instant fromTs, Instant toTs, Pageable pageable) {

        AuditReader reader = getAuditReader();
        List<RevisionSummaryDTO> all = new ArrayList<>();

        List<AuditedEntityType> types = (entityTypeFilter == null || entityTypeFilter.isBlank())
                ? List.of(AuditedEntityType.values())
                : List.of(AuditedEntityType.resolve(entityTypeFilter));

        // Query mỗi entity type, lấy tối đa 200 revision mới nhất → merge trong memory
        // Trade-off: với data hiện tại < 100 rev/entity OK. Khi >1000, cần native SQL — note ở plan.
        for (AuditedEntityType t : types) {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = reader.createQuery()
                    .forRevisionsOfEntity(t.getEntityClass(), false, true)
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .setMaxResults(200)
                    .getResultList();

            for (Object[] row : rows) {
                Object entity = row[0];
                CustomRevisionEntity rev = (CustomRevisionEntity) row[1];
                RevisionType revType = (RevisionType) row[2];

                if (usernameFilter != null && !usernameFilter.isBlank()
                        && !usernameFilter.equals(rev.getUsername())) continue;
                Instant ts = Instant.ofEpochMilli(rev.getTimestamp());
                if (fromTs != null && ts.isBefore(fromTs)) continue;
                if (toTs != null && ts.isAfter(toTs)) continue;

                Object id = extractId(entity);
                String label = extractLabel(entity, t);

                all.add(new RevisionSummaryDTO(
                        rev.getId(), ts, rev.getUsername(),
                        t.getCode(), t.getDisplayName(), id, label, revType));
            }
        }

        all.sort((a, b) -> b.timestamp().compareTo(a.timestamp()));
        return paginate(all, pageable);
    }

    @Override
    public AuditDiffDTO diff(String entityType, Integer entityId, int fromRev, int toRev) {
        AuditedEntityType type = AuditedEntityType.resolve(entityType);
        AuditReader reader = getAuditReader();

        Object fromEntity = reader.find(type.getEntityClass(), entityId, fromRev);
        Object toEntity = reader.find(type.getEntityClass(), entityId, toRev);

        Map<String, Object> fromMap = fromEntity != null ? toFlatMap(fromEntity) : Map.of();
        Map<String, Object> toMap = toEntity != null ? toFlatMap(toEntity) : Map.of();

        List<AuditDiffDTO.FieldChange> changes = new ArrayList<>();
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(fromMap.keySet());
        allKeys.addAll(toMap.keySet());
        for (String k : allKeys) {
            Object before = fromMap.get(k);
            Object after = toMap.get(k);
            if (!Objects.equals(before, after)) {
                changes.add(new AuditDiffDTO.FieldChange(k, before, after));
            }
        }

        CustomRevisionEntity fromRevInfo = reader.findRevision(CustomRevisionEntity.class, fromRev);
        CustomRevisionEntity toRevInfo = reader.findRevision(CustomRevisionEntity.class, toRev);

        return new AuditDiffDTO(
                type.getCode(), entityId,
                fromRev, fromRevInfo != null ? Instant.ofEpochMilli(fromRevInfo.getTimestamp()) : null,
                toRev, toRevInfo != null ? Instant.ofEpochMilli(toRevInfo.getTimestamp()) : null,
                changes);
    }

    // =========================================================
    // Helpers — reflection-based extract
    // =========================================================

    private RevisionType resolveRevisionType(AuditReader reader, Class<?> clazz, Object id, int revisionId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = reader.createQuery()
                    .forRevisionsOfEntity(clazz, false, true)
                    .add(AuditEntity.id().eq(id))
                    .add(AuditEntity.revisionNumber().eq(revisionId))
                    .getResultList();
            if (!rows.isEmpty()) return (RevisionType) rows.get(0)[2];
        } catch (Exception e) {
            log.warn("[Audit] resolveRevisionType failed for {}#{} rev={}", clazz.getSimpleName(), id, revisionId);
        }
        return null;
    }

    private Object extractId(Object entity) {
        for (Field f : declaredFields(entity.getClass())) {
            if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
                f.setAccessible(true);
                try {
                    return f.get(entity);
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return null;
    }

    private String extractLabel(Object entity, AuditedEntityType type) {
        for (String fieldName : LABEL_FIELD_CANDIDATES) {
            try {
                Field f = entity.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Object v = f.get(entity);
                if (v != null) return v.toString();
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return type.getDisplayName() + " #" + extractId(entity);
    }

    /**
     * Flatten entity → Map field-name → value, chỉ giữ simple types và enum.
     * Bỏ qua relationship (entity, collection) để tránh trigger lazy load → LazyInitializationException.
     */
    private Map<String, Object> toFlatMap(Object entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Field f : declaredFields(entity.getClass())) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                Object v = f.get(entity);
                if (v == null) {
                    out.put(f.getName(), null);
                } else if (isSimple(v)) {
                    out.put(f.getName(), v);
                } else {
                    // Relationship: thử lấy ID nested để hiển thị
                    Object nestedId = tryExtractNestedId(v);
                    if (nestedId != null) {
                        out.put(f.getName(), v.getClass().getSimpleName() + "#" + nestedId);
                    }
                    // Collection / lazy → bỏ qua (tránh load)
                }
            } catch (Exception ex) {
                out.put(f.getName(), "<unavailable>");
            }
        }
        return out;
    }

    private boolean isSimple(Object v) {
        if (v.getClass().isEnum()) return true;
        if (v.getClass().isPrimitive()) return true;
        return SIMPLE_TYPES.contains(v.getClass());
    }

    private Object tryExtractNestedId(Object obj) {
        try {
            return extractId(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Field> declaredFields(Class<?> clazz) {
        List<Field> all = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            all.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        return all;
    }

    private <T> Page<T> paginate(List<T> all, Pageable pageable) {
        if (pageable.isUnpaged()) return new PageImpl<>(all, pageable, all.size());
        int offset = (int) pageable.getOffset();
        if (offset >= all.size()) return new PageImpl<>(List.of(), pageable, all.size());
        int end = Math.min(all.size(), offset + pageable.getPageSize());
        return new PageImpl<>(all.subList(offset, end), pageable, all.size());
    }
}
