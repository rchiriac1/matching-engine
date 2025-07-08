package radu.dto;

import radu.engine.OrderType;
import radu.engine.TimeInForce;

import java.time.Instant;

/**
 * DTO for creating a new order via API.
 */
public class OrderRequest {
    public OrderType type;
    public String symbol;
    public double price;
    public int quantity;
    public Instant expiry;
    public TimeInForce tif;
}
