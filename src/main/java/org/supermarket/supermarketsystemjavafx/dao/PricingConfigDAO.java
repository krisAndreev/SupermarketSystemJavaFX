package org.supermarket.supermarketsystemjavafx.dao;

import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.models.PricingConfig;

public interface PricingConfigDAO {
    static PricingConfig loadConfig() throws DatabaseException {
        // Implement database loading logic here
        // This could be from a properties file, database table, etc.
        // For now, return default values
        return new PricingConfig(0.30, 0.40, 7, 0.20);
    }

    static void saveConfig(PricingConfig config) throws DatabaseException {
        // Implement database saving logic here
    }
}