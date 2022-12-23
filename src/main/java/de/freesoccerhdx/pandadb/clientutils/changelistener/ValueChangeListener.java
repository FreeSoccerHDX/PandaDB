package de.freesoccerhdx.pandadb.clientutils.changelistener;

public abstract class ValueChangeListener extends ChangeListener {

    abstract public void onChange(ChangeReason changeReason, Double oldvalue, Double newvalue);

}
