package org.finance;

public class Trade
{
    private final long price;
    private final long quantity;
    private final String bidId;
    private final String askId;

    public Trade( final long price, final long quantity, final String bidId, final String askId )
    {
        this.price = price;
        this.quantity = quantity;
        this.bidId = bidId;
        this.askId = askId;
    }

    public long getPrice()
    {
        return price;
    }

    public long getQuantity()
    {
        return quantity;
    }

    public String getBidId()
    {
        return bidId;
    }

    public String getAskId()
    {
        return askId;
    }
}
