package service;

import dao.FilterMapper;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class FilterService {
    private final FilterMapper filterMapper;

    public List<String> getCityNames() {
        return filterMapper.selectCityNames();
    }

    public List<String> getPortCodes() {
        return filterMapper.selectPortCodes();
    }

    public List<String> getSeaNames() {
        return filterMapper.selectSeaNames();
    }
}
