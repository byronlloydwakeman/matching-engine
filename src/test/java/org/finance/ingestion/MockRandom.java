package org.finance.ingestion;

import java.util.Random;

/**
 * Mocked Random class to produce known 'random' results
 */
public class MockRandom extends Random
{
    private int intCounter = 0;
    private int[] intValues;
    private int booleanCounter = 0;
    private boolean[] booleanValues; // Bid, ask, bid

    @Override
    public int nextInt( int bound )
    {
        int val = Math.min(intValues[ intCounter ], bound - 1);
        intCounter += 1;
        return val;
    }

    @Override
    public boolean nextBoolean()
    {
        boolean val = booleanValues[ booleanCounter ];
        booleanCounter += 1;
        return val;
    }

    public void setIntValues( final int[] intValues )
    {
        this.intValues = intValues;
    }

    public void setBooleanValues( final boolean[] booleanValues )
    {
        this.booleanValues = booleanValues;
    }
}
