package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.dto.GametypeResponse;
import lux.dartgame.repository.GametypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public final class GametypeService {

    private final GametypeRepository gametypeRepository;

    @Autowired
    public GametypeService(final GametypeRepository gametypeRepositoryParam) {
        this.gametypeRepository = gametypeRepositoryParam;
    }

    public List<GametypeResponse> findAll() {
        log.info("Fetching all gametypes");
        return gametypeRepository.findAll().stream()
                .map(gt -> new GametypeResponse(gt.getGametypeId(), gt.getGametype()))
                .collect(Collectors.toList());
    }
}
