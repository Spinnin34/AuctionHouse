package com.spawnchunk.auctionhouse.modules;

public enum SortOrder {
    CHRONO_OLDEST,
    CHRONO_NEWEST,
    PRICE_LOWEST,
    PRICE_HIGHEST;


    public String key() {
        String key = "";
        switch (this.name()) {
            case "CHRONO_OLDEST": {
                key = "message.listing.order.oldest";
                break;
            }
            case "CHRONO_NEWEST": {
                key = "message.listing.order.newest";
                break;
            }
            case "PRICE_LOWEST": {
                key = "message.listing.order.lowest_price";
                break;
            }
            case "PRICE_HIGHEST": {
                key = "message.listing.order.highest_price";
            }
        }
        return key;
    }

    public SortOrder next() {
        int ordinal = this.ordinal() + 1;
        if (ordinal >= SortOrder.values().length) {
            ordinal = 0;
        }
        return SortOrder.values()[ordinal];
    }
}

