package com.spawnchunk.auctionhouse.modules;

public enum ListingType {
    PLAYER_LISTING,
    PLAYER_AUCTION,
    SERVER_LISTING,
    SERVER_LISTING_UNLIMITED;


    public boolean isServer() {
        return this.name().startsWith("SERVER_");
    }
}

