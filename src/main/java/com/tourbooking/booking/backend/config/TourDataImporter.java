package com.tourbooking.booking.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.dto.importer.TourImportDto;
import com.tourbooking.booking.backend.model.entity.Category;
import com.tourbooking.booking.backend.model.entity.City;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.model.entity.TourHighlight;
import com.tourbooking.booking.backend.model.entity.TourImage;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.model.entity.enums.TourStatus;
import com.tourbooking.booking.backend.repository.CategoryRepository;
import com.tourbooking.booking.backend.repository.CityRepository;
import com.tourbooking.booking.backend.repository.TourHighlightRepository;
import com.tourbooking.booking.backend.repository.TourImageRepository;
import com.tourbooking.booking.backend.repository.TourRepository;
import com.tourbooking.booking.backend.repository.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tour.import", name = "enabled", havingValue = "true")
public class TourDataImporter implements CommandLineRunner {

    private final TourImportProperties properties;
    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final TourHighlightRepository tourHighlightRepository;
    private final TourImageRepository tourImageRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Tour data importer enabled, scanning {}/*.json", properties.getDirectory());
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:" + properties.getDirectory() + "/*.json");
            if (resources.length == 0) {
                log.warn("No tour data files found under {}", properties.getDirectory());
                return;
            }
            for (Resource resource : resources) {
                process(resource);
            }
        } catch (Exception ex) {
            log.error("Unable to load tour data", ex);
        }
    }

    private void process(Resource resource) {
        try {
            TourImportDto dto = objectMapper.readValue(resource.getInputStream(), TourImportDto.class);
            if (dto.getExternalId() == null) {
                log.warn("Skipping tour file {} because externalId is missing", resource.getFilename());
                return;
            }

            Category category = resolveCategory(dto.getCategoryName(), dto.getCategoryDescription());
            City city = resolveCity(dto);

            Tour tour = tourRepository.findByExternalId(dto.getExternalId()).orElse(new Tour());
            mapFields(dto, tour, category, city);
            Tour saved = tourRepository.save(tour);

            cleanUpDependencies(saved);
            persistHighlights(saved, dto.getHighlights());
            persistImages(saved, dto.getImages());
            persistSchedules(saved, dto.getSchedules());

            log.info("Imported tour '{}' (externalId={}) with id={}", saved.getTourName(), saved.getExternalId(), saved.getId());
        } catch (Exception ex) {
            log.error("Failed to import tour from {}", resource.getFilename(), ex);
        }
    }

    private void mapFields(TourImportDto dto, Tour tour, Category category, City city) {
        tour.setExternalId(dto.getExternalId());
        tour.setTourName(dto.getTourName());
        tour.setDescription(dto.getDescription());
        tour.setPrice(dto.getPrice());
        tour.setOriginalPrice(dto.getOriginalPrice());
        tour.setDuration(dto.getDuration());
        tour.setStartLocation(dto.getStartLocation());
        tour.setEndLocation(dto.getEndLocation());
        tour.setTransportType(dto.getTransportType());
        tour.setCategory(category);
        tour.setCity(city);
        tour.setInclusions(joinWithNewline(dto.getInclusions()));
        tour.setExclusions(joinWithNewline(dto.getExclusions()));
        tour.setTips(dto.getTips());
        tour.setItinerary(dto.getItinerary());
        tour.setPaymentPolicy(dto.getPaymentPolicy());
        tour.setCancellationPolicy(dto.getCancellationPolicy());
        tour.setChildPolicy(dto.getChildPolicy());
        tour.setWhyChooseUs(dto.getWhyChooseUs());
        tour.setSuitableAges(dto.getSuitableAges());
        tour.setBestTime(dto.getBestTime());
        tour.setWeatherInfo(dto.getWeatherInfo());
        tour.setGuideInfo(dto.getGuideInfo());
        tour.setMetaTitle(dto.getMetaTitle());
        tour.setMetaDescription(dto.getMetaDescription());
        tour.setMinDepositRate(dto.getMinDepositRate());
        tour.setRefundGracePeriod(dto.getRefundGracePeriod());
        tour.setRating(dto.getRating());
        tour.setHasPickup(defaultFalse(dto.getHasPickup()));
        tour.setHasLunch(defaultFalse(dto.getHasLunch()));
        tour.setIsInstantConfirmation(defaultFalse(dto.getIsInstantConfirmation()));
        tour.setIsDaily(defaultFalse(dto.getIsDaily()));
        tour.setSource("IMPORT");
    }

    private void cleanUpDependencies(Tour tour) {
        tourHighlightRepository.deleteAllByTour(tour);
        tourImageRepository.deleteAllByTour(tour);
        tourScheduleRepository.deleteAllByTour(tour);
    }

    private void persistHighlights(Tour tour, List<String> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return;
        }
        for (String value : highlights) {
            if (value == null || value.isBlank()) {
                continue;
            }
            TourHighlight highlight = new TourHighlight();
            highlight.setTour(tour);
            highlight.setHighlight(value.trim());
            tourHighlightRepository.save(highlight);
        }
    }

    private void persistImages(Tour tour, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (String url : images) {
            if (url == null || url.isBlank()) {
                continue;
            }
            TourImage image = new TourImage();
            image.setTour(tour);
            image.setImageUrl(url.trim());
            tourImageRepository.save(image);
        }
    }

    private void persistSchedules(Tour tour, List<TourImportDto.TourScheduleImportDto> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        for (TourImportDto.TourScheduleImportDto dto : schedules) {
            TourSchedule schedule = new TourSchedule();
            schedule.setTour(tour);
            LocalDate startDate = parseDate(dto.getStartDate());
            LocalDate endDate = parseDate(dto.getEndDate());
            if (endDate == null && startDate != null) {
                int daysToAdd = (tour.getDuration() != null && tour.getDuration() > 0) ? tour.getDuration() - 1 : 0;
                endDate = startDate.plusDays(daysToAdd);
            }
            schedule.setStartDate(startDate);
            schedule.setEndDate(endDate);
            schedule.setAvailableSlots(dto.getAvailableSlots());
            schedule.setStatus(resolveStatus(dto.getStatus()));
            tourScheduleRepository.save(schedule);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            log.warn("Unable to parse date '{}'", value);
            return null;
        }
    }

    private TourStatus resolveStatus(String raw) {
        if (raw == null) {
            return TourStatus.OPEN;
        }
        try {
            return TourStatus.valueOf(raw.toUpperCase());
        } catch (Exception ex) {
            log.warn("Unknown tour status '{}', defaulting to OPEN", raw);
            return TourStatus.OPEN;
        }
    }

    private Category resolveCategory(String name, String description) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return categoryRepository.findByCategoryNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setCategoryName(name.trim());
                    cat.setDescription(description);
                    return categoryRepository.save(cat);
                });
    }

    private City resolveCity(TourImportDto dto) {
        if (dto.getCityName() == null || dto.getCityName().isBlank()) {
            return null;
        }
        return cityRepository.findByCityNameIgnoreCase(dto.getCityName().trim())
                .orElseGet(() -> {
                    City city = new City();
                    city.setCityName(dto.getCityName().trim());
                    city.setCenterLatitude(defaultBigDecimal(dto.getCityLatitude()));
                    city.setCenterLongitude(defaultBigDecimal(dto.getCityLongitude()));
                    return cityRepository.save(city);
                });
    }

    private String joinWithNewline(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join("\n", values.stream().filter(Objects::nonNull).map(String::trim).toList());
    }

    private Boolean defaultFalse(Boolean value) {
        return value != null && value;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
