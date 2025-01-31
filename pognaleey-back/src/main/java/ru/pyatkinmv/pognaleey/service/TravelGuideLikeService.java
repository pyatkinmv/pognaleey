package ru.pyatkinmv.pognaleey.service;

import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.repository.TravelGuideLikeRepository;

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

  public Optional<Long> findIdByUserIdAndGuideId(Long id, long guideId) {
    return likeRepository.findByUserIdAndGuideId(id, guideId).map(TravelGuideLike::getId);
  }

  public void deleteById(Long id) {
    log.info("delete like: {}", id);
    likeRepository.deleteById(id);
  }

  public Set<Long> findGuidesIdsByUserId(Long id, int pageSize, int offset) {
    return likeRepository.findGuidesIdsByUserId(id, pageSize, offset);
  }

  public int countByUserId(Long id) {
    return likeRepository.countByUserId(id);
  }
}
