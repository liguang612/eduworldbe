package com.example.eduworldbe.service;

import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.repository.MatchingPairRepository;
import com.example.eduworldbe.dto.MatchingPairBatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchingPairService {
  @Autowired
  private MatchingPairRepository matchingPairRepository;

  public MatchingPair create(MatchingPair matchingPair) {
    return matchingPairRepository.save(matchingPair);
  }

  public Optional<MatchingPair> getById(String id) {
    return matchingPairRepository.findById(id);
  }

  public List<MatchingPair> getAll() {
    return matchingPairRepository.findAll();
  }

  public List<MatchingPair> getByQuestionId(String questionId) {
    return matchingPairRepository.findByQuestionId(questionId);
  }

  public MatchingPair update(String id, MatchingPair updated) {
    MatchingPair existing = matchingPairRepository.findById(id).orElseThrow();
    if (updated.getFrom() != null)
      existing.setFrom(updated.getFrom());
    if (updated.getTo() != null)
      existing.setTo(updated.getTo());
    if (updated.getQuestionId() != null)
      existing.setQuestionId(updated.getQuestionId());
    return matchingPairRepository.save(existing);
  }

  public void delete(String id) {
    matchingPairRepository.deleteById(id);
  }

  @Transactional
  public List<MatchingPair> createBatch(MatchingPairBatchRequest request) {
    List<MatchingPair> pairs = request.getPairs().stream()
        .map(pair -> {
          MatchingPair matchingPair = new MatchingPair();
          matchingPair.setQuestionId(request.getQuestionId());
          matchingPair.setFrom(pair.getFrom());
          matchingPair.setTo(pair.getTo());
          return matchingPair;
        })
        .collect(Collectors.toList());

    return matchingPairRepository.saveAll(pairs);
  }
}