package club.mindtech.mindbot.database;

public enum Table {
    WHITELIST("whitelist", "(discord_id bigint NOT NULL UNIQUE, mc_name varchar(16) NOT NULL UNIQUE, mc_uuid uuid NOT NULL UNIQUE)"),
    REMINDME("remindme", "(discord_id bigint NOT NULL UNIQUE, message text NOT NULL, expire_time timestamp with time zone NOT NULL)"),
//    APPLICATIONS("applications", "()"),
    SCAMS("scams", "(link text NOT NULL UNIQUE)"),
    POLL("poll", "(message_id text NOT NULL UNIQUE, expire_time timestamp NOT NULL)"),
//    PERMISSION("permission", ""),
    TIMEZONE("timezone", "(zone varchar(8) NOT NULL)");

    private final String name;
    private final String dataTypes;

    Table(String name, String dataTypes) {
        this.name = name;
        this.dataTypes = dataTypes;
    }

    public String getName() {
        return name;
    }

    public String getDataTypes() {
        return dataTypes;
    }
}
