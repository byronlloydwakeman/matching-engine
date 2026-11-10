package org.finance.command;

public sealed interface Command permits NewOrder, Cancel, Shutdown {

}
