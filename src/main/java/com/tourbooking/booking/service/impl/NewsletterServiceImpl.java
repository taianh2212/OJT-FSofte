package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.mapper.NewsletterMapper;
import com.tourbooking.booking.model.dto.request.NewsletterRequest;
import com.tourbooking.booking.model.dto.response.NewsletterResponse;
import com.tourbooking.booking.model.entity.Newsletter;
import com.tourbooking.booking.repository.NewsletterRepo;
import com.tourbooking.booking.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterRepo newsletterRepo;

    @Override
    public List<NewsletterResponse> getAllNewsletters() {
        return newsletterRepo.findAll().stream()
                .map(NewsletterMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NewsletterResponse getNewsletterById(Long id) {
        Newsletter newsletter = newsletterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Newsletter subscription not found with id: " + id));
        return NewsletterMapper.toResponse(newsletter);
    }

    @Override
    public NewsletterResponse createNewsletter(NewsletterRequest request) {
        Newsletter newsletter = NewsletterMapper.toEntity(request);
        Newsletter savedNewsletter = newsletterRepo.save(newsletter);
        return NewsletterMapper.toResponse(savedNewsletter);
    }

    @Override
    public NewsletterResponse updateNewsletter(Long id, NewsletterRequest request) {
        Newsletter existingNewsletter = newsletterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Newsletter subscription not found with id: " + id));
        existingNewsletter.setEmail(request.getEmail());
        Newsletter updatedNewsletter = newsletterRepo.save(existingNewsletter);
        return NewsletterMapper.toResponse(updatedNewsletter);
    }

    @Override
    public void deleteNewsletter(Long id) {
        if (!newsletterRepo.existsById(id)) {
            throw new RuntimeException("Newsletter subscription not found with id: " + id);
        }
        newsletterRepo.deleteById(id);
    }
}
