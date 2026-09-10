package io.arcnode.dercontrol.dispatch.dto;

/**
 * Canonical arcnode boolean measurement payload (system_adr §13). Serialized as {@code {"ts":
 * "...", "value": <bool>}}. Used for unitless two-state channels (unit slot {@code none}).
 *
 * @param ts RFC3339 / ISO-8601 UTC timestamp with trailing {@code Z}
 * @param value the two-state reading
 */
public record BooleanSample(String ts, boolean value) {}
