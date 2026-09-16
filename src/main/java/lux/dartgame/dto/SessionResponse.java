package lux.dartgame.dto;

import java.util.List;

public record SessionResponse(long id,
                              String date,
                              List<GametypeResponse> gametypes,
                              boolean isActive) {
}
