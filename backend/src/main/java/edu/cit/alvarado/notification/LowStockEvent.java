package edu.cit.alvarado.notification; 
public record 
    LowStockEvent(String productId,String productName,int stock,int threshold){
        
    }
