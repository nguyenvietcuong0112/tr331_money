package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils;

import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import java.util.List;

public class TransactionUpdateEvent {


    private List<TransactionModel> transactionList;

    public TransactionUpdateEvent(List<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    public List<TransactionModel> getTransactionList() {
        return transactionList;
    }
}