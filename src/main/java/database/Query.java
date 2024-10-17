package database;


public interface Query {
    String LOAD_DATA_INFILE = "LOAD DATA INFILE ? INTO TABLE `%s` FIELDS TERMINATED BY ',' IGNORE 1 LINES";
    String CREATE_USER = "CREATE USER ? IDENTIFIED BY ? DEFAULT ROLE ?";
    String INSERT_USER = "INSERT INTO user(account, password, type) values(?, ?, ?)";
    String CALL_SHOW_PORT = "CALL showPort(?, ?, ?)";
    String CALL_SHOW_RECORD = "CALL showRecord(?, ?, ?)";
    String CALL_SHOW_SAMPLE = "CALL showSample(?, ?, ?)";
    String CALL_SHOW_NO_STATION = "CALL showNoStation()";
    String CALL_SHOW_SAMPLE_STATISTICS = "CALL showSampleStatistics(?)";
    String CALL_SHOW_ANNUAL_REPORT = "CALL showAnnualReport(?, ?)";
    String CALL_DELETE_FROM_WHERES = "CALL deleteFromWheres(?, ?, ?, ?, ?)";
    String SELECT_CITY_NAME = "SELECT DISTINCT city_name FROM port_view";
    String SELECT_PORT_CODE = "SELECT DISTINCT port_code FROM port_view";
    String SELECT_SEA = "SELECT DISTINCT sea FROM port_view";
    String SELECT_ROLE = "SELECT DISTINCT type FROM user";
    String SELECT_USER = "SELECT * FROM port.user WHERE type LIKE ?";
    String CURRENT_ROLE = "SELECT CURRENT_ROLE()";
    String TURN_ON_SAFE_UPDATES = "SET SQL_SAFE_UPDATES = 1";
    String TURN_OFF_SAFE_UPDATES = "SET SQL_SAFE_UPDATES = 0";
}