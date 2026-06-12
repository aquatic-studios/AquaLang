package com.aquaticstudios.aqualang.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fired before a player's language changes through the API. Cancellable, and the
 * target language can be overridden with {@link #setNewLanguage(String)}.
 *
 * Developed by @Senkex
 */
public class PlayerLanguageChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String previousLanguage;
    private String newLanguage;
    private boolean cancelled;

    /**
     * @param uuid the affected player UUID
     * @param previousLanguage the language before the change, or {@code null} if none
     * @param newLanguage the language being applied
     */
    public PlayerLanguageChangeEvent(@NotNull UUID uuid,
                                    @Nullable String previousLanguage,
                                    @NotNull String newLanguage) {
        this.uuid = uuid;
        this.previousLanguage = previousLanguage;
        this.newLanguage = newLanguage;
    }

    /**
     * @return the affected player UUID
     */
    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * @return the language before this change, or {@code null} if the player had none
     */
    @Nullable
    public String getPreviousLanguage() {
        return previousLanguage;
    }

    /**
     * @return the language that will be applied
     */
    @NotNull
    public String getNewLanguage() {
        return newLanguage;
    }

    /**
     * Overrides the language that will be applied.
     *
     * @param newLanguage the language to apply instead
     */
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
