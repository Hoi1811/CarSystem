package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.mapper.CarMapper;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.UserActivityLogRepository;
import web.car_system.Car_Service.service.PersonalizationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalizationServiceImpl implements PersonalizationService {

    private static final int MAX_LIMIT = 24;
    private static final int SEED_LIMIT = 3;

    private final UserActivityLogRepository activityLogRepository;
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CarResponseDTO> getRecentViewedCars(Long userId, int limit) {
        if (userId == null) return List.of();
        int safeLimit = clampLimit(limit);

        Pageable page = PageRequest.of(0, safeLimit);
        List<Integer> ids = activityLogRepository.findRecentViewedCarIds(userId, page);
        if (ids.isEmpty()) return List.of();

        return fetchAndPreserveOrder(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponseDTO> getCarsByIds(List<Integer> ids, int limit) {
        if (ids == null || ids.isEmpty()) return List.of();
        int safeLimit = clampLimit(limit);

        List<Integer> deduped = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(safeLimit)
                .collect(Collectors.toList());
        if (deduped.isEmpty()) return List.of();

        return fetchAndPreserveOrder(deduped);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponseDTO> getMaybeInterestedCars(Long userId, List<Integer> seedCarIds, int limit) {
        int safeLimit = clampLimit(limit);

        List<Integer> seeds = resolveSeedIds(userId, seedCarIds);
        if (seeds.isEmpty()) return List.of();

        List<Car> seedCars = carRepository.findAllByCarIdIn(seeds);
        if (seedCars.isEmpty()) return List.of();

        Set<Integer> seedSegments = seedCars.stream()
                .map(Car::getSegmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> seedManufacturers = seedCars.stream()
                .map(Car::getManufacturerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> excludeIds = new HashSet<>(seeds);

        if (seedSegments.isEmpty() && seedManufacturers.isEmpty()) return List.of();

        Specification<Car> spec = buildCandidateSpec(seedSegments, seedManufacturers, excludeIds);
        Pageable candidatePage = PageRequest.of(0, Math.max(safeLimit * 3, 24));
        List<Car> candidates = carRepository.findAll(spec, candidatePage).getContent();
        if (candidates.isEmpty()) return List.of();

        return candidates.stream()
                .sorted(Comparator.<Car>comparingInt(c -> -scoreCar(c, seedCars)))
                .limit(safeLimit)
                .map(carMapper::toCarResponseDTO)
                .collect(Collectors.toList());
    }

    private List<Integer> resolveSeedIds(Long userId, List<Integer> providedSeeds) {
        if (providedSeeds != null && !providedSeeds.isEmpty()) {
            return providedSeeds.stream().filter(Objects::nonNull).distinct().limit(SEED_LIMIT).collect(Collectors.toList());
        }
        if (userId == null) return List.of();
        Pageable page = PageRequest.of(0, SEED_LIMIT);
        return activityLogRepository.findRecentViewedCarIds(userId, page);
    }

    private Specification<Car> buildCandidateSpec(Set<Integer> segments, Set<Integer> manufacturers, Set<Integer> excludeIds) {
        List<Specification<Car>> orParts = new ArrayList<>();
        if (!segments.isEmpty()) {
            orParts.add((root, q, cb) -> root.get("segmentId").in(segments));
        }
        if (!manufacturers.isEmpty()) {
            orParts.add((root, q, cb) -> root.get("manufacturerId").in(manufacturers));
        }
        Specification<Car> orSpec = orParts.stream().reduce(Specification::or).orElse(null);
        Specification<Car> excludeSpec = (root, q, cb) -> cb.not(root.get("carId").in(excludeIds));
        return orSpec == null ? excludeSpec : orSpec.and(excludeSpec);
    }

    private int scoreCar(Car candidate, List<Car> seeds) {
        int best = 0;
        for (Car seed : seeds) {
            boolean segMatch = Objects.equals(candidate.getSegmentId(), seed.getSegmentId());
            boolean manuMatch = Objects.equals(candidate.getManufacturerId(), seed.getManufacturerId());
            int score = (segMatch ? 2 : 0) + (manuMatch ? 1 : 0);
            if (score > best) best = score;
        }
        return best;
    }

    private List<CarResponseDTO> fetchAndPreserveOrder(List<Integer> ids) {
        List<Car> cars = carRepository.findAllByCarIdIn(ids);
        Map<Integer, Car> byId = new HashMap<>();
        for (Car c : cars) byId.put(c.getCarId(), c);

        List<CarResponseDTO> out = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            Car c = byId.get(id);
            if (c != null) out.add(carMapper.toCarResponseDTO(c));
        }
        return out;
    }

    private int clampLimit(int limit) {
        if (limit <= 0) return 8;
        return Math.min(limit, MAX_LIMIT);
    }
}
