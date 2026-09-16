package lux.dartgame.controller;

import lux.dartgame.dto.GametypeResponse;
import lux.dartgame.service.GametypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameModeControllerTest {

    @Mock
    private GametypeService gametypeService;

    @InjectMocks
    private GameModeController gameModeController;

    @Test
    void findAllModes_delegatesToServiceAndReturnsAllModes() {
        List<GametypeResponse> responses = List.of(
                new GametypeResponse(1L, "501"),
                new GametypeResponse(2L, "301"));

        when(gametypeService.findAll()).thenReturn(responses);

        List<GametypeResponse> result = gameModeController.findAllModes();

        assertThat(result).containsExactly(
                new GametypeResponse(1L, "501"),
                new GametypeResponse(2L, "301"));
        verify(gametypeService).findAll();
    }
}