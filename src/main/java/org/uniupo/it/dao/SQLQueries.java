package org.uniupo.it.dao;

public final class SQLQueries {
    private SQLQueries() {
    }

    public static final class Drink {
        public static final String GET_ALL_DRINKS = """
                SELECT * FROM machine."Drink"
                """;


    }

    public static final class Machine {
        public static final String INSERT_COIN = """
                UPDATE machine."Machine" SET "totalCredit" = "totalCredit" + ?;
                """;
    }
}
