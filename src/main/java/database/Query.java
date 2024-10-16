package database;


public interface Query {
    String LOAD_DATA_INFILE = "LOAD DATA INFILE ? INTO TABLE `%s`"
            + " FIELDS TERMINATED BY ',' IGNORE 1 LINES";
    String CREATE_USER = "CREATE USER ? IDENTIFIED BY ? DEFAULT ROLE user";
    String INSERT_USER = "INSERT INTO user(account, password) values(?, ?)";
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
    String SELECT_ROLE = "SELECT CURRENT_ROLE()";
    String SELECT_USER = "SELECT * FROM port.user";
    String TURN_ON_SAFE_UPDATES = "SET SQL_SAFE_UPDATES = 1";
    String TURN_OFF_SAFE_UPDATES = "SET SQL_SAFE_UPDATES = 0";
}