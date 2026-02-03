package com.example.library.enam;

public enum UserStatus {
    EMAIL_NOT_CONFIRMED, // зарегистрировался, но не подтвердил почту
    PENDING_APPROVAL,    // почту подтвердил, ждёт админа
    APPROVED,            // админ одобрил — полный доступ
    REJECTED             // админ отклонил
}

