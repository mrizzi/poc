package io.mrizzi.qute;

import java.math.BigDecimal;

public class Item {
    public Integer id;
    public String name;
    public BigDecimal price;

    public Item(Integer id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
