package com.ech.kitchen.entity;

/**
 * There is already a temperature related Enum in the order module.
 * But, I think it make sense to define this specific to the shelf to avoid coupling.
 */
public enum ShelfTemperatureEnum {
    Frozen, Cold, Hot, Any
}
