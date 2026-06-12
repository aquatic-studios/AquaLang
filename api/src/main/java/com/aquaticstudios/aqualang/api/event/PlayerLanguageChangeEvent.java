package com.aquaticstudios.aqualang.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerLanguageChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String previousLanguage;
    private String newLanguage;
    private boolean cancelled;

    public PlayerLanguageChangeEvent(@NotNull UUID uuid,
                                    @Nullable String previousLanguage,
                                    @NotNull String newLanguage) {
        this.uuid = uuid;
        this.previousLanguage = previousLanguage;
        this.newLanguage = newLanguage;
    }

    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @Nullable
    public String getPreviousLanguage() {
        return previousLanguage;
    }

    @NotNull
    public String getNewLanguage() {
        return newLanguage;
    }

    public void setNewLanguage(@NotNull String newLanguage) {
        this.newLanguage = newLanguage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
