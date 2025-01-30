package org.uniupo.it.dao;

import org.uniupo.it.model.Drink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DrinkDaoImpl implements DrinkDao{
    @Override
    public List<Drink> getAllDrinks() {
        try(Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQLQueries.Drink.GET_ALL_DRINKS)) {

            ResultSet rs = stmt.executeQuery();
            List<Drink> drinks = new ArrayList<>();
            while (rs.next()) {
                drinks.add(new Drink(rs.getString("code"), rs.getDouble("price"), rs.getString("description"),rs.getString("name")));
            }
            return drinks;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving drinks", e);

        }

    }

    @Override
    public void insertCoin(double coin) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Machine.INSERT_COIN)) {
            stmt.setDouble(1, coin);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting coin", e);
        }
    }
}
