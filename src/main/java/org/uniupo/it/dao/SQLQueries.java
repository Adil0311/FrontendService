package org.uniupo.it.dao;

public final class SQLQueries {
    private SQLQueries() {
    }

    public static String getSchemaName(String instituteId, String machineId) {
        return String.format("machine_%s_%s",
                instituteId.toLowerCase().replace("-", "_"),
                machineId.toLowerCase().replace("-", "_"));
    }


    public static final class Drink {
        private static final String GET_ALL_DRINKS = """
                SELECT * FROM %s."Drink"
                """;

        public static String getGetAllDrinks(String instituteId, String machineId) {
            return String.format(GET_ALL_DRINKS, getSchemaName(instituteId, machineId));
        }


    }

    public static final class Machine {
        private static final String INSERT_COIN = """
                UPDATE %s."Machine" SET "totalCredit" = "totalCredit" + ?;
                """;

        public static String getInsertCoin(String instituteId, String machineId) {
            return String.format(INSERT_COIN, getSchemaName(instituteId, machineId));
        }
        private static final String CHECK_BALANCE = """
            SELECT "totalBalance", "maxBalance" FROM %s."Machine";
            """;
        public static String getCheckBalance(String instituteId, String machineId) {
            return String.format(CHECK_BALANCE, getSchemaName(instituteId, machineId));
        }
    }
}
