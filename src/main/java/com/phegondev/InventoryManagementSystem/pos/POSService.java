package com.phegondev.InventoryManagementSystem.pos;

import com.phegondev.InventoryManagementSystem.common.Response;

import java.math.BigDecimal;

public interface POSService {
    Response openSession(POSSessionDTO sessionDTO);
    Response closeSession(Long sessionId, BigDecimal closingBalance);
    Response getActiveSession();
    Response getAllSessions();
    Response addTransaction(Long sessionId, POSTransactionDTO transactionDTO);
    Response getSessionTransactions(Long sessionId);
    Response voidTransaction(Long transactionId);
    Response getDailySales();
}
