package io.arcnode.dercontrol.derevent;

/**
 * Lifecycle state of a DERControl event, from the IEEE 2030.5 {@code EventStatus.currentStatus}
 * field. 2030.5 code 3 ("Cancelled with Randomization") folds into {@link #CANCELLED} for MVP.
 */
public enum DerControlStatus {
  SCHEDULED,
  ACTIVE,
  CANCELLED,
  SUPERSEDED
}
