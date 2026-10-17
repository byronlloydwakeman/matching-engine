package org.finance;

public class Order
{
    private TradeType tradeType;
    private String orderId;
    private long price; // In pence
    private long quantity;

    public Order( final TradeType tradeType, final String orderId, final long price, final long quantity )
    {
        this.tradeType = tradeType;
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
    }

    public Order()
    {
    }

    public TradeType getTradeType()
    {
        return tradeType;
    }

    public void setTradeType( final TradeType tradeType )
    {
        this.tradeType = tradeType;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId( final String orderId )
    {
        this.orderId = orderId;
    }

    public long getPrice()
    {
        return price;
    }

    public void setPrice( final long price )
    {
        this.price = price;
    }

    public long getQuantity()
    {
        return quantity;
    }

    public void setQuantity( final long quantity )
    {
        this.quantity = quantity;
    }
}
