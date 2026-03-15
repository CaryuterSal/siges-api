package dev.spiffocode.sigesapi.agenda.application;

import java.time.LocalTime;

public interface TimeRange {
    LocalTime start();
    LocalTime end();
}
