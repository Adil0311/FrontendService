package org.uniupo.it.dao;

import org.uniupo.it.model.Drink;

import java.util.List;

public interface DrinkDao {
    List<Drink> getAllDrinks();
    void insertCoin(double coin);
    boolean verifyBalanceCoin(double coin);
}
