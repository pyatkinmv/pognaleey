package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.repository.TravelGuideLikeRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelGuideLikeService {
    private final TravelGuideLikeRepository likeRepository;

    public void save(TravelGuideLike guideLike) {
        log.info("save {}", guideLike);
        likeRepository.save(guideLike);
    }

    public int countByGuideId(long guideId) {
        return likeRepository.countByGuideId(guideId);
    }

    public Optional<TravelGuideLike> findByUserIdAndGuideId(Long id, long guideId) {
        return likeRepository.findByUserIdAndGuideId(id, guideId);
    }

    public void delete(TravelGuideLike like) {
        log.info("delete {}", like);
        likeRepository.delete(like);
    }

    public List<Long> findGuidesIdsByUserId(Long id, int pageSize, int offset) {
        return likeRepository.findGuidesIdsByUserId(id, pageSize, offset);
    }

    public int countByUserId(Long id) {
        return likeRepository.countByUserId(id);
    }
}
