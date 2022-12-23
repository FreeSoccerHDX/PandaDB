package de.freesoccerhdx.pandadb.clientutils.changelistener;

public abstract class TextChangeListener extends ChangeListener{

    abstract public void onChange(ChangeReason changeReason, String oldvalue, String newvalue);

}
