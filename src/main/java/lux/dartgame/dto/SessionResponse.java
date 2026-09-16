package lux.dartgame.dto;

import lux.dartgame.model.Gametype;

import java.util.List;

public record SessionResponse(long id,
                              String date,
                              List<Gametype> gametypes,
                              boolean isActive) {
}
