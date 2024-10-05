package dao;

import org.apache.ibatis.annotations.Select;
import java.util.List;

import static database.Query.*;

public interface FilterMapper {
    @Select(SELECT_CITY_NAME)
    List<String> selectCityNames();

    @Select(SELECT_PORT_CODE)
    List<String> selectPortCodes();

    @Select(SELECT_SEA_NAME)
    List<String> selectSeaNames();
}
