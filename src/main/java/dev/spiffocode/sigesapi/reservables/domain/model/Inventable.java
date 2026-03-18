package dev.spiffocode.sigesapi.reservables.domain.model;

import org.springframework.data.annotation.Transient;

public interface Inventable {

    @Transient
    InventoryItem getInventoryItem();

    @Transient
    default String getInventoryNum() {
        return getInventoryItem().getInventoryNum();
    }

    @Transient
    default void setInventoryNum(String inventoryNum) {
        getInventoryItem().setInventoryNum(inventoryNum);
    }

}
