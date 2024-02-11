package com.example.easystudy.entities

enum class EventType {
    LECTURE,
    EXAM,
    SEMINAR,
    PRACTICE
}
fun EventType.toUkrainianString(): String {
    return when (this) {
        EventType.LECTURE -> "Лекція"
        EventType.EXAM -> "Іспит"
        EventType.SEMINAR -> "Семінар"
        EventType.PRACTICE -> "Практика"
    }
}