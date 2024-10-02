package database;


public interface Query {
    public static final String LOAD_DATA_INFILE = "LOAD DATA INFILE ? INTO TABLE ?"
            + " FIELDS TERMINATED BY ',' IGNORE 1 LINES";
    public static final String CREATE_USER = "CREATE USER ? IDENTIFIED BY ? DEFAULT ROLE user";
    public static final String INSERT_USER_INFO = "INSERT INTO user(account, password)"
            + " values(?, ?)";
    public static final String INSERT_SHIPPING_INFO = "INSERT INTO shipping_info(account, name, email,"
            + " phone, province, city, district, address) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String CALL_SHOW_PORT = "CALL showPort(?, ?, ?)";
    public static final String CALL_SHOW_RECORD = "CALL showRecord(?, ?, ?)";
    public static final String CALL_SHOW_SAMPLE = "CALL showSample(?, ?, ?)";
    public static final String CALL_SHOW_NO_STATION = "CALL showNoStation()";
    public static final String CALL_SHOW_SAMPLE_STATISTICS = "CALL showSampleStatistics(?)";
    public static final String CALL_SHOW_ANNUAL_REPORT = "CALL showAnnualReport(?, ?);";
    public static final String SELECT_CITY_NAME = "SELECT DISTINCT city_name FROM port_view";
    public static final String SELECT_PORT_CODE = "SELECT DISTINCT port_code FROM port_view";
    public static final String SELECT_SEA_NAME = "SELECT DISTINCT sea_name FROM port_view";
    public static final String SELECT_ROLE = "SELECT type FROM user WHERE account = ?";
    public static final String SELECT_SHIPPING_INFO = "SELECT account, name, email, phone, province,"
            + " city, district, address FROM shipping_info";
}