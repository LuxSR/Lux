package lux.dartgame.service;

import lux.dartgame.dto.GametypeResponse;
import lux.dartgame.model.Gametype;
import lux.dartgame.repository.GametypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GametypeServiceTest {

    @Mock
    private GametypeRepository gametypeRepository;

    @InjectMocks
    private GametypeService gametypeService;

    private Gametype gametype(final long id, final String name) {
        Gametype gametype = new Gametype();
        gametype.setGametypeId(id);
        gametype.setGametype(name);
        return gametype;
    }

    @Test
    void findAll_returnsAllGametypesAsResponses() {
        when(gametypeRepository.findAll()).thenReturn(
                List.of(gametype(1L, "501"), gametype(2L, "301")));

        List<GametypeResponse> result = gametypeService.findAll();

        assertThat(result).containsExactly(
                new GametypeResponse("501"),
                new GametypeResponse("301"));
    }

    @Test
    void findAll_noGametypes_returnsEmptyList() {
        when(gametypeRepository.findAll()).thenReturn(List.of());

        List<GametypeResponse> result = gametypeService.findAll();

        assertThat(result).isEmpty();
    }
}