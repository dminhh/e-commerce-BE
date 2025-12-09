package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.SeasonInfo;
import com.prod.models.details.Season;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ISeasonFacade {
    ResponseObject<Season> createSeason(SeasonInfo seasonInfo);
    ResponseObject<Season> updateSeason(SeasonInfo seasonInfo);
    ResponseObject<Season> getSeasonById(int id);
    ResponseObject<Page<Season>> getAllSeasons(int page, int size, String field, String direct);
    ResponseObject<Page<Season>> getAllSeasonsByYear(String year, int page, int size, String field, String direct);
    ResponseObject<Boolean> deleteSeason(SeasonInfo seasonInfo);
}
